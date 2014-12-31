package fi.mamk.osa.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PreservationPlan {

    private static final Logger logger = Logger.getLogger(PreservationPlan.class);
    private Vector<ArchiveLevel> hierarchy = new Vector<ArchiveLevel>();

    public PreservationPlan(File configuration) {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    		
    	try {
    		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        	Document doc = dBuilder.parse(configuration);
        	doc.getDocumentElement().normalize();
        	NodeList nodes = doc.getElementsByTagName("hierarchy");
        	
        	for (int i=0; i<nodes.getLength(); i++) {
        	    Node node = nodes.item(i);
        	    if (node.getNodeType() == Node.ELEMENT_NODE) {
        		    NodeList levelNodes = node.getChildNodes();
    
        		    for (int j=0; j<levelNodes.getLength(); j++) {
        		        Node level = levelNodes.item(j);
        		        if (level.getNodeType() == Node.ELEMENT_NODE) {
        				    // level
        		            if ("level".equalsIgnoreCase(level.getNodeName())) {
        		                ArchiveLevel cLevel = new ArchiveLevel();
        		                cLevel.setOrder(Integer.parseInt(level.getAttributes().getNamedItem("order").getNodeValue()));
        						
        		                // if maxOrder exists
        						if (level.getAttributes().getNamedItem("maxOrder") != null) {
        						    cLevel.setMaxOrder(Integer.parseInt(level.getAttributes().getNamedItem("maxOrder").getNodeValue()));
        						}
        						
        						NodeList childNodes = level.getChildNodes();
        						for (int x=0; x<childNodes.getLength(); x++) {
                                    Node child = childNodes.item(x);
                                    if (child.getNodeType() == Node.ELEMENT_NODE) {
                                        // contentmodels
                                        if ("contentModels".equalsIgnoreCase(child.getNodeName())) {
                                            NodeList contentmodelElements = child.getChildNodes();
                                            for (int y=0; y<contentmodelElements.getLength(); y++) {
                                                Node contentmodelElement = contentmodelElements.item(y);
                                                if (contentmodelElement.getNodeType() == Node.ELEMENT_NODE) {
                                                    ContentmodelMetadataElement cElement = new ContentmodelMetadataElement();
                                                    cElement.setName(contentmodelElement.getAttributes().getNamedItem("name").getNodeValue());
                                                    cElement.setContentModel(contentmodelElement.getAttributes().getNamedItem("contentModel").getNodeValue());
                                                    cElement.setAllowDocuments(Boolean.parseBoolean(contentmodelElement.getAttributes().getNamedItem("allowDocuments").getNodeValue()));
                                                    cLevel.getContentmodels().add(cElement);
                                                }
                                            }
                                        // inheritance    
                                        } else if ("inheritance".equalsIgnoreCase(child.getNodeName())) {
                                            String contentModel = "";
                                            if (child.getAttributes().getNamedItem("contentModel") != null) {
                                                contentModel = child.getAttributes().getNamedItem("contentModel").getNodeValue();
                                            } else {
                                                contentModel = "all";
                                            }
                                            NodeList inheritanceElements = child.getChildNodes();
                                            for (int z=0; z<inheritanceElements.getLength(); z++) {
                                                Node inheritanceElement = inheritanceElements.item(z);
                                                if (inheritanceElement.getNodeType() == Node.ELEMENT_NODE) {
                                                    InheritedMetadataElement iElement = new InheritedMetadataElement();
                                                    iElement.setName(inheritanceElement.getAttributes().getNamedItem("name").getNodeValue());
                                                    iElement.setBehaviour(inheritanceElement.getAttributes().getNamedItem("behaviour").getNodeValue());
                                                    iElement.setDefaultInheritance(Boolean.parseBoolean(inheritanceElement.getAttributes().getNamedItem("default").getNodeValue()));
                                                    iElement.setManualInheritance(Boolean.parseBoolean(inheritanceElement.getAttributes().getNamedItem("manual").getNodeValue()));
                                                    iElement.setContentModel(contentModel);
                                                    cLevel.getInheritance().add(iElement);
                                                }
                                            }
                                        }
                                    }
                                }
        						
        						this.hierarchy.add(cLevel);
        					}
        				}
        			}
        		}
        	}
    		
    	} catch (ParserConfigurationException e) {
    	    logger.error(e);
    	} catch (SAXException e) {
    	    logger.error(e);
    	} catch (IOException e) {
    	    logger.error(e);
    	}
    	
    }
    
    /**
     * Get metadata elements to be inherited
     * @param order
     * @param cModel
     * @return
     */
    public ArrayList<InheritedMetadataElement> getInheritedElements(int order, String cModel) {
        ArrayList<InheritedMetadataElement> inheritedElements = new ArrayList<InheritedMetadataElement>();
        for (ArchiveLevel level : getHierarchy()) {
            if (level.getOrder() == order) {
                for (InheritedMetadataElement element : level.getInheritance()) {
                    if (element.getContentModel().equalsIgnoreCase(cModel) || element.getContentModel().equalsIgnoreCase("all")) {
                        inheritedElements.add(element);
                    }
                }
            }
        }
        return inheritedElements;
    }

    public int getMaxLevel(){
    	int maxlvl = 1;
    	for (ArchiveLevel level : this.hierarchy) {
    		if (level.getOrder() > maxlvl) {
    			maxlvl = level.getOrder();
    		}
    		if (level.getMaxOrder() > maxlvl) {
    		    maxlvl = level.getMaxOrder();
    		}
    	}
    	return maxlvl;
    }
    
    public Vector<ArchiveLevel> getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(Vector<ArchiveLevel> hierarchy) {
        this.hierarchy = hierarchy;
    }

}