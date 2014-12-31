package fi.mamk.osa.ui;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.log4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import fi.mamk.osa.bean.FedoraBean;
import fi.mamk.osa.bean.MetaDataElement;
import fi.mamk.osa.core.Osa;
import fi.mamk.osa.fedora.RepositoryManager;
import fi.mamk.osa.solr.SolrField;
import fi.mamk.osa.stripes.OsaActionBeanContext;

/**
 * Class that the wraps look & feel for a organization 
 *
 */
public class Gui implements Serializable {

    private static final long serialVersionUID = -6157579742585725399L;
    private static final Logger logger = Logger.getLogger(Gui.class);
    
    // Form variables
    public static final String FORM_SEARCH         = "search";
    public static final String DEFAULT_MAIN_HEADER = "/layout/osa_header_main.jsp";
    public static final String DEFAULT_SUB_HEADER  = "/layout/osa_header.jsp";
    public static final String DEFAULT_APP_TITLE   = "OSA Web Client";
    
    // Layout
    private String cssFile 	   = null;
    private String logoFile    = null;
    private String logoFileAlt = null;
    private String appTitle    = DEFAULT_APP_TITLE;
    private String mainHeader  = DEFAULT_MAIN_HEADER;
    private String subHeader   = DEFAULT_SUB_HEADER;
    
    private String resultType  = "";
    private int queueSize;
    private int initialBasecollectionId = 0;
    
    // map between FormElement and datastream
    public MultiValueMap mapDataStreams = null;
    
    private SearchConfiguration searchConfiguration = null;    
    private SearchResultConfiguration searchResults = null;
    // contains all FormElements to be shown in GUI
    private FormConfiguration formConfiguration 	= null;
    
    // Disposal
    private String disposeMethod = "";
    private boolean disposeChilds = false;
    
