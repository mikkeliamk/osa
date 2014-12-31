package fi.mamk.osa.fedora;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.generated.access.DatastreamType;

import fi.mamk.osa.auth.User;
import fi.mamk.osa.bean.CaptureBean;
import fi.mamk.osa.bean.FedoraBean;
import fi.mamk.osa.bean.MetaDataElement;
import fi.mamk.osa.bean.DataStream.State;
import fi.mamk.osa.core.ArchiveLevel;
import fi.mamk.osa.core.DestructionPlan;
import fi.mamk.osa.core.Osa;
import fi.mamk.osa.core.PreservationPlan;
import fi.mamk.osa.database.mongo.MongoManager;
import fi.mamk.osa.ui.Gui;

public abstract class RepositoryManager {
    
    // Fedora versions
    public static final String FEDORA3 = "fedora3";
    public static final String FEDORA4 = "fedora4";
    
    // content model prefix
    public static final String CONTENTMODEL    = "osa-system:contentmodel-";
    
    // data types
    public static final String ACTION          = "action";
    public static final String AGENT           = "agent";
    public static final String APPLICATION     = "application";
    public static final String AUDIO           = "audio";
    public static final String BASECOLLECTION  = "basecollection";
    public static final String COLLECTION      = "collection";
    public static final String DEFAULT_TYPE    = "default";
    public static final String DOCUMENT        = "document";
    public static final String DRAWING         = "drawing";
    public static final String EVENT           = "event";    
    public static final String GROUPCOLLECTION = "groupcollection";
    public static final String IMAGE           = "image";
    public static final String MAP             = "map";
    public static final String MOVINGIMAGE     = "movingimage";
    public static final String PHYSICALOBJECT  = "physicalobject";
    public static final String PLACE           = "place";
    public static final String ROOT            = "root";    
    public static final String UNITCOLLECTION  = "unitcollection";
    
    public static final String PID_COLLECTION_AGENTS         = ":C-agents";
    public static final String PID_COLLECTION_ACTIONS        = ":C-actions";
    public static final String PID_COLLECTION_CONTEXTS       = ":C-contexts";
    public static final String PID_COLLECTION_EVENTS         = ":C-events";
    public static final String PID_COLLECTION_LYACTIONS      = ":C-lyactions";
    public static final String PID_COLLECTION_PLACES         = ":C-places";
    public static final String PID_COLLECTION_TOL2008ACTIONS = ":C-tol2008actions";
    
    /** Abbreviations for object types. Lookup with PID's object type's identifier.
     * 
     */
    public static final Map<String, String> DATATYPES_MAP = 
            Collections.unmodifiableMap(new HashMap<String, String>() {
                private static final long serialVersionUID = 1L;
            { 
                put("F", RepositoryManager.ACTION);
                put("W", RepositoryManager.AGENT);
                put("S", RepositoryManager.APPLICATION);
                put("A", RepositoryManager.AUDIO);
                put("C", RepositoryManager.COLLECTION);
                put("T", RepositoryManager.DOCUMENT);
                put("D", RepositoryManager.DRAWING);
                put("E", RepositoryManager.EVENT);
                put("P", RepositoryManager.IMAGE);
                put("M", RepositoryManager.MAP);
                put("V", RepositoryManager.MOVINGIMAGE);
                put("O", RepositoryManager.PHYSICALOBJECT);
                put("L", RepositoryManager.PLACE);
                put("X", RepositoryManager.DEFAULT_TYPE);
            }});

    /** Letters for different kind of object types. Lookup with object type as plain, normal text.
     * 
     */
    public static final Map<String, String> DATATYPES_MAP_REVERSE = 
            Collections.unmodifiableMap(new HashMap<String, String>() {
                private static final long serialVersionUID = 1L;
            { 
                put(RepositoryManager.ACTION,        "F");
                put(RepositoryManager.AGENT,         "W");
                put(RepositoryManager.APPLICATION,   "S");
                put(RepositoryManager.AUDIO,         "A");
                put(RepositoryManager.COLLECTION,    "C");
                put(RepositoryManager.DOCUMENT,      "T");
                put(RepositoryManager.DRAWING,       "D");
                put(RepositoryManager.EVENT,         "E");
                put(RepositoryManager.IMAGE,         "P");
                put(RepositoryManager.MAP,           "M");
                put(RepositoryManager.MOVINGIMAGE,   "V");
                put(RepositoryManager.PHYSICALOBJECT,"O");
                put(RepositoryManager.PLACE,         "L");
                put(RepositoryManager.DEFAULT_TYPE,  "X");
            }});

