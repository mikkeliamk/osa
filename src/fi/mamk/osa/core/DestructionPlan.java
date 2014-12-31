package fi.mamk.osa.core;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DestructionPlan {
    
    private static final Logger logger = Logger.getLogger(DestructionPlan.class);
    
    public static final String DESTRUCTIONTYPE_Dispose = "dispose";
    public static final String DESTRUCTIONTYPE_Delete = "delete";
    public static final String DESTRUCTIONREASON_Scheduled = "scheduled";
    
    private String approvalPolicy = "";
    private String disposeMethod = "";
    private boolean allowOrphans = false;
    private boolean createResidualObjects = false;
    private boolean recursiveDestruction = false; // TODO: check if needed
    private boolean scheluded = false;
    private List<String> residualDatastreams = new ArrayList<String>();
    private List<String> residualMetadata = new ArrayList<String>();
    
    public DestructionPlan(File configuration) {
        
        try {
        	
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(configuration);
            doc.getDocumentElement().normalize();
            
        	NodeList disposeSettings = doc.getElementsByTagName("destruction");
            
            for (int i=0; i<disposeSettings.getLength(); i++) {

                Node item = disposeSettings.item(i);
            	if (item.getNodeType() == Node.ELEMENT_NODE)	{

            	    NodeList childNodes = item.getChildNodes();
            		for (int j=0; j<childNodes.getLength(); j++) {
            			Node subItem = childNodes.item(j);
            			if (subItem.getNodeType() == Node.ELEMENT_NODE) {
            				if (subItem.getNodeName().equalsIgnoreCase("method")) {
            					setDisposeMethod(subItem.getTextContent());
            				}
            				
            				if (subItem.getNodeName().equalsIgnoreCase("approval")) {
                                setApprovalPolicy(subItem.getTextContent());
                            }
            				
            				if (subItem.getNodeName().equalsIgnoreCase("scheduled")) {
            				    setScheluded(Boolean.parseBoolean(subItem.getTextContent()));
                            }
            				// TODO: check if needed
            				if (subItem.getNodeName().equalsIgnoreCase("recursiveDestruction")) {
            					setRecursiveDestruction(Boolean.parseBoolean(subItem.getTextContent()));
            				}
            				
            				if (subItem.getNodeName().equalsIgnoreCase("allowOrphans")) {
            				    setAllowOrphans(Boolean.parseBoolean(subItem.getTextContent()));
            				}
            				
            				if (subItem.getNodeName().equalsIgnoreCase("createResidualObjects")) {
                                setCreateResidualObjects(Boolean.parseBoolean(subItem.getTextContent()));
                            }
            				
            				if (subItem.getNodeName().equalsIgnoreCase("residualObjectDefinition")) {
            				    
            				    NodeList residualObject_metadata = doc.getElementsByTagName("metadata").item(0).getChildNodes();
            				    // get metadata to be preserved
                                for (int x=0; x<residualObject_metadata.getLength(); x++) {
                                    Node metadata = residualObject_metadata.item(x);
                                    if (metadata.getNodeType() == Node.ELEMENT_NODE) {
                                        String mdName = metadata.getAttributes().getNamedItem("name").getNodeValue();
                                        residualMetadata.add(mdName);
                                    }
                                }
            				    
            				    NodeList residualObject_datastreams = doc.getElementsByTagName("dataStreams").item(0).getChildNodes();
            				    // get datastream names to be preserved
                                for (int x=0; x<residualObject_datastreams.getLength(); x++) {
                                    Node datastream = residualObject_datastreams.item(x);
                                    if (datastream.getNodeType() == Node.ELEMENT_NODE) {
                                        String dsName = datastream.getAttributes().getNamedItem("id").getNodeValue();
                                        residualDatastreams.add(dsName);
                                    }
                                }
                            }

            			}
            		}			
            	}	
            }
    		
        } catch(Exception e) {
        	logger.error(e);
        }
        // TODO Load the disposal plan
    }
    
    public String getApprovalPolicy() {
        return approvalPolicy;
    }
    
    public void setApprovalPolicy(String approvalPolicy) {
        this.approvalPolicy = approvalPolicy;
    }
    
    public void setCreateResidualObjects(boolean createResidualObjects) {
        this.createResidualObjects = createResidualObjects;
    }
    
    public boolean isCreateResidualObjects() {
        return createResidualObjects;
    }
    
    public String getDisposeMethod() {
    	return disposeMethod;
    }
    
    public void setDisposeMethod(String disposeMethod) {
    	this.disposeMethod = disposeMethod;
    }
    
    public boolean isRecursiveDestruction() {
    	return recursiveDestruction;
    }
    
    public void setRecursiveDestruction(boolean recursiveDestruction) {
    	this.recursiveDestruction = recursiveDestruction;
    }
    
    public boolean isAllowOrphans() {
        return allowOrphans;
    }
    
    public void setAllowOrphans(boolean allowOrphans) {
        this.allowOrphans = allowOrphans;
    }
    
    public void setScheluded(boolean scheluded) {
        this.scheluded = scheluded;
    }
    
    public boolean isScheluded() {
        return scheluded;
    }
    
    public List<String> getResidualMetadata() {
        return residualMetadata;
    }
	
    public List<String> getResidualDatastreams() {
        return residualDatastreams;
    }
        
}
