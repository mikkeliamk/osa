package fi.mamk.osa.fedora;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.FileUtils;
import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.FedoraCredentials;
import com.yourmediashelf.fedora.client.request.FedoraRequest;
import com.yourmediashelf.fedora.client.request.Ingest;
import com.yourmediashelf.fedora.client.request.ListDatastreams;
import com.yourmediashelf.fedora.client.response.FedoraResponse;
import com.yourmediashelf.fedora.client.response.FindObjectsResponse;
import com.yourmediashelf.fedora.client.response.GetDatastreamHistoryResponse;
import com.yourmediashelf.fedora.client.response.GetDatastreamResponse;
import com.yourmediashelf.fedora.client.response.GetObjectHistoryResponse;
import com.yourmediashelf.fedora.client.response.IngestResponse;
import com.yourmediashelf.fedora.client.response.ListDatastreamsResponse;
import com.yourmediashelf.fedora.client.response.RiSearchResponse;
import com.yourmediashelf.fedora.generated.access.DatastreamType;
import com.yourmediashelf.fedora.generated.access.FedoraObjectHistory;
import com.yourmediashelf.fedora.generated.management.DatastreamProfile;

import fi.mamk.osa.solr.SolrManager;
import fi.mamk.osa.ui.FormElement;
import fi.mamk.osa.ui.Gui;
import fi.mamk.osa.auth.AccessRight;
import fi.mamk.osa.auth.User;
import fi.mamk.osa.bean.AttachmentBean;
import fi.mamk.osa.bean.CaptureBean;
import fi.mamk.osa.bean.DCBean;
import fi.mamk.osa.bean.DataStream;
import fi.mamk.osa.bean.DataStream.State;
import fi.mamk.osa.bean.FedoraBean;
import fi.mamk.osa.bean.ManagementBean;
import fi.mamk.osa.bean.MetaDataElement;
import fi.mamk.osa.bean.MetaDataElement.MetaDataType;
import fi.mamk.osa.bean.ThumbBean;
import fi.mamk.osa.core.DestructionPlan;
import fi.mamk.osa.core.Osa;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.jpedal.exception.PdfException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Class for handling fedora3-client
 *
 */
public class Fedora3Manager extends RepositoryManager {

    public static final String DATASTREAM         = "datastream";
    public static final String DATASTREAM_CONTENT = "datastream_content";
    public static final String OBJECT             = "object";
    
    String previousTime;
    int pidcount = 0;
    public String schemaPath;
    
    private static final Logger logger = Logger.getLogger(Fedora3Manager.class);
    private FedoraCredentials fedoracredential = null;
    private FedoraClient fedoraClient = null;
    
    /**
     * Constructor
     */
    public Fedora3Manager(String host, String port, String uname, String pword, String path) {
        baseUrl = "http://"+host+":"+port+"/fedora/";
        username = uname;
        password = pword;
        schemaPath = path;
        
        try {
            
            fedoracredential = new FedoraCredentials(baseUrl, username, password);
            fedoraClient = new FedoraClient(fedoracredential);
            
            if (fedoraClient != null) {
                // set this instance of as the default for all requests
                FedoraRequest.setDefaultClient(fedoraClient);
            }
            
        } catch (Exception e) {
            logger.error("Fedora3Manager error: Failed to construct Fedora3Manager, "+e );
        }
    }
    
    /**
     * Create datastream for attachment
     * @param file    attachment
     * @param pid     A persistent, unique identifier for the object
     * @param fObject
     * @param user    user name
     */
    public void createAttachmentDatastream(File file, String pid, FedoraBean fObject, String user) {
        
        if (file != null) {
            // FILE
            try {
                
                String dataStreamName = FedoraBean.DATASTREAM_ATTACHMENT;
                String strIndex = "";
                
                if (fObject != null) {
                    
                    List<String> dsList = listDataStreams(pid);
                    String currentDataStreamId = null;
                                        
                    // get latest attachment datastream
                    for (int i=0; i<dsList.size(); i++) {
                        currentDataStreamId = dsList.get(i).toString();
                        if (currentDataStreamId.equalsIgnoreCase(FedoraBean.DATASTREAM_ATTACHMENT)) {
                            int index = 2;
                            strIndex = Integer.toString(index);
                            dataStreamName = currentDataStreamId + strIndex;
                            
                        } else if (currentDataStreamId.startsWith(FedoraBean.DATASTREAM_ATTACHMENT)) {
                            int number = Integer.parseInt(currentDataStreamId.substring(FedoraBean.DATASTREAM_ATTACHMENT.length(),currentDataStreamId.length()));
                            number++;
                            strIndex = Integer.toString(number);
                            dataStreamName = FedoraBean.DATASTREAM_ATTACHMENT + strIndex;
                        }
                    }
                }

                FedoraClient.addDatastream(pid, dataStreamName)
                            .versionable(false)
                            .dsLabel(pid + FedoraBean.DATASTREAM_ATTACHMENT_LABEL + strIndex + "." + FilenameUtils.getExtension(file.getName())+" ("+user+")")
                            .controlGroup("M")
                            .content(file)
                            .execute(fedoraClient);
         
                createPreviewDatastream(file.getAbsolutePath(), pid, dataStreamName, user);
                
                // add RELS-INT relationship
                String subject = String.format("info:fedora/%s/%s", pid, dataStreamName);
                // rdf-about/predicate
                String rdfAbout = "info:fedora/fedora-system:def/relations-internal#isAttachmentOf";
                // rdf-resource/object
                String rdfResource = "info:fedora/"+pid+"/"+FedoraBean.DATASTREAM_ORIGINAL;
                FedoraClient.addRelationship(subject).predicate(rdfAbout).object(rdfResource).execute(fedoraClient);
                
            } catch (FedoraClientException e) {
                logger.error("Fedora3Manager error: creating datastream for attachment: "+e);
            }
            
            //logger.info("Fedora3Manager: attachment "+file.getAbsolutePath()+" added");
        }
    }
    
    /**
     * createPreview for documents
     * @param pdfPathAndFileName    absolute path 
     * @param pid                   A persistent, unique identifier for the object
     * @throws IOException
     * @throws PdfException
     */
    public void createPreviewDatastream(String filePath, String pid, String datastreamName, String user) {
        
        // Creating a preview is an optional task.
        // Failing to create one is not a critical error.
        try {
            // create thumb file
        	File thumbFile = null;
            String filename = FilenameUtils.getName(filePath);
            filename = FilenameUtils.removeExtension(filename);
            String path = FilenameUtils.getPath(filePath);
            
            thumbFile = new File("/"+path + ThumbBean.THUMB_DSID+"_"+filename+"."+ThumbBean.THUMB_EXTENSION);
	        
	        if (!thumbFile.exists()) {
	            path = path.replace("ingest", "upload");
	            thumbFile = new File("/"+path + ThumbBean.THUMB_DSID+"_"+filename+"."+ThumbBean.THUMB_EXTENSION);
	        }
	        
	        // create label: pid + -PA | -PA1
	        String label = pid + FedoraBean.DATASTREAM_THUMB_LABEL;
	        if (datastreamName.equals(FedoraBean.DATASTREAM_ORIGINAL)) {
	            label += "O";
	        } else if (datastreamName.equals(FedoraBean.DATASTREAM_ATTACHMENT)) {
	            label += "A";
	        } else if (datastreamName.startsWith(FedoraBean.DATASTREAM_ATTACHMENT)) {
	            label += "A" + datastreamName.charAt(datastreamName.length()-1);
	        }
	        label += "." + FilenameUtils.getExtension(thumbFile.getName());
	        
            // add to datastream and remove
            if (thumbFile.canRead()) {
                try {
                    // NOTE: Ensure DC, RELS-EXT and RELS-INT are versionable if using Managed Content
                    FedoraClient.addDatastream(pid, FedoraBean.DATASTREAM_THUMB+"_"+datastreamName)
                                .versionable(false)
                                .dsLabel(label+" ("+user+")")
                                .controlGroup("M")
                                .content(thumbFile)
                                .execute(fedoraClient);
                    
                    // add RELS-INT relationship
                    String subject = String.format("info:fedora/%s/%s", pid, FedoraBean.DATASTREAM_THUMB+"_"+datastreamName);
                    // rdf-about/predicate
                    String rdfAbout = "info:fedora/fedora-system:def/relations-internal#isThumbnailOf";
                    // rdf-resource/object
                    String rdfResource = "info:fedora/"+pid+"/"+datastreamName;
                    FedoraClient.addRelationship(subject).predicate(rdfAbout).object(rdfResource).execute(fedoraClient);
                    
                } catch (FedoraClientException e) {
                    logger.error("Fedora3Manager error: createPreviewDatastream() "+e.getMessage());
                }
                
            	thumbFile.delete();
            	//logger.info("Fedora3Manager: Thumbnail file for "+filePath+" added");
            }
        } catch (Exception ex) {
            logger.error("Fedora3Manager: Failed to create thumbnail preview. " + ex.getMessage());
        }
    }
    