    public static final Map<String, String> CONTEXTOBJECT_COLLECTIONS = 
            Collections.unmodifiableMap(new HashMap<String, String>() {
                private static final long serialVersionUID = 1L;
            { 
                put(RepositoryManager.ACTION, RepositoryManager.PID_COLLECTION_ACTIONS);
                put(RepositoryManager.AGENT, RepositoryManager.PID_COLLECTION_AGENTS);
                put(RepositoryManager.EVENT, RepositoryManager.PID_COLLECTION_EVENTS);
                put(RepositoryManager.PLACE, RepositoryManager.PID_COLLECTION_PLACES);
            }});
    
    private static final Logger logger = Logger.getLogger(RepositoryManager.class);
    
    public String username;
    public String password;
    public String baseUrl;
    // content data
    Vector<FedoraBean> vectFedoraObjects = new Vector<FedoraBean>();
    
    public abstract void createAttachmentDatastream(File file, String pid, FedoraBean fObject, String user);
    public abstract void createPreviewDatastream(String filePath, String pid, String datastreamName, String user);
    public abstract FedoraBean createFedoraBean(boolean bIngest, User user, Gui userGui, String pid, String type, LinkedHashMap<String, MetaDataElement> lhmElements);
    public abstract void createRootObject(String rootName, Gui userGui, User user);
    public abstract String getAuditTrail(String pid);
    public abstract void deleteObject(String pid);
    public abstract void createResidualObject(String pid, DestructionPlan plan);
    public abstract void deleteRootObject(String rootName);
    public abstract void deleteDataStream(String pid, String dsid);
    public abstract void deleteAllobjects(String orgContext);
    public abstract void deleteActionObjects(String orgContext, String type);
    public abstract List<String> findAllObjects(String orgContext);
    public abstract FedoraBean findObject(String pid);
    public abstract String getChildren(String pid);
    public abstract ArrayList<HashMap<String, String>> getAllChildren(String pid);
    public abstract String getCollectionObjects(String organization);
    public abstract Document getSchema(String type, String organization);
    public abstract String getNextPID(String organizationName, String mime);
    public abstract List<MetaDataElement> getObjectHistory(String pid);
    public abstract String ingest(Gui userGui, User user, String organization, File file, List<File> attachment, 
        String type, String pid, LinkedHashMap<String, MetaDataElement> lhmCaptureElements, FedoraBean oldFedoraBean) throws SAXException;
    public abstract void ingestWithPid(boolean bIngest, User user, Gui userGui, String organization, File file, String type, LinkedHashMap<String, MetaDataElement> lhmElements);
    public abstract boolean ingestFile(String organization, String path);
    public abstract boolean ingestObject(boolean bIngest, User user, String organization, String type, File file, List<File> attachment, FedoraBean fedoraBean) throws SAXException;
    public abstract void moveObject(String pid, String oldParent, String newParent, User user) throws FedoraClientException;
    public abstract void updateObjectPosition(String pid, boolean isChild, String oldParent, String newParent, User user) throws FedoraClientException;
    public abstract String getPath(String pid);
    public abstract int getObjLevel(String collectionpid);
    public abstract List<String> listDataStreams(String pid);
    public abstract List<DatastreamType> listRawDataStreams(String pid);
    public abstract void purgeRelationship(String pid, String relationship, String object, boolean isLiteral);
    public abstract String cleanEntry(String entry);
    public abstract void purgeRelationshipsFromObjects(String pid);
    public abstract void setObjectState(String pid, State state);
    public abstract void writeDatastreamToFile(String uploadDir, String pid, String datastreamName, String origExt) throws UnsupportedEncodingException, IOException;
    
