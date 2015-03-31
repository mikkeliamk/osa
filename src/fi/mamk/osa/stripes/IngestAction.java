package fi.mamk.osa.stripes;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.LocalizableMessage;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.LocalizableError;
import net.sourceforge.stripes.validation.SimpleError;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.list.SetUniqueList;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;
import org.apache.commons.io.comparator.NameFileComparator;
import org.apache.commons.io.comparator.PathFileComparator;
import org.apache.commons.io.comparator.ExtensionFileComparator;
import org.apache.commons.io.comparator.SizeFileComparator;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.jersey.api.client.ClientHandlerException;
import com.yourmediashelf.fedora.client.FedoraClientException;

import fi.mamk.osa.auth.AccessRight;
import fi.mamk.osa.auth.User;
import fi.mamk.osa.bean.AttachmentBean;
import fi.mamk.osa.bean.CaptureBean;
import fi.mamk.osa.bean.DataStream;
import fi.mamk.osa.bean.FedoraBean;
import fi.mamk.osa.bean.ManagementBean;
import fi.mamk.osa.bean.MetaDataElement;
import fi.mamk.osa.bean.ThumbBean;
import fi.mamk.osa.core.InheritedMetadataElement;
import fi.mamk.osa.core.Osa;
import fi.mamk.osa.core.PreservationPlan;
import fi.mamk.osa.database.mongo.MongoManager;
import fi.mamk.osa.fedora.RepositoryManager;
import fi.mamk.osa.ui.FormElement;
import fi.mamk.osa.ui.FormElement.FieldType;
import fi.mamk.osa.ui.Gui;
import fi.mamk.osa.ui.TreeNode;
import fi.mamk.osa.workflow.WorkflowManager;

@UrlBinding("/Ingest.action")
public class IngestAction extends OsaBaseActionBean {
    private static final Logger logger = Logger.getLogger(IngestAction.class);
    public static final String  TARGET = "/ingest.jsp?index=";

    public static enum FileType {
        Original, Attachment
    }

    private boolean bRemoveFile;
    private boolean bRemoveAttachment;
    private boolean fromQueue = false;

    // container for FedoraBeans
    private Vector<FedoraBean> fedoraBeans      = new Vector<FedoraBean>();
    private Vector<FedoraBean> fedorabeanvector = new Vector<FedoraBean>();

    // list for displaying ingest form
    private LinkedHashMap<String, MetaDataElement> allCaptureElements;
    private LinkedHashMap<String, FormElement>     customForm;
    private LinkedHashSet<String>                  customTabs;

    // list for displaying version history of an object
    private List<MetaDataElement> historyElements  = new ArrayList<MetaDataElement>();

    private int nodeLevel;

    private Vector<String> uploadedFile;
    private Vector<String> uploadedAttachment;
    private Vector<String> watchedFiles;
    private Vector<String> inconsistantFields;
    private Vector<String> pidArray;
    private List<String>   formNames;
    private HashMap<String, String> attachmentNames;

    private Boolean hasThumbnail;
    private Boolean plainView;
    private Boolean addNew = false;

    private String browsename;
    private String cmodel;
    private String currentOrganization;
    private String deleteAttachment;
    private String dsName = "";
    private String filename;
    private String fileToDelete;
    private String index;
    private String isPartOf;
    private String link;
    private String logName;
    private String mdElement;
    private String movedpids;
    private String newParent;
    private String onkiAccessKey;
    private String ontologyId;
    private String oldParent;
    private String pid;    
    private String root;
    private String rootpid;
    private String selectedrelationname;
    private String term;
    private String toFolder;
    private String watchFields;
    private String watchedFile;
    private String watchTable;
   
    // Datatables variables for sorting and paging
    private int    sEcho;
    private int    iSortCol_0;
    private int    iDisplayStart;
    private int    iDisplayLength;
    private String sSortDir_0;
    private String sSearch;

    @DefaultHandler
    public Resolution init() {
        return new ForwardResolution(INDEX);
    }
    
    @HandlesEvent("create")
    public Resolution create() {
        
        // Parent object selected in browse-view
        if (isPartOf != null) {
            // Check preservation plan
            PreservationPlan plan = (PreservationPlan) this.getContext().getRequest().getSession().getAttribute("preservationPlan");
            User user = this.getContext().getUser();
            String objParent = getIsPartOf();
            String objectType = "";
            int nodeLevel = Osa.fedoraManager.getObjLevel(objParent);
            boolean isCollection = false;
    
            if (index.endsWith(RepositoryManager.COLLECTION)) {
                // collection itself has information in preservation plan
                objectType = index;
                isCollection = true;
            } else {
                // parent has information in preservation plan
                objectType = Osa.searchManager.getObjectType(objParent, user);
            }
    
            String validationText = Osa.fedoraManager.isObjectPermitted(plan, objectType, nodeLevel, objParent, isCollection);
            if (validationText != "") {
                if (!validationText.startsWith("error.preservation")) {
                    logger.error("Max level in your preservation plan is " + plan.getMaxLevel());
                    this.getContext().getValidationErrors().add("error", new SimpleError(validationText));
                } else if (isCollection) {
                    logger.error("According to your preservation plan adding collection to this level is not allowed.");
                    this.getContext().getValidationErrors().add("error", new LocalizableError(validationText));
                } else {
                    logger.error("According to your preservation plan adding object to this level is not allowed.");
                    this.getContext().getValidationErrors().add("error", new LocalizableError(validationText));
                }
                return new ForwardResolution("/browse.jsp");
            }
        }
        
        // If using add new-button, do not add unnecessary empty bean to vector
        if (!addNew) {
            FedoraBean fedoraBean = new FedoraBean();
            fedoraBeans.add(fedoraBean);
        }
        if (RepositoryManager.ACTION.equalsIgnoreCase(index)) { return new RedirectResolution(WORKSPACE); }
        return new ForwardResolution(TARGET + this.index);
    }

    
    @HandlesEvent("add")
    public Resolution add() throws FedoraClientException, Exception {

        User user = this.getContext().getUser();
        // get user specific organization part of the next Pid
        String organization = this.getContext().getUser().getOrganization().getName().toUpperCase();
        // get user specific gui
        Gui userGui = this.getContext().getGui();
        String createdPid = "";

        if (organization == null || organization == "") {
            logger.error("organization does not exist!");
            return new RedirectResolution(TARGET + this.index);
        }
        
        for (FedoraBean fObject : fedoraBeans) {
            String objParent = "";
            allCaptureElements = fObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements();
            MetaDataElement element = allCaptureElements.get(CaptureBean.RELATION_IsPartOf);
            if (element.getValue() == null || element.getValue().equals("")) {
                // set relation for context objects
                if (index.equals(RepositoryManager.ACTION)) {
                    objParent = organization + RepositoryManager.PID_COLLECTION_ACTIONS;
                } else if (index.equals(RepositoryManager.AGENT)) {
                    objParent = organization + RepositoryManager.PID_COLLECTION_AGENTS;
                } else if (index.equals(RepositoryManager.EVENT)) {
                    objParent = organization + RepositoryManager.PID_COLLECTION_EVENTS;
                } else if (index.equals(RepositoryManager.PLACE)) {
                    objParent = organization + RepositoryManager.PID_COLLECTION_PLACES;
                } else {
                    objParent = organization + ":root";
                }
                allCaptureElements.get(CaptureBean.RELATION_IsPartOf).setValue(objParent);

            } else {
                objParent = element.getValue();
            }

            // Check preservation plan
            PreservationPlan plan = (PreservationPlan) this.getContext().getRequest().getSession().getAttribute("preservationPlan");
            String objectType = null;
            boolean isCollection = false;

            if (index.endsWith(RepositoryManager.COLLECTION)) {
                // collection itself has information in preservation plan
                objectType = allCaptureElements.get("type").getValue();
                isCollection = true;
            } else {
                // parent has information in preservation plan
                objectType = Osa.searchManager.getObjectType(objParent, user);
            }

            String validationText = Osa.fedoraManager.isObjectPermitted(plan, objectType, -1, objParent, isCollection);
            if (validationText != "") {
                if (!validationText.startsWith("error.preservation")) {
                    logger.error("Max level in your preservation plan is " + plan.getMaxLevel());
                    this.getContext().getValidationErrors().add("error", new SimpleError(validationText));
                } else if (isCollection) {
                    logger.error("According to your preservation plan adding collection to this level is not allowed.");
                    this.getContext().getValidationErrors().add("error", new LocalizableError(validationText));
                } else {
                    logger.error("According to your preservation plan adding object to this level is not allowed.");
                    this.getContext().getValidationErrors().add("error", new LocalizableError(validationText));
                }
                return this.getContext().getSourcePageResolution();
            }

            // Check access right of parent object (new object does not have PID yet)
            String path = Osa.fedoraManager.getPath(objParent);
            String publicityLevel = ((CaptureBean) fObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE)).getPublicityLevel();

            if (!this.hasRights(path, publicityLevel, AccessRight.ACCESSRIGHTLEVEL_ADD_DOC)) { 
                return new StreamingResolution(MIME_TEXT, "Unauthored user - access denied."); 
            }

            ArrayList<File> attachmentList = new ArrayList<File>();

            // Check if there are no original file to upload,
            // then check if there are attachments to upload and upload them
            if (uploadedFile == null || uploadedFile.size() == 0 || uploadedFile.isEmpty()) {

                File file = null;
                bRemoveFile = false;
                bRemoveAttachment = false;

                if (uploadedAttachment != null && uploadedAttachment.size() > 0) {
                    for (String filename : uploadedAttachment) {
                        // When removing uploaded files from upload list, it
                        // leaves nulls in array
                        // TODO change jsp/js so it does not leave nulls, or
                        // leave it like this
                        if (filename != null) {
                            attachmentList.add(this.handleXhrUploadedFiles(filename, FileType.Attachment));
                        }
                    }
                }

                try {
                    createdPid = Osa.fedoraManager.ingest(userGui, user, organization, file, attachmentList, index, pid, allCaptureElements, null);

                } catch (SAXParseException e) {
                    this.getContext().getValidationErrors().add("error", new LocalizableError("error.validate"));
                    logger.error("Schema validation found errors: " + e);
                    return this.getContext().getSourcePageResolution();

                } catch (SAXException e) {
                    this.getContext().getValidationErrors().add("error", new LocalizableError("error.ingest"));
                    return this.getContext().getSourcePageResolution();
                }
                // Has file(s) (original) to upload
            } else {
                for (int i = 0; i < uploadedFile.size(); i++) {
                    File file = null;
                    bRemoveFile = false;
                    bRemoveAttachment = false;

                    if (index.endsWith(RepositoryManager.DOCUMENT)
                            || index.endsWith(RepositoryManager.IMAGE)
                            || index.endsWith(RepositoryManager.MOVINGIMAGE)
                            || index.endsWith(RepositoryManager.AUDIO)
                            || index.endsWith(RepositoryManager.DRAWING)
                            || index.endsWith(RepositoryManager.MAP))
                    {
                        if (uploadedFile.get(i) != null) {
                            file = this.handleXhrUploadedFiles(uploadedFile.get(i),
                                    FileType.Original);
                        }
                    }

                    if (uploadedAttachment != null && uploadedAttachment.size() > 0) {
                        for (String filename : uploadedAttachment) {
                            // When removing uploaded files from upload list, it
                            // leaves nulls in array
                            // TODO change jsp/js so it does not leave nulls, or
                            // leave it like this
                            if (filename != null) {
                                attachmentList.add(this.handleXhrUploadedFiles(filename, FileType.Attachment));
                            }
                        }
                    }

                    if (organization == null || organization == "") {
                        logger.error("organization does not exist!");
                        return new RedirectResolution(TARGET + this.index);
                    }

                    try {
                        createdPid = Osa.fedoraManager.ingest(userGui, user, organization, file, attachmentList, index, pid, allCaptureElements, null);

                    } catch (SAXParseException e) {
                        this.getContext().getValidationErrors().add("error", new LocalizableError("error.validate"));
                        logger.error("Schema validation found errors: " + e);
                        return this.getContext().getSourcePageResolution();

                    } catch (SAXException e) {
                        this.getContext().getValidationErrors().add("error", new LocalizableError("error.ingest"));
                        return this.getContext().getSourcePageResolution();
                    }

                    if (bRemoveFile) {
                        // remove file if created to upload directory
                        file.delete();
                    }
                }
            }

            if (bRemoveAttachment) {
                // remove file if created to home directory
                for (File attachment : attachmentList) {
                    attachment.delete();
                }
            }
            
            String objectName = "";
            String objectId = "";
            if (allCaptureElements.containsKey(CaptureBean.TITLE)) {
                objectName = allCaptureElements.get(CaptureBean.TITLE).getValue();
            } else if (allCaptureElements.containsKey(CaptureBean.PREFERREDNAME)) {
                objectName = allCaptureElements.get(CaptureBean.PREFERREDNAME).getValue();
            }
            if (allCaptureElements.containsKey(CaptureBean.IDENTIFIER)) {
                objectId = allCaptureElements.get(CaptureBean.IDENTIFIER).getValue();
            }
            Osa.dbManager.get("mongo").addToLatestEdited(this.getContext().getUser(), createdPid, objectName, objectId, MongoManager.EVENT_ADD);

        } // for
          // Add message to inform user about successful add
        this.getContext().getMessages().add(new LocalizableMessage("message.added." + index));