    /**
     * Creates FedoraBean object from metadata elements from GUI
     * @param bIngest       ingest vs. update
     * @param pid           A persistent, unique identifier for the object
     * @param type          object type
     * @param lhmElements   metadata elements in linked hashmap
     * @return
     */
    public FedoraBean createFedoraBean(boolean bIngest, User user, Gui userGui, String pid, String type, LinkedHashMap<String, MetaDataElement> lhmElements) {
      
        FedoraBean fedoraBean = new FedoraBean(pid, type);
        
        if (lhmElements.containsKey(CaptureBean.TITLE)) {
            String title = lhmElements.get(CaptureBean.TITLE).getValue();
            fedoraBean.setObjectData(FedoraBean.OBJ_LABEL, title);
        }
                
        Iterator<Entry<String, MetaDataElement>> iter = lhmElements.entrySet().iterator();
        
        // Go through metadata elements from GUI
        while (iter.hasNext()) {
            
            Map.Entry<String, MetaDataElement> entry = (Map.Entry<String, MetaDataElement>) iter.next();
            MetaDataElement element = entry.getValue();
            
            // TODO: check this
            if (element.getValue() == null && element.getValues().size() > 0) {
                String value = element.getValues().get(0);
                if (value != null) {
                    element.setValue(value);
                }
            }
            
            if (element != null && element.getName() != null 
                && (element.getValue() != null 
                    || !element.getNestedElements().isEmpty()) 
                    || element.getGenerated() == true) {
                
                String name = element.getName();
                MetaDataType eType = element.getMetaDataType();
                
                if (element.getGenerated()) {
                    // value set to be generated automatically, if empty
                    if (name.equalsIgnoreCase(CaptureBean.IDENTIFIER)) {
                        String parentPid = lhmElements.get(CaptureBean.RELATION_IsPartOf).getValue();
                        
                        if (bIngest && type.endsWith(Fedora3Manager.COLLECTION)) {
                            Osa.dbManager.get("sql").initIdGeneration(pid, 0);
                        }

                        if (element.getValue() == null) {
                            String idValue = "";
                            // get id of parent object
                            String parentId = Osa.searchManager.getObjectValue(parentPid, SolrManager.SOLRFIELD_Id, user);
                            if (parentId != "") {
                                idValue = parentId+".";
                            }
                            // get next available id below parent object
                            String nextId = Osa.dbManager.get("sql").getNextId(parentPid);
                            idValue = idValue.concat(nextId);
                            element.setValue(idValue);
                            
                        }
                    } else {
                        continue;
                    }
                }
                
                // Get datastream(s) to metadata element from configuration
                List<String> datastreams = userGui.getDataStreamMappings(name);
                
                // One metadata may belong to several datastreams
                for (int j=0; j<datastreams.size(); j++) {
                    String datastream = datastreams.get(j).toString();
                    
                    // Get visible name for attribute in the current datastream from configuration (osa-system:contentmodel-*)
                    String visibleName = userGui.getVisibleName(name, datastream);
                    element.setVisibleName(visibleName);
                    // set metadata element to the datastream of fedoraBean
                    fedoraBean.getDataStream(datastream).setMetaDataElement(element);
                    
                    if (eType.equals(MetaDataType.relation)) {
                        // update relation to its datastream
                        fedoraBean.getDataStream(FedoraBean.DATASTREAM_RELS_EXT).setMetaDataElement(element);
                    }
                }
            }
        }
        
        // set ancestor metadata to management-datastream
        CaptureBean captureDatastream = (CaptureBean)fedoraBean.getDataStream(FedoraBean.DATASTREAM_CAPTURE);
        String parent = captureDatastream.getParent();
        ManagementBean managementDatastream = (ManagementBean)fedoraBean.getDataStream(FedoraBean.DATASTREAM_MANAGEMENT);
        
        // set ancestor
        if (parent == null || parent.contains(":root")) {
            // object is ancestor itself
            managementDatastream.setAncestor(pid);
            
        } else {
            String newAncestor = this.getAncestor(parent);
            managementDatastream.setAncestor(newAncestor); 
        }
        
        // set path metadata to management-datastream
        String path = this.getPath(parent);
        if (path.endsWith("/")) {
            path = path + pid;
        } else {
            path = path+"/"+pid;
        }
        managementDatastream.setPath(path);
        
        return fedoraBean;
    }

