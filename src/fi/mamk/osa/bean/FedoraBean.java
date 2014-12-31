package fi.mamk.osa.bean;

import fi.mamk.osa.bean.DCBean;
import fi.mamk.osa.bean.MetaDataElement;
import fi.mamk.osa.fedora.RepositoryManager;
import fi.mamk.osa.ui.FormElement;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.ValidatorHandler;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Class for Fedora Object
 * Object consists of several datastreams.
 * Each datastream has MetaDataElement-container.
 */
public class FedoraBean {
    
    private static final Logger logger = Logger.getLogger(FedoraBean.class);
    public static final String DATASTREAM_DC          = "DC";
    public static final String DATASTREAM_CAPTURE     = "CAPTURE";
    public static final String DATASTREAM_AUDIT_TRAIL = "AUDIT_TRAIL";
    public static final String DATASTREAM_RELS_EXT    = "RELS-EXT";
    public static final String DATASTREAM_RELS_INT    = "RELS-INT";
    public static final String DATASTREAM_THUMB       = "THUMB";
    public static final String DATASTREAM_ATTACHMENT  = "ATTACHMENT";
    public static final String DATASTREAM_MANAGEMENT  = "MANAGEMENT";
    
    public static final String DATASTREAM_ORIGINAL = "ORIGINAL";
    public static final String DATASTREAM_ORIGINAL_LABEL = "-O";
    public static final String DATASTREAM_THUMB_LABEL = "-P";
    public static final String DATASTREAM_ATTACHMENT_LABEL = "-A";
    
    public static final String OBJ_LABEL        = "objLabel";
    public static final String OBJ_CONTENTMODEL = "model";
    public static final String OBJ_CREATIONDATE = "objCreateDate";
    public static final String OBJ_LASTMODIFIED = "objLastModDate";
    public static final String OBJ_OWNERID      = "objOwnerId";
    public static final String OBJ_STATE        = "objState";
    public static final String OBJ_DISSINDEX    = "objDissIndexViewURL";
    public static final String OBJ_ITEMINDEX    = "objItemIndexViewURL";

    // datastreams
    DCBean dublinCoreData = null;
    CaptureBean captureData = null;
    OriginalBean original = null;
    RelationsBean relations = null;
    ThumbBean thumb = null;
    AttachmentBean attachment = null;
    ManagementBean management = null;
    
    // common object data
    private String objectLabel;
    private String objectContentModel;
    private String objectCreationDate;
    private String objectLastModified;
    private String objectOwnedId;
    private DataStream.State eObjectState;
    
    public String PID;    
    public String type; // document, image, etc.
    public String oldParent;
    
    public boolean bUpdateCaptureDatastream;
    public boolean bUpdateDcDatastream;
    public boolean bUpdateReslExtDatastream;
    public boolean bUpdateReslIntDatastream;
    public boolean bUpdateManagementDatastream;

    // Default constructor
    public FedoraBean() {
        PID = null;
        type = null;
        oldParent = null;
        dublinCoreData = new DCBean(PID);
        captureData = new CaptureBean(PID);
        original = new OriginalBean(PID);
        relations = new RelationsBean(PID);
        thumb = new ThumbBean(PID);
        attachment = new AttachmentBean(PID);
        management = new ManagementBean(PID);
        bUpdateCaptureDatastream = false;
        bUpdateDcDatastream = false;
        bUpdateReslExtDatastream = false;
        bUpdateReslIntDatastream = false;
        bUpdateManagementDatastream = false;
    }
    
    // Constructor
    public FedoraBean(String PID) {
        this.setPID(PID);
        this.setTypeFromPID(PID);
        oldParent = null;
        dublinCoreData = new DCBean(PID);
        captureData = new CaptureBean(PID);
        original = new OriginalBean(PID);
        relations = new RelationsBean(PID);
        thumb = new ThumbBean(PID);
        attachment = new AttachmentBean(PID);
        management = new ManagementBean(PID);
        bUpdateCaptureDatastream = false;
        bUpdateDcDatastream = false;
        bUpdateReslExtDatastream = false;
        bUpdateReslIntDatastream = false;
        bUpdateManagementDatastream = false;
    }
    
    // Constructor
    public FedoraBean(String PID, String type) {
        this.setPID(PID);
        this.type = type;
        oldParent = null;
        dublinCoreData = new DCBean(PID);
        captureData = new CaptureBean(PID);
        original = new OriginalBean(PID);
        relations = new RelationsBean(PID);
        thumb = new ThumbBean(PID);
        attachment = new AttachmentBean(PID);
        management = new ManagementBean(PID);
        bUpdateCaptureDatastream = false;
        bUpdateDcDatastream = false;
        bUpdateReslExtDatastream = false;
        bUpdateReslIntDatastream = false;
        bUpdateManagementDatastream = false;
    }
    