        // If clicked "Add new" button on the form, redirect to new form with
        // the same metadatas
        if (addNew) {
            setPid(null);
            if (allCaptureElements.containsKey(CaptureBean.IDENTIFIER)) {
                allCaptureElements.get(CaptureBean.IDENTIFIER).removeValue();
            }
            return create();
        }
        
        // If has uploaded files/attachments, send command to delete form
        // associated with them from sessionStorage after successful add
        if (uploadedFile != null || uploadedAttachment != null) {
            return new RedirectResolution(WORKSPACE).addParameter("rmst", "true");
        } else {
            return new RedirectResolution(WORKSPACE);
        }
    }

    @HandlesEvent("addNew")
    public Resolution addNew() throws FedoraClientException, Exception {
        setAddNew(true);
        return add();
    }

    @HandlesEvent("getOnkiHttp")
    public Resolution getOnkiHttp() throws HttpException, IOException {
        String json = "";
        String language = getUserLocale().toLanguageTag().split("-")[0];
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
        httpClient.getParams().setParameter("http.protocol.content-charset", "UTF-8");
        
        GetMethod get = new GetMethod("http://onki.fi/key-"+this.getOnkiAccessKey()+"/api/v2/http/onto/" + ontologyId + "/search?q=" + term + "&l=" + language);
        httpClient.executeMethod(get);
        if (get.getStatusCode() == 200) {
            json = get.getResponseBodyAsString();
        }
        //logger.info("getOnkiHttp(): "+json);
        return new StreamingResolution(MIME_JS, json);
    }

    @HandlesEvent("getContextTime")
    public Resolution getCtxTimeStamp() {
        String organization = this.getContext().getUser().getOrganization().getName();
        double ctxtime = Osa.dbManager.get("sql").getCtxTime(organization);
        String ctxtimeString = String.valueOf(ctxtime);
        return new StreamingResolution(MIME_TEXT, ctxtimeString);
    }

    @HandlesEvent("setContextTime")
    public Resolution setCtxTimeStamp() {
        String organization = this.getContext().getUser().getOrganization().getName();
        Osa.dbManager.get("sql").setCtxTime(organization);
        return new StreamingResolution(MIME_TEXT, "success");
    }

    @HandlesEvent("addRoot")
    public Resolution addRoot() throws FedoraClientException, Exception {
        Gui userGui = this.getContext().getGui();
        User user = this.getContext().getUser();

        if (root != null) {
            logger.info("Adding root = " + root);
            Osa.fedoraManager.createRootObject(root, userGui, user);

        } else if (currentOrganization != null) {
            logger.info("Adding root for organization " + root);
            Osa.fedoraManager.createRootObject(currentOrganization, userGui, user);
        }

        return new RedirectResolution(ADMIN);
    }

    /**
     * Clears edit form by redirecting to that page
     * 
     * @return Redirect
     */
    @HandlesEvent("clear")
    public Resolution clear() {
        return new RedirectResolution(TARGET + this.index);
    }

    @HandlesEvent("deleteRoot")
    public Resolution deleteRoot() throws FedoraClientException, Exception {
        String organization = "";
        if (root != null) {
            organization = root.toUpperCase();

        } else if (currentOrganization != null) {
            organization = currentOrganization.toUpperCase();
        }

        if (organization != "") {
            logger.info("Deleting root object from organization: " + organization);
            Osa.fedoraManager.deleteRootObject(organization);
        }
        return new RedirectResolution(ADMIN);
    }

    @HandlesEvent("delAllFromOrg")
    public Resolution deleteAllObjects() throws FedoraClientException, Exception {
        String organization = "";
        if (root != null) {
            organization = root.toUpperCase();

        } else if (currentOrganization != null) {
            organization = currentOrganization.toUpperCase();
        }

        if (organization != "") {
            logger.info("Deleting objects from organization: " + organization);
            Osa.fedoraManager.deleteAllobjects(organization);
        }
        return new RedirectResolution(ADMIN);
    }

    @HandlesEvent("deleteActionObjects")
    public Resolution deleteActionObjects() throws FedoraClientException, Exception {
        String organization = "";

        if (root != null) {
            organization = root.toUpperCase();

        } else if (currentOrganization != null) {
            organization = currentOrganization.toUpperCase();
        }

        if (organization != "") {
            logger.info("Deleting TOL 2008 and Liikearkisto action objects from organization: " + organization);
            Osa.fedoraManager.deleteActionObjects(organization, ":F-tol2008action*");
            Osa.fedoraManager.deleteActionObjects(organization, ":F-lyaction*");
        }
        return new RedirectResolution(ADMIN);
    }

    @HandlesEvent("edit")
    public Resolution edit() throws Exception {
        String original = null;
        FedoraBean prevFObject = null;
        User user = this.getContext().getUser();

        for (FedoraBean fObject : fedoraBeans) {

            allCaptureElements = fObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements();

            // Check if object is not editable
            MetaDataElement noneditable = allCaptureElements.get("noneditableObject");
            if (pid != null && noneditable != null && noneditable.getValue().equals("true")) {
                this.getContext().getValidationErrors().add("error", new LocalizableError("error.noneditable"));
                return this.getContext().getSourcePageResolution();
            }

            // Get original
            MetaDataElement originalElement = allCaptureElements.get("original");
            if (originalElement != null) {
                original = originalElement.getValue();
            }

            File file = null;
            bRemoveFile = false;
            bRemoveAttachment = false;

            // If there is original file to upload, upload it
            // This solution supports uploading multiple files at once, although
            // only one original file is allowed
            if (uploadedFile != null && uploadedFile.size() > 0) {
                // Check, that original does not exists
                if (original == null) {
                    for (int i = 0; i < uploadedFile.size(); i++) {
                        file = this.handleXhrUploadedFiles(uploadedFile.get(i), FileType.Original);
                    }
                } else {
                    logger.error("Original datastream already exists!");
                }
            }

            ArrayList<File> attachmentList = new ArrayList<File>();
            // If there are attachment files to upload, upload them
            if (uploadedAttachment != null && uploadedAttachment.size() > 0) {
                for (String filename : uploadedAttachment) {
                    if (filename != null) {
                        attachmentList.add(this.handleXhrUploadedFiles(filename, FileType.Attachment));
                    }
                }
            }

            if (pid != null) {

                // Get original object before updating (to check updated fields)
                prevFObject = Osa.fedoraManager.getObject(pid);

                LinkedHashMap<String, MetaDataElement> originalRelations = prevFObject.getDataStream(FedoraBean.DATASTREAM_RELS_EXT).getMetaDataElements();
                Iterator<Entry<String, MetaDataElement>> iterOriginalRelations = originalRelations.entrySet().iterator();

                // Check if relations have been updated
                while (iterOriginalRelations.hasNext()) {

                    Map.Entry<String, MetaDataElement> entry = (Map.Entry<String, MetaDataElement>) iterOriginalRelations.next();
                    MetaDataElement originalMDElement = entry.getValue();

                    MetaDataElement currentMDElement = allCaptureElements.get(originalMDElement.getName());

                    if (currentMDElement != null) {
                        if ((currentMDElement.getValue() == null && originalMDElement.getValue() != null)
                            || (currentMDElement.getValue() != null && !currentMDElement.getValue().equalsIgnoreCase(originalMDElement.getValue())))
                        {
                            String relationName = originalMDElement.getName();

                            if (relationName.equalsIgnoreCase(CaptureBean.RELATION_IsPartOf)) {
                                PreservationPlan plan = (PreservationPlan) this.getContext().getRequest().getSession().getAttribute("preservationPlan");
                                String objectType = RepositoryManager.DATATYPES_MAP.get(pid.split("-")[0].split(":")[1]);
                                boolean isCollection = false;

                                if (objectType.endsWith(RepositoryManager.COLLECTION)) {
                                    // collection itself has information in
                                    // preservation plan
                                    objectType = allCaptureElements.get("type").getValue();
                                    isCollection = true;
                                } else {
                                    // parent has information in preservation
                                    // plan
                                    objectType = Osa.searchManager.getObjectType(currentMDElement.getValue(), user);
                                }

                                String validationText = Osa.fedoraManager.isObjectPermitted(plan, objectType, -1, currentMDElement.getValue(), isCollection);
                                if (validationText != "") {
                                    if (!validationText.startsWith("error.preservation")) {
                                        logger.error("Max level in your preservation plan is " + plan.getMaxLevel());
                                        this.getContext().getValidationErrors().add("error", new SimpleError(validationText));
                                    } else if (isCollection) {
                                        logger.error("According to your preservation plan adding collection to this level is not allowed.");
                                        this.getContext().getValidationErrors().add("error", new LocalizableError(validationText));
                                    } else {
                                        logger.error("According to your preservation plan adding object to this level is not allowed.");
                                        this.getContext().getValidationErrors().add("error", new LocalizableError(validationText));
                                    }
                                    return this.getContext().getSourcePageResolution();
                                }
                            }

                            String object = originalMDElement.getValue();
                            // remove relationship
                            Osa.fedoraManager.purgeRelationship(pid, relationName, object, false);
                        }
                    }
                }
            } // if

            // get user specific gui
            Gui userGui = this.getContext().getGui();
            // get user specific organization part of the next Pid
            String organization = this.getContext().getUser().getOrganization().getName().toUpperCase();

            try {
                Osa.fedoraManager.ingest(userGui, user, organization, file,
                                         attachmentList, index, pid, allCaptureElements, prevFObject);
                
                String objectName = "";
                String objectId = "";
                if (allCaptureElements.containsKey(CaptureBean.TITLE)) {
                    objectName = allCaptureElements.get(CaptureBean.TITLE).getValue();
                } else if (allCaptureElements.containsKey(CaptureBean.PREFERREDNAME)) {
                    objectName = allCaptureElements.get(CaptureBean.PREFERREDNAME).getValue();
                }
                if (allCaptureElements.containsKey(CaptureBean.IDENTIFIER)) {
                    objectId = allCaptureElements.get(CaptureBean.IDENTIFIER).getValue();
                }
                Osa.dbManager.get("mongo").addToLatestEdited(this.getContext().getUser(), pid, objectName, objectId, MongoManager.EVENT_UPDATE);

            } catch (SAXParseException e) {
                this.getContext().getValidationErrors().add("error", new LocalizableError("error.validate"));
                logger.error("Schema validation found errors: " + e);
                return this.getContext().getSourcePageResolution();

            } catch (SAXException e) {
                this.getContext().getValidationErrors().add("error", new LocalizableError("error.ingest"));
                return this.getContext().getSourcePageResolution();
            }

            if (this.bRemoveFile) {
                // remove file if created to upload directory
                file.delete();
            }
            if (bRemoveAttachment) {
                // remove file if created to home directory
                for (File attachment : attachmentList) {
                    attachment.delete();
                }
            }
        }

        // Add message to inform user about successful update
        this.getContext().getMessages().add(new LocalizableMessage("message.update.success"));

        // If has uploaded files/attachments, send command to delete form
        // associated with them from sessionStorage after successful edit
        if (uploadedFile != null || uploadedAttachment != null) {
            return new RedirectResolution("/Ingest.action?pid=" + pid + "&plainView=true&view=").addParameter("rmst", "true");
        } else {
            return new RedirectResolution("/Ingest.action?pid=" + pid + "&plainView=true&view=");
        }
    }

    /**
     * Checks archive model when setting isPartOf-property through browsing
     * window.
     * 
     * @return <b>"allowed"</b> string if object can be inserted into selected
     *         element.<br/>
     *         <b>Localized error message</b> if cannot be inserted into
     *         selected element
     */
    @HandlesEvent("checkIsPartOf")
    public Resolution checkIsPartOf() {
        PreservationPlan plan = (PreservationPlan) this.getContext().getRequest().getSession().getAttribute("preservationPlan");
        boolean isCollection = false;
        User user = this.getContext().getUser();
        String objectType = "";
        String returnMsg = "";
        
        // Only check for isPartOf property, for now
        if (!CaptureBean.RELATION_IsPartOf.equalsIgnoreCase(selectedrelationname)) { return new StreamingResolution(MIME_TEXT, returnMsg); }

        if (index.endsWith(RepositoryManager.COLLECTION)) {
            isCollection = true;
            objectType = index;
        } else {
            // parent has information in preservation plan
            objectType = Osa.searchManager.getObjectType(newParent, user);
        }
      
        returnMsg = Osa.fedoraManager.isObjectPermitted(plan, objectType, nodeLevel, null, isCollection);

        if (!returnMsg.isEmpty()) {
            // If type is collection, add collection type to error message to
            // create more detailed message.
            if (isCollection) {
                String collectionParam = new LocalizableMessage("ingest." + index).getMessage(getUserLocale());
                return new StreamingResolution(MIME_TEXT, new LocalizableMessage(returnMsg, collectionParam).getMessage(getUserLocale()));
            }
            return new StreamingResolution(MIME_TEXT, new LocalizableMessage(returnMsg).getMessage(getUserLocale()));
        }

        return new StreamingResolution(MIME_TEXT, "allowed");
    }
    
    /**
     * Get type of object
     */
    @HandlesEvent("getCollectionType")
    public Resolution getCollectionType() {
           String objectType = "";
           // parent has information in preservation plan
           objectType = Osa.searchManager.getObjectType(newParent, this.getContext().getUser());
           return new StreamingResolution(MIME_TEXT, objectType);
    }

    /**
     * Checks if moving selected object to selected target is allowed according
     * to preservation plan
     * 
     * @return True / false in text format
     */
    @HandlesEvent("checkIfMoveAllowed")
    public Resolution checkIfMoveAllowed() {
        PreservationPlan plan = (PreservationPlan) this.getContext().getRequest().getSession().getAttribute("preservationPlan");
        boolean isCollection = false;
        User user = this.getContext().getUser();
        String objectType = "";
        
        if (pid != null) {
            String type = pid.split("-")[0].split(":")[1];
            objectType = RepositoryManager.DATATYPES_MAP.get(type);
        }

        if (objectType.endsWith(RepositoryManager.COLLECTION)) {
            // collection itself has information in preservation plan
            objectType = Osa.searchManager.getObjectType(pid, user);
            isCollection = true;
        } else {
            // parent has information in preservation plan
            objectType = Osa.searchManager.getObjectType(newParent, user);
        }

        String validationText = Osa.fedoraManager.isObjectPermitted(plan, objectType, nodeLevel, null, isCollection);
        if (validationText != "") {
            if (!validationText.startsWith("error.preservation")) {
                logger.error("Max level in your preservation plan is " + plan.getMaxLevel());
                this.getContext().getValidationErrors().add("error", new SimpleError(validationText));
            } else if (isCollection) {
                logger.error("According to your preservation plan adding collection to this level is not allowed.");
                this.getContext().getValidationErrors().add("error", new LocalizableError(validationText));
            } else {
                logger.error("According to your preservation plan adding object to this level is not allowed.");
                this.getContext().getValidationErrors().add("error", new LocalizableError(validationText));
            }
            return new StreamingResolution(MIME_TEXT, "false");
        }

        return new StreamingResolution(MIME_TEXT, "true");
    }

    /**
     * Moves objects (and its children) under other collections
     * 
     */
    @HandlesEvent("moveObject")
    public void moveObject() {
        User user = this.getContext().getUser();
        ArrayList<HashMap<String, String>> pids = new flexjson.JSONDeserializer<ArrayList<HashMap<String, String>>>().deserialize(movedpids);

        for (int x = 0; x < pids.size(); x++) {

            PreservationPlan plan = (PreservationPlan) this.getContext().getRequest().getSession().getAttribute("preservationPlan");
            String type = pids.get(x).get("pid").split("-")[0].split(":")[1];
            String pid = pids.get(x).get("pid");
            boolean isCollection = false;

            String objectType = RepositoryManager.DATATYPES_MAP.get(type);
            if (objectType.endsWith(RepositoryManager.COLLECTION)) {
                // collection itself has information in preservation plan
                objectType = Osa.searchManager.getObjectType(pid, user);
                isCollection = true;
            } else {
                // parent has information in preservation plan
                objectType = Osa.searchManager.getObjectType(newParent, user);
            }

            String validationText = "";
            validationText = Osa.fedoraManager.isObjectPermitted(plan, objectType, -1, newParent, isCollection);
            if (!validationText.isEmpty()) {
                if (!validationText.startsWith("error.preservation")) {
                    logger.error("Max level in your preservation plan is " + plan.getMaxLevel());
                    this.getContext().getValidationErrors().add("error", new SimpleError(validationText));
                } else if (isCollection) {
                    logger.error("According to your preservation plan adding collection to this level is not allowed.");
                    this.getContext().getValidationErrors().add("error", new LocalizableError(validationText));
                } else {
                    logger.error("According to your preservation plan adding object to this level is not allowed.");
                    this.getContext().getValidationErrors().add("error", new LocalizableError(validationText));
                }

            } else {
                try {
                    Osa.fedoraManager.moveObject(pid, oldParent, newParent, user);
                } catch (FedoraClientException e) {
                    logger.error("Error moving object: " + e);
                }
            }
        }
    }

    public TreeNode createNodes(String organization, HashMap<ArrayList<String>, String> paths) {
        HashMap<String, TreeNode> namemap = new HashMap<String, TreeNode>();
        TreeNode root = new TreeNode(organization, organization);
        TreeNode current;
        for (Entry<ArrayList<String>, String> path : paths.entrySet()) {
            current = root;
            for (String nodename : path.getKey()) {
                TreeNode p;
                if (!namemap.containsKey(nodename)) {
                    String objName = getObjName(nodename, organization);
                    p = new TreeNode(nodename, objName);
                    namemap.put(nodename, p);
                } else {
                    p = namemap.get(nodename);
                }
                current.addChild(p);
                current = p;
            }

        }
        return root;
    }

    public String getParent(ArrayList<HashMap<String, String>> resultlist, String child, String organization) {
        if (child.equals(organization + ":root")) { return child; }

        for (int i = 0; i < resultlist.size(); i++) {
            if (Osa.fedoraManager.cleanEntry(resultlist.get(i).get("child")).equals(child)) { 
                return Osa.fedoraManager.cleanEntry(resultlist.get(i).get("parent")); 
            }
        }
        return "Parent not found";
    }

    /**
     * Same principle as handleFileUpload() except this one does not upload the
     * file,
     * only reads the file from given location
     * 
     * @param filename
     *            File to upload
     * @param type
     *            Type of file (original/attachment)
     * */
    public File handleXhrUploadedFiles(String filename, FileType type) {
        
        File file = null;
        String dir = this.getUploadDirectory(true);
        
        try {
            filename = cleanUtf8Encoded(filename);
            file = new File(dir + filename);

            if (!type.equals(FileType.Original)) {
                if (this.attachmentNames.get(filename) != null) {
                    File attWithName = new File(dir + attachmentNames.get(filename));
                    FileUtils.moveFile(file, attWithName);
                    file = attWithName;
                }
            }

        } catch (IOException e) {
            logger.error(e);
        }

        if (file.canRead()) {
            this.modifyReference(type, true);
        } else {
            return null;
        }

        return file;
    }

    // Handles the actual upload of files done with xhr upload
    // (multiupload-component)
    /**
     * Temporary uploads file(s) to server's temp directory
     * 
     * @return Response of success of upload in json format
     */
    @HandlesEvent("xhrUpload")
    public Resolution handleXhrFileUpload() {
        String response = null;
        InputStream is = null;
        FileOutputStream fos = null;

        String dir = this.getUploadDirectory(true);
        
        if (this.isDocumentAdder()) {

            HttpServletRequest request = this.getContext().getRequest();
            String filename = request.getHeader("X-File-Name");
            String mimeType = request.getHeader("X-Mime-Type");
            
            try {
                filename = URLDecoder.decode(filename, "UTF-8");
                is = request.getInputStream();
                fos = new FileOutputStream(new File(dir + filename));
                IOUtils.copy(is, fos);
            } catch (FileNotFoundException ex) {
                logger.error(ex);
                response = "{success:false}";
                return new StreamingResolution(MIME_JS, response);
            } catch (IOException ex) {
                logger.error(ex);
                response = "{success:false}";
                return new StreamingResolution(MIME_JS, response);
            } finally {
                try {
                    fos.close();
                    is.close();
                } catch (IOException e) {
                    logger.error(e);
                    response = "{success:false}";
                    return new StreamingResolution(MIME_JS, response);
                }
            }

            ThumbBean.createThumbFileAfterUpload(mimeType, filename, dir, this.getContext());
            response = "{success:true}";

        } else {
            response = "{success:false}";
        }
        
        return new StreamingResolution(MIME_JS, response);
    }

    /**
     * Gets uploaded image's thumbnail image after successful upload.
     * 
     * @return Image as a stream.
     */
    @HandlesEvent("showThumbAfterUpload")
    public Resolution showThumbAfterUpload() {
        String mimeFormat = "";
        String filename = "";
        DataInputStream dis = null;
        FileInputStream fis = null;
        String dir = this.getUploadDirectory(true);
        
        try {
            filename = cleanUtf8Encoded(watchedFile);
            filename = FilenameUtils.removeExtension(filename);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        File thumbfile = new File(dir + ThumbBean.THUMB_DSID + "_" + filename + "." + ThumbBean.THUMB_EXTENSION);
        if (thumbfile.canRead()) {
            mimeFormat = "image/" + ThumbBean.THUMB_EXTENSION;
            try {
                fis = new FileInputStream(thumbfile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            dis = new DataInputStream(fis);
        }
        return new StreamingResolution(mimeFormat, dis).setAttachment(false);
    }

    /**
     * Removes original file from uploaded list so user can upload a new one
     * incase they uploaded a wrong one at first.
     * 
     * @return Success of removal
     * @throws UnsupportedEncodingException
     */
    @HandlesEvent("removeFileFromUpload")
    public Resolution removeFileFromUpload() throws UnsupportedEncodingException {
        File file = null;
        String response = "";
        String dir = this.getUploadDirectory(true);
        
        fileToDelete = URLDecoder.decode(fileToDelete, "UTF-8");
        file = new File(dir + fileToDelete);

        if (file.canRead()) {
            // Delete possible thumbnail image
            new File(dir + "thumb_" + FilenameUtils.removeExtension(fileToDelete) + ".png").delete();
            if (file.delete()) {
                response = "true";
            } else {
                response = "false";
            }
        } else {
            response = "false";
        }
        return new StreamingResolution(MIME_TEXT, response);
    }

    /**
     * Uploads file to a watched folder
     * 
     * @return Success in json format (true / false)
     */
    @HandlesEvent("watchUpload")
    public Resolution uploadToWatchedfolder() {
        String response = null;
        InputStream is = null;
        FileOutputStream fos = null;
        String organization = getUserOrganizationName();
        String username = getUserName().replaceAll(" ", "");
        String dir = this.getImportDirectory(true);
        File orgdir = null;
        String fileName = "";
        
        if (watchedFile.equals("ROOT")) {
            orgdir = new File(dir);
        } else {
            orgdir = new File(dir + watchedFile);
        }

        if (!orgdir.exists()) {
        	orgdir.setWritable(true, true);
            orgdir.mkdirs();
        }

        HttpServletRequest request = this.getContext().getRequest();
        String filename = request.getHeader("X-File-Name");
        File targetFile = null;
        try {
        	filename = URLDecoder.decode(filename, "UTF-8");
            is = request.getInputStream();

            targetFile = new File(orgdir + "/" + filename);
            targetFile.setWritable(true);

            if (targetFile.exists()) {
                int count = 1;
                while (targetFile.exists()) {
                    targetFile = new File(orgdir + "/" + filename + "(" + count + ")");
                    count++;
                }
            }

            fos = new FileOutputStream(targetFile);
            IOUtils.copy(is, fos);
            fileName = targetFile.getName();
            
        } catch (FileNotFoundException ex) {
            logger.error(ex);
            response = "{success:false}";
            return new StreamingResolution(MIME_JS, response);
        } catch (IOException ex) {
            logger.error(ex);
            response = "{success:false}";
            return new StreamingResolution(MIME_JS, response);
        } finally {
            try {
                fos.close();
                is.close();
            } catch (IOException e) {
                logger.error(e);
                response = "{success:false}";
                return new StreamingResolution(MIME_JS, response);
            }
        }
        
        Map<String, Object> opts = new HashMap<String, Object>();
        opts.put(WorkflowManager.OPT_USERNAME, username);
        opts.put(WorkflowManager.OPT_USERMAIL, this.getContext().getUser().getMail());
        opts.put(WorkflowManager.OPT_ORG, organization);
        opts.put(WorkflowManager.OPT_IMPORTDIR, this.getImportDirectory(true));
        opts.put(WorkflowManager.OPT_UPLOADDIR, this.getUploadDirectory(true));
        opts.put(WorkflowManager.OPT_INGESTDIR, this.getIngestDirectory(true));
        opts.put(WorkflowManager.OPT_FAILEDDIR, this.getFailedDirectory());
        opts.put(WorkflowManager.OPT_FILENAME, fileName);
        
        // Start workflow and load files to mongo
        boolean isHandled =  false;
        try {
            isHandled = Osa.workflowManager.startWorkflow(WorkflowManager.WORKFLOW_PREINGEST, opts);
            
        } catch (ClientHandlerException e) {
            logger.error(e);
        }
                
        if (!isHandled) {
            response = "{success:false}";
            targetFile.delete();
        } else {
            response = "{success:true}";
        }
        
        return new StreamingResolution(MIME_JS, response);
    }

    @HandlesEvent("moveFile")
    public Resolution moveFile() {

        String watchedDir = this.getImportDirectory(false);
        String uploadDir = this.getUploadDirectory(false);

        File fileToMove = new File(watchedDir+ watchedFile);
        File targetFile = new File(uploadDir + watchedFile);

        if (!fileToMove.canRead()) {
            String response = "{ \"success\" : \"filenotfound\" }";
            return new StreamingResolution(MIME_JS, response);
        }

        InputStream inStream = null;
        OutputStream outStream = null;

        try {
            inStream = new FileInputStream(fileToMove);
            outStream = new FileOutputStream(targetFile);

            byte[] buffer = new byte[1024];

            int length;
            // copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

            inStream.close();
            outStream.close();

            // delete the original file
            fileToMove.delete();

        } catch (FileNotFoundException e) {
            logger.error("IngestAction error moving file, " + e);
        } catch (IOException e) {
            logger.error("IngestAction error moving file, " + e);
        }

        String response = "{success:true}";
        return new StreamingResolution(MIME_JS, response);

    }

    /**
     * Moves given file to given watched folder
     * 
     */
    @HandlesEvent("moveToWatchedFolder")
    public Resolution moveToWatchedFolder() {
        String watchedDir = this.getIngestDirectory(false);
        String response   = "";

        for (String filename : watchedFiles) {
            File file = new File(watchedDir + "/" + filename);
            File targetfile = null;
            if (toFolder.equals("ROOT")) {
                targetfile = new File(this.getIngestDirectory(true) + file.getName());
            } else {
                targetfile = new File(this.getIngestDirectory(true) + toFolder + "/" + file.getName());
            }

            try {
                FileUtils.moveFile(file, targetfile);
            } catch (IOException e) {
                logger.error("IngestAction moveToWatchedFolder error, " + e);
                response = "false";
                return new StreamingResolution(MIME_TEXT, response);
            }
        }
        response = "true";
        return new StreamingResolution(MIME_TEXT, response);

    }

    /**
     * Deletes watched given folder
     * 
     * @return Success in text (true / false)
     */
    @HandlesEvent("deleteWatchedFolder")
    public Resolution deleteWatchedFolder() {
        String ingestDir = this.getIngestDirectory(true);
        File dir = new File(ingestDir + watchedFile);
        String response = "";
        
        try {
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            response = "false";
            e.printStackTrace();
            return new StreamingResolution(MIME_TEXT, response);
        }
        response = "true";
        return new StreamingResolution(MIME_TEXT, response);
    }

    /**
     * Removes file(s) from watched folder
     * 
     * @return Success/file in map, serialized as JSON
     */
    @HandlesEvent("removeFromWatchedFolder")
    public Resolution removeFromWatchedFolder() {
        String watchedDir = this.getIngestDirectory(false);
        String queueDir = this.getIngestDirectory(false);
        String uploadDir = this.getUploadDirectory(false);
        File file = null;
        File thumb = null;
        HashMap<String, String> responseMap = new HashMap<String, String>();

        for (String filename : watchedFiles) {
            if (fromQueue) {
                file = new File(queueDir + "/" + filename);
            } else {
                file = new File(watchedDir + "/" + filename);
            }

            if (file.canRead()) {
                if (file.delete()) {
                	responseMap.put(file.getName(), "true");
                } else {
                	responseMap.put(file.getName(), "false");
                }
            } else {
            	responseMap.put(file.getName(), "false");
            }
            
            // remove thumb
            String baseName = FilenameUtils.getBaseName(filename);
            String thumbnailPathAndName = filename.replace(baseName, ThumbBean.THUMB_DSID+"_"+baseName);
            thumbnailPathAndName = FilenameUtils.removeExtension(thumbnailPathAndName)+"."+ThumbBean.THUMB_EXTENSION;
            thumb = new File(uploadDir + "/" + thumbnailPathAndName);
            if (thumb.canRead()) {
                thumb.delete();
            }
            
            // remove from mongo
            User user = this.getContext().getUser();
            Osa.dbManager.get("mongo").removeMetadataFile(user, FilenameUtils.getName(filename));
        }
        return new StreamingResolution(MIME_JS, new flexjson.JSONSerializer().serialize(responseMap));
    }

    @HandlesEvent("massEdit")
    public Resolution createCustomForm() throws FedoraClientException, Exception {
        List uniqueFormNames = new ArrayList();
        LinkedHashMap<String, FormElement> form = new LinkedHashMap<String, FormElement>();
        LinkedHashSet<String> tabs = new LinkedHashSet<String>();
        Vector<LinkedHashMap<String, FormElement>> list = new Vector<LinkedHashMap<String, FormElement>>();

        // If only one object is chosen, process via view()
        if (pidArray.size() == 1) {
            pid = pidArray.get(0);
            return view();
        }

        // Gets all unique form names to prevent unnecessary looping through
        // multiple same form names
        uniqueFormNames = SetUniqueList.decorate(formNames);

        FedoraBean fObject = null;
        for (String pid : pidArray) {
            fObject = Osa.fedoraManager.findObject(pid);
            this.fedoraBeans.add(fObject);
        }

        // Gets form information for given form names
        for (int k = 0; k < uniqueFormNames.size(); k++) {
            list.add(this.getContext().getGui().getGUIElements(uniqueFormNames.get(k).toString()));
        }

        // Filters fields that are not in every given form. If only one form is
        // given, no need to filter
        if (list.size() > 1) {
            for (int i = 1; i < list.size(); i++) {
                list.get(0).keySet().retainAll(list.get(i).keySet());
            }
        }
        // Constructs a map containing new custom form based on fields filtered
        // above
        for (String field : list.get(0).keySet()) {
            // Exclude pid, type & original fields
            if (list.size() > 1) {
                if (!field.equalsIgnoreCase("pid") && !field.equalsIgnoreCase("type") && !field.equalsIgnoreCase("original")) {
                    form.put(field, list.get(0).get(field));
                    tabs.add(list.get(0).get(field).getTab());
                }
            } else {
                form.put(field, list.get(0).get(field));
                tabs.add(list.get(0).get(field).getTab());
            }
        }

        setCustomForm(form);
        setCustomTabs(tabs);
        return new ForwardResolution(TARGET + "mass");
    }

    @HandlesEvent("addMetaDataFiles")
    public Resolution addMetaDataFiles() {
        
        allCaptureElements = fedoraBeans.get(0).getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements();
        watchedFiles = (Vector<String>) this.getContext().getRequest().getSession().getAttribute("watchedFiles");
        boolean massEdit = watchedFiles.size() > 1 ? true:false;
        
        for (int i = 0; i < watchedFiles.size(); i++) {
            LinkedHashMap<String, MetaDataElement> map = new LinkedHashMap<String, MetaDataElement>();
            for (Entry<String, MetaDataElement> entry : allCaptureElements.entrySet()) {
                // Do not store empty fields
                if (!entry.getValue().isEmpty()) {
                    map.put(entry.getKey(), entry.getValue());
                }
            }
            
            Osa.dbManager.get("mongo").addMetadataFile(this.getContext().getUser(), watchedFiles.get(i), map, massEdit);
        }

        this.getContext().getRequest().getSession().removeAttribute("watchedFiles");
        return new RedirectResolution("/workspace.jsp").addParameter("tab", "ingest-auto");
    }

    @HandlesEvent("getMetaDataFiles")
    public Resolution getMetaDataFiles() {
        inconsistantFields = new Vector<String>();
        LinkedHashMap<String, MetaDataElement> oneFile = new LinkedHashMap<String, MetaDataElement>();
        LinkedHashMap<String, MetaDataElement> combinedMetadata = new LinkedHashMap<String, MetaDataElement>();
        
        FedoraBean fbean = new FedoraBean();
        this.fedoraBeans.add(fbean);
        
        HttpSession session = this.getContext().getRequest().getSession();
        session.setAttribute("watchedFiles", watchedFiles);
        int filesHandled = 0;
        
        for (String filename : watchedFiles) {
            oneFile = Osa.dbManager.get("mongo").getMetadataFile(this.getContext().getUser(), filename);
            for (Entry<String, MetaDataElement> entry : oneFile.entrySet()) {
                // If combined metadata map already has field currently in loop
                if (combinedMetadata.containsKey(entry.getKey())) {
                    // If combined metadata does not have same value as current
                    // field, insert empty value.
                    if (!combinedMetadata.get(entry.getKey()).getValue().equals(entry.getValue().getValue())) {
                        combinedMetadata.put(entry.getKey(), 
                                             new MetaDataElement(entry.getKey(),
                                                                 entry.getKey(),
                                                                 "",
                                                                 entry.getValue().getMetaDataType()));
                        // Add field name to vector for later use
                        inconsistantFields.add(entry.getKey());
                    }
                } else {
                    // If the first file has not a value and the later files have
                    if (filesHandled>0 && !entry.getValue().getValue().isEmpty()) {
                        combinedMetadata.put(entry.getKey(), 
                                new MetaDataElement(entry.getKey(),
                                                    entry.getKey(),
                                                    "",
                                                    entry.getValue().getMetaDataType()));
                        
                        // Add field name to vector for later use
                        inconsistantFields.add(entry.getKey());
                        
                    } else {
                        combinedMetadata.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            filesHandled++;
        }
        // If one file selected and its saved type value does not match selected
        // form, inform user
//        if (watchedFiles.size() == 1 && !oneFile.isEmpty()) {
//            if (!oneFile.get("type").getValue().equalsIgnoreCase(index)) inconsistantFields.add(oneFile.get("type").getName());
//        }
        this.fedoraBeans.get(0).getDataStream(FedoraBean.DATASTREAM_CAPTURE).setMetaDataElements(combinedMetadata);
        return new ForwardResolution(TARGET + index).addParameter("metadata", 1);
    }

    @HandlesEvent("addWatchedFolder")
    public Resolution addWatchedFolder() {
        String response = "";
        String watchedDir = this.getIngestDirectory(true);
        File dir = new File(watchedDir + watchedFile);

        if (dir.mkdirs()) {
            response = "true";
        } else {
            response = "false";
        }

        return new StreamingResolution(MIME_TEXT, response);
    }

    @HandlesEvent("listWatchedFolders")
    public Resolution listWatchedFolder() {
        ArrayList<HashMap<String, String>> folders = new ArrayList<HashMap<String, String>>();
        // CHECK THIS Does import have / will have sub directories, or just ingest dir?
        String watchedDir = this.getIngestDirectory(true);
        File dir = new File(watchedDir);

        HashMap<String, String> root = new HashMap<String, String>();
        root.put("name", "ROOT");
        folders.add(root);
        int rootcount = 0;

        if (!dir.exists()) {
        	folders.get(0).put("count", String.valueOf(rootcount));
        	return new StreamingResolution(MIME_JS, new flexjson.JSONSerializer().serialize(folders));
    	}
        File[] folderList = dir.listFiles();
        // Sort folders by their modification date, descending order
        Arrays.sort(folderList, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
        for (File file : folderList) {
            if (file.isDirectory()) {
                HashMap<String, String> entry = new HashMap<String, String>();
                entry.put("name", file.getName());
                int count = file.listFiles().length;
                entry.put("count", String.valueOf(count));
                folders.add(entry);
            } else {
                rootcount++;
            }
        }

        folders.get(0).put("count", String.valueOf(rootcount));
        
        return new StreamingResolution(MIME_JS, new flexjson.JSONSerializer().serialize(folders));
    }

    /** Gets given directory's content, files only.<br/>
     *  Uses Java 7's NIO<br/>
     *  <b>See:</b><br/>
     *  http://www.rgagnon.com/javadetails/java-get-directory-content-faster-with-many-files.html<br/>
     *  http://blog.eyallupu.com/2011/11/java-7-working-with-directories.html
     * 
     * @param directory			Directory path as string where the files will be read from.
     * @param globFilter		String filter in glob syntax, f.ex "*.{txt,log}" gets all .txt and .log files
     * @param isRecursive		Boolean flag indicating whether directory should be read recursively
     * @return					List of files
     * @throws IOException
     */
    public List<File> getDirectoryContent(String directory, String globFilter, final boolean isRecursive) throws IOException {
        DirectoryStream<Path> stream = null;
		ArrayList<File> files = new ArrayList<File>();
		
		try {
			// Get default file system for current system
			FileSystem fs = FileSystems.getDefault();
			// Converts given directory path string to Path
			Path path = fs.getPath(directory);
			
			// Open DirectoryStream pointing to given path, e.g. gets all the files and directories from given location
			stream = Files.newDirectoryStream(path);
			// Iterate through paths in stream
			for (Path file : stream) {
				// Iterate directories recursively if set so 
				if (Files.isDirectory(file)) {
					if (isRecursive) {
						files.addAll(getDirectoryContent(file.toAbsolutePath().toString(), globFilter, isRecursive));
					}
				}
				else {
					//TODO Update matcher to match full path, at the moment it only matches the filename
					// If filter is given, get files matching to it and add them to the list
					if (globFilter != null && StringUtils.isNotEmpty(globFilter)) {
						String filter = "glob:" + "*"+globFilter+"*";
						PathMatcher matcher = fs.getPathMatcher(filter);
						if (matcher.matches(file.getFileName())) {
							files.add(file.toFile());
						}
					} else {
						files.add(file.toFile());
					}
				}
			}
		} catch (IOException ioE) {
			ioE.printStackTrace();
		} finally {
			try {
				// Stream must be closed
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return files;
    }

    /**
     * Gets the content of watched folder
     * 
     * @return Files in watched folder in json format
     * @throws IOException
     */
    @HandlesEvent("getWatchedFolderContent")
    public Resolution getWatchedFolderContent() throws IOException {
        String importDir = this.getImportDirectory(false);
        String ingestDir = this.getIngestDirectory(false);
        String logDir	 = this.getLogDirectory(null, true);
        String failedDir = Osa.dataDirectory + Osa.failedDirectory;
        
        boolean isRecursive = false;
        boolean isImport 	= false;
        boolean isIngest 	= false;
        boolean isQueue 	= true;
        boolean isLog 		= false;
        
        ResourceBundle msgBundle = ResourceBundle.getBundle("fi.mamk.osa.core.messages", getUserLocale());
        
        User user = this.getContext().getUser();
        String organization = getUserOrganizationName();
        String username = getUserName().replaceAll(" ", "");

        LinkedHashMap<String, MetaDataElement> mdElement = new LinkedHashMap<String, MetaDataElement>();
        File dir 		   = null;
        String json 	   = "";
        String sorting 	   = "";
        String pathToClean = "";

        HashMap<String, Vector<String>> tableHeaders = new HashMap<String, Vector<String>>();
        Vector<String> IMPORT_headers = new Vector<String>();
        Vector<String> INGEST_headers = new Vector<String>();
        Vector<String> QUEUE_headers  = new Vector<String>();
        Vector<String> LOG_headers    = new Vector<String>();

        if ("IMPORT".equals(watchTable)) {
            dir = new File(importDir + "/" + organization + "/" + username);
            pathToClean = importDir;
            isImport = true;
        } else if ("LOG".equals(watchTable)) {
        	dir = new File(logDir);
        	pathToClean = logDir;
        	isRecursive = true;
        	isLog = true;
        } else {
            pathToClean = ingestDir;
            isIngest = true;
            // Selects root folder
            if ("INGEST".equals(watchTable) && "ROOT".equals(watchedFile)) {
                dir = new File(ingestDir + "/" + organization + "/" + username);
            }
            // Selects subfolder from root folder
            else {
                dir = new File(ingestDir + "/" + organization + "/" + username + "/" + watchedFile);
            }
        }
        
        //TODO Define in config?
        if (isImport) {
	        IMPORT_headers.add("checkbox");
	        IMPORT_headers.add("filename");
	        IMPORT_headers.add("type");
	        IMPORT_headers.add("size");
	        IMPORT_headers.add("metadata");
	        IMPORT_headers.add("path");
	        tableHeaders.put("IMPORT", IMPORT_headers);
        }
        
        else if (isIngest) {
	        INGEST_headers.add("checkbox");
	        INGEST_headers.add("filename");
	        INGEST_headers.add("contentmodel");
	        INGEST_headers.add("size");
	        INGEST_headers.add("metadata");
	        INGEST_headers.add("ispartof");
	        INGEST_headers.add("path");
	        tableHeaders.put("INGEST", INGEST_headers);
        }
        
        else if (isLog) {
	        LOG_headers.add("checkbox");
	        LOG_headers.add("logfilename");
	        LOG_headers.add("microservicetype");
	        LOG_headers.add("size");
	        LOG_headers.add("modified");
	        tableHeaders.put("LOG", LOG_headers);
        }
        
    	QUEUE_headers.add("filename");
    	QUEUE_headers.add("type");
    	QUEUE_headers.add("size");
    	QUEUE_headers.add("metadata");
    	QUEUE_headers.add("state");
    	tableHeaders.put("QUEUE", QUEUE_headers);

        if (!dir.exists()) { 
        	json = "{" +
                    "\"sEcho\": " + sEcho + "," +
                    "\"iTotalRecords\": " + 0 + "," +
                    "\"iTotalDisplayRecords\": " + 0 + ",";
        	json += "\"aaData\": [";
        	json += "]}";
        	return new StreamingResolution(MIME_JS, json);
        }

        String filter = null;
        if (sSearch != null) {
            filter = sSearch;
        }

        // Get directory content
        List<File> filelist = getDirectoryContent(dir.getAbsolutePath(), filter, isRecursive);
        
        // Sorting
        sorting = tableHeaders.get(watchTable).get(iSortCol_0) + "_" + sSortDir_0;
        switch (sorting) {
            case "filename_asc":
                Collections.sort(filelist, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
                break;
            case "filename_desc":
            	Collections.sort(filelist, NameFileComparator.NAME_INSENSITIVE_REVERSE);
                break;
            case "size_asc":
            	Collections.sort(filelist, SizeFileComparator.SIZE_COMPARATOR);
                break;
            case "size_desc":
            	Collections.sort(filelist, SizeFileComparator.SIZE_REVERSE);
                break;
            case "type_asc":
            	Collections.sort(filelist, ExtensionFileComparator.EXTENSION_INSENSITIVE_COMPARATOR);
                break;
            case "type_desc":
            	Collections.sort(filelist,  ExtensionFileComparator.EXTENSION_INSENSITIVE_REVERSE);
                break;
            case "path_asc":
            case "microservicetype_asc":
            case "logfilename_asc":
            	Collections.sort(filelist, PathFileComparator.PATH_INSENSITIVE_COMPARATOR);
                break;
            case "path_desc":
            case "microservicetype_desc":
            case "logfilename_desc":
            	Collections.sort(filelist, PathFileComparator.PATH_INSENSITIVE_REVERSE);
                break;
            case "modified_asc":
            	Collections.sort(filelist, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
            	break;
            case "modified_desc":
            	Collections.sort(filelist, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
            	break;
            default:
            	Collections.sort(filelist, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
            	break;
        }

        int filecount = filelist.size();

        json = "{" +
                "\"sEcho\": " + sEcho + "," +
                "\"iTotalRecords\": " + filecount + "," +
                "\"iTotalDisplayRecords\": " + filecount + ",";

        json += "\"aaData\": [";

        int showUntil = (iDisplayStart + iDisplayLength > filecount) ? filecount : iDisplayStart + iDisplayLength;
        for (int i = iDisplayStart; i < showUntil; i++) {
        	File file = filelist.get(i);
        	boolean hasMetadata = false;
        	
        	// Only get metadata files if not browsing log files
        	if (!"LOG".equals(watchTable)) {
	        	mdElement = Osa.dbManager.get("mongo").getMetadataFile(this.getContext().getUser(), file.getName());
        	}
    		if (mdElement != null && !mdElement.isEmpty()) {
    			hasMetadata = true;
    		}
        	
            json += "[";
            for (int j = 0; j < tableHeaders.get(watchTable).size(); j++) {
            	String headerName = tableHeaders.get(watchTable).get(j);
            	
                if ("checkbox".equals(headerName)) {
                    json += "\"<input class='watchcheck' type='checkbox'" +
                            " data-filepath='" + file.getAbsolutePath().replace(pathToClean, "") + "'" +
                            " data-filename='" + file.getName() + "'/>\",";
                }
                else if ("filename".equals(headerName)) {
                    json += "\"<span title='" +file.getName()+ "'>" + file.getName() + "</span>\",";
                }
                else if ("logfilename".equals(headerName)) {
                	String logFile = file.getParentFile().getName()+ "/" + file.getName();
                	String fileDisplayName = file.getParentFile().getName()+ "-" + file.getName();
                	// Gets log type from path, e.g "microservices" or "workflow"
                	String logType = file.getAbsolutePath().replace(logDir+"/", "").split("/")[0];
                	
                    json += "\"<a href='Ingest.action?filename="+logFile+"&logName="+logType+"&showLogFile=' target='_blank' title='"+msgBundle.getString("message.showlogfile")+"'>"+fileDisplayName+"</a>\",";
                }
                else if ("type".equals(headerName)) {
                    json += "\"" + FilenameUtils.getExtension(file.getName()) + "\",";
                }
                else if ("contentmodel".equals(headerName)) {
                	String type = "";
                	if (mdElement.containsKey(CaptureBean.TYPE)) {
                		type = msgBundle.getString("ingest."+mdElement.get(CaptureBean.TYPE).getValue());
                	}
                	json += "\"" +type+ "\",";
                }
                else if ("size".equals(headerName)) {
                    json += "\"" + FileUtils.byteCountToDisplaySize(file.length()) + "\",";
                }
                else if ("metadata".equals(headerName)) {
                    json += "\"" + (hasMetadata ? msgBundle.getString("general.yes") : msgBundle.getString("general.no")) + "\",";
                }
                else if ("ispartof".equals(headerName)) {
                	String isPartOf = (mdElement.get(CaptureBean.RELATION_IsPartOf) != null) ? mdElement.get(CaptureBean.RELATION_IsPartOf).getValue() : "";
                	if (StringUtils.isNotEmpty(isPartOf)) {
                		isPartOf = Osa.searchManager.getItemByPid(isPartOf, user, getUserLocale());
                	}
                	json += "\"" + isPartOf + "\",";
                }
                else if ("path".equals(headerName)) {
                    json += "\"<span title='" +file.getAbsolutePath().replace(pathToClean, "")+ "'>" + file.getAbsolutePath().replace(pathToClean, "") + "</span>\",";
                }
                else if ("microservicetype".equals(headerName)) {
                    json += "\""+ msgBundle.getString("workflow."+file.getParentFile().getName().toLowerCase()) +"\",";
                }
                else if ("modified".equals(headerName)) {
                	SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                    json += "\""+ sdf.format(new Date(file.lastModified())) +"\",";
                }
                else if ("state".equals(headerName)) {
                    json += "\"Not started\",";
                }
            } // for tableHeaders
            if (filecount > 0) {
                json = json.substring(0, json.length() - 1);		// strip last colon
            }
            json += "],";
        } // for files
        if (filecount > 0) {
            json = json.substring(0, json.length() - 1);		// strip last colon
        }
        json += "]}";
        
        return new StreamingResolution(MIME_JS, json);
    }

    /**
     * Gets the content of given collection for displaying
     * 
     * @return Content of given collection in json format
     */
    @HandlesEvent("getCollectionContent")
    public Resolution getCollectionContent() {
        List<TreeNode> resultlist = Osa.searchManager.getChildren(pid, null, this.getContext().getUser(), null, null);
        String jsonData = new flexjson.JSONSerializer().serialize(resultlist);
        
        return new StreamingResolution(MIME_JS, jsonData);
    }

    /**
     * Creates a tree view with given object type.
     * selectedrelationname sets the object's type
     * 
     * @return Tree view data in json format
     */
    // structure TODO: refactor something to improve performance..
    @HandlesEvent("listCollections")
    public Resolution listCollections() {
        
        String jsonData = "";
        String organization = "";
        String userAccessRights = null;
        String origRootPid = rootpid;
        HashMap<String, String> filters = null;
        User user = this.getContext().getUser();
        
        if (user != null) {
            organization = getUserOrganizationName().toUpperCase();
            userAccessRights = Osa.searchManager.parseAccessRights(user);
        }

        if (rootpid.equalsIgnoreCase(":root") && selectedrelationname != null &&
                (selectedrelationname.contains(RepositoryManager.ACTION)
                        || selectedrelationname.contains(RepositoryManager.AGENT)
                        || selectedrelationname.contains(RepositoryManager.EVENT)
                        || selectedrelationname.contains(RepositoryManager.PLACE)))
        {
            selectedrelationname = selectedrelationname.replaceFirst("Id", "");
            rootpid = organization + RepositoryManager.CONTEXTOBJECT_COLLECTIONS.get(selectedrelationname);
            
        } else if (rootpid.equalsIgnoreCase(":root")) {
            rootpid = organization + rootpid;
        }
                       
        // Check, which button has been clicked (if null, show all nodes)
        String pidType = null;
        if (selectedrelationname != null) {
            if (selectedrelationname.equals(CaptureBean.RELATION_IsPartOf)) {
                selectedrelationname = RepositoryManager.COLLECTION;
                // Fetch only collections when browsing for isPartOf
                filters = new HashMap<String, String>();
                filters.put("c.type", "*collection");
                
            } else {
                selectedrelationname = selectedrelationname.replaceFirst("Id", "");
                if (selectedrelationname.equals(RepositoryManager.ACTION) && index != null) {
                    if (rootpid.contains("tol2008action")) {
                        pidType = "F-tol2008action";
                    } else {
                        pidType = "F-lyaction";
                    }
                }
            }

            if (pidType == null) {
                // get abbreviation for mime from HashMap
                pidType = RepositoryManager.DATATYPES_MAP_REVERSE.get(selectedrelationname);
            }
        }

        List<TreeNode> elements = new ArrayList<TreeNode>();
        elements = Osa.searchManager.getChildren(rootpid, pidType, user, filters, userAccessRights);
        
        for (TreeNode node : elements) {
            if (node.getId().contains(RepositoryManager.PID_COLLECTION_TOL2008ACTIONS)) {
                pidType = "F-tol2008action";
            } else if (node.getId().contains(RepositoryManager.PID_COLLECTION_LYACTIONS)) {
                pidType = "F-lyaction";
            }        
            node.setChildren(Osa.searchManager.getChildren(node.getId(), pidType, user, filters, userAccessRights));
        }
        
        // If browsing actions, agents, events or places and is first load
        // (origRootPid is :root)
        if (origRootPid.contains(":root") && selectedrelationname != null &&
                (selectedrelationname.contains(RepositoryManager.ACTION)
                        || selectedrelationname.contains(RepositoryManager.AGENT)
                        || selectedrelationname.contains(RepositoryManager.EVENT)
                        || selectedrelationname.contains(RepositoryManager.PLACE)))
        {
            TreeNode tree = new TreeNode();
            tree.setId(rootpid);
            tree.setType(RepositoryManager.DATATYPES_MAP_REVERSE.get(RepositoryManager.COLLECTION));
            tree.setName(Osa.searchManager.getItemByPid(rootpid, user, getUserLocale()));
            tree.setChildren(elements);
            tree.setLoadOnDemand(true);
            
            jsonData = new flexjson.JSONSerializer().deepSerialize(tree);
            
        } else if (origRootPid.contains(":root")) {
            TreeNode tree = new TreeNode();
            tree.setId(organization + ":root");
            tree.setName(organization);
            tree.setChildren(elements);
            tree.setLoadOnDemand(true);
            
            jsonData = new flexjson.JSONSerializer().deepSerialize(tree);
            
        } else {
            jsonData = new flexjson.JSONSerializer().deepSerialize(elements);
            
        }
                
        return new StreamingResolution(MIME_JS, jsonData);
    }

    /**
     * Used to get only collection objects from fedora which match the search.
     * Term provided by jquery autocompelete
     * 
     * @return Objects with path in json format
     */
    @HandlesEvent("findRelations")
    public Resolution getObjPaths() {
        String collectionData = null;
        String jsonData = "";

        if (this.getContext().getUser() != null) {
            String organization = getUserOrganizationName().toUpperCase();

            if (organization != null) {
                collectionData = Osa.fedoraManager.getCollectionObjects(organization);

                HashMap<String, ArrayList<HashMap<String, String>>> results = new flexjson.JSONDeserializer<HashMap<String, ArrayList<HashMap<String, String>>>>().deserialize(collectionData);
                ArrayList<HashMap<String, String>> resultlist = results.get("results");
                ArrayList<HashMap<String, Object>> paths = new ArrayList<HashMap<String, Object>>();

                for (int i = 0; i < resultlist.size(); i++) {
                    ArrayList<String> path = new ArrayList<String>();

                    String parent = Osa.fedoraManager.cleanEntry(resultlist.get(i).get("parent"));
                    String child = Osa.fedoraManager.cleanEntry(resultlist.get(i).get("child"));

                    if ((child.contains(organization + ":C")) || (child.contains(organization + ":X"))) {

                        if (getObjName(child, organization).toLowerCase().contains(term.toLowerCase())) {

                            path.add(child);
                            while (!parent.equals(organization + ":root")) {
                                path.add(parent);
                                parent = getParent(resultlist, parent, organization);

                            }
                            path.add(organization + ":root");

                            String name = "";

                            while (!path.isEmpty()) {
                                String objName = getObjName(path.get(path.size() - 1), organization);
                                name += "/" + objName;
                                path.remove(path.size() - 1);
                            }

                            HashMap<String, Object> EntryO = new HashMap<String, Object>();
                            EntryO.put("label", name);
                            EntryO.put("value", child);

                            paths.add(EntryO);
                        }
                    }
                }

                jsonData = new flexjson.JSONSerializer().deepSerialize(paths);

            }
        }
        return new StreamingResolution(MIME_JS, jsonData);
    }

    /**
     * Creates a path to given object, including links to objects that are in
     * the path
     * 
     * @param pid   Object whose path is to be created
     * @return Path to given object in string format
     */
    @HandlesEvent("getObjectBreadcrumb")
    public Resolution getObjectBreadcrumb() {
    	FedoraBean fObject = Osa.fedoraManager.getObject(pid);
    	  /*TODO: 
         * - check publicity level of the path-component
         * - if name can not be shown, replace with text: confidential/restricted
         * */
        User user = this.getContext().getUser();
        String path = "";
        plainView = (plainView != null) ? true : false;
        String publicityLevel = ((CaptureBean) fObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE)).getPublicityLevel();

        if (user.isAnonymous()) { //+
            if (publicityLevel.equals("public")) {
                path = Osa.searchManager.getObjectPath(pid, user, plainView); 
            } else {      
                path = "confidential/restricted";
            }
        } else {
        	path = Osa.searchManager.getObjectPath(pid, user, plainView); 
        }
	       
        return new StreamingResolution(MIME_TEXT, path);
    }

    /**
     * Tells system which type of file is to be deleted when uploading files
     * 
     * @param type
     *            Type of the file (original / attachment)
     * @param b
     *            Boolean value (true / false)
     */
    public void modifyReference(FileType type, boolean b) {
        if (type.equals(FileType.Original)) {
            this.bRemoveFile = b;
        } else if (type.equals(FileType.Attachment)) {
            this.bRemoveAttachment = b;
        }
    }

    @HandlesEvent("showDatastream")
    public Resolution showDatastream() throws MalformedURLException, IOException {
        String username = Osa.fedoraManager.username;
        String password = Osa.fedoraManager.password;
        String credentials = username + ":" + password;
        byte[] encoded = new Base64().encode(credentials.getBytes("UTF-8"));
        String encoding = new String(encoded);
        
        URL url = new URL(Osa.fedoraManager.baseUrl +link);
        URLConnection conn = url.openConnection();
        conn.setDoInput(true);
        conn.setRequestProperty("Authorization", String.format("Basic %s", encoding));
        InputStream is = conn.getInputStream();
        DataInputStream in = new DataInputStream(is);
        return new StreamingResolution("text/xml", in).setFilename("history.xml");
    }

    /**
     * Deletes attachment file
     * 
     * @return Returns generic success string
     */
    @HandlesEvent("removeAttachment")
    public Resolution removeAttachment() {
        Osa.fedoraManager.deleteDataStream(pid, deleteAttachment);
        Osa.fedoraManager.deleteDataStream(pid, "THUMB_"+deleteAttachment);
        return new StreamingResolution(MIME_TEXT, "removed");
    }

    /**
     * Displays user-selected file for downloading/viewing.
     * Gets type (original/attachment) by dsName variable that is send from jsp
     * file
     * 
     * @return Selected file (original or attachment)
     * @throws MalformedURLException
     * @throws IOException
     */
    @HandlesEvent("showFile")
    public Resolution showFile() throws MalformedURLException, IOException {

        String format = "";
        String filename = "";
        DataStream dataStream = null;
        // TODO add better user authorization
        if (this.getContext().getUser() != null) {
            FedoraBean fObject = Osa.fedoraManager.getObject(pid);

            // Check access rights
            String path = ((ManagementBean) fObject.getDataStream(FedoraBean.DATASTREAM_MANAGEMENT)).getPath();
            String publicityLevel = ((CaptureBean) fObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE)).getPublicityLevel();

            if (!this.hasRights(path, publicityLevel, AccessRight.ACCESSRIGHTLEVEL_READ_DOC)) { return new ForwardResolution(ERROR_404); }

            // get datastream properties
            if (dsName.contains(FedoraBean.DATASTREAM_ATTACHMENT)) {
                dataStream = fObject.getDataStream(FedoraBean.DATASTREAM_ATTACHMENT);
                AttachmentBean attachments = (AttachmentBean) dataStream;
                filename = attachments.getElementProperties().get(dsName).getLabel();
                format = attachments.getElementProperties().get(dsName).getMimeFormat();

            } else if (dsName.contains(FedoraBean.DATASTREAM_ORIGINAL)) {
                dataStream = fObject.getDataStream(FedoraBean.DATASTREAM_ORIGINAL);
                filename = dataStream.getLabel();
                format = dataStream.getMimeFormat();
            }

            // metadata element holds objectUrl
            MetaDataElement element = dataStream.getMetaDataElements().get(dsName);

            String username = Osa.fedoraManager.username;
            String password = Osa.fedoraManager.password;
            String credentials = username + ":" + password;
            byte[] encoded = new Base64().encode(credentials.getBytes("UTF-8"));
            String encoding = new String(encoded);

            URL url = new URL(element.getValue());
            URLConnection conn = url.openConnection();
            conn.setDoInput(true);
            conn.setRequestProperty("Authorization", String.format("Basic %s", encoding));
            InputStream is = conn.getInputStream();
            DataInputStream in = new DataInputStream(is);

            return new StreamingResolution(format, in).setFilename(filename);
        } else {
            return new ForwardResolution(ERROR_404);
        }
    }
    
    @HandlesEvent("showLogFile")
    public Resolution showLogFile() {
    	if (!isDocumentAdder()) {
    		return new ForwardResolution(ERROR_500);
    	}
    	FileInputStream fis = null;
    	DataInputStream dis = null;
    	File logFile = new File(this.getLogDirectory(logName,true)+ "/" +filename);
    	
    	if (logFile.canRead()) {
            try {
                fis = new FileInputStream(logFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return new StreamingResolution(MIME_TEXT, new LocalizableMessage("error.filenotfound").getMessage(getUserLocale()));
            }
            dis = new DataInputStream(fis);
        } else {
        	return new StreamingResolution(MIME_TEXT, new LocalizableMessage("error.filenotfound").getMessage(getUserLocale()));
        }
    	    	
    	// Force view in browser
    	StreamingResolution streamRes = new StreamingResolution(MIME_TEXT, dis).setAttachment(false);
    	streamRes.setCharacterEncoding("UTF-8");
        return streamRes;
    }

    @HandlesEvent("removeLogFile")
    public Resolution removeLogFile() {
        String logDir = this.getLogDirectory(null, true);
        
        File file = null;
        HashMap<String, String> responseMap = new HashMap<String, String>();

        for (String filename : watchedFiles) {
            file = new File(logDir + "/" + filename);

            if (file.canRead()) {
                if (file.delete()) {
                	responseMap.put(file.getName(), "true");
                } else {
                	responseMap.put(file.getName(), "false");
                }
            } else {
            	responseMap.put(file.getName(), "false");
            }
        }
        return new StreamingResolution(MIME_JS, new flexjson.JSONSerializer().serialize(responseMap));
    }
    
    /**
     * Shows a list of object's versions
     * 
     * @return Redirect to history.jsp with version history data
     */
    @HandlesEvent("showVersionHistory")
    public Resolution showVersionHistory() {

        if (pid != null) {
            
            List<MetaDataElement> changeInfo = Osa.fedoraManager.getObjectHistory(pid);
            Iterator<MetaDataElement> iterChangeInfo = changeInfo.iterator();
            
            while (iterChangeInfo.hasNext()) {
                MetaDataElement infoElement = iterChangeInfo.next();

                if (infoElement.getName().equals("date")) {
                    // remove "Z" from date value
                    String value = infoElement.getValue().replaceFirst("Z", "");
                    value = value.replaceFirst("T", " ");
                    infoElement.setValue(value);
                    this.getHistoryElements().add(infoElement);

                } else {
                    this.getHistoryElements().add(infoElement);
                }
            }
        }
        
        return new RedirectResolution("/history.jsp").flash(this);
    }
    
    @HandlesEvent("getInheritedElements")
    public Resolution getInheritedElements() {
        
        String jsonData = "";
        cmodel = RepositoryManager.CONTENTMODEL + cmodel;
        User user = this.getContext().getUser();
        int order = Osa.fedoraManager.getObjLevel(pid);

        PreservationPlan plan = (PreservationPlan) this.getContext().getRequest().getSession().getAttribute("preservationPlan");
        Gui gui = (Gui) this.getContext().getRequest().getSession().getAttribute("gui");
        LinkedHashMap<String, FormElement> formElements = gui.getGUIElements(index);
        
        if (plan == null) {
            return new StreamingResolution(MIME_JS, jsonData);
        }
        // get inherited elements from preservation plan
        ArrayList<InheritedMetadataElement> currentInheritance = plan.getInheritedElements(order, cmodel);

        FedoraBean fObject = Osa.fedoraManager.findObject(pid);

        for (int i = 0; i < currentInheritance.size(); i++) {
            InheritedMetadataElement iElement = currentInheritance.get(i);
            // if the value is inherited by default to child objects
            if (iElement.isDefaultInheritance()) {
                String currentName = iElement.getName();
                // get inherited values
                MetaDataElement element = fObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements().get(currentName);
               
                if (element != null && element.getName().equals(currentName)) {
                    // get field type from form configuration
                    FieldType fieldType = formElements.get(element.getName()).getFieldType();
                    iElement.setFieldType(fieldType.toString());
                    
                    if (element.getValues().size() == 1) {
                        iElement.setValue(element.getValue());
                        
                        if (fieldType.equals(FieldType.relation)) {
                            String visibleValue = Osa.searchManager.getItemByPid(element.getValue(), user, getUserLocale());
                            iElement.setVisibleValue(visibleValue);
                        }
                        
                    } else {
                        Vector<String> values = element.getValues();
                        String allValues = "";
                        String allVisibleValues = "";

                        for (int j=0; j<values.size(); j++) {
                            if (allValues != "") {
                                allValues = allValues +";";
                            }
                            allValues = allValues + values.get(j);
                            if (fieldType.equals(FieldType.relation)) {
                                if (allVisibleValues != "") {
                                    allVisibleValues = allVisibleValues +";";
                                }
                                String visibleValue = Osa.searchManager.getItemByPid(values.get(j), user, getUserLocale());
                                allVisibleValues = allVisibleValues + visibleValue;
                            }
                        }
                        
                        iElement.setValue(allValues);
                        iElement.setVisibleValue(allVisibleValues);
                    }
                }
            }
        }

        // TODO: find out if inheriting empty value is needed feature,
        // for now disable inheritance of 'empty'
        Iterator<InheritedMetadataElement> it = currentInheritance.iterator();
        while (it.hasNext()) {
            InheritedMetadataElement iElement = it.next();
            if (iElement.getValue().equals("")) {
                it.remove();
            }
        }
        
        jsonData = new flexjson.JSONSerializer().exclude("*.class").serialize(currentInheritance);
        return new StreamingResolution(MIME_JS, jsonData);
    }

    /**
     * TODO: help to add breadcrumb
     * @return Redirect to corresponding ingest form with all metadata elements
     * @throws FedoraClientException
     * @throws Exception
     */
    @HandlesEvent("view")
    public Resolution view() throws FedoraClientException, Exception {
        String index = null;
        FedoraBean fObject = null;
        int acRight = AccessRight.ACCESSRIGHTLEVEL_DENY_META;

        if (pid != null) {
            fObject = Osa.fedoraManager.findObject(pid);
            
            if (fObject != null) {
                String path = ((ManagementBean) fObject.getDataStream(FedoraBean.DATASTREAM_MANAGEMENT)).getPath();
                String publicityLevel = ((CaptureBean) fObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE)).getPublicityLevel();
                acRight = this.getAccessRightForObject(path, publicityLevel);
                if (acRight < AccessRight.ACCESSRIGHTLEVEL_READ_META) { return new ForwardResolution(ERROR_404); }

                // Get url parameter
                if (fObject.type.equals(RepositoryManager.COLLECTION)) {
                    // Check if base/group/unit collection
                    String collectionType = fObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements().get("type").getValue();
                    collectionType = collectionType.replace(" ", "");
                    index = collectionType;
                } else {
                    index = fObject.type;
                }

                // Find right name for element according to name in form
                LinkedHashMap<String, MetaDataElement> elements = fObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements();
                Iterator<Entry<String, MetaDataElement>> iter = elements.entrySet().iterator();               
                this.getContext().getGui().getGUIElements(index);
                // Container for elements, which name in the form differs from
                // fedora (f.ex. objectdescription/description)
                LinkedHashMap<String, MetaDataElement> newElements = new LinkedHashMap<String, MetaDataElement>();
                
                while (iter.hasNext()) {                	
                    Map.Entry<String, MetaDataElement> entry = (Map.Entry<String, MetaDataElement>) iter.next();                    
                    MetaDataElement element = entry.getValue();                    
                    String elementName = element.getName();
                    String objectFormName = this.getContext().getGui().getMetadataName(elementName, FedoraBean.DATASTREAM_CAPTURE);

                    // compare object name and visible name
                    if (objectFormName != null && !objectFormName.equals(elementName)) {
                        newElements.put(objectFormName, new MetaDataElement(element));
                        iter.remove();
                    }
                }
                elements.putAll(newElements);              
                if (acRight < AccessRight.ACCESSRIGHTLEVEL_READ_DOC) {
                    // remove original datastream
                    fObject.removeOriginalDatastream();
                }
                
                this.fedoraBeans.add(fObject);
                
            } else {
                // Object not found on Fedora
                return new ForwardResolution(ERROR_404);
            }
        }
        
        if (this.getContext().getUser() == null) { return new ForwardResolution(INDEX); }

        if (index == null || pid.contains(":root")) { return new ForwardResolution(BROWSE); }

        // If coming from link that leads to view site
        if (plainView != null && plainView == true) { return new ForwardResolution(VIEW).addParameter("index", index); }

        // Redirect user based on their role
        if (acRight == AccessRight.ACCESSRIGHTLEVEL_DENY_META) {
            return new ForwardResolution(INDEX);
        } else if (acRight <= AccessRight.ACCESSRIGHTLEVEL_READ_DOC) {
            return new ForwardResolution(VIEW).addParameter("index", index);
        } else {
            return new ForwardResolution(INGEST).addParameter("index", index);
        }

    }

    /**
     * Checks if object has original file and its thumbnail
     * 
     * @return String of results
     */
    @HandlesEvent("checkIfHasOriginal")
    public Resolution checkIfHasOriginal() {
        String sHasOriginal = "";

        try {
            FedoraBean fObject = Osa.fedoraManager.getObject(pid);

            String path = ((ManagementBean) fObject.getDataStream(FedoraBean.DATASTREAM_MANAGEMENT)).getPath();
            String publicityLevel = ((CaptureBean) fObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE)).getPublicityLevel();
            int acRight = this.getAccessRightForObject(path, publicityLevel);
            if (acRight < AccessRight.ACCESSRIGHTLEVEL_READ_META) { return new StreamingResolution(MIME_TEXT, "Unauthored user - access denied."); }

            DataStream originalDatastream = fObject.getDataStream(FedoraBean.DATASTREAM_ORIGINAL);
            DataStream thumbDatastream = fObject.getDataStream(FedoraBean.DATASTREAM_THUMB);

            // Check if object has original file and thumbnail for it
            if (originalDatastream.getMetaDataElements().containsKey(FedoraBean.DATASTREAM_ORIGINAL)
                    && thumbDatastream.getMetaDataElements().containsKey(FedoraBean.DATASTREAM_THUMB + "_" + FedoraBean.DATASTREAM_ORIGINAL))
            {
                // [has original]|[has thumbnail], maybe change it to a map or a
                // vector...
                if (acRight < AccessRight.ACCESSRIGHTLEVEL_READ_DOC) {
                    sHasOriginal = "false|true";
                } else {
                    sHasOriginal = "true|true";
                }
            }
            else {
                sHasOriginal = "true|false"; // If has original file but no
                                             // preview file
            }

            // If object does not have original file
            if (!originalDatastream.getMetaDataElements().containsKey(FedoraBean.DATASTREAM_ORIGINAL)) {
                sHasOriginal = "false|false";
            }

        } catch (Exception e) {
            sHasOriginal = "false|false";
            return new StreamingResolution(MIME_TEXT, sHasOriginal);
        }
        return new StreamingResolution(MIME_TEXT, sHasOriginal);
    }

    /**
     * Creates a map where key is attachment datastream name and value is
     * boolean state for its thumbnail image.
     * 
     * @return Json formatted string containing the map.
     */
    @HandlesEvent("checkIfHasAttachments")
    public Resolution checkIfHasAttachments() {
        LinkedHashMap<String, Boolean> attMap = new LinkedHashMap<String, Boolean>();

        FedoraBean fObject = Osa.fedoraManager.getObject(pid);

        // Check access rights
        String path = ((ManagementBean) fObject.getDataStream(FedoraBean.DATASTREAM_MANAGEMENT)).getPath();
        String publicityLevel = ((CaptureBean) fObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE)).getPublicityLevel();

        if (!this.hasRights(path, publicityLevel, AccessRight.ACCESSRIGHTLEVEL_READ_DOC)) { 
            return new StreamingResolution(MIME_JS, new flexjson.JSONSerializer().deepSerialize(attMap)); 
        }

        DataStream attachmentsDataStream = fObject.getDataStream(FedoraBean.DATASTREAM_ATTACHMENT);
        DataStream thumbsDataStream = fObject.getDataStream(FedoraBean.DATASTREAM_THUMB);
        Iterator<Entry<String, MetaDataElement>> iterAttachments = attachmentsDataStream.getMetaDataElements().entrySet().iterator();

        while (iterAttachments.hasNext()) {
            Map.Entry<String, MetaDataElement> entry = (Map.Entry<String, MetaDataElement>) iterAttachments.next();
            MetaDataElement element = entry.getValue();

            if (element.getName().startsWith(FedoraBean.DATASTREAM_ATTACHMENT)) {

                if (thumbsDataStream.getMetaDataElements().containsKey(FedoraBean.DATASTREAM_THUMB + "_" + element.getName())) {
                    attMap.put(element.getName(), true);
                } else {
                    attMap.put(element.getName(), false);
                }
            }
        }
        String attMapJson = new flexjson.JSONSerializer().deepSerialize(attMap);
        return new StreamingResolution(MIME_JS, attMapJson);
    }

    /**
     * Displays a thumbnail image for given datastream.
     * If datastream does not have thumbnail image, display generic no-preview
     * image.
     * hasThumbnail value is sent from jsp
     * 
     * @return StreamingResolution containing the image
     */
    @HandlesEvent("getFileThumb")
    public Resolution getFileThumb() {
        String dataStreamName = FedoraBean.DATASTREAM_THUMB + "_" + dsName;
        DataInputStream dis = null;
        String mimeFormat = "";
        FedoraBean fObject = Osa.fedoraManager.getObject(pid);

        // Check access rights
        String path = ((ManagementBean) fObject.getDataStream(FedoraBean.DATASTREAM_MANAGEMENT)).getPath();
        String publicityLevel = ((CaptureBean) fObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE)).getPublicityLevel();

        if (!this.hasRights(path, publicityLevel, AccessRight.ACCESSRIGHTLEVEL_READ_META)) { return new StreamingResolution(MIME_TEXT, "Unauthored user - access denied."); }

        try {
            if (hasThumbnail) {
                ThumbBean thumbDatastream = (ThumbBean) fObject.getDataStream(FedoraBean.DATASTREAM_THUMB);
                mimeFormat = thumbDatastream.getElementProperties().get(dataStreamName).getMimeFormat();
                InputStream is = thumbDatastream.getMetaDataElements().get(dataStreamName).getInputStream();
                dis = new DataInputStream(is);
            } else {
                // If has file but no preview for it, print generic no preview
                // image
                File nopreview = new File(this.getContext().getServletContext().getRealPath("/img/nopreview.png"));
                mimeFormat = MIME_PNG;
                FileInputStream fis = new FileInputStream(nopreview);
                dis = new DataInputStream(fis);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        // Set attachment to false so browser does not force download
        return new StreamingResolution(mimeFormat, dis).setAttachment(false);
    }

    /**
     * Creates a list of children objects of given object.
     * Used in ingest page for collections / objects that can have children
     * objects.
     * 
     * @param pid
     *            Pid of object whose children will be fetched
     * @return List of children objects and their informations
     */
    public ArrayList<LinkedHashMap<String, String>> listChildren(String pid) {
        List<TreeNode> children = Osa.searchManager.getChildren(pid, null, this.getContext().getUser(), null, null);
        ArrayList<LinkedHashMap<String, String>> childMap = new ArrayList<LinkedHashMap<String, String>>();

        for (int i = 0; i < children.size(); i++) {
            LinkedHashMap<String, String> entry = new LinkedHashMap<String, String>();
            String childpid = children.get(i).getId();
            String childname = children.get(i).getName();
            String childtype = children.get(i).getType();
            entry.put("pid", childpid);
            entry.put("name", childname);
            entry.put("type", RepositoryManager.DATATYPES_MAP.get(childtype));
            entry.put("imagename", SearchAction.DATAICONS_MAP.get(entry.get("type")));
            childMap.add(entry);
        }

        return childMap;
    }

    /**
     * Replaces pid with name in ingest form
     * 
     * @param name
     *            name of relation, f.ex isPartOf
     * @return
     */
    public String getRelationName(String name) {
        String relationPid = "";
        String relationName = "";
        User user = this.getContext().getUser();
        
        if (!name.isEmpty() || name != null || name != "") {
            if (getAllCaptureElements() != null) {
                // If given parameter is pid (most of the cases this matches for
                // pids) and not metadata element's name
                if (name.startsWith(getUserOrganizationName().toUpperCase() + ":")) {
                    relationPid = name;
                } else {
                    MetaDataElement element = getAllCaptureElements().get(name);
                    if (element != null && name.equalsIgnoreCase(element.getName())) {
                        if (element.getValue() != null && !element.getValue().equals("")) {
                            relationPid = element.getValue();
                        }
                    }
                }
                if (relationPid != "") {
                    if (relationPid.endsWith(":root")) {
                        // Removes the ":root" string from pid, displays
                        // organization's name only
                        int endIndex = relationPid.indexOf(":root");
                        relationName = relationPid.substring(0, endIndex);

                    } else {
                        relationName = Osa.searchManager.getItemByPid(relationPid, user, getUserLocale());
                    }
                }
            }
        }
        return relationName;
    }

    /**
     * Replaces pid with name in ingest form
     * 
     * @param parent
     *            parent element for nested element
     * @param name
     *            relation name
     * @param index
     *            nested element index in vector
     * @return
     */
    public String getNestedRelationName(String parent, String name, int index) {
        String relationPid = "";
        String relationName = "";
        User user = this.getContext().getUser();
        
        if (!name.isEmpty() || name != null || name != "") {
            if (getAllCaptureElements() != null) {
                MetaDataElement element = getAllCaptureElements().get(parent);
                if (element != null && !element.getNestedElements().isEmpty()) {

                    Vector<LinkedHashMap<String, MetaDataElement>> nestedElements = element.getNestedElements();
                    LinkedHashMap<String, MetaDataElement> lhm = nestedElements.get(index);
                    Iterator<Entry<String, MetaDataElement>> iterLhm = lhm.entrySet().iterator();
                    while (iterLhm.hasNext()) {
                        Map.Entry<String, MetaDataElement> entryLhm = (Map.Entry<String, MetaDataElement>) iterLhm.next();
                        MetaDataElement nestedElement = entryLhm.getValue();

                        if (name.equalsIgnoreCase(nestedElement.getName())) {
                            relationPid = nestedElement.getValue();
                            break;
                        }
                    }
                }

                if (relationPid != null && relationPid != "") {
                    relationName = Osa.searchManager.getItemByPid(relationPid, user, getUserLocale());
                }
            }
        }
        return relationName;
    }

    /**
     * Constructs comma-delimited string with values for multivalue fields where
     * multiple values are separated with a comma.
     * For example subject: "subject, subject2, subject3..."
     * 
     * @param mdElement       Name of the metadata element
     * @return String of values, separated with a comma
     */
    public String getDelimitedMultivalue(String mdElement) {
        String valuesToRow = "";
        if (fedoraBeans.get(0).getCaptureData().getMetaDataElements().get(mdElement) != null) {
            Vector<String> multiValues = fedoraBeans.get(0).getCaptureData().getMetaDataElements().get(mdElement).getValues();
            // set metadata values, which type is
            // FormElement.MULTIVALUE_DELIMITED, to same row in .jsp
            for (int i = 0; i < multiValues.size(); i++) {
                if (multiValues.get(i) != null) {
                    if (valuesToRow != "") {
                        valuesToRow += ", ";
                    }
                    valuesToRow += multiValues.get(i);
                }
            }
        }
        return valuesToRow;
    }

    /**
     * Gets given metadata element's nested child objects
     * 
     * @param mdElement
     *            Parent metadataelement whose nested child objects will be
     *            listed
     * @return Vector containing LinkedHashMaps containing the children and
     *         their properties
     */
    public Vector<LinkedHashMap<String, MetaDataElement>> getNestedChildren(String mdElement) {
        Vector<LinkedHashMap<String, MetaDataElement>> nestedChildren = null;
        if (getAllCaptureElements().get(mdElement) != null) {
            nestedChildren = getAllCaptureElements().get(mdElement).getNestedElements();
        }
        return nestedChildren;
    }

    /**
     * Gets given metadata element's nested elements for dynamic created fields
     * 
     * @return Elements in json format
     */
    public Resolution getNestedChildrenJson() {
        String json = "";
        Vector<FormElement> nestedElements = this.getContext().getGui().getGUIElements(index).get(mdElement).getNestedFormElements();
        if (nestedElements != null) {
            json = "[";
            for (FormElement nestedElement : nestedElements) {
                // If field's type is not select
                if (!FormElement.FieldType.select.equals(nestedElement.getFieldType())) {
                    json += "{\"name\":\"" + nestedElement.getName() + "\",";
                    json += "\"placeholder\":\"" + new LocalizableMessage("capture." + nestedElement.getName()).getMessage(getUserLocale()) + "\",";
                    json += "\"fieldType\":\"" + nestedElement.getFieldType() + "\"},";
                } else {
                    // If fieldtype is select, get its enum values
                    json += "{\"name\":\"" + nestedElement.getName() + "\",";
                    json += "\"fieldType\":\"" + nestedElement.getFieldType() + "\",";
                    // Non-translated values for select
                    json += "\"enumValues\":[";
                    for (String enumValue : nestedElement.getEnumValues()) {
                        json += "\"" + enumValue + "\",";
                    }
                    json = json.substring(0, json.length() - 1);
                    json += "],";
                    // Translated values for user
                    json += "\"enumValuesLocal\":[";
                    for (String enumValue : nestedElement.getEnumValues()) {
                        json += "\"" + new LocalizableMessage("capture." + nestedElement.getName() + "." + enumValue).getMessage(getUserLocale()) + "\",";
                    }
                    json = json.substring(0, json.length() - 1);
                    json += "]},";
                }

            }
            json = json.substring(0, json.length() - 1);
            json += "]";
        }
        return new StreamingResolution(MIME_JS, json);
    }

    public Resolution getMultifieldElement() {
        HashMap<String, Object> fieldJson = new HashMap<String, Object>();
        FormElement multifield = this.getContext().getGui().getGUIElements(index).get(mdElement);

        fieldJson.put("name", multifield.getName());
        fieldJson.put("fieldType", multifield.getFieldType());
        fieldJson.put("placeholder", new LocalizableMessage("capture." + multifield.getName()).getMessage(getUserLocale()));

        return new StreamingResolution(MIME_JS, new flexjson.JSONSerializer().deepSerialize(fieldJson));
    }

    // The PID of the datastream
    public void setPid(String pid) {
        this.pid = pid;
    }

    // The PID of the datastream
    public String getPid() {
        return this.pid;
    }

    public String getLink() {
        return this.link;
    }

    public void setLink(String value) {
        this.link = value;
    }

    public String getIndex() {
        return this.index;
    }

    public void setIndex(String value) {
        this.index = value;
    }

    public String getRoot() {
        return this.root.toUpperCase();
    }

    public void setRoot(String value) {
        this.root = value.toUpperCase();
    }

    // File upload
    public Vector<String> getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(Vector<String> uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    // Attachment upload
    public Vector<String> getUploadedAttachment() {
        return uploadedAttachment;
    }

    public void setUploadedAttachment(Vector<String> uploadedAttachment) {
        this.uploadedAttachment = uploadedAttachment;
    }

    public void setAllCaptureElements(LinkedHashMap<String, MetaDataElement> list) {
        this.allCaptureElements = list;
    }

    public LinkedHashMap<String, MetaDataElement> getAllCaptureElements() {
        return fedoraBeans.get(0).getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements();
    }

    public Vector<FedoraBean> getAllFedoraBeans() {
        return this.fedoraBeans;
    }

    public void setAllFedoraBeans(Vector<FedoraBean> allFedoraBeans) {
        this.fedoraBeans = allFedoraBeans;
    }

    // History elements in list
    public void setHistoryElements(List<MetaDataElement> list) {
        this.historyElements = list;
    }

    public List<MetaDataElement> getHistoryElements() {
        return historyElements;
    }

    public String getBrowsename() {
        return this.browsename;
    }

    public void setBrowsename(String value) {
        this.browsename = value;
    }

    public String getSelectedrelationname() {
        return selectedrelationname;
    }

    public void setSelectedrelationname(String selectedrelationname) {
        this.selectedrelationname = selectedrelationname;
    }

    public String getCurrentOrganization() {
        return currentOrganization;
    }

    public void setCurrentOrganization(String currentOrganization) {
        this.currentOrganization = currentOrganization;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getDeleteAttachment() {
        return deleteAttachment;
    }

    public void setDeleteAttachment(String deleteAttachment) {
        this.deleteAttachment = deleteAttachment;
    }

    public String getNewParent() {
        return newParent;
    }

    public void setNewParent(String newparent) {
        this.newParent = newparent;
    }

    public String getOldParent() {
        return oldParent;
    }

    public void setOldParent(String oldParent) {
        this.oldParent = oldParent;
    }

    public String getCmodel() {
        return cmodel;
    }

    public void setCmodel(String cmodel) {
        this.cmodel = cmodel;
    }
    
    public String getRootpid() {
        return rootpid;
    }

    public void setRootpid(String rootpid) {
        this.rootpid = rootpid;
    }

    // Datastream name for showing original or attachment file
    public String getdsName() {
        return dsName;
    }

    public void setdsName(String dsName) {
        this.dsName = dsName;
    }

    public String getWatchedFile() {
        return watchedFile;
    }

    public void setWatchedFile(String watchedFile) {
        this.watchedFile = watchedFile;
    }

    public Vector<String> getWatchedFiles() {
        return watchedFiles;
    }

    public void setWatchedFiles(Vector<String> watchedFiles) {
        this.watchedFiles = watchedFiles;
    }

    public int getNodeLevel() {
        return nodeLevel;
    }

    public void setNodeLevel(int nodeLevel) {
        this.nodeLevel = nodeLevel;
    }

    public HashMap<String, String> getAttachmentNames() {
        return attachmentNames;
    }

    public void setAttachmentNames(HashMap<String, String> attachmentNames) {
        this.attachmentNames = attachmentNames;
    }

    public String getToFolder() {
        return toFolder;
    }

    public void setToFolder(String toFolder) {
        this.toFolder = toFolder;
    }

    public boolean isFromQueue() {
        return fromQueue;
    }

    public void setFromQueue(boolean fromQueue) {
        this.fromQueue = fromQueue;
    }

    public String getWatchFields() {
        return watchFields;
    }

    public void setWatchFields(String watchFields) {
        this.watchFields = watchFields;
    }

    public String getMovedpids() {
        return movedpids;
    }

    public void setMovedpids(String movedpids) {
        this.movedpids = movedpids;
    }

    public String getFileToDelete() {
        return fileToDelete;
    }

    public void setFileToDelete(String fileToDelete) {
        this.fileToDelete = fileToDelete;
    }

    public Boolean getHasThumbnail() {
        return hasThumbnail;
    }

    public void setHasThumbnail(Boolean hasThumbnail) {
        this.hasThumbnail = hasThumbnail;
    }

    public String getMdElement() {
        return mdElement;
    }

    public void setMdElement(String mdElement) {
        this.mdElement = mdElement;
    }

    public Boolean getPlainView() {
        return plainView;
    }

    public void setPlainView(Boolean plainView) {
        this.plainView = plainView;
    }

    public int getsEcho() {
        return sEcho;
    }

    public void setsEcho(int sEcho) {
        this.sEcho = sEcho;
    }

    public int getiSortCol_0() {
        return iSortCol_0;
    }

    public void setiSortCol_0(int iSortCol_0) {
        this.iSortCol_0 = iSortCol_0;
    }

    public String getsSortDir_0() {
        return sSortDir_0;
    }

    public void setsSortDir_0(String sSortDir_0) {
        this.sSortDir_0 = sSortDir_0;
    }

    public int getiDisplayStart() {
        return iDisplayStart;
    }

    public void setiDisplayStart(int iDisplayStart) {
        this.iDisplayStart = iDisplayStart;
    }

    public int getiDisplayLength() {
        return iDisplayLength;
    }

    public void setiDisplayLength(int iDisplayLength) {
        this.iDisplayLength = iDisplayLength;
    }

    public String getsSearch() {
        return sSearch;
    }

    public void setsSearch(String sSearch) {
        this.sSearch = sSearch;
    }

    public String getWatchTable() {
        return watchTable;
    }

    public void setWatchTable(String watchTable) {
        this.watchTable = watchTable;
    }

    public LinkedHashMap<String, FormElement> getCustomForm() {
        return customForm;
    }

    public void setCustomForm(LinkedHashMap<String, FormElement> customForm) {
        this.customForm = customForm;
    }

    public LinkedHashSet<String> getCustomTabs() {
        return customTabs;
    }

    public void setCustomTabs(LinkedHashSet<String> customTabs) {
        this.customTabs = customTabs;
    }

    public List<String> getFormNames() {
        return formNames;
    }

    public void setFormNames(List<String> formNames) {
        this.formNames = formNames;
    }

    public Vector<String> getPidArray() {
        return pidArray;
    }

    public void setPidArray(Vector<String> pidArray) {
        this.pidArray = pidArray;
    }

    public Vector<FedoraBean> getFedorabeanvector() {
        return fedorabeanvector;
    }

    public void setFedorabeanvector(Vector<FedoraBean> fedorabeanvector) {
        this.fedorabeanvector = fedorabeanvector;
    }

    public Vector<String> getInconsistantFields() {
        return inconsistantFields;
    }

    @HandlesEvent("getInconsistantFields")
    public Resolution getInconsistantFieldsJson() {
        Vector<String> fields = this.getInconsistantFields();
        String json = new flexjson.JSONSerializer().serialize(fields);
        return new StreamingResolution(MIME_JS, json);
    }

    public void setInconsistantFields(Vector<String> inconsistantFields) {
        this.inconsistantFields = inconsistantFields;
    }

    // Access key to ONKI services
    public void setOnkiAccessKey(String key) {
        this.onkiAccessKey = key;
    }

    public String getOnkiAccessKey() {
        if (this.onkiAccessKey == null || this.onkiAccessKey.isEmpty()) {
            setOnkiAccessKey(Osa.onkiAccessKey);
            return Osa.onkiAccessKey;

        } else {
            return this.onkiAccessKey;
        }
    }

    public Boolean getAddNew() {
        return addNew;
    }

    public void setAddNew(Boolean addNew) {
        this.addNew = addNew;
    }

    public String getOntologyId() {
        return ontologyId;
    }

    public void setOntologyId(String ontologyId) {
        this.ontologyId = ontologyId;
    }

	public String getFilename() {
		return filename;
	}

	public void setFilename(String fileName) {
		this.filename = fileName;
	}

	public String getLogName() {
		return logName;
	}

	public void setLogName(String logName) {
		this.logName = logName;
	}
		
	public String getIsPartOf() {
        return isPartOf;
    }
	
    public void setIsPartOf(String parent) {
        this.isPartOf = parent;
    }

} // class