    /**
     * Create root object and static collections for context objects (actions, agents, events, places)
     * @param root      organization name
     * @param userGui   holds information of datastream mappings
     */
    public void createRootObject(String rootName, Gui userGui, User user) {
        // f.ex. mamk:root
        rootName = rootName.toUpperCase();
        String rootPid = rootName+":root";
        String contextsPid = rootName+RepositoryManager.PID_COLLECTION_CONTEXTS;
        String agentsPid = rootName+RepositoryManager.PID_COLLECTION_AGENTS;
        String actionsPid = rootName+RepositoryManager.PID_COLLECTION_ACTIONS;
        String tol2008ActionsPid = rootName+RepositoryManager.PID_COLLECTION_TOL2008ACTIONS;
        String lyActionsPid = rootName+RepositoryManager.PID_COLLECTION_LYACTIONS;
        String eventsPid = rootName+RepositoryManager.PID_COLLECTION_EVENTS;
        String placesPid = rootName+RepositoryManager.PID_COLLECTION_PLACES;
        IngestResponse response = null;
        LinkedHashMap<String, MetaDataElement> lhmElements = null;
        MetaDataElement mdElement = null;
        
        // Initialize id for basecollection in idGen-table in MariaDB
        Osa.dbManager.get("sql").initIdGeneration(rootPid, userGui.getInitialBasecollectionId());
        
        try {
            
            if (this.findObject(rootPid) == null) {
                response = new Ingest(rootPid).label(rootName).execute(fedoraClient);
                logger.info("Fedora3Manager: root object "+rootPid+" created.");
            }

            // get datastream mappings
            userGui.getGUIElements(RepositoryManager.BASECOLLECTION);
            
            // Collection for Contexts-object
            if (this.findObject(contextsPid) == null) {
                lhmElements = new LinkedHashMap<String, MetaDataElement>();
                // pid
                mdElement = new MetaDataElement("pid", "pid", contextsPid, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                lhmElements.put("pid", mdElement);
                // type
                mdElement = new MetaDataElement(CaptureBean.TYPE, CaptureBean.TYPE, RepositoryManager.BASECOLLECTION, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                lhmElements.put(CaptureBean.TYPE, mdElement);
                // title
                mdElement = new MetaDataElement(CaptureBean.TITLE, CaptureBean.TITLE, "Kontekstiobjektit", MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                lhmElements.put(CaptureBean.TITLE, mdElement);
                // accessRights
                mdElement = new MetaDataElement(CaptureBean.PUBLICITY_LEVEL, CaptureBean.PUBLICITY_LEVEL, AccessRight.PUBLICITYLEVEL_Public, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                lhmElements.put(CaptureBean.PUBLICITY_LEVEL, mdElement);
                // relation
                mdElement = new MetaDataElement(CaptureBean.RELATION_IsPartOf, CaptureBean.RELATION_IsPartOf, rootPid, MetaDataType.relation, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                lhmElements.put(CaptureBean.RELATION_IsPartOf, mdElement);
                this.ingest(userGui, user, rootName, null, null, RepositoryManager.BASECOLLECTION, contextsPid, lhmElements, null);
                logger.info("Fedora3Manager: object "+contextsPid+" created.");
            }
            
            if (this.findObject(contextsPid) != null) {
                // Collection for Agent-objects
                if (this.findObject(agentsPid) == null) {
                    lhmElements = new LinkedHashMap<String, MetaDataElement>();
                    // pid
                    mdElement = new MetaDataElement("pid", "pid", agentsPid, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put("pid", mdElement);
                    // type
                    mdElement = new MetaDataElement(CaptureBean.TYPE, CaptureBean.TYPE, RepositoryManager.GROUPCOLLECTION, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.TYPE, mdElement);
                    // title
                    mdElement = new MetaDataElement(CaptureBean.TITLE, CaptureBean.TITLE, "Toimijat", MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.TITLE, mdElement);
                    // accessRights
                    mdElement = new MetaDataElement(CaptureBean.PUBLICITY_LEVEL, CaptureBean.PUBLICITY_LEVEL, AccessRight.PUBLICITYLEVEL_Public, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.PUBLICITY_LEVEL, mdElement);
                    // relation          
                    mdElement = new MetaDataElement(CaptureBean.RELATION_IsPartOf, CaptureBean.RELATION_IsPartOf, contextsPid, MetaDataType.relation, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.RELATION_IsPartOf, mdElement);
                    this.ingest(userGui, user, rootName, null, null, RepositoryManager.GROUPCOLLECTION, agentsPid, lhmElements, null);
                    logger.info("Fedora3Manager: object "+agentsPid+" created.");
                }
                
                // Collection for Action-objects
                if (this.findObject(actionsPid) == null) {
                    lhmElements = new LinkedHashMap<String, MetaDataElement>();
                    // pid
                    mdElement = new MetaDataElement("pid", "pid", actionsPid, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put("pid", mdElement);
                    // type
                    mdElement = new MetaDataElement(CaptureBean.TYPE, CaptureBean.TYPE, RepositoryManager.GROUPCOLLECTION, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.TYPE, mdElement);
                    // title
                    mdElement = new MetaDataElement(CaptureBean.TITLE, CaptureBean.TITLE, "Toiminnat", MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.TITLE, mdElement);
                    // accessRights
                    mdElement = new MetaDataElement(CaptureBean.PUBLICITY_LEVEL, CaptureBean.PUBLICITY_LEVEL, AccessRight.PUBLICITYLEVEL_Public, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.PUBLICITY_LEVEL, mdElement);
                    // relation           
                    mdElement = new MetaDataElement(CaptureBean.RELATION_IsPartOf, CaptureBean.RELATION_IsPartOf, contextsPid, MetaDataType.relation, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.RELATION_IsPartOf, mdElement);
                    this.ingest(userGui, user, rootName, null, null, RepositoryManager.GROUPCOLLECTION, actionsPid, lhmElements, null);
                    logger.info("Fedora3Manager: object "+actionsPid+" created.");
                }
                
                // Collection for TOL 2008 actions (used for basecollection metadata)
                if (this.findObject(tol2008ActionsPid) == null) {
                    lhmElements = new LinkedHashMap<String, MetaDataElement>();
                    // pid
                    mdElement = new MetaDataElement("pid", "pid", tol2008ActionsPid, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put("pid", mdElement);
                    // type
                    mdElement = new MetaDataElement(CaptureBean.TYPE, CaptureBean.TYPE, RepositoryManager.GROUPCOLLECTION, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.TYPE, mdElement);
                    // title
                    mdElement = new MetaDataElement(CaptureBean.TITLE, CaptureBean.TITLE, "TOL 2008", MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.TITLE, mdElement);
                    // accessRights
                    mdElement = new MetaDataElement(CaptureBean.PUBLICITY_LEVEL, CaptureBean.PUBLICITY_LEVEL, AccessRight.PUBLICITYLEVEL_Public, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.PUBLICITY_LEVEL, mdElement);
                    // relation           
                    mdElement = new MetaDataElement(CaptureBean.RELATION_IsPartOf, CaptureBean.RELATION_IsPartOf, actionsPid, MetaDataType.relation, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.RELATION_IsPartOf, mdElement);
                    this.ingest(userGui, user, rootName, null, null, RepositoryManager.GROUPCOLLECTION, tol2008ActionsPid, lhmElements, null);
                    logger.info("Fedora3Manager: object "+tol2008ActionsPid+" created.");
                }
                
                // Ingest TOL 2008 objects for actions
                if (this.findObject(tol2008ActionsPid) != null && this.findObject(rootName+":F-tol2008actionA") == null) {
                    String path = schemaPath+"wip/actionobjectsTOL";
                    File folder = new File(path);
                    File[] listOfFiles = folder.listFiles();
                    for (int i=0; i<listOfFiles.length; i++) {
                        File file = listOfFiles[i];
                        if (file.isFile() && file.getName().endsWith(".xml")) {
                            String fileLocation = path+"/"+file.getName();
                            this.ingestFile(rootName, fileLocation);
                        } 
                    }
                }
                
                // Collection for Liikearkistoyhdistys actions (used for group/unitcollection metadata)
                if (this.findObject(lyActionsPid) == null) {
                    lhmElements = new LinkedHashMap<String, MetaDataElement>();
                    // pid
                    mdElement = new MetaDataElement("pid", "pid", lyActionsPid, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put("pid", mdElement);
                    // type
                    mdElement = new MetaDataElement(CaptureBean.TYPE, CaptureBean.TYPE, RepositoryManager.GROUPCOLLECTION, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.TYPE, mdElement);
                    // title
                    mdElement = new MetaDataElement(CaptureBean.TITLE, CaptureBean.TITLE, "Liikearkistoyhdistys", MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.TITLE, mdElement);
                    // accessRights
                    mdElement = new MetaDataElement(CaptureBean.PUBLICITY_LEVEL, CaptureBean.PUBLICITY_LEVEL, AccessRight.PUBLICITYLEVEL_Public, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.PUBLICITY_LEVEL, mdElement);
                    // relation           
                    mdElement = new MetaDataElement(CaptureBean.RELATION_IsPartOf, CaptureBean.RELATION_IsPartOf, actionsPid, MetaDataType.relation, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.RELATION_IsPartOf, mdElement);
                    this.ingest(userGui, user, rootName, null, null, RepositoryManager.GROUPCOLLECTION, lyActionsPid, lhmElements, null);
                    logger.info("Fedora3Manager: object "+lyActionsPid+" created.");
                }
                    
                // Ingest Liikearkistoyhdistys objects for actions
                if (this.findObject(lyActionsPid) != null && this.findObject(rootName+":F-lyaction00") == null) {
                    String path = schemaPath+"wip/actionobjectsLY";
                    File folder = new File(path);
                    File[] listOfFiles = folder.listFiles();

                    for (int i=0; i<listOfFiles.length; i++) {
                        File file = listOfFiles[i];
                        if (file.isFile() && file.getName().endsWith(".xml")) {
                            String fileLocation = path+"/"+file.getName();
                            this.ingestFile(rootName, fileLocation);
                        } 
                    }                   
                }
                
                // Collection for Event-objects
                if (this.findObject(eventsPid) == null) {
                    lhmElements = new LinkedHashMap<String, MetaDataElement>();
                    // pid
                    mdElement = new MetaDataElement("pid", "pid", eventsPid, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put("pid", mdElement);
                    // type
                    mdElement = new MetaDataElement(CaptureBean.TYPE, CaptureBean.TYPE, RepositoryManager.GROUPCOLLECTION, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.TYPE, mdElement);
                    // title
                    mdElement = new MetaDataElement(CaptureBean.TITLE, CaptureBean.TITLE, "Tapahtumat", MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.TITLE, mdElement);
                    // accessRights
                    mdElement = new MetaDataElement(CaptureBean.PUBLICITY_LEVEL, CaptureBean.PUBLICITY_LEVEL, AccessRight.PUBLICITYLEVEL_Public, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.PUBLICITY_LEVEL, mdElement);
                    // relation
                    mdElement = new MetaDataElement(CaptureBean.RELATION_IsPartOf, CaptureBean.RELATION_IsPartOf, contextsPid, MetaDataType.relation, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.RELATION_IsPartOf, mdElement);
                    this.ingest(userGui, user, rootName, null, null, RepositoryManager.GROUPCOLLECTION, eventsPid, lhmElements, null);
                    logger.info("Fedora3Manager: object "+eventsPid+" created.");
                }
                
                // Collection for Place-objects
                if (this.findObject(placesPid) == null) {
                    lhmElements = new LinkedHashMap<String, MetaDataElement>();
                    // pid
                    mdElement = new MetaDataElement("pid", "pid", placesPid, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put("pid", mdElement);
                    // type
                    mdElement = new MetaDataElement(CaptureBean.TYPE, CaptureBean.TYPE, RepositoryManager.GROUPCOLLECTION, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.TYPE, mdElement);
                    // title
                    mdElement = new MetaDataElement(CaptureBean.TITLE, CaptureBean.TITLE, "Paikat", MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.TITLE, mdElement);
                    // accessRights
                    mdElement = new MetaDataElement(CaptureBean.PUBLICITY_LEVEL, CaptureBean.PUBLICITY_LEVEL, AccessRight.PUBLICITYLEVEL_Public, MetaDataType.string, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.PUBLICITY_LEVEL, mdElement);
                    // relation           
                    mdElement = new MetaDataElement(CaptureBean.RELATION_IsPartOf, CaptureBean.RELATION_IsPartOf, contextsPid, MetaDataType.relation, FormElement.MULTIVALUE_NONE, null, null, CaptureBean.CAPTURE_DSID);
                    lhmElements.put(CaptureBean.RELATION_IsPartOf, mdElement);
                    this.ingest(userGui, user, rootName, null, null, RepositoryManager.GROUPCOLLECTION, placesPid, lhmElements, null);
                    logger.info("Fedora3Manager: object "+placesPid+" created.");
                }
            }
            
        } catch (FedoraClientException e) {
            logger.error("Fedora3Manager error: "+e);
        } catch (SAXException e) {
            logger.error("Fedora3Manager error: "+e);
        }
        
        if (response != null && response.getStatus() == 200) {
            logger.info("Fedora3Manager: rootObject '"+rootPid+"' created.");
        }
    }
    
    public String getAuditTrail(String pid) {
    	String auditTrail = "";
    	int tabs = 0;
    	
    	try {
			FedoraResponse resp = FedoraClient.getObjectXML(pid).execute(fedoraClient);
			String response = resp.getEntity(String.class);
			String[] lines = response.split("\n");
			
			for (int i=0; i<lines.length; i++) {
				String line = lines[i];
				if (line.startsWith("<audit:auditTrail")) {
					
					while (!line.contains("</audit:auditTrail>")) {
						
						if (line.startsWith("</")) {
							tabs--;
						}
						
						for (int j=0; j<tabs; j++) {
							auditTrail += "\t";
						}

						if (line.startsWith("<") && !line.contains("</") && !line.contains("/>")) {
							tabs++;
						}
						
						auditTrail += line+"\n";
						i++;
						line = lines[i];
						
					}
					auditTrail += line;
					i = lines.length;
				}
			}			
		} catch (FedoraClientException e) {
		    logger.error("Fedora3Manager:getAuditTrail error: "+e);
		}
    	
    	return auditTrail;
    }
    
    /**
     * Delete object from Fedora repository
     * @param pid       PID
     * @return
     */
    public void deleteObject(String pid) {
        try {
            // Purge object
            FedoraClient.purgeObject(pid).execute(fedoraClient);
            logger.info("Fedora3Manager: object "+pid+" removed.");
            
        } catch (FedoraClientException e) {
            logger.error("Fedora3Manager error: "+e);
        }
    }
    
    /**
     * Dispose object from Fedora repository
     * @param pid         PID
     * @param plan        destruction plan for disposing
     * @return
     */
    public void createResidualObject(String pid, DestructionPlan plan) {

        List<String> datastreams = plan.getResidualDatastreams();
        List<String> objectDatastreams = this.listDataStreams(pid);
        
        // remove datastreams, that are not in DestructionPlan
        for (String objectDatastream : objectDatastreams) {
            if (!datastreams.contains(objectDatastream)) {
                this.deleteDataStream(pid, objectDatastream);
            }
        }
        
        // set object state
        this.setObjectState(pid, State.D);
        logger.info("Fedora3Manager: object "+pid+" removed and residual object created.");
    }
    
    /**
     * Delete root object 
     * @param rootName      organization name
     */
    public void deleteRootObject(String rootName) {
        try {
            String pid = rootName+":root";
            FedoraClient.purgeObject(pid).execute(fedoraClient);
            logger.info("Fedora3Manager: root object "+pid+" removed.");
            
        } catch (FedoraClientException e) {
            logger.error("Fedora3Manager error: "+e);
        }
    }
    
    public void deleteDataStream(String pid, String dsid) {
    	
    	try {
			FedoraClient.purgeDatastream(pid, dsid).execute(fedoraClient);
						
		} catch (FedoraClientException e) {
			logger.error("Fedora3Manager error: Cannot remove datastream: "+dsid+" from "+pid);
			e.printStackTrace();
		}
    }
    
    /**
     * Delete all objects
     * @param orgContext
     */
    public void deleteAllobjects(String orgContext) {
    	
    	List<String> pids = findAllObjects(orgContext);
    	
    	while (pids.size() > 1) {
			for (int i=0; i<pids.size(); i++) {
				if (!pids.get(i).equals(orgContext+":root")) {
		            try {
		                FedoraClient.purgeObject(pids.get(i)).execute(fedoraClient);
					} catch (FedoraClientException e) {
						logger.error("Fedora3Manager error: deleteAllobjects() "+e);
					}
		            logger.info("Fedora3Manager: object "+pids.get(i)+" removed.");
				}
			}
			pids = findAllObjects(orgContext);
    	}
    }
    
    /**
     * Deletes all objects below TOL 2008 Actions
     * @param orgContext        organization name
     * @param type              pid type of action object (":F-tol2008action*"/":F-lyaction*")
     */
    public void deleteActionObjects(String orgContext, String type) {

        List<String> pids = null;
        String queryString = "pid~"+orgContext+type;
        FindObjectsResponse response = null;
        
        try {
            response = FedoraClient.findObjects().pid().maxResults(200).query(queryString).execute(fedoraClient);
            pids = response.getPids();
            
            while (pids.size() > 0) {
                logger.info("Fedora3Manager: Removing "+pids.size()+" objects.");
                for (int i=0; i<pids.size(); i++) {
                    FedoraClient.purgeObject(pids.get(i)).execute(fedoraClient);
                    logger.info("Fedora3Manager: object "+pids.get(i)+" removed.");
                }
                response = FedoraClient.findObjects().pid().maxResults(200).query(queryString).execute(fedoraClient);
                pids = response.getPids();
                break;
            }
            
        } catch (FedoraClientException e) {
            logger.error("Fedora3Manager:deleteActionObjects error: "+e);
        }
    }
       
    /**
     * Function to search all objects by organization
     * @param orgContext
     * @return
     */
    public List<String> findAllObjects(String orgContext) {
    	
    	List<String> pids = null;
    	FindObjectsResponse response = null;

        try {
        	response = FedoraClient.findObjects().pid().query("pid~"+orgContext+"*").execute(fedoraClient);
        	pids = response.getPids();
        	
        } catch (FedoraClientException e) {
        	logger.error("Fedora3Manager:findAllObjects error: "+e);
        }
    		
        return pids;
    }
    
    /**
     * Search object from Fedora repository
     * @param pid       A persistent, unique identifier for the object
     */
    public FedoraBean findObject(String pid) {
        
        //logger.info("Fedora3Manager: searching object "+pid+" ");
        boolean bFound = false;
        FedoraBean fObject = new FedoraBean(pid);
        
        // get object related common data
        bFound = getData(Fedora3Manager.OBJECT, pid, "", fObject);
        
        if (!bFound) {
            // object not found
            return null;
        }
        
        List<String> dsList = listDataStreams(pid);
        String currentDataStreamId = null;

        // handle all datastreams for pid
        for (int i=0; i<dsList.size(); i++) {
            currentDataStreamId = dsList.get(i).toString();
            // read datastream profile data
            getData(Fedora3Manager.DATASTREAM, pid, currentDataStreamId, fObject);
            // read datastream content
            getData(Fedora3Manager.DATASTREAM_CONTENT, pid, currentDataStreamId, fObject);
        }
        
        // add object to vector
        vectFedoraObjects.add(fObject);
        return fObject;
    }
    
    /**
     * 
     * @param pid
     * @return
     */
    public String getChildren(String pid) {
    	
        String query = String.format("select $child from <#ri> where $child <fedora-rels-ext:isPartOf> <info:fedora/"+pid+">");        	
        String collectionObjs = "";
        
        try {
            RiSearchResponse response = null;
            response = FedoraClient.riSearch(query).lang("itql").format("json").execute(fedoraClient);
            String result = response.getEntity(String.class);
            collectionObjs = result;

        } catch (FedoraClientException e) {
            logger.error("Fedora3Manager error: "+e);
        }
        return collectionObjs;
    }
    
    /**
     * Gets all sub children of an object
     * @param pid	Pid of an parent object
     * @return Arraylist of children pids
     */
	public ArrayList<HashMap<String, String>> getAllChildren(String pid) {
	    
		String initChildren = this.getChildren(pid);
	    HashMap<String, ArrayList<HashMap<String, String>>> jsonElement = new flexjson.JSONDeserializer<HashMap<String, ArrayList<HashMap<String, String>>>>().deserialize(initChildren); 
	    ArrayList<HashMap<String, String>> resultlist = jsonElement.get("results");
	
		for (int i=0; i<resultlist.size(); i++) {
			String child = cleanEntry(resultlist.get(i).get("child"));
			HashMap<String, ArrayList<HashMap<String, String>>> jsonChildren = new flexjson.JSONDeserializer<HashMap<String, ArrayList<HashMap<String, String>>>>().deserialize(this.getChildren(child)); 
	        ArrayList<HashMap<String, String>> resultlist2 = jsonChildren.get("results");
	        
	        if (resultlist2.size() > 0) {
	        	resultlist.addAll(resultlist2);
	        }
		}
		return resultlist;
	}
	
    /**
     * Get root and all descendents
     * @param organization      user organization name
     */
    public String getCollectionObjects(String organization) {
        String query = String.format("select $parent $child from <#ri> " +
        		"where walk ($child <fedora-rels-ext:isPartOf> <info:fedora/"+organization+":root> and " +
        		"$child <fedora-rels-ext:isPartOf> $parent)");
        	
        String collectionObjs = "";
        
        try {
            RiSearchResponse response = null;
            response = FedoraClient.riSearch(query).lang("itql").format("json").execute(fedoraClient); 
            String result = response.getEntity(String.class);
            collectionObjs = result;
            
        } catch (FedoraClientException e) {
            logger.error("Fedora3Manager error: "+e);
        }
        
        return collectionObjs;
    }
    
    /**
     * Get schema from Fedora
     * schema is in datastream of object's contentmodel
     * @param type      object type (document|image|...)
     * @return          schema for type
     */
    public Document getSchema(String type, String organization) {

        Document doc = null;
        InputStream inputstream = null;
        FedoraResponse response = null;
        String pid = RepositoryManager.CONTENTMODEL + type;
        String dsID = "OSA-SCHEMA";
        
        try {
            // organization specific schema
            String dsID_org = dsID+"-"+organization;
            List<String> dsList = listDataStreams(pid);
            
            if (dsList.contains(dsID_org)) {
                // organization specific schema
                response = FedoraClient.getDatastreamDissemination(pid, dsID_org).execute(fedoraClient);
                
            } else {
                // default schema
                response = FedoraClient.getDatastreamDissemination(pid, dsID).execute(fedoraClient);
            }
            
            inputstream = response.getEntityInputStream();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(inputstream);
            
        } catch (FedoraClientException e) {
            logger.error("Fedora3Manager:getSchema error: "+e);
        } catch (Exception e) {
            logger.error("Fedora3Manager:getSchema error: "+e);
        }
        
        return doc;
    }
    
    /**
     * Function to get object and datastream content
     * @param pid       A persistent, unique identifier for the object
     * @param dsID      An identifier for the datastream that is unique within the digital object
     */
    private boolean getData(String dataType, String pid, String dsID, FedoraBean fObject) {
        boolean retValue = false;
        InputStream inputstream = null;
        
        if (pid == null) {
            return retValue;
        }
        
        try {
            
            if (dataType.equalsIgnoreCase(Fedora3Manager.OBJECT)) {
                FedoraResponse response = FedoraClient.getObjectProfile(pid).execute(fedoraClient);
                inputstream = response.getEntityInputStream();
                
            } else if (dataType.equalsIgnoreCase(Fedora3Manager.DATASTREAM)) {
                GetDatastreamResponse gdr = FedoraClient.getDatastream(pid, dsID).execute(fedoraClient);
                inputstream = gdr.getEntityInputStream();
                
            } else if (dataType.equalsIgnoreCase(Fedora3Manager.DATASTREAM_CONTENT)) {
                
                if (dsID.equalsIgnoreCase(FedoraBean.DATASTREAM_ORIGINAL)
                    || dsID.startsWith(FedoraBean.DATASTREAM_THUMB)
                    || dsID.startsWith(FedoraBean.DATASTREAM_ATTACHMENT)
                    ) {
                    
                    String value = baseUrl+"objects/"+pid+"/datastreams/"+dsID+"/content";
                    InputStream is = null;
                    String credentials = username + ":" + password;

                    try {
                        byte[] encoded = new Base64().encode(credentials.getBytes("UTF-8"));
                        String encoding = new String(encoded);
                        URL url = new URL(value);
                        URLConnection conn = url.openConnection();
                        conn.setDoInput(true);
                        conn.setRequestProperty("Authorization", String.format("Basic %s", encoding));
                        is = conn.getInputStream();
                    
                        if (dsID.startsWith(FedoraBean.DATASTREAM_ATTACHMENT) || dsID.equals(FedoraBean.DATASTREAM_ATTACHMENT)) {
                            fObject.getDataStream(FedoraBean.DATASTREAM_ATTACHMENT).setMetaDataElement(dsID, dsID, value, MetaDataType.string, null, null, is);
                        } else if (dsID.startsWith(FedoraBean.DATASTREAM_THUMB) || dsID.equals(FedoraBean.DATASTREAM_THUMB)) {
                            fObject.getDataStream(FedoraBean.DATASTREAM_THUMB).setMetaDataElement(dsID, dsID, value, MetaDataType.string, null, null, is);                            
                        } else {
                            fObject.getDataStream(FedoraBean.DATASTREAM_ORIGINAL).setMetaDataElement(dsID, dsID, value, MetaDataType.string, null, null, is);
                        }
                    
                    } catch (UnsupportedEncodingException e) {
                        logger.error("Fedora3Manager:getData error: "+e);
                    } catch (MalformedURLException e) {
                        logger.error("Fedora3Manager:getData error: "+e);
                    } catch (IOException e) {
                        logger.error("Fedora3Manager:getData error: "+e);
                    }
                    return true;
                    
                } else if (dsID.equalsIgnoreCase(FedoraBean.DATASTREAM_RELS_EXT)) {
                    // Handle RELS_EXT datastream
                    FedoraResponse response = FedoraClient.getRelationships(pid).execute(fedoraClient);
                    Model model = ModelFactory.createDefaultModel();
                    model.read(response.getEntityInputStream(), null, FileUtils.langXML);
                    
                    StmtIterator it = model.listStatements();
                    Statement s;
                    while (it.hasNext()) {
                        
                        s = (Statement) it.next();
                        // name of relation
                        String name = s.getPredicate().toString();
                        String[] nameParts = name.split("/");
                        name = nameParts[nameParts.length-1];
                        name = name.substring(name.indexOf("#")+1);
                        // relation to object
                        String value = s.getObject().toString();
                        String[] valueParts = value.split("/");
                        value = valueParts[valueParts.length-1];
                        
                        // store data to fedora object
                        fObject.getDataStream(dsID).setMetaDataElement(name, name, value, MetaDataType.relation);
                    }
                    return true;
                } else {
                    // Handle other datastreams
                    FedoraResponse response = FedoraClient.getDatastreamDissemination(pid, dsID).execute(fedoraClient);
                    inputstream = response.getEntityInputStream();
                }
            }
                            
            XMLStreamReader reader = null;
            String name = "";
            String value = "";
            String parent = "";

            try {
                reader = XMLInputFactory.newInstance().createXMLStreamReader(inputstream);    
                while (reader.hasNext()) {
                    value = "";
                    int eventType = reader.next();
                    
                    if (reader.isStartElement()) {
                        // node name
                        name = reader.getLocalName();                      
                        if (parent.equals("") && !reader.getLocalName().equalsIgnoreCase(FedoraBean.DATASTREAM_CAPTURE)) {
                            // set parent node name
                            parent = reader.getLocalName();
                        }
                        
                    } else if (reader.isEndElement()) { 
                        if (reader.getLocalName().equals(parent)) {
                            // clear parent node name
                            parent = "";
                        }
                    }
                    
                    if (eventType == XMLStreamConstants.CHARACTERS) {
                        if (!reader.getText().trim().isEmpty()) {
                            // node value
                            value = reader.getText().trim();
                        }
                    }
                    
                    if (!value.isEmpty()) {
                        
                        if (dataType.equalsIgnoreCase(Fedora3Manager.OBJECT)) {
                            fObject.setObjectData(name, value);
                            
                        } else if (dataType.equalsIgnoreCase(Fedora3Manager.DATASTREAM)) {
                            if (dsID.startsWith(FedoraBean.DATASTREAM_ATTACHMENT) || dsID.equals(FedoraBean.DATASTREAM_ATTACHMENT)) {
                                // set propertydata to several attachments
                                AttachmentBean currentAttachment = (AttachmentBean)fObject.getDataStream(FedoraBean.DATASTREAM_ATTACHMENT);
                                currentAttachment.setElementProperty(dsID, name, value);
                                
                            } else if (dsID.startsWith(FedoraBean.DATASTREAM_THUMB) || dsID.equals(FedoraBean.DATASTREAM_THUMB)) {
                                // set propertydata to several thumbs
                                ThumbBean currentThumb = (ThumbBean)fObject.getDataStream(FedoraBean.DATASTREAM_THUMB);
                                currentThumb.setElementProperty(dsID, name, value);
                                
                            } else if (fObject.getDataStream(dsID) != null) {
                                // store data to fedora object
                                fObject.getDataStream(dsID).setProperty(name, value);
                            }
                            
                        } else if (dataType.equalsIgnoreCase(Fedora3Manager.DATASTREAM_CONTENT)) {
                            
                            if (fObject.getDataStream(dsID) != null) {
                                
                                if (dsID.equalsIgnoreCase(FedoraBean.DATASTREAM_CAPTURE)
                                    && !parent.equals(name)) {
                                    // if parent node exists, add values to nested elements vector
                                    fObject.getDataStream(dsID).setNestedMetaDataElement(parent, name, name, value, MetaDataType.string);
                                } else {
                                    // store data to fedora object                                
                                    fObject.getDataStream(dsID).setMetaDataElement(name, name, value, MetaDataType.string);
                                }
                            }
                        }
                    }
                }
                    
                // no more elements...
                reader.close();
                retValue = true;
                
            } catch (XMLStreamException e) {
                logger.error("Fedora3Manager:getDataFromDataStream XMLError: "+e);
            }

        } catch (FedoraClientException e) {
            logger.error("Fedora3Manager:getDataStream error: "+e);
        }
        
        return retValue;
    }
    
    /**
     * Generate PID (organization + mimetype + timestamp)
     * @param organizationName      organization of the user
     * @param mime                  object type
     * @return                      pid
     */
    public String getNextPID(String organizationName, String mime) {
        String PID = organizationName.toUpperCase() + ":";
        mime = mime.toLowerCase();
        boolean bFound = false;
        
        // get abbreviation for mime from HashMap
        for (Entry<String, String> entry : DATATYPES_MAP.entrySet()) {
            if (entry.getValue().equals(mime)) {
                PID = PID + entry.getKey();
                bFound = true;
                break;
            }
        }

        // if not found, check if collection
        if (!bFound && mime.endsWith(RepositoryManager.COLLECTION)) {
            PID = PID + RepositoryManager.DATATYPES_MAP_REVERSE.get(RepositoryManager.COLLECTION);
            bFound = true;
        }
        
        // if not found, use default value
        if (!bFound) {
            for (Entry<String, String> entry : DATATYPES_MAP.entrySet()) {
                if (entry.getValue().equals(RepositoryManager.DEFAULT_TYPE)) {
                    PID = PID + entry.getKey();
                    break;
                }
            }
        }
        
        String tstamp = String.valueOf(System.currentTimeMillis());
        
        if (tstamp.equals(this.previousTime)) {
            this.pidcount++;
            PID = PID +"-"+tstamp+"-"+pidcount;
        } else {
            this.previousTime = tstamp;
            this.pidcount = 0;
            PID = PID +"-"+tstamp+"-"+pidcount;
        }
        // add timestamp to PID
        
        return PID;
    }
    
    /**
     * Get history of the object
     * @param pid       The PID of the object.
     */
    public List<MetaDataElement> getObjectHistory(String pid) {
        FedoraObjectHistory history = null;
        List<MetaDataElement> listHistory = new ArrayList<MetaDataElement>();
        // get existing datastreams for pid
        List<String> existingDatastreams = listDataStreams(pid);
        int size = existingDatastreams.size();
        
        try {
            GetObjectHistoryResponse response = FedoraClient.getObjectHistory(pid).execute(fedoraClient);
            history = response.getObjectHistory();
            
            // Get all datastream profiles
            List<GetDatastreamHistoryResponse> listDsResponses = new ArrayList<GetDatastreamHistoryResponse>();
            GetDatastreamHistoryResponse dsResponse = null;

            for (int i=0; i<size; i++) {
                String currentDatastream = existingDatastreams.get(i);
                dsResponse = FedoraClient.getDatastreamHistory(pid, currentDatastream).execute(fedoraClient);
                listDsResponses.add(dsResponse);
            }
            
            // Get change dates
            List<String> changeDates = history.getObjectChangeDate();
            Iterator<String> iterChangeDates = changeDates.iterator();
            
            while (iterChangeDates.hasNext()) {
                String changeDate = (String) iterChangeDates.next();
                Boolean bFound = false;
                
                for (int i=0; i<listDsResponses.size(); i++) {
                    dsResponse = listDsResponses.get(i);
                    List<DatastreamProfile> listDsProfile = dsResponse.getDatastreamProfile().getDatastreamProfile();
                    Iterator<DatastreamProfile> iterDsProfile = listDsProfile.iterator();
                                        
                    if (dsResponse != null) {
                        
                        if (iterDsProfile != null) {
                            
                            while (iterDsProfile.hasNext()) {
                                DatastreamProfile profile = (DatastreamProfile) iterDsProfile.next();

                                // check if date of version history matches to datastream version
                                if (profile.getDsCreateDate().toString().matches(changeDate)) {
                                    // Link to current datastream version, read baseUrl in IngestAction
                                    // http://127.0.0.1:8080/fedora/objects/ORG:T-1234567890/datastreams/DC/content?asOfDateTime=2014-01-01T10:00:00.000Z
                                    String link = "objects/"+pid+"/datastreams/";
                                    link = link + profile.getDsID() + "/";
                                    link = link + "content?asOfDateTime=";
                                    link = link + changeDate;
                                    
                                    // date and label (modifier)
                                    String value = changeDate+"<br>"+profile.getDsLabel();
                                    MetaDataElement dateElement = new MetaDataElement("date", null, value, MetaDataType.date);
                                    listHistory.add(dateElement);
                                    
                                    // datastream version info and link
                                    MetaDataElement dsVersionElement = new MetaDataElement(profile.getDsVersionID(), null, link, MetaDataType.link);
                                    listHistory.add(dsVersionElement);
                                    
                                    bFound = true;
                                    
                                    break;
                                }
                            }
                        }
                        if (bFound == true) break;
                    }
                }
            }
            
        } catch (FedoraClientException e) {
            logger.error("Fedora3Manager:getObjectHistory error: "+e);
        }
        return listHistory;
    }
    
    /**
     * Ingest/update to the Fedora repository.
     * @param userGui             gui
     * @param user                user
     * @param organization        user organization
     * @param file                file to ingest
     * @param attachment          attachment to ingest
     * @param type                collection/doc/image etc.
     * @param pid                 identifier
     * @param lhmCaptureElements  metadata
     * @param oldFedoraBean       current object in repository
     */
    public String ingest(Gui userGui, 
                       User user, 
                       String organization, 
                       File file, 
                       List<File> attachment, 
                       String type, 
                       String pid, 
                       LinkedHashMap<String, MetaDataElement> lhmCaptureElements, 
                       FedoraBean oldFedoraBean) throws SAXException 
    {
        boolean bIngest = true;
        
        if (pid == null) {
            // generate new pid if the first ingest
            pid = getNextPID(organization, type);
        } else {
            if (pid.endsWith(PID_COLLECTION_ACTIONS) 
                || pid.endsWith(PID_COLLECTION_TOL2008ACTIONS)
                || pid.endsWith(PID_COLLECTION_LYACTIONS)
                || pid.endsWith(PID_COLLECTION_AGENTS)
                || pid.endsWith(PID_COLLECTION_EVENTS)
                || pid.endsWith(PID_COLLECTION_PLACES)
                || pid.endsWith(PID_COLLECTION_CONTEXTS)) {
                    bIngest = true;
                } else {
                    bIngest = false;
                }
            
            if (type == null || type == "") {
                type = Osa.searchManager.getObjectType(pid, user);
            } else if (type.equals(RepositoryManager.COLLECTION)) {
                // get exact type
                if (lhmCaptureElements.containsKey(CaptureBean.TYPE)) {
                    type = lhmCaptureElements.get(CaptureBean.TYPE).getValue();
                }
            }
        }
        
        // check isPartOf relation exist
        boolean bFound = false;
        
        if (lhmCaptureElements.get(CaptureBean.RELATION_IsPartOf) != null) {
            String value = lhmCaptureElements.get(CaptureBean.RELATION_IsPartOf).getValue();
            
            if (value != null) {
                bFound = true;
            } else if (value == null) {
                lhmCaptureElements.get(CaptureBean.RELATION_IsPartOf).setValue(organization+":root");
                bFound = true;
            }
        }
        
        if (!bFound) {
            // relation not found, add relation to 'organization:root'
            String name = CaptureBean.RELATION_IsPartOf;
            String value = organization+":root";//"info:fedora/"+organization+":root";
            MetaDataType eType = MetaDataType.relation;
            
            MetaDataElement mdElement = new MetaDataElement(name, name, value, eType);
            lhmCaptureElements.put(name, mdElement);
        }
        
        // Create new or update existing Fedora object
        FedoraBean newFedoraBean = this.createFedoraBean(bIngest, user, userGui, pid, type, lhmCaptureElements);
        
        // In case of update update check, which datastreams have been changed
        if (!bIngest) {
            newFedoraBean = checkIfDatastreamsModified(oldFedoraBean, newFedoraBean);
        }
        
        ingestObject(bIngest, user, organization, type, file, attachment, newFedoraBean);
        
        return pid;
    }
    
    /**
     * Ingest/update to the Fedora repository.
     * @param bIngest
     * @param userGui
     * @param organization
     * @param file
     * @param type
     * @param lhmElements
     */
    public void ingestWithPid(boolean bIngest, User user, Gui userGui, String organization, File file, String type, LinkedHashMap<String, MetaDataElement> lhmElements) {
        String pid = null;
        
        pid = lhmElements.get("pid").getValue();
        
        if (pid == null) {
            logger.error("Fedora3Manager error: ingest update without pid.");
            return;
        }
        
        // Create new or update existing Fedora object
        FedoraBean fedoraBean = this.createFedoraBean(bIngest, user, userGui, pid, type, lhmElements);
        try {
            ingestObject(bIngest, user, organization, type, file, null, fedoraBean);
        } catch (SAXException e) {
            logger.error("Fedora3Manager:ingestWithPid error: "+e);
        }
    }
    
    /**
     * Function to ingest file to Fedora repository
     * @param organization      organization name
     * @param path              file path
     * @return
     */
    public boolean ingestFile(String organization, String path) {
        IngestResponse response;
        File file = new File(path);
        File organizationFile = null;
        BufferedReader reader;
        
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = "";
            String text = "";
            String orgText = "XXXX";
            // files has PID f.ex. 'XXXX:F-tol2008actionA' to be replaced with organization name
            while ((line = reader.readLine()) != null) {
                if (line.contains(orgText)) {
                    line = line.replaceAll(orgText, organization);
                }
                text += line + "\r\n";
            }
            reader.close();
            
            // create temporary file for ingesting object to Fedora
            // file has now organization specific PID (f.ex. ORG:F-tol2008actionA)
            path = path.replace(".xml", "temp.xml");
            organizationFile = new File(path);
            FileWriter writer = new FileWriter(organizationFile);
            writer.write(text);
            writer.close();
            
            response = new Ingest().content(organizationFile).execute(fedoraClient);
            
        } catch (FileNotFoundException e) {
            logger.error("Fedora3Manager:ingestFile error: "+e);
        } catch (IOException e) {
            logger.error("Fedora3Manager:ingestFile error: "+e);
        } catch (FedoraClientException e) {
            logger.error("Fedora3Manager:ingestFile error: "+e);
        }
        
        organizationFile.delete();
        return true;
    }
    
    /**
     * Ingest/update object to the Fedora repository.
     * @param bIngest       ingest vs. update
     * @param user          user name
     * @param organization  name of the user organization
     * @param type          object type
     * @param file          object file
     * @param attachment    attachment to ingest
     * @param fedoraBean    fedora object to hold metadata
     * @return
     */
    public boolean ingestObject(boolean bIngest, User user, String organization, String type, File file, List<File> attachment, FedoraBean fedoraBean) throws SAXException {
        
        IngestResponse response;
        String userName = user.getCn();
        
        try {
            
            String pid = fedoraBean.getPID();
            String label = fedoraBean.getObjectValue(FedoraBean.OBJ_LABEL);
            
            // Get schema for object type
            Document schemaDocument = this.getSchema(type, organization);
            
            // Get capture datastream
            // getXMLDataStream() function validates datastream as well
            String captureContent = fedoraBean.getXMLDataStream(FedoraBean.DATASTREAM_CAPTURE, schemaDocument);
            
            if (bIngest) {
                response = new Ingest(pid).label(label).execute(fedoraClient);
                //logger.info("Fedora3Manager: ingestObject done with pid:"+pid+", status "+response.getStatus());
            }
                        
            if (file != null) {
                // FILE
                FedoraClient.addDatastream(pid, FedoraBean.DATASTREAM_ORIGINAL)
                            .versionable(false)
                            .dsLabel(pid+FedoraBean.DATASTREAM_ORIGINAL_LABEL+"."+FilenameUtils.getExtension(file.getName())+" ("+userName+")")
                            .controlGroup("M")
                            .content(file)
                            .execute(fedoraClient);
                
                //logger.info("Fedora3Manager: file: "+file.getAbsolutePath()+" added");
                
                // Create thumbnail for preview
                createPreviewDatastream(file.getAbsolutePath(), pid, FedoraBean.DATASTREAM_ORIGINAL, userName);
            }
            
            if (attachment != null && attachment.size() > 0) {
                // ATTACHMENT
            	for (File item : attachment) {
            		createAttachmentDatastream(item, pid, fedoraBean, userName);
            	}
            }
            
            // DC
            if (bIngest || fedoraBean.bUpdateDcDatastream) {
                String dcContent = fedoraBean.getXMLDataStream(FedoraBean.DATASTREAM_DC, null);
                FedoraClient.addDatastream(pid, FedoraBean.DATASTREAM_DC)
                            .versionable(true)
                            .dsLabel(DCBean.DC_LABEL+" ("+userName+")")
                            .content(dcContent)
                            .mimeType("text/xml")
                            .execute(fedoraClient);
                //logger.info("Fedora3Manager: DC datastream updated");
            }

            // CAPTURE
            if (bIngest || fedoraBean.bUpdateCaptureDatastream) {
                FedoraClient.addDatastream(pid, FedoraBean.DATASTREAM_CAPTURE)
                            .versionable(true)
                            .dsLabel(CaptureBean.CAPTURE_LABEL+" ("+userName+")")
                            .content(captureContent)
                            .execute(fedoraClient);
                //logger.info("Fedora3Manager: CAPTURE datastream updated");
            }
                               
            // RELATIONSHIPS
            if (bIngest || fedoraBean.bUpdateReslExtDatastream) {
                // Set model
                String modelRelation = "info:fedora/fedora-system:def/model#hasModel";
                if (fedoraBean.bUpdateReslExtDatastream) {
                    FedoraClient.purgeRelationship(modelRelation);
                }
                FedoraClient.addRelationship(pid).predicate(modelRelation).object("info:fedora/"+fedoraBean.getContentModel()).execute(fedoraClient);
                
                // Set capture relationships to RELS-EXT
                LinkedHashMap<String, MetaDataElement> captureElements = fedoraBean.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements();
                Iterator<Entry<String, MetaDataElement>> iter = captureElements.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry<String, MetaDataElement> entry = (Map.Entry<String, MetaDataElement>) iter.next();
                    MetaDataElement element = entry.getValue();
                    
                    if (element.getMetaDataType().equals(MetaDataElement.MetaDataType.relation)) {
                        FedoraClient.addRelationship("info:fedora/"+ pid)
                                    .predicate("info:fedora/fedora-system:def/relations-external#" +element.getName())
                                    .object("info:fedora/"+ element.getValue())
                                    .execute(fedoraClient);
                    }
                }
                
                //logger.info("Fedora3Manager: RELS-EXT datastream updated");
            }
            
            // MANAGEMENT
            if (bIngest || fedoraBean.bUpdateManagementDatastream) {
                String managementContent = fedoraBean.getXMLDataStream(FedoraBean.DATASTREAM_MANAGEMENT, null);
                FedoraClient.addDatastream(pid, FedoraBean.DATASTREAM_MANAGEMENT)
                            .versionable(false)
                            .dsLabel(ManagementBean.MANAGEMENT_LABEL+" ("+userName+")")
                            .content(managementContent).execute(fedoraClient);
                //logger.info("Fedora3Manager: MANAGEMENT datastream updated");
                
                // if updating collection, update management datastream of child objects as well
                if (type.endsWith(RepositoryManager.COLLECTION) && fedoraBean.bUpdateManagementDatastream) {
                    String newParent = fedoraBean.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements().get(CaptureBean.RELATION_IsPartOf).getValue();
                    ArrayList<HashMap<String, String>> pidList = this.getAllChildren(pid);
                    for (int i=0; i<pidList.size(); i++)
                    {
                        String childPid = cleanEntry(pidList.get(i).get("child"));
                        updateObjectPosition(childPid, true, fedoraBean.getOldParent(), newParent, user);
                    }
                }
            }
            
            logger.info("Fedora3Manager: ingest/update for "+pid+" done.");

        } catch (FedoraClientException e) {
            logger.error("Fedora3Manager:ingestObject error: ",e);
        }
        
        Osa.dbManager.get("sql").setCtxTime(organization); //update timestamp for the organization context
        
        return true;
    }
    
    /**
     * Move object in hierarchy
     * @param pid
     * @param oldParent
     * @param newParent
     * @param user
     * @throws FedoraClientException
     */
    public void moveObject(String pid, String oldParent, String newParent, User user) throws FedoraClientException {
        boolean isChild = false;
        ArrayList<HashMap<String, String>> pidList = this.getAllChildren(pid);
    	logger.info("Fedora3Manager: moving object "+pid);
    	
    	updateObjectPosition(pid, isChild, oldParent, newParent, user);
        
        // If has children
    	if (!pidList.isEmpty()) {
    	    isChild = true;
    		for (int i=0; i<pidList.size(); i++) {
        		String childPid = cleanEntry(pidList.get(i).get("child"));
        		updateObjectPosition(childPid, isChild, oldParent, newParent, user);
        	}
    	}
    }
    
    /**
     * Update object metadata, when moving it below another collection
     * @param pid           pid of object
     * @param isChild       indicates if child object of object, that is to be moved
     * @param oldParent     original parent pid
     * @param newParent     new parent pid
     * @param user          user
     * @throws FedoraClientException
     */
    public void updateObjectPosition(String pid, boolean isChild, String oldParent, String newParent, User user) throws FedoraClientException {
        boolean ancestorUpdated = false;
        DataStream captureStream = null;
        DataStream managementStream = null;
        String newParentPath = this.getPath(newParent);
        String organization = user.getOrganization().getName().toUpperCase();
        
        if (oldParent != null && newParent != null && oldParent.equals(newParent)) {
            // no need to update
            return;
        }
        
        if (oldParent != null) {
            purgeRelationship(pid, CaptureBean.RELATION_IsPartOf, oldParent, false);
        }
        
        FedoraBean fObject = findObject(pid);
        captureStream = fObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE);
        managementStream = fObject.getDataStream(FedoraBean.DATASTREAM_MANAGEMENT);
        MetaDataElement ancestorElement = (MetaDataElement)managementStream.getMetaDataElements().get(ManagementBean.DATA_isAncestor);
        MetaDataElement pathElement = (MetaDataElement)managementStream.getMetaDataElements().get(ManagementBean.DATA_objectPath);
        
        if (!isChild) {
            // remove oldParent
            captureStream.getMetaDataElements().get(CaptureBean.RELATION_IsPartOf).removeValue();
            // set new parent
            captureStream.getMetaDataElements().get(CaptureBean.RELATION_IsPartOf).setValue(newParent);
        }
        
        // update objectPath-metadata
        if (pathElement != null) {
            // remove old
            managementStream.getMetaDataElements().get(ManagementBean.DATA_objectPath).removeValue();
            
            // handle child objects
            if (isChild) {
                String childPath = this.getPath(pid);
                
                if (oldParent != null) {
                    // when moving collections and childobjects in browse.jsp
                    // objects have not been removed yet
                    String oldParentPath = this.getPath(oldParent);
                    String newChildPath = childPath.replace(oldParentPath, newParentPath);
                    pathElement.setValue(newChildPath);
                    
                } 

            } else {
                pathElement.setValue(newParentPath+"/"+pid);
            }
        }
        
        // update ancestor-metadata
        if (ancestorElement != null) {
            boolean updateAncestor = true;
            
            if (isChild) {
                // check if new parent needs to be updated
                String[] pids = newParentPath.split("/");
                String oldAncestor = managementStream.getMetaDataElements().get(ManagementBean.DATA_isAncestor).getValue(); 
                String newAncestor = "";
                
                if (pids.length >= 2) {
                    newAncestor = pids[1];
                    if (oldAncestor.equals(newAncestor)) {
                        updateAncestor = false;
                    }
                }
            }
            
            if (updateAncestor) {
                // remove oldParent
                managementStream.getMetaDataElements().get(ManagementBean.DATA_isAncestor).removeValue();
                if (newParent.contains(":root")) {
                    ancestorElement.setValue(pid);
                    ancestorUpdated = true;
                } else {
                    ancestorElement.setValue(this.getAncestor(newParent));
                    ancestorUpdated = true;
                }
            }
        }
        
        // if ancestor field has not been updated, it doesn't exist and needs to be created
        // Not needed for child objects
        if (!ancestorUpdated && !isChild) {
            if (newParent.contains(":root")) {
                managementStream.getMetaDataElements().put(ManagementBean.DATA_isAncestor, 
                                                           new MetaDataElement(ManagementBean.DATA_isAncestor, 
                                                                               ManagementBean.DATA_isAncestor, 
                                                                               pid, 
                                                                               MetaDataElement.MetaDataType.getEnum("string")));
            } else {
                managementStream.getMetaDataElements().put(ManagementBean.DATA_isAncestor,
                                                           new MetaDataElement(ManagementBean.DATA_isAncestor, 
                                                                               ManagementBean.DATA_isAncestor, 
                                                                               this.getAncestor(newParent), 
                                                                               MetaDataElement.MetaDataType.getEnum("string")));
            }
        }
        
        try {

            if (!isChild) {
                String pidType = pid.split(":")[1].split("-")[0];
                String objType = RepositoryManager.DATATYPES_MAP.get(pidType);
                // Get schema for object type
                Document schemaDocument = this.getSchema(objType, organization);
                String captureContent = fObject.getXMLDataStream(FedoraBean.DATASTREAM_CAPTURE, schemaDocument);
                FedoraClient.addDatastream(pid, FedoraBean.DATASTREAM_CAPTURE)
                            .versionable(true)
                            .dsLabel(CaptureBean.CAPTURE_LABEL+" ("+user.getCn()+")")
                            .content(captureContent)
                            .execute(fedoraClient);
                
                FedoraClient.addRelationship("info:fedora/"+ pid)
                            .predicate("info:fedora/fedora-system:def/relations-external#isPartOf")
                            .object("info:fedora/"+newParent)
                            .execute(fedoraClient);
            }
        
            String managementContent = fObject.getXMLDataStream(FedoraBean.DATASTREAM_MANAGEMENT, null);
            FedoraClient.addDatastream(pid, FedoraBean.DATASTREAM_MANAGEMENT)
                        .versionable(false)
                        .dsLabel(ManagementBean.MANAGEMENT_LABEL+" ("+user.getCn()+")")
                        .content(managementContent)
                        .execute(fedoraClient);
          
        } catch (FedoraClientException e) {
            logger.error("Fedora3Manager:moveObject error: "+e);
        } catch (SAXException e) {
            logger.error("Fedora3Manager:moveObject error: "+e);
        }

    }
    
    private String cleanResponse(String response) {    	
    	String[] responseparts = response.split("/");
    	String objectpid = responseparts[(responseparts.length - 1)].split(">")[0];
    	return objectpid;
    }
    
    /**
     * RISearch query to find ancestor value (the first collection below root object)
     * @param parent
     * @return
     */
    private String getAncestor(String parentpid) {
    	Boolean ancestor = false;
    	
    	while (!ancestor) {
    		
            try {
            	String query = String.format("select $parent from <#ri> where <info:fedora/"+parentpid+"> <fedora-rels-ext:isPartOf> $parent");
            	
                RiSearchResponse response = null;
                response = FedoraClient.riSearch(query).lang("itql").format("simple").execute(fedoraClient);
                String result = response.getEntity(String.class);
                
                if (result.contains(":root") || result.isEmpty()) {
                	ancestor = true;
                } else {
                	parentpid = cleanResponse(result);
                }
                
            } catch (FedoraClientException e) {
                logger.error("Fedora3Manager error: "+e);
            }
    	}
    	
        return parentpid;
	}
    
    /**
     * RISearch query to find object loaction (path)
     * @param pid
     * @return
     */
    public String getPath(String pid) {
        String path = "";
        String organization = "";
        String parentpid = pid;
        Boolean rootFound = false;
        
        if (pid == null) {
            return path;
        } else {
            // get organization from pid
            String[] parts = pid.split(":");
            organization = parts[0];
        }
        
        if (pid.contains(":root")) {
            path = pid + "/";
            // object is below root
            return path;
        } else {
            path = pid;
        }
        
        while (!rootFound) {
            
            try {
                String query = String.format("select $parent from <#ri> where <info:fedora/"+parentpid+"> <fedora-rels-ext:isPartOf> $parent");
                
                RiSearchResponse response = null;
                response = FedoraClient.riSearch(query).lang("itql").format("simple").execute(fedoraClient);
                
                if (response.getStatus() != 200) { 
                    return null;
                }
                
                String result = response.getEntity(String.class);
                
                if (result.contains(":root") || result.isEmpty()) {
                    parentpid = cleanResponse(result);
                    // if empty, set below root object
                    if (parentpid == null || parentpid.isEmpty()) {
                        parentpid = organization+":root";
                    }
                    
                    path = parentpid+"/"+path;
                    rootFound = true;
                } else {
                    parentpid = cleanResponse(result);
                    path = parentpid+"/"+path;
                }
                
            } catch (FedoraClientException e) {
                logger.error("Fedora3Manager error: "+e);
            }
        }
        return path;
    }
    
    /**
     * RISearch query to calculate level of collection object in hierarchy
     * @param   collectionpid
     * @return  level
     */
    public int getObjLevel(String collectionpid) {
    	if (collectionpid == null) {
    		return 1;
    	}
    	int level = 1;
    	String parentpid = collectionpid;
    	boolean root = false;
    	
    	if (parentpid.contains(":root")) {
    		level = 0;
    	} else {
    	    
        	while (!root) {
    			
    	        try {
    	        	String query = String.format("select $parent from <#ri> where <info:fedora/"+parentpid+"> <fedora-rels-ext:isPartOf> $parent");
    	            RiSearchResponse response = null;
    	            response = FedoraClient.riSearch(query).lang("itql").format("simple").execute(fedoraClient);
    	            String result = response.getEntity(String.class);
    	
    	            if (result.contains(":root")) {
    	            	root = true;
    	            } else {
    	            	parentpid = cleanResponse(result);
    	            	level++;
    	            }

    	        } catch (FedoraClientException e) {
    	            logger.error("Fedora3Manager error: "+e);
    	        }
        	}
    	}
    	
    	return level;
    }
    
	/**
     * Function to find all datastreams for PID
     * @param pid       A persistent, unique identifier for the object
     * @return          list of datastream ids
     */
    public List<String> listDataStreams(String pid) {
        ListDatastreamsResponse response;
        List<DatastreamType> dsList = new ArrayList<DatastreamType>();
        List<String> dataStreamIDsList = new ArrayList<String>();
        
        if (pid == null || pid == "") {
            return dataStreamIDsList;
        }
        
        try {
            response = new ListDatastreams(pid).execute(fedoraClient);
            dsList = response.getDatastreams();
            String contentaction = null;
            
            for (int i=0;i<dsList.size();i++) {
                contentaction = dsList.get(i).getDsid();
                dataStreamIDsList.add(contentaction);
            }
        } catch (FedoraClientException e) {
            logger.error("Fedora3Manager:listDataStreams error: "+e);
        }
        
        return dataStreamIDsList;
    }
    
    public List<DatastreamType> listRawDataStreams(String pid) {
        ListDatastreamsResponse response;
        List<DatastreamType> dsList = new ArrayList<DatastreamType>();
  
        try {
            response = new ListDatastreams(pid).execute(fedoraClient);
            dsList = response.getDatastreams();
            
        } catch (FedoraClientException e) {
            logger.error("Fedora3Manager:listDataStreams error"+e);
        }
        
        return dsList;
    }
    
    /**
     * Remove relation from fedora object
     * @param pid           The PID of the object.
     * @param relationship  The predicate, null matches any predicate.
     * @param object        The object, null matches any object.
     * @param isLiteral     A boolean value indicating whether the object is a literal.
     */
    public void purgeRelationship(String pid, String relationship, String object, boolean isLiteral) {
        
        try {
            FedoraResponse response = FedoraClient.purgeRelationship("info:fedora/"+ pid)
                                                  .predicate("info:fedora/fedora-system:def/relations-external#" +relationship)
                                                  .object("info:fedora/"+ object, isLiteral)
                                                  .execute(fedoraClient);
            
            if (response != null && response.getStatus() ==  200) {
                logger.info("Fedora3Manager:purgeRelationship "+relationship+" for "+object+" done.");
            }
            
        } catch (FedoraClientException e) {
            logger.error("Fedora3Manager:purgeRelationship error: "+e);
        }
    }

    /**
     * Cleans given string of fedora-specific strings
     * 
     * @param entry
     *            String to be cleaned
     * @return Cleaned string (if applicable)
     */
    public String cleanEntry(String entry) {
    	if (entry.contains("info:fedora/")) {
    		return entry.replace("info:fedora/", "");
    	} else {
    		return entry;
    	} 	
    }
    
    /**
     * Purge all relationships from Fedora object
     * @param pid       The PID of the object.
     * @throws FedoraClientException 
     */
    public void purgeRelationshipsFromObjects(String pid) {

        String organization = pid.split(":")[0];
        HashMap<String, ArrayList<HashMap<String, String>>> relations = new flexjson.JSONDeserializer<HashMap<String, ArrayList<HashMap<String, String>>>>().deserialize(this.getChildren(pid)); 
        ArrayList<HashMap<String, String>> relationlist = relations.get("results");
        ArrayList<String> pids = new ArrayList<String>();
        
        for (int i = 0; i < relationlist.size();i++) {
        	pids.add(this.cleanEntry(relationlist.get(i).get("child")));	
        }
        
        for (int j = 0;j<pids.size();j++) {
        	
        	FedoraBean fbean = this.findObject(pids.get(j));
        	this.purgeRelationship(pids.get(j), "isPartOf", pid, false);       
        	fbean.bUpdateCaptureDatastream = true;
        	fbean.bUpdateReslExtDatastream = true;
        }
    }

    /**
     * Change object state in Fedora repository
     * @param pid
     * @param state     active/inactive
     */
    public void setObjectState(String pid, State state) {
        try {
            if (state.equals("I")) {
                // state inactive
                this.purgeRelationshipsFromObjects(pid);
            }
            FedoraClient.modifyObject(pid).state(state.toString()).execute(fedoraClient);
            
        } catch (FedoraClientException e) {
            logger.error("Fedora3Manager:setObjectState error: "+e);
        }
    }
        
    public void writeDatastreamToFile(String uploadDir, String pid, String datastreamName, String origExt) throws UnsupportedEncodingException, IOException {
        
    	String objectUrl = this.baseUrl+"objects/"+pid+"/datastreams/"+datastreamName+"/content";
    	String datastreamNameLower = datastreamName.toLowerCase();

    	FedoraObjectHistory history = null;
    	
		String username = this.username;
        String password = this.password;
        String credentials = username + ":" + password;
        byte[] encoded = new Base64().encode(credentials.getBytes("UTF-8"));
        String encoding = new String(encoded);
    	
		OutputStream outputStream = null;
        File contentFile = new File("");
        
        URL url = new URL(objectUrl);
        URLConnection conn = url.openConnection();
        conn.setDoInput(true);
        conn.setRequestProperty("Authorization", String.format("Basic %s", encoding));
        InputStream is = conn.getInputStream();
        
	    int read = 0;
	    byte[] bytes = new byte[1024];
	    
    	if (FedoraBean.DATASTREAM_DC.equalsIgnoreCase(datastreamName) 
    	     || FedoraBean.DATASTREAM_CAPTURE.equalsIgnoreCase(datastreamName) 
    	     || FedoraBean.DATASTREAM_RELS_EXT.equalsIgnoreCase(datastreamName) 
    	     || FedoraBean.DATASTREAM_RELS_INT.equalsIgnoreCase(datastreamName) 
    	     || FedoraBean.DATASTREAM_MANAGEMENT.equalsIgnoreCase(datastreamName)
    	     )
    	{
    		File xmlfile = new File(uploadDir+pid+"/metadata/"+datastreamNameLower+"/"+datastreamNameLower+".xml");	
    		xmlfile.getParentFile().mkdirs();
        	outputStream = new FileOutputStream(xmlfile);
    	    while ((read = is.read(bytes)) != -1) {
    	    	outputStream.write(bytes, 0, read);
    	    }
    	    logger.info("Fedora3Manager: added "+datastreamNameLower+" xml file to package");
    	    
    	    try {
                GetObjectHistoryResponse response = FedoraClient.getObjectHistory(pid).execute(fedoraClient);
                history = response.getObjectHistory();
                
                List<String> changeDates = history.getObjectChangeDate();
                Iterator<String> iterChangeDates = changeDates.iterator();
                
                while (iterChangeDates.hasNext()) {
	                String changeDate = (String) iterChangeDates.next();
                    GetDatastreamHistoryResponse dsResponse = null;
                    List<DatastreamProfile> listDsProfile = null;
                    Iterator<DatastreamProfile> iterDsProfile = null;
                    
                    dsResponse = FedoraClient.getDatastreamHistory(pid, datastreamName).execute(fedoraClient);
                    listDsProfile = dsResponse.getDatastreamProfile().getDatastreamProfile();
                    iterDsProfile = listDsProfile.iterator();
                    
                    if (iterDsProfile != null) {                        
                        while (iterDsProfile.hasNext()) {
                            DatastreamProfile profile = (DatastreamProfile) iterDsProfile.next();
                            // check if date of version history matches to datastream version
                            if (profile.getDsCreateDate().toString().matches(changeDate)) {
                                objectUrl = this.baseUrl+"objects/"+pid+"/datastreams/"+datastreamName+"/content?asOfDateTime="+changeDate;
                                File historyxml = new File(uploadDir+pid+"/metadata/history/"+datastreamNameLower+"-history/"+profile.getDsVersionID()+"-"+changeDate+".xml");
                                historyxml.getParentFile().mkdirs();
                                
                                url = new URL(objectUrl);
                                conn = url.openConnection();
                                conn.setDoInput(true);
                                conn.setRequestProperty("Authorization", String.format("Basic %s", encoding));
                                is = conn.getInputStream();
                                
                            	outputStream = new FileOutputStream(historyxml);
                        	    while ((read = is.read(bytes)) != -1) {
                        	    	outputStream.write(bytes, 0, read);
                        	    }
                            }
                        }
                    }
                }
                logger.info("Fedora3Manager: added "+datastreamNameLower+" xml history files to package");
                
            } catch (FedoraClientException e) {
            	e.printStackTrace();
            }
    	    outputStream.close();
    	    
    	} else if (FedoraBean.DATASTREAM_ORIGINAL.equalsIgnoreCase(datastreamName) 
    	           || datastreamName.startsWith(FedoraBean.DATASTREAM_ATTACHMENT) 
    	           ||  datastreamName.startsWith(FedoraBean.DATASTREAM_THUMB)) {

    		if (FedoraBean.DATASTREAM_ORIGINAL.equalsIgnoreCase(datastreamName)) {
    			if (origExt != null) {
    				contentFile = new File(uploadDir+pid+"/"+datastreamNameLower+"/"+datastreamNameLower+"."+origExt);
    				logger.info("Fedora3Manager: added "+datastreamNameLower+" "+origExt+" file to package");
    			}
    		}

    		if (datastreamName.startsWith(FedoraBean.DATASTREAM_ATTACHMENT)) {
            	List<DatastreamType> dslist = this.listRawDataStreams(pid);
            	String format = "";
            	
            	for (DatastreamType ds : dslist) {
            		if (datastreamName.equals(ds.getDsid())) {
            			format = ds.getMimeType();	
            		}
            	}
         
                String attExt = format.substring(format.indexOf("/")+1, format.length());
    			contentFile = new File(uploadDir+pid+"/attachments/"+datastreamNameLower+"."+attExt);
    			logger.info("Fedora3Manager: added "+datastreamNameLower+" "+attExt+" file to package");
    		}

    		if (datastreamName.startsWith(FedoraBean.DATASTREAM_THUMB)) {
    			List<DatastreamType> dslist = this.listRawDataStreams(pid);
            	String format = "";
            	
            	for (DatastreamType ds : dslist) {
            		if (datastreamName.equals(ds.getDsid())) {
            			format = ds.getMimeType();	
            		}
            	}
         
                String thumbExt = format.substring(format.indexOf("/")+1, format.length());
				contentFile = new File(uploadDir+pid+"/thumbnails/"+datastreamNameLower+"."+thumbExt);
				logger.info("Fedora3Manager: added "+datastreamNameLower+" "+thumbExt+" file to package");
    		}
    		
    		contentFile.getParentFile().mkdirs();
    		outputStream = new FileOutputStream(contentFile);
    		while ((read = is.read(bytes)) != -1) {
    			outputStream.write(bytes, 0, read);
    		}
    		outputStream.close();
    	}

		File auditContentFile = new File(uploadDir+pid+"/metadata/history/audit/audit.xml");
		if (!auditContentFile.exists()) {
			auditContentFile.getParentFile().mkdirs();
			outputStream = new FileOutputStream(auditContentFile);
			PrintStream printStream = new PrintStream(outputStream);
			printStream.print(this.getAuditTrail(pid));
			logger.info("Fedora3Manager: added audit trail xml file to package");
			
			printStream.close();
			outputStream.close();
		}
        is.close();
    }
    
}