    public Gui(File xmlConfiguration, OsaActionBeanContext context) {
        
        mapDataStreams = new MultiValueMap();
        searchConfiguration = new SearchConfiguration();
        
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(xmlConfiguration);
            doc.getDocumentElement().normalize();
            getIngestForms(doc);
            getSearchForm(doc);
            getResultLayout(doc);
            getLayout(doc, context);
            getUploadConfig(doc);
            getIdGenerationConfig(doc);
            getFacetFieldsFromConf(doc);
            getFacetFieldsforAdvBrowse(doc);
        } catch (Exception e) {
            logger.error("Failed to load configuration file: " + xmlConfiguration+", "+ e);
        }
    }

    /**
     * Returns all datastreams for the form element name
     * Reads <metadata> tags in osa-system:contentmodel-*
     * @param name      name of the form element
     * @return          list of datastreams
     */
    public List<String> getDataStreamMappings(String name) {
        List<String> retValue = new ArrayList<String>();
        List<MetaDataElement> list;
        Set entrySet = this.mapDataStreams.entrySet();
        Iterator it = entrySet.iterator();
               
        while (it.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) it.next();
            list = (List<MetaDataElement>) this.mapDataStreams.get(mapEntry.getKey());
                        
            for (int i=0; i<list.size(); i++) {
                if (name.equalsIgnoreCase((String) mapEntry.getKey())) {
                    MetaDataElement el = (MetaDataElement)list.get(i);
                    retValue.add(el.getDatastream());
                }
            }
        }
        
        return retValue;
    }
    
    /**
     * Find right name for element according to name in form (keyName)
     * @param keyName           element visible name in form
     * @param elementName       element name
     * @param dataStream        datastream name
     * @return
     */
    public String getMetadataName(String elementName, String dataStream) {
        String retValue = null;
        List<MetaDataElement> list;
        Set entrySet = this.mapDataStreams.entrySet();
        Iterator it = entrySet.iterator();
               
        while (it.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) it.next();
            list = (List<MetaDataElement>) this.mapDataStreams.get(mapEntry.getKey());
            
            for (int i=0; i<list.size(); i++) {
                MetaDataElement el = (MetaDataElement)list.get(i);
                if (el.getDatastream().equalsIgnoreCase(dataStream) && el.getVisibleName().equalsIgnoreCase(elementName)) {
                    retValue = (String) mapEntry.getKey();
                    break;
                }
            }
        }
        
        return retValue;
    }
    
    /**
     * Find attribute visible name used in datastream
     * @param name          attribute name
     * @param dataStream    datastream name
     * @return              visible name
     */
    public String getVisibleName(String name, String dataStream) {
        String retValue = null;
        List<MetaDataElement> list;
        Set entrySet = this.mapDataStreams.entrySet();
        Iterator it = entrySet.iterator();
        
        while (it.hasNext()) {
            Map.Entry mapEntry = (Map.Entry) it.next();
            list = (List<MetaDataElement>) this.mapDataStreams.get(mapEntry.getKey());
                        
            for (int i=0; i<list.size(); i++) {
                if (name.equalsIgnoreCase((String) mapEntry.getKey())) {
                    MetaDataElement el = (MetaDataElement)list.get(i);
                    
                    if (el.getDatastream().equalsIgnoreCase(FedoraBean.DATASTREAM_CAPTURE)) {
                        retValue = el.getVisibleName();
                    }
                }
            }
        }
        
        return retValue;
    }
    
    /**
     * Get GUI elements from xml configuration file
     * @param doc           document
     * @param elementName   name of an element
     */
    public void getElements(Document doc, String elementName, String form) {
        
        String formName = null;
        String tabName = null;
        
        if (form.equalsIgnoreCase(FORM_SEARCH)) {
            NodeList searchNodes = doc.getElementsByTagName("search");
            for (int i=0; i<searchNodes.getLength(); i++) {
                Node searchNode = searchNodes.item(i);
                if (searchNode.getNodeType() == Node.ELEMENT_NODE) {
                    NodeList formNodes = searchNode.getChildNodes();
                    for (int j=0; j<formNodes.getLength(); j++) {
                        Node formNode = formNodes.item(j);
                        if (formNode.getNodeType() == Node.ELEMENT_NODE) {
                            NodeList fieldNodes = formNode.getChildNodes();
                            for (int k=0; k<fieldNodes.getLength(); k++) {
                                Node fieldNode = fieldNodes.item(k);
                                if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
                                    if (fieldNode.getNodeName().equals("field")) {
                                        this.parseFieldNode(fieldNode, form, null, null);
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
        } else {
            try {
                NodeList formsNodes = doc.getElementsByTagName("forms");
                for (int i=0; i<formsNodes.getLength(); i++) {
                    Node formsNode = formsNodes.item(i);
                    if (formsNode.getNodeType() == Node.ELEMENT_NODE) {
                        NodeList formNodes = formsNode.getChildNodes();
                        for (int j=0; j<formNodes.getLength(); j++) {
                            Node formNode = formNodes.item(j);
                            if (formNode.getNodeType() == Node.ELEMENT_NODE) {
                                formName = formNode.getAttributes().getNamedItem("name").getNodeValue();
                                // tabs-list
                                NodeList tabsNodes = formNode.getChildNodes();
                                for (int k=0; k<tabsNodes.getLength(); k++) {
                                    // tabs
                                    Node tabsNode = tabsNodes.item(k);
                                    if (tabsNode.getNodeType() == Node.ELEMENT_NODE) {
                                        // tab-list
                                        NodeList tabNodes = tabsNode.getChildNodes();
                                        for (int l=0; l<tabNodes.getLength(); l++) {
                                            // tab
                                            Node tabNode = tabNodes.item(l);
                                            if (tabNode.getNodeType() == Node.ELEMENT_NODE) {
                                                tabName = tabNode.getAttributes().getNamedItem("name").getNodeValue();
                                                formConfiguration.setTab(formName, tabName);
                                                // field-list
                                                NodeList fieldNodes = tabNode.getChildNodes();
                                                for (int m=0; m<fieldNodes.getLength(); m++) {
                                                    // field
                                                    Node fieldNode = fieldNodes.item(m);
                                                    if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
                                                        if (fieldNode.getNodeName().equals("field")) {
                                                            this.parseFieldNode(fieldNode, form, formName, tabName);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
            } catch (Exception e) {
                logger.error("Gui:getElements() "+e);
            }
        }
    }

    public void parseFieldNode(Node node, String form, String formName, String tabName) {
        
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }
        
        if (form.equalsIgnoreCase(FORM_SEARCH)) {
            // Search fields
            SearchFormField searchFormField = new SearchFormField();
            
            try {
                searchFormField.setName(node.getAttributes().getNamedItem("name").getNodeValue());
                searchFormField.setType(SearchFormField.Type.valueOf(node.getAttributes().getNamedItem("type").getNodeValue()));
            }
            catch (Exception ex) {
                logger.error(ex);
            }
            
            // Map visible search fields to backend search fields.
            // In this case Solr is the search provider, 
            // and fields from Solr schema are mapped via the organization configuration.
            NodeList subNodes = node.getChildNodes();
            for (int j=0; j<subNodes.getLength(); j++) {
                Node subNode = subNodes.item(j);
                if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                    // Solr fields bound to the search field
                    if ("solr".equalsIgnoreCase(subNode.getNodeName())) {
                        NodeList solrFieldNodes = subNode.getChildNodes();
                        for (int k=0; k<solrFieldNodes.getLength(); k++) {
                            Node solrFieldNode = solrFieldNodes.item(k);
                            if (solrFieldNode.getNodeType() == Node.ELEMENT_NODE) {
                                SolrField solrField = new SolrField();
                                solrField.setName(solrFieldNode.getAttributes().getNamedItem("name").getNodeValue());
                                if (solrFieldNode.getAttributes().getNamedItem("boost") != null) {
                                    solrField.setBoost(Integer.parseInt(solrFieldNode.getAttributes().getNamedItem("boost").getNodeValue()));
                                }
                                if (solrFieldNode.getAttributes().getNamedItem("ngramBoost") != null) {
                                    solrField.setNgramBoost(Integer.parseInt(solrFieldNode.getAttributes().getNamedItem("ngramBoost").getNodeValue()));
                                }
                                // Read solr fields bound to freetext field in conf file 
                                if ("freetext".equalsIgnoreCase(subNode.getParentNode().getAttributes().getNamedItem("name").getNodeValue())) {
                                    this.searchConfiguration.getFreetextField().add(solrField);
                                } else {
                                    // Solr fields bound to rest of the fields
                                    searchFormField.getSolrFields().add(solrField);
                                }
                                
                            }
                        }
                    }
                    if ("subFields".equalsIgnoreCase(subNode.getNodeName())) {

                        NodeList subNodes2 = subNode.getChildNodes();
                        for (int l=0; l<subNodes2.getLength(); l++) {
                            Node subNode2 = subNodes2.item(l);
                            if (subNode2.getNodeType() == Node.ELEMENT_NODE) {
                                
                                SearchFormField subField = new SearchFormField();
                                subField.setName(subNode2.getAttributes().getNamedItem("name").getNodeValue());
                                subField.setType(SearchFormField.Type.valueOf(subNode2.getAttributes().getNamedItem("type").getNodeValue()));
                                
                                // Solr fields bound to the search field
                                if ("solr".equalsIgnoreCase(subNode2.getNodeName())) {
                                    NodeList solrFieldNodes = subNode2.getChildNodes();
                                    for (int m=0; m<solrFieldNodes.getLength(); m++) {
                                        Node solrFieldNode = solrFieldNodes.item(m);
                                        if (solrFieldNode.getNodeType() == Node.ELEMENT_NODE) {
                                            SolrField solrField = new SolrField();
                                            solrField.setName(solrFieldNode.getAttributes().getNamedItem("name").getNodeValue());
                                            if (solrFieldNode.getAttributes().getNamedItem("boost") != null) {
                                                solrField.setBoost(Integer.parseInt(solrFieldNode.getAttributes().getNamedItem("boost").getNodeValue()));
                                            }
                                            if (solrFieldNode.getAttributes().getNamedItem("ngramBoost") != null) {
                                                solrField.setNgramBoost(Integer.parseInt(solrFieldNode.getAttributes().getNamedItem("ngramBoost").getNodeValue()));
                                            }
                                            subField.getSolrFields().add(solrField);
                                        }
                                    }
                                }
                                searchFormField.getSubFields().put(subField.getName(), subField);
                            }
                        }
                    }
                }
            }
            // Add parsed field information to search configuration wrapper
            searchConfiguration.getSearchForm().put(searchFormField.getName(), searchFormField);
            
        } else {    // All other forms
            String name = null;
            FormElement.FieldType fieldType = FormElement.FieldType.undefined;
            List<String> enumValues = null;
            boolean isReadonly = false;
            boolean isGenereted = false;
            boolean isRequired = false;
            String multivalue = "false";
            String source = null;
            String nestingStyle = null;
            Vector<FormElement> nestedElements = new Vector<FormElement>();
            
            // current element name
            name = node.getAttributes().getNamedItem("element").getNodeValue();
            
            if (node.getAttributes().getNamedItem("readonly") != null) {
                isReadonly = Boolean.parseBoolean(node.getAttributes().getNamedItem("readonly").getNodeValue());
            }
            if (node.getAttributes().getNamedItem("generated") != null) {
                isGenereted = Boolean.parseBoolean(node.getAttributes().getNamedItem("generated").getNodeValue());
            }
            if (node.getAttributes().getNamedItem("required") != null) {
                isRequired = Boolean.parseBoolean(node.getAttributes().getNamedItem("required").getNodeValue());
            }
            if (node.getAttributes().getNamedItem("type") != null) {
                fieldType = FormElement.FieldType.getEnum(node.getAttributes().getNamedItem("type").getNodeValue());
                
                if (fieldType.equals(FormElement.FieldType.select) || fieldType.equals(FormElement.FieldType.units)) {
                    // read enum values
                    enumValues = getOptionsList(node, "item");
                    
                } else if (fieldType.equals(FormElement.FieldType.nested) 
                           || fieldType.equals(FormElement.FieldType.timerange)
                           || fieldType.equals(FormElement.FieldType.linked)) {
                    
                	if (node.getAttributes().getNamedItem("nestingStyle") != null) {
                		nestingStyle = node.getAttributes().getNamedItem("nestingStyle").getNodeValue();
                	}
                	
                    NodeList nestedFields = node.getChildNodes();
                    for (int j=0; j<nestedFields.getLength(); j++) {
                        Node nestedNode = nestedFields.item(j);
                        if (nestedNode.getNodeType() == Node.ELEMENT_NODE) {
                            FormElement nestedElement = new FormElement(); 
                            nestedElement.setName(nestedNode.getAttributes().getNamedItem("element").getNodeValue());
                            nestedElement.setReadonly(Boolean.parseBoolean(nestedNode.getAttributes().getNamedItem("readonly").getNodeValue()));
                            nestedElement.setRequired(Boolean.parseBoolean(nestedNode.getAttributes().getNamedItem("required").getNodeValue()));
                            nestedElement.setFieldType(FormElement.FieldType.getEnum(nestedNode.getAttributes().getNamedItem("type").getNodeValue()));
                            if (nestedNode.getAttributes().getNamedItem("type").getNodeValue().equalsIgnoreCase(FormElement.FieldType.select.toString())) {
                                nestedElement.setEnumValues(getOptionsList(nestedNode, "item"));
                            }
                            nestedElements.add(nestedElement);
                        }
                    }
                    
                }
            }
            if (node.getAttributes().getNamedItem("multivalue") != null) {
                
                switch (node.getAttributes().getNamedItem("multivalue").getNodeValue()) {
                    case FormElement.MULTIVALUE_DELIMITED:
                        multivalue = FormElement.MULTIVALUE_DELIMITED;
                        // do something
                        break;
                    case FormElement.MULTIVALUE_MULTIFIELD:
                        multivalue = FormElement.MULTIVALUE_MULTIFIELD;
                        // do something
                        break;
                    default:
                        multivalue = FormElement.MULTIVALUE_NONE; 
                        break;
                }
            }
            if (node.getAttributes().getNamedItem("source") != null) {
                source = node.getAttributes().getNamedItem("source").getNodeValue();
            }
            
            FormElement formElement = new FormElement(name, 
                                                      formName, 
                                                      tabName, 
                                                      fieldType, 
                                                      enumValues, 
                                                      isReadonly, 
                                                      isGenereted, 
                                                      isRequired, 
                                                      multivalue, 
                                                      source, 
                                                      nestingStyle,
                                                      nestedElements);

            formConfiguration.setFormElement(formName, formElement);
        }
    }
    
    /**
     * Get elements to be shown in the form
     * @param formName      name of the form
     * @return              vector of elements
     */
    public LinkedHashMap<String, FormElement> getGUIElements(String formName) {
        return this.formConfiguration.getForm(formName);
    }
    
    /**
     * Get tabs to be shown in the form
     * @param formName      name of the form
     * @return              vector of tab names
     */
    public Vector<String> getGUITabs(String formName) {
        return this.formConfiguration.getTabs(formName);
    }
    
    /**
     * Get enumerated items for field element
     * @param node      field element node
     * @return          list of enumerated items
     */
    private List<String> getOptionsList(Node node, String nodeName) {
        List<String> itemList = new ArrayList<String>();
        
        if (node != null) {
            NodeList nodes = node.getChildNodes();
            
            for (int i=0; i<nodes.getLength(); i++) {
                Node currentNode = nodes.item(i);
                if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
                    // check node name
                    if (currentNode.getNodeName().equalsIgnoreCase(nodeName)) {
                        if (!currentNode.getTextContent().isEmpty()) {
                            itemList.add(currentNode.getTextContent());
                        }
                    } else if (currentNode.getNodeName().equalsIgnoreCase("authoritative_list")) {
                        return getOptionsList(currentNode, nodeName);
                    }
                }
            }
            
        }
        return itemList;
    }

    /**
     * Find ParentNode by type
     * @param node      current node
     * @param nodeType  type of node (f.ex. form, tab, etc.)
     * @return
     * @throws Exception 
     */
    private Node getParentNode(Node node, String nodeType)
    {
        Node parentNode = null;
        if (node.getParentNode() != null) {
            parentNode = node.getParentNode();
            String parentNodeType = node.getParentNode().getNodeName();

            // check if types match
            if (nodeType != parentNodeType) {
                return getParentNode(parentNode, nodeType);
            }
            
        } else {
            parentNode = null;
        }

        return parentNode;
    }
    
    public void getLayout(Document doc, OsaActionBeanContext context) {
        try {
        	NodeList layoutSettings = doc.getElementsByTagName("layout");
        	for (int i=0; i<layoutSettings.getLength(); i++){
        		Node item = layoutSettings.item(i);
        		if (item.getNodeType() == Node.ELEMENT_NODE){
        			NodeList childNodes = item.getChildNodes();
        			for (int j=0; j<childNodes.getLength(); j++) {
        				Node subItem = childNodes.item(j);
        				if (subItem.getNodeName().equals("cssFile")) {
        					this.setCssFile(subItem.getFirstChild().getNodeValue());
        				}
        				if (subItem.getNodeName().equals("logoFile")) {
        					this.setLogoFile(subItem.getFirstChild().getNodeValue());
        					//TODO do empty check for alt attribute
        					this.setLogoFileAlt(subItem.getAttributes().getNamedItem("alt").getNodeValue());
        				}
        				if (subItem.getNodeName().equals("applicationTitle")) {
        					if (subItem.getFirstChild().getNodeValue() != null) {
        						this.setAppTitle(subItem.getFirstChild().getNodeValue());
        					}
        				}
        				
        				// Organization specific header files
        				if (subItem.getNodeName().equals("headerJsp")) {
        					NodeList headerFiles = subItem.getChildNodes();
                			for (int k = 0; k < headerFiles.getLength(); k++) {
                				Node headerFile = headerFiles.item(k);
                				if (headerFile.getNodeName().equals("mainPage")) {
                					// First check if config has setting for header file
                					if(headerFile.getFirstChild().getNodeValue() != null) {
                						File main = new File(context.getServletContext().getRealPath(headerFile.getFirstChild().getNodeValue()));
                						// Then check if file set in config actually exists
                						if (main.exists() && !main.isDirectory()) {
                							this.setMainHeader(headerFile.getFirstChild().getNodeValue());
                						}
                					}
                				}
                				if (headerFile.getNodeName().equals("subPage")) {
                					// First check if config has setting for header file
                					if(headerFile.getFirstChild().getNodeValue() != null) {
                						File sub = new File(context.getServletContext().getRealPath(headerFile.getFirstChild().getNodeValue()));
                						// Then check if file set in config actually exists
                						if (sub.exists() && !sub.isDirectory()) {
                							this.setSubHeader(headerFile.getFirstChild().getNodeValue());
                						}
                					}
                				}
                			}
        				}
        			}
        		}
        	}
        } catch (Exception e) {
        	
        }
    }
    
    /**
     * Reads upload settings from organization config xml
     * @param doc  document (config xml)
     * @return
     */
    public void getUploadConfig(Document doc) {
        try {
        	NodeList uploadSettings = doc.getElementsByTagName("upload");
        
        	for (int i=0; i<uploadSettings.getLength(); i++) {
        		Node item = uploadSettings.item(i);
        		if (item.getNodeType() == Node.ELEMENT_NODE) {
        			NodeList childNodes = item.getChildNodes();
        			for (int j=0; j<childNodes.getLength(); j++) {
        				Node subItem = childNodes.item(j);
        				if (subItem.getNodeName().equalsIgnoreCase("queue")) {
        					this.setQueueSize(Integer.parseInt(subItem.getAttributes().getNamedItem("size").getNodeValue()));
        				}
        			}			
        		}	
        	}
        	
        } catch(Exception e) {
        	logger.error(e);
        }
    }
    
    /**
     * Reads initial basecollection settings from organization config xml
     * @param doc  document (config xml)
     * @return
     */
    public void getIdGenerationConfig(Document doc) {
        try {
            NodeList uploadSettings = doc.getElementsByTagName("idgeneration");
        
            for (int i=0; i<uploadSettings.getLength(); i++) {
                Node item = uploadSettings.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    NodeList childNodes = item.getChildNodes();
                    for (int j=0; j<childNodes.getLength(); j++) {
                        Node subItem = childNodes.item(j);
                        if (subItem.getNodeName().equalsIgnoreCase("basecollection")) {
                            this.setInitialBasecollectionId(Integer.parseInt(subItem.getAttributes().getNamedItem("id").getNodeValue()));
                        }
                    }           
                }   
            }
            
        } catch(Exception e) {
            logger.error(e);
        }
    }
    
    public void getFacetFieldsFromConf(Document doc) {
        
        Vector<String> facetfields = new Vector<String>();
        try {
        	NodeList facetSettings = doc.getElementsByTagName("facets");
        	
        	for (int i=0; i<facetSettings.getLength(); i++) {
        		Node item = facetSettings.item(i);
        		if (item.getNodeType() == Node.ELEMENT_NODE) {
        			NodeList childNodes = item.getChildNodes();
        			for (int j=0; j<childNodes.getLength(); j++) {
        				Node subItem = childNodes.item(j);
        				if (subItem.getNodeName().equalsIgnoreCase("facetField")) {
        					facetfields.add(subItem.getAttributes().getNamedItem("name").getNodeValue());
        				}
        			}			
        		}	
        	}
        	searchConfiguration.setFacetFields(facetfields);
        
        } catch (Exception e) {
        	logger.error(e);
        }
    }
    
    public void getFacetFieldsforAdvBrowse(Document doc) {
        
        Vector<String> facetfields = new Vector<String>();
        try {
        	NodeList facetSettings = doc.getElementsByTagName("enabledFacets");
        	
        	for (int i=0; i<facetSettings.getLength(); i++) {
        		Node item = facetSettings.item(i);
        		if (item.getNodeType() == Node.ELEMENT_NODE) {
        			NodeList childNodes = item.getChildNodes();
        			for (int j=0; j<childNodes.getLength(); j++) {
        				Node subItem = childNodes.item(j);
        				if (subItem.getNodeName().equalsIgnoreCase("facetField")) {
        					facetfields.add(subItem.getAttributes().getNamedItem("name").getNodeValue());
        				}
        			}			
        		}	
        	}
        	
        	searchConfiguration.setAdvBrowseFacetFields(facetfields);
        
        } catch (Exception e) {
        	logger.error(e);
        }
    }
    
    public List<String> getAllObjects() {
        return this.formConfiguration.getAllForms();
    }
    
    public void getIngestForms(Document doc) {
        try {

            formConfiguration = new FormConfiguration();
            getElements(doc, "field", "ingest");
            // map elements to datastreams
            this.mapMetadataElements(doc);
            
            // check that
            List<String> contentmodels = Osa.fedoraManager.findAllObjects("osa-system:contentmodel-*");
            for (String cm : contentmodels) {
                String formName = cm.replace(RepositoryManager.CONTENTMODEL, "");
                if (formConfiguration.getForm(formName) == null) {
                    logger.error("Gui:getIngestForms() missing definitions for form: "+formName+" in "+doc.getDocumentURI());
                }
            }

        } catch (Exception e) {
            logger.error("failed to read: "+doc.getDocumentURI()+", "+e);
        }
    }
    
    /**
     * Read elements to the search form
     * @param xmlFile   xml file for the organization
     */
    public void getSearchForm(Document doc) {
        try {
            getElements(doc, "field", Gui.FORM_SEARCH);

        } catch (Exception e) {
            logger.error("failed to read: "+doc.getDocumentURI()+", "+e);
        }
    }
    
    public void getResultLayout(Document doc) {
    	try {
    		NodeList nodes = doc.getElementsByTagName("results");
            for (int i=0; i<nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                	searchResults = new ColumnResult(SearchResultConfiguration.Type.columns);
                	
                	NodeList subNodes = node.getChildNodes(); // results-tag's children
                	for (int j=0; j<subNodes.getLength(); j++) {
                		Node subNode = subNodes.item(j);
                		if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                			if ("resultLayout".equalsIgnoreCase(subNode.getNodeName())) {
                				searchResults.getLayoutTypelist().add(subNode.getAttributes().getNamedItem("type").getNodeValue());
                				if (subNode.getAttributes().getNamedItem("defaultSort") != null) {
                					searchResults.setDefaultSort(subNode.getAttributes().getNamedItem("defaultSort").getNodeValue());
                				}
                				
                				NodeList layoutSubnodes = subNode.getChildNodes(); // results children's children
                            	for (int k=0; k<layoutSubnodes.getLength(); k++) {
                            		Node layoutSubnode = layoutSubnodes.item(k);
                            		if (layoutSubnode.getNodeType() == Node.ELEMENT_NODE) {
                            			NamedNodeMap attributes = layoutSubnode.getAttributes();
                                        Vector<String> headerVector = new Vector<String>();
                                        
                                        String name 	 = attributes.getNamedItem("name").getNodeValue();
                                        String headerkey = attributes.getNamedItem("headerKey").getNodeValue();
                                        String sortable  = attributes.getNamedItem("sortable").getNodeValue();
                                        String width 	 = attributes.getNamedItem("width").getNodeValue();
                                        
                                        headerVector.add(name);
                                        headerVector.add(headerkey);
                                        headerVector.add(sortable);
                                        headerVector.add(width);
                                        
                                        searchResults.getHeaderKeys().add(headerVector);
                                        searchResults.getColumnNames().add(name);
                            		}             		
                            	}
                			}
                			
                  			if ("paging".equalsIgnoreCase(subNode.getNodeName())) {
                				NodeList layoutSubnodes = subNode.getChildNodes();
                            	for (int l=0; l<layoutSubnodes.getLength(); l++) {
                            		Node layoutSubnode = layoutSubnodes.item(l);
                            		if (layoutSubnode.getNodeType() == Node.ELEMENT_NODE) {
                            		    if (layoutSubnode.getNodeName().equalsIgnoreCase("rows")) {
                            		        searchResults.setRows(Integer.parseInt(layoutSubnode.getFirstChild().getNodeValue()));
                            			}
                            			
                            		    if (layoutSubnode.getNodeName().equalsIgnoreCase("images")) {
                            				
                            			}
                              		}
                            	}
                			}
                		}
                	}
                }
            }
            
    	} catch (Exception e) {
    		logger.error("ERROR reading xml: " + e.getMessage()); 
    	}
    }
    
    /**
     * Returns text content of the tag
     * @param doc           xml document
     * @param tagName       tag name
     * @return              text content of the tag
     */
    public String getTextContent(Document doc, String tagName) {
        String retValue = null;
        NodeList nodes = doc.getElementsByTagName(tagName);
        
        for (int i=0; i<nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Element) {
                retValue = node.getTextContent();
            }
        }
        
        return retValue;
    }
    
    /**
     * Maps metadata elements to the datastreams
     */
    public void mapMetadataElements(Document doc) {
        try {
            NodeList nodes = null;
            NodeList metadataNodes = doc.getElementsByTagName("metadatamapping");
            for (int i=0; i<metadataNodes.getLength(); i++) {
                Node metadataNode = metadataNodes.item(i);
                if (metadataNode.getNodeType() == Node.ELEMENT_NODE) {
                    nodes = metadataNode.getChildNodes();
                }
            }
            
            for (int i=0; i<nodes.getLength(); i++) {
                Node node = nodes.item(i);
                
                String name = null;
                MetaDataElement.MetaDataType eType = null;
                // Enumerated values for field
                List<String> enumValues = null;
                
                if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals("element")) { 
                    if (node.getAttributes().getNamedItem("name") != null) {
                        name = node.getAttributes().getNamedItem("name").getNodeValue();
                    }
                    
                    // Get datastreams
                    NodeList dataStreamNodes = node.getChildNodes();
                    for (int j=0; j<dataStreamNodes.getLength(); j++) {
                        Node dsNode = dataStreamNodes.item(j);
                        
                        if (dsNode.getNodeType() == Node.ELEMENT_NODE) {
                            // check node name
                            if (dsNode.getNodeName().equalsIgnoreCase("mapping")) {
                                if (dsNode.getAttributes().getNamedItem("schema") != null) {
                                    String dataStream = dsNode.getAttributes().getNamedItem("schema").getNodeValue();
                                    String displayName = dsNode.getAttributes().getNamedItem("name").getNodeValue();
                                    
                                    MetaDataElement metaDataElement = new MetaDataElement(name, displayName, null, eType, null, null, null, dataStream);
                                    metaDataElement.setEnumValues(enumValues);
                                    
                                    // add datastream for form element in GUI
                                    mapDataStreams.put(name, metaDataElement);
                                }
                            }
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }        
    }
    
	/**
	 * @return the searchConfiguration
	 */
	public SearchConfiguration getSearchConfiguration() {
		return searchConfiguration;
	}

	/**
	 * @param searchConfiguration the searchConfiguration to set
	 */
	public void setSearchConfiguration(SearchConfiguration searchConfiguration) {
		this.searchConfiguration = searchConfiguration;
	}

	public String getCssFile() {
		return cssFile;
	}

	public void setCssFile(String cssFile) {
		this.cssFile = cssFile;
	}

	public String getLogoFile() {
		return logoFile;
	}

	public void setLogoFile(String logoFile) {
		this.logoFile = logoFile;
	}
	
	/**
	 * @return the searchResults
	 */
	public SearchResultConfiguration getSearchResults() {
		return searchResults;
	}

	public void setSearchResultType() {
		if (resultType.equals("columns")) {
			this.searchResults.setType(SearchResultConfiguration.Type.columns);
		} else if (resultType.equals("grid")) {
			this.searchResults.setType(SearchResultConfiguration.Type.grid);
		} else {
			this.searchResults.setType(SearchResultConfiguration.Type.columns);
		}
	}
	
	/**
	 * @param searchResults the searchResults to set
	 */
	public void setSearchResults(SearchResultConfiguration searchResults) {
		this.searchResults = searchResults;
	}

	public String getResultType() {
		return resultType;
	}

	public void setResultType(String resultType) {
		this.resultType = resultType;
	}
	
	// TODO Refactor to somewhere else
	public int getQueueSize() {
		return queueSize;
	}

	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}
	
	public int getInitialBasecollectionId() {
        return initialBasecollectionId;
    }

    public void setInitialBasecollectionId(int value) {
        this.initialBasecollectionId = value;
    }

	public String getMethod() {
		return disposeMethod;
	}

	public void setMethod(String method) {
		this.disposeMethod = method;
	}

	public boolean isDisposeChilds() {
		return disposeChilds;
	}

	public void setDisposeChilds(boolean disposeChilds) {
		this.disposeChilds = disposeChilds;
	}

	public String getLogoFileAlt() {
		return logoFileAlt;
	}

	public void setLogoFileAlt(String logoFileAlt) {
		this.logoFileAlt = logoFileAlt;
	}

	public String getMainHeader() {
		return mainHeader;
	}

	public void setMainHeader(String mainHeader) {
		this.mainHeader = mainHeader;
	}

	public String getSubHeader() {
		return subHeader;
	}

	public void setSubHeader(String subHeader) {
		this.subHeader = subHeader;
	}

	public String getAppTitle() {
		return appTitle;
	}

	public void setAppTitle(String appTitle) {
		this.appTitle = appTitle;
	}	
}