    /**
     * Get content model
     * @return      name of content model
     */
    public String getContentModel() {
        
        String model = null;
        
        if (this.type != null) {
            model = RepositoryManager.CONTENTMODEL + this.type;
        }
        
        return model;
    }
    
    /**
     * Find datastream according to datastream id
     * @param dataStreamID
     * @return
     */
    public DataStream getDataStream(String dataStreamID) {
        
        if (dataStreamID.equalsIgnoreCase(DATASTREAM_DC)) {
            return this.dublinCoreData;
            
        } else if (dataStreamID.equalsIgnoreCase(DATASTREAM_CAPTURE)) {
            return this.captureData;
            
        } else if (dataStreamID.equalsIgnoreCase(DATASTREAM_ORIGINAL)) {
            return this.original;
            
        } else if (dataStreamID.equalsIgnoreCase(DATASTREAM_RELS_EXT)) {
            return this.relations;
            
        } else if (dataStreamID.equalsIgnoreCase(DATASTREAM_THUMB)) {
            return this.thumb;
            
        } else if (dataStreamID.equalsIgnoreCase(DATASTREAM_ATTACHMENT)) {
            return this.attachment;
            
        } else if (dataStreamID.equalsIgnoreCase(DATASTREAM_MANAGEMENT)) {
            return this.management;
            
        }
        
        return null;
    }
    
    public String getObjectValue(String name) {
        
        String strValue = null;
        
        if (FedoraBean.OBJ_LABEL.equals(name)) {
            strValue = this.objectLabel;
            
        } else if (FedoraBean.OBJ_CONTENTMODEL.equals(name)) {
            strValue = this.objectContentModel;
            
        } else if (FedoraBean.OBJ_CREATIONDATE.equals(name)) {
            strValue = this.objectCreationDate;
            
        } else if (FedoraBean.OBJ_LASTMODIFIED.equals(name)) {
            strValue = this.objectLastModified;
            
        } else if (FedoraBean.OBJ_OWNERID.equals(name)) {
            strValue = this.objectOwnedId;
            
        } else if (FedoraBean.OBJ_STATE.equals(name)){
            strValue = this.eObjectState.toString();
            
        }
        
        return strValue;
    }
    
    /**
     * Set object profile related data
     * @param name      object variable name
     * @param value     object variable value
     */
    public void setObjectData(String name, String value) {
        
        if (FedoraBean.OBJ_LABEL.equals(name)) {
            this.objectLabel = value;
            
        } else if (FedoraBean.OBJ_CONTENTMODEL.equals(name)) {
            this.objectContentModel = value;
            
        } else if (FedoraBean.OBJ_CREATIONDATE.equals(name)) {
            this.objectCreationDate = value;
            
        } else if (FedoraBean.OBJ_LASTMODIFIED.equals(name)) {
            this.objectLastModified = value;
            
        } else if (FedoraBean.OBJ_OWNERID.equals(name)) {
            this.objectOwnedId = value;
            
        } else if (FedoraBean.OBJ_STATE.equals(name)) {
            this.eObjectState = DataStream.State.getEnum(value.toString());
            
        }
    }
    
    public String getPID() { return this.PID; }
    
    public void setPID(String value) { this.PID = value; }
    
    public String getType() { return this.type; }
    
    public void setType(String value) { 
        this.type = value; 
        // if type is general collection
        if (type.equals(RepositoryManager.COLLECTION)) {
            // set exact collection type (base-, group- or unitcollection)
            if (captureData.getMetaDataElements().containsKey(CaptureBean.TYPE)) {
                type = captureData.getMetaDataElements().get(CaptureBean.TYPE).getValue();
            }
        }
    }
    
    public String getOldParent() { return this.oldParent; }
    
    public void setOldParent(String value) { this.oldParent = value; }
       