    /**
     * Check if datastreams have been modified before updating to Fedora repository
     * @param oldObject     current object in Fedora Repository
     * @param newObject     new object to be updated to Fedora Repository
     * @return              object updated with boolean values, which tells if datastreams modified
     */
    public FedoraBean checkIfDatastreamsModified(FedoraBean oldObject, FedoraBean newObject) {
        
        if (oldObject == null) {
            return newObject;
        }
        
        // get existing datastreams for pid
        List<String> existingDatastreams = listDataStreams(oldObject.getPID());
        int size = existingDatastreams.size();
        
        // Handle all datastreams
        for (int i=0; i<size; i++) {
            
            String currentDatastream = existingDatastreams.get(i);
            String newDS = null;
            String oldDS = null;
            
            if (currentDatastream.equalsIgnoreCase(FedoraBean.DATASTREAM_CAPTURE)) {
                newDS = newObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements().toString();                
                oldDS = oldObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements().toString();
                
            } else if (currentDatastream.equalsIgnoreCase(FedoraBean.DATASTREAM_DC)) {
                
                // sort by key
                List<String> keysNew = new ArrayList<String>(newObject.getDataStream(FedoraBean.DATASTREAM_DC).getMetaDataElements().keySet());
                Collections.sort(keysNew);
             
                Map<String, MetaDataElement> tempNew = new LinkedHashMap<String, MetaDataElement>();
                for (String key: keysNew) {
                    tempNew.put(key, newObject.getDataStream(FedoraBean.DATASTREAM_DC).getMetaDataElements().get(key));
                }
                
                List<String> keysOld = new ArrayList<String>(oldObject.getDataStream(FedoraBean.DATASTREAM_DC).getMetaDataElements().keySet());
                Collections.sort(keysOld);
             
                Map<String, MetaDataElement> tempOld = new LinkedHashMap<String, MetaDataElement>();
                for (String key: keysOld) {
                    tempOld.put(key, oldObject.getDataStream(FedoraBean.DATASTREAM_DC).getMetaDataElements().get(key));
                }

                // remove name=identified before comparing datastreams
                tempOld.remove("identifier");
                
                newDS = tempNew.toString();
                oldDS = tempOld.toString();
                
            } else if (currentDatastream.equalsIgnoreCase(FedoraBean.DATASTREAM_RELS_EXT)) {
                
                LinkedHashMap<String, MetaDataElement> tempNew = newObject.getDataStream(FedoraBean.DATASTREAM_RELS_EXT).getMetaDataElements();
                LinkedHashMap<String, MetaDataElement> tempOld = oldObject.getDataStream(FedoraBean.DATASTREAM_RELS_EXT).getMetaDataElements();
                
                // remove name=hasModel before comparing datastreams
                tempOld.remove("hasModel");
                
                newDS = tempNew.toString();
                oldDS = tempOld.toString();
                
            } else if (currentDatastream.equalsIgnoreCase(FedoraBean.DATASTREAM_MANAGEMENT)) {
                
                newDS = newObject.getDataStream(FedoraBean.DATASTREAM_MANAGEMENT).getMetaDataElements().toString();
                oldDS = oldObject.getDataStream(FedoraBean.DATASTREAM_MANAGEMENT).getMetaDataElements().toString();
                
            } else if (currentDatastream.equalsIgnoreCase(FedoraBean.DATASTREAM_RELS_INT)) {
                // TODO: check this
            }
            
            MessageDigest m;
            String newChecksum = null;
            String oldChecksum = null;
            
            try {
                
                m = MessageDigest.getInstance("MD5");
                
                if (oldDS != null) {
                    m.update(oldDS.getBytes(), 0, oldDS.length());
                    oldChecksum = new BigInteger(1,m.digest()).toString(16);
                    m.reset();
                }
                
                if (newDS != null) {
                    m.update(newDS.getBytes(), 0, newDS.length());
                    newChecksum = new BigInteger(1,m.digest()).toString(16);
                }
                
                if ((newChecksum != null && oldChecksum != null && !oldChecksum.equalsIgnoreCase(newChecksum))
                    || newChecksum != null && oldChecksum == null
                   )
                {
                    if (currentDatastream.equalsIgnoreCase(FedoraBean.DATASTREAM_CAPTURE)) {
                        // Datastream modified
                        
                        // Check if title has been edited, update it to basket items
                        if (newObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements().containsKey(CaptureBean.TITLE) && 
                            oldObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements().containsKey(CaptureBean.TITLE)) {
                            if (!newObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements().get(CaptureBean.TITLE).getValue().equals(
                                 oldObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements().get(CaptureBean.TITLE).getValue())) {
                                Osa.dbManager.get("mongo").syncBaskets(newObject.getPID(), 
                                                                       MongoManager.EVENT_RENAME, 
                                                                       newObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements().get(CaptureBean.TITLE).getValue());
                            }
                        }
                        
                        newObject.bUpdateCaptureDatastream = true;
                        // store old parent (value is used when updating path, if object and child objects moved)
                        String oldParent = oldObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements().get(CaptureBean.RELATION_IsPartOf).getValue();
                        newObject.setOldParent(oldParent);
                        
                    } else if (currentDatastream.equalsIgnoreCase(FedoraBean.DATASTREAM_DC)) {
                        // datastream modified
                        newObject.bUpdateDcDatastream = true;
                    } else if (currentDatastream.equalsIgnoreCase(FedoraBean.DATASTREAM_RELS_EXT)) {
                        // datastream modified
                        newObject.bUpdateReslExtDatastream = true;
                    } else if (currentDatastream.equalsIgnoreCase(FedoraBean.DATASTREAM_RELS_INT)) {
                        // datastream modified
                        newObject.bUpdateReslIntDatastream = true;
                    } else if (currentDatastream.equalsIgnoreCase(FedoraBean.DATASTREAM_MANAGEMENT)) {
                        // datastream modified
                        newObject.bUpdateManagementDatastream = true;
                    }
                }
                
            } catch (NoSuchAlgorithmException e) {
                logger.error("RepositoryManager error: "+e);
            }
        }
        
        return newObject;
    }
    