    /**
     * Convert datastream to XML
     * @param dataStreamID      datastream id
     * @param schemaDocument    schema for object type
     * @return XML datastream as String
     */
    public String getXMLDataStream(String dataStreamID, Document schemaDocument) throws SAXException {

        String OSA_NS = "http://www.mamk.fi";
        String DS_NS = "";
        String ELEMENT_NS = "";
        String retValue = "";
        LinkedHashMap<String, MetaDataElement> elements = null;
        Element rootElement = null;
        String rootElementName = "";
        
        if (dataStreamID.equalsIgnoreCase(DATASTREAM_DC)) {
            
            elements = this.getDataStream(DATASTREAM_DC).getMetaDataElements();
            rootElementName = dataStreamID;
            DS_NS = "http://www.mamk.fi/dc";
            ELEMENT_NS = "dc:";
            
        } else if (dataStreamID.equalsIgnoreCase(DATASTREAM_CAPTURE)) {
            
            // sort elements according to schema
            this.sortDataStream(DATASTREAM_CAPTURE, schemaDocument);
            
            elements = this.getDataStream(DATASTREAM_CAPTURE).getMetaDataElements();
            rootElementName = dataStreamID.toLowerCase();
            ELEMENT_NS = "c:";
            
        } else if (dataStreamID.equalsIgnoreCase(DATASTREAM_MANAGEMENT)) {
            
            elements = this.getDataStream(DATASTREAM_MANAGEMENT).getMetaDataElements();
            rootElementName = dataStreamID.toLowerCase();
            ELEMENT_NS = "m:";
        }
        
        try {
            
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            docFactory.setNamespaceAware(true);
            docFactory.setValidating(true);
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = null;
            
            if (dataStreamID.equalsIgnoreCase(DATASTREAM_DC) 
                || dataStreamID.equalsIgnoreCase(DATASTREAM_RELS_EXT)
                )
            {
                // root element
                doc = docBuilder.newDocument();
                rootElement = doc.createElement(rootElementName);
                doc.appendChild(rootElement);
                
            } else if (dataStreamID.equalsIgnoreCase(DATASTREAM_CAPTURE)
                       || dataStreamID.equalsIgnoreCase(DATASTREAM_MANAGEMENT)
                       ) 
            {
                // root element
                doc = docBuilder.newDocument();
                rootElement = doc.createElement("osa:"+rootElementName);
                doc.appendChild(rootElement);

                // add additional namespaces to the root element
                doc.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xs", "http://www.w3.org/2001/XMLSchema");
                doc.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:osa", OSA_NS);
                
                if (dataStreamID.equalsIgnoreCase(DATASTREAM_CAPTURE)) {
                    doc.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:c", OSA_NS);
                } else if (dataStreamID.equalsIgnoreCase(DATASTREAM_MANAGEMENT)) {
                    doc.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:m", OSA_NS);
                }
            }
            
            Iterator<Entry<String, MetaDataElement>> iter = elements.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, MetaDataElement> entry = (Map.Entry<String, MetaDataElement>) iter.next();
                MetaDataElement element = entry.getValue();
                
                // Handle multivalues
                // ingest form
                if (element.getMultivalueType().equals(FormElement.MULTIVALUE_DELIMITED) && element.getSource() == null) {
                    String[] multivalues= element.getValue().split(",");
                    for (int i=0; i<multivalues.length; i++) {
                        Element xmlElement = doc.createElementNS(DS_NS, ELEMENT_NS+element.getVisibleName());
                        xmlElement.appendChild(doc.createTextNode(multivalues[i].trim())); // using trim() to remove possible leading and trailing spaces from splitted values
                        doc.getDocumentElement().appendChild(xmlElement);
                    }
                } else if (element.getMultivalueType().equals(FormElement.MULTIVALUE_MULTIFIELD)
                           || !element.getNestedElements().isEmpty()) {
                    // parent element
                    Element parentXmlElement = null;
                    // child elements in hashmap
                    Vector<LinkedHashMap<String, MetaDataElement>> nestedElements = element.getNestedElements();
                    
                    for (int i=0; i<nestedElements.size(); i++) {
                        LinkedHashMap<String, MetaDataElement> lhm = nestedElements.get(i);
                        
                        if (lhm != null) {
                            Iterator<Entry<String, MetaDataElement>> iterLhm = lhm.entrySet().iterator();
                            int index = 0;
                            
                            while (iterLhm.hasNext()) {
                                Map.Entry<String, MetaDataElement> entryLhm = (Map.Entry<String, MetaDataElement>) iterLhm.next();
                                MetaDataElement nestedElement = entryLhm.getValue();
                                
                                if (nestedElement.getName() != null && nestedElement.getValue() != null) {
                                    
                                    if (index == 0) {
                                        // parent element
                                        parentXmlElement = doc.createElementNS(DS_NS, ELEMENT_NS+element.getVisibleName());
                                        // add parent element
                                        doc.getDocumentElement().appendChild(parentXmlElement);
                                    }
                                    // add child elements
                                    Element childElement = doc.createElement(ELEMENT_NS+nestedElement.getName());
                                    childElement.appendChild(doc.createTextNode(nestedElement.getValue()));
                                    parentXmlElement.appendChild(childElement);
                                }
                                index++;
                            }
                        }
                    }
                // move object
                // create element
                } else if (element.getValues().size() > 0) {
                    for (int i=0; i<element.getValues().size(); i++) {
                        if (element.getValues().get(i) != null) {
                            Element xmlElement = doc.createElementNS(DS_NS, ELEMENT_NS+element.getVisibleName());
                            xmlElement.appendChild(doc.createTextNode(element.getValues().get(i)));
                            doc.getDocumentElement().appendChild(xmlElement);
                        }
                    }
                }
            }
                        
            // write the content
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);    
            StreamResult result = new StreamResult(new StringWriter());
            transformer.transform(source, result);
            
            retValue = result.getWriter().toString();
            
            // validate capture datastream
            if (dataStreamID.equalsIgnoreCase(DATASTREAM_CAPTURE) && type != null) {
                this.validateDatastream(retValue, schemaDocument);
            }
            
        } catch (ParserConfigurationException e) {
            logger.error("FedoraBean:getXMLDataStream error: "+e);
        } catch (TransformerException e) {
            logger.error("FedoraBean:getXMLDataStream error: "+e);
        } catch (DOMException e) {
            logger.error("FedoraBean:getXMLDataStream error: "+e);
        }
        return retValue;
    }
    
    /**
     * Set type from Pid
     * @param pid
     */
    public void setTypeFromPID(String pid) {
        
        if (pid != null && pid.contains(":")) {
            
            String pidType = pid.split(":")[1].split("-")[0];
            if (RepositoryManager.DATATYPES_MAP.containsKey(pidType)) {
                // get object type from HashMap
                this.type = RepositoryManager.DATATYPES_MAP.get(pidType);
            } else {
                this.type = RepositoryManager.DOCUMENT;
            }
            
            // handle default_types as document
            if (pidType.equalsIgnoreCase("X")) {
                this.type = RepositoryManager.DOCUMENT;
            }
        }
    }
    
    /**
     * Validate datastream according to schema
     * @param strDocument   datastream content
     * @param schemaDoc     schema for object type (document|image|etc.)
     */
    public void validateDatastream(String strDocument, Document schemaDoc) throws SAXException {
        
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance(); 
            spf.setNamespaceAware(true);
            XMLReader reader = spf.newSAXParser().getXMLReader(); 
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            XMLSerializer serializer = new XMLSerializer(outputStream, new OutputFormat(schemaDoc));
            serializer.serialize(schemaDoc);
            InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
            SchemaFactory xsFact = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = xsFact.newSchema(new StreamSource(is));
            
            // Validator
            ValidatorHandler vh = schema.newValidatorHandler(); 
            reader.setContentHandler(vh);
            
            InputSource inputSource = new InputSource(new StringReader(strDocument));
            reader.parse(inputSource);
            
        } catch (IOException e) {
            // instance document is invalid!
            logger.error("FedoraBean:validateDatastream, capture datastream is invalid: "+e);
        } catch (ParserConfigurationException e) {
            // instance document is invalid!
            logger.error("FedoraBean:validateDatastream, capture datastream is invalid: "+e);
        }
    }
    
    public CaptureBean getCaptureData() {
        return captureData;
    }

    public void setCaptureData(CaptureBean captureData) {
        this.captureData = captureData;
    }
    
    public void removeOriginalDatastream() {
        this.original = null;
    }
    
    /**
     * Re-arranges datastream according to the order of elements in schema
     * @param ds                datastream
     * @param schemaDocument    schema for object type (groupcollection|document|image|etc.)
     */
    public void sortDataStream(String ds, Document schemaDocument) {
        
        LinkedHashMap<String, MetaDataElement> elements = this.getDataStream(ds).getMetaDataElements();
        LinkedHashMap<String, MetaDataElement> orderedElements = new LinkedHashMap<String, MetaDataElement>();
        
        XPath xPath = XPathFactory.newInstance().newXPath();
        String expression = "//element[@name='capture']//element";
        NodeList result = null;

        try {
            // Elements in schema
            result= (NodeList) xPath.compile(expression).evaluate(schemaDocument, XPathConstants.NODESET);
            
        } catch (Exception e) {
            logger.error("FedoraBean:sortDataStream error, "+e);
        }
        
        // re-arrange datastream
        for (int i=0; i<result.getLength(); i++) {
            Element e = (Element)result.item(i);
            String key = e.getAttribute("name");
            MetaDataElement element = null;
            element = elements.get(key);
            
            if (element == null) {
                // check if name and visible name differs
                Iterator<Entry<String, MetaDataElement>> iter = elements.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, MetaDataElement> entry = (Map.Entry<String, MetaDataElement>) iter.next();
                    MetaDataElement mdElement = entry.getValue();
                    if (mdElement.getVisibleName() != null && key.equals(mdElement.getVisibleName())) {
                        element = mdElement;
                        break;
                    }
                }                
            }
            
            if (element != null) {
                orderedElements.put(key, element);
            }
        }
        
        // update datastream 
        this.captureData.setMetaDataElements(orderedElements);
    }
    
}