    /**
     * Get fedora object from FedoraManager
     * @param pid       The PID of the object.
     * @return          fedora object
     */
    public FedoraBean getObject(String pid) {
        Iterator<FedoraBean> iterFedoraObjects = this.vectFedoraObjects.iterator();
        FedoraBean fedoraObject = null;        
        
        while (iterFedoraObjects.hasNext()) {
            FedoraBean currentObject = (FedoraBean) iterFedoraObjects.next();
            
            if (currentObject.getPID().equalsIgnoreCase(pid)) {
                fedoraObject = currentObject;
            }
        }
        return fedoraObject;
    }
    
    /**
     * Check if object is allowed in storage level
     * @param plan          Preservation plan
     * @param objectType    document/image/base collection/group collection/ etc.
     * @param order         storage level if available (default -1)
     * @param objParent     pid of parent object
     * @param isCollection  parameter to define if object is collection
     * @return              validation error text if object can not be stored
     */
    public String isObjectPermitted(PreservationPlan plan, String objectType, int order, String objParent, boolean isCollection) {
        String validationErrorText = "";
        if (objectType == null && objParent == null) {
            return "error.preservation";
        } else if (objParent == null && order == -1) {
            return "error.preservation";
        } else if (objParent != null && order == -1) {
            order = this.getObjLevel(objParent);
        }
        
        if (isCollection) {
            // collection itself has information in preservation plan
            order = order+1;
        }
        
        // check preservation plan
        if (order <= plan.getMaxLevel()) {
            for (ArchiveLevel level : plan.getHierarchy()) {
                if (order == level.getOrder() || order < level.getMaxOrder()) {
                    if (isCollection) {
                        // adding collection
                        // check type of collection, and check if it is allowed to this level
                        if (!level.getAllowCollectionsForType(objectType)) {
                            validationErrorText = "error.preservation.collection";
                        }
                        break;
                        
                    } else {
                        // adding other type
                        // check type of objParent, and getAllowDocuments according to this value                     
                        if (!level.getAllowDocumentsForType(objectType)) {
                            validationErrorText = "error.preservation";
                        }
                        break;
                    }
                }
            }
        } else {
            validationErrorText = "max level is "+ plan.getMaxLevel();
        }
        return validationErrorText;
    }
}
