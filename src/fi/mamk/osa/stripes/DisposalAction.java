package fi.mamk.osa.stripes;

import fi.mamk.osa.auth.User;
import fi.mamk.osa.bean.CaptureBean;
import fi.mamk.osa.bean.FedoraBean;
import fi.mamk.osa.bean.MetaDataElement;
import fi.mamk.osa.bean.DataStream.State;
import fi.mamk.osa.core.DestructionPlan;
import fi.mamk.osa.core.Osa;
import fi.mamk.osa.database.mongo.MongoManager;
import fi.mamk.osa.fedora.RepositoryManager;
import fi.mamk.osa.ui.DisposalListItem;
import fi.mamk.osa.ui.Gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.LocalizableMessage;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.LocalizableError;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

@UrlBinding("/Disposal.action")
public class DisposalAction extends OsaBaseActionBean {

    private static final Logger logger = Logger.getLogger(DisposalAction.class);
    private String pid;
    private Vector<String>    pidArray;
    private ArrayList<String> childrenPids;

    @HandlesEvent("showDisposalListDatatables")
    public Resolution showDisposalListDatatables() {
        
        StringBuilder json = new StringBuilder();
        Locale userLocale = this.getContext().getUser().getLocale();
        List<DisposalListItem> disposalItems = new ArrayList<DisposalListItem>();
        disposalItems = Osa.dbManager.get("mongo").getDisposalListItems(getUserOrganizationName());
        
        json.append("{" +
                "\"sEcho\": 1," +
                "\"iTotalRecords\": "+disposalItems.size()+"," +
                "\"iTotalDisplayRecords\": " +disposalItems.size()+",");
        
        json.append("\"aaData\": [");
        
        if (disposalItems.size() > 0) {
            for (DisposalListItem disposalItem : disposalItems) {
                json.append("[");
                // #1 col
                json.append("\"<input type='checkbox' value='"+disposalItem.getPid()+"'/>\","); 
                // #2 col
                json.append("\"<img src='img/nopreview.png' alt='img' width='80%'/>\","); 
                // #3 col
                json.append("\"<div class='datatables-td-container'>"+  
                            "<a href='Ingest.action?pid="+disposalItem.getPid()+"&view='><h3 class='datatables-header'>"+disposalItem.getObjectName()+"</h3></a>"+
                            "<div>"+
                                "<div class='displayInlineBlock'>"+
                                    "<span>"+new LocalizableMessage("disposal.type").getMessage(userLocale)+"</span><br/>"+
                                    "<span>"+new LocalizableMessage("disposal.requested.by").getMessage(userLocale)+"</span><br/>"+
                                    "<span>"+new LocalizableMessage("disposal.requested.date").getMessage(userLocale)+"</span><br/>"+
                                "</div>"+
                                "<div class='displayInlineBlock'>"+
                                    "<span>"+new LocalizableMessage("link.add."+disposalItem.getObjectType()).getMessage(userLocale)+"</span><br/>"+
                                    "<span>"+disposalItem.getDeleter()+"</span><br/>"+
                                    "<span>"+disposalItem.getDisposalDateString()+"</span><br/>"+
                                "</div>"+
                                "<img src='img/icons/silk/delete.png' "+
                                    "id='"+disposalItem.getPid()+"|"+disposalItem.getObjectName()+"' "+
                                    "class='pointerCursor cancel disposalaction' alt='[-]' "+
                                    "data-action='deleteFromDisposalList' "+
                                    "title='"+new LocalizableMessage("message.deletefromdisposallist").getMessage(userLocale)+"'/>"+
                                "<img src='img/icons/silk/cancel.png' "+
                                    "id='"+disposalItem.getPid()+"|"+disposalItem.getObjectName()+"' "+
                                    "class='pointerCursor delete disposalaction' alt='[x]' "+
                                    "data-action='deleteFromFedora' "+
                                    "title='"+new LocalizableMessage("button.delete").getMessage(userLocale)+"'/>"+
                            "</div>"+
                          "</div>\",");

                json.deleteCharAt(json.length()-1);
                json.append("],");
            }
            json.deleteCharAt(json.length()-1);
        }
        json.append("]}");
        return new StreamingResolution(MIME_JS, json.toString());
    }
    
    @HandlesEvent("deleteFromDisposalList")
    public Resolution deleteFromDisposalList() {
        
        String retValue = "false";
        String logContent = "";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String disposalTime = dateFormat.format(date);
        User user = this.getContext().getUser();
        String logDir = this.getLogDirectoryForOrganization();
        
    	for (int i = 0; i < pidArray.size(); i++) {
    	    // check if active parent is found
    	    FedoraBean fObject = Osa.fedoraManager.findObject(pidArray.get(i));
    	    String parentPid = fObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements().get(CaptureBean.RELATION_IsPartOf).getValue();
    	    String parentType = Osa.searchManager.getObjectType(parentPid, user);
    	    if (parentType.equals("") || parentType.equals("null")) {
    	        logger.info("Parent Pid "+parentPid+" not found active, can not remove object from disposal list.");
    	        
    	    } else {
        		Osa.dbManager.get("mongo").deleteFromDisposalList(pidArray.get(i), getUserOrganizationName());
        		Osa.fedoraManager.setObjectState(pidArray.get(i), State.A);
        		
        		logContent += "[" + disposalTime + "] Removing object form disposal list \""
                        + this.getObjName(pidArray.get(i), getUserOrganizationName())
                        + "\" [" + pidArray.get(i) + "]\n";
        		
        		retValue = "true";
    	    }
    	}
    	
    	if (logContent != "") {
    	    // create log
    	    logDisposal(DestructionPlan.DESTRUCTIONTYPE_Dispose, logContent, logDir);
    	}
    	
        return new StreamingResolution(MIME_TEXT, retValue);
    }
    
    @HandlesEvent("deleteFromFedora")
    public Resolution deleteFromFedora() {
        
        DestructionPlan destructionPlan = (DestructionPlan) this.getContext().getRequest().getSession().getAttribute("destructionPlan");
        boolean createResidualObject = destructionPlan.isCreateResidualObjects();
        List<String> residualMetadata = destructionPlan.getResidualMetadata();
        User user = this.getContext().getUser();
        Gui userGui = this.getContext().getGui();
        String organization = user.getOrganization().getName().toUpperCase();
        String logDir = this.getLogDirectoryForOrganization();
        String logContent = "";
        
        // get items from mongo
        List<DisposalListItem> disposalItems = Osa.dbManager.get("mongo").getDisposalListItems(getUserOrganizationName());
        
    	for (int i = 0; i < pidArray.size(); i++) {
    	    String currentPid = pidArray.get(i);
    	    FedoraBean fedoraBean = Osa.fedoraManager.findObject(currentPid);
    	    
    	    if (fedoraBean == null) continue;
    	    
    	    LinkedHashMap<String, MetaDataElement> allCaptureElements = fedoraBean.getDataStream(FedoraBean.DATASTREAM_CAPTURE).getMetaDataElements();
    	    
    	    String objName = "";
    	    if (allCaptureElements.containsKey(CaptureBean.TITLE)) {
    	        objName = allCaptureElements.get(CaptureBean.TITLE).getValue();
            } else if (allCaptureElements.containsKey(CaptureBean.PREFERREDNAME)) {
                objName = allCaptureElements.get(CaptureBean.PREFERREDNAME).getValue();
            }
    	    
    	    // get details from mongo
    	    for (DisposalListItem disposalItem : disposalItems) {
    	        if (disposalItem.getPid().equals(currentPid)) {
    	            
    	            // create residual metadata according to conf: residualObjectDefinition/metadata
    	            for (String metadata : residualMetadata) {
    	                MetaDataElement mdElement = new MetaDataElement();
    	                mdElement.setName(metadata);
    	                mdElement.setVisibleName(metadata);
    	                
    	                if (metadata.equals("destructionDate")) {
    	                    mdElement.setValue(disposalItem.getDisposalDateString());
    	                } else if (metadata.equals("destructionAuthorization")) {
    	                    mdElement.setValue(disposalItem.getDeleter());
    	                } else if (metadata.equals("destructionReason")) {
    	                    mdElement.setValue("--");
    	                } else if (metadata.equals("destructionMethod")) {
                            mdElement.setValue("--");
                        }
    	                allCaptureElements.put(metadata, mdElement);
    	            }
    	            break;
    	        }
    	    }
    	    
    	    Osa.dbManager.get("mongo").deleteFromDisposalList(currentPid, getUserOrganizationName());
    	    
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date();
            String disposalTime = dateFormat.format(date);
    	    
    	    if (createResidualObject) {
    	        // create the final capture datastream
    	        try {
    	            Osa.fedoraManager.ingest(userGui, user, organization, null, null, fedoraBean.getType(), currentPid, allCaptureElements, fedoraBean);
    	            logger.info("DisposalAction: CAPTURE datastream updated");
    	        } catch (SAXParseException e) {
                    this.getContext().getValidationErrors().add("error", new LocalizableError("error.validate"));
                    logger.error("Schema validation found errors: " + e);
                    return this.getContext().getSourcePageResolution();

                } catch (SAXException e) {
                    logger.error(e);
                    this.getContext().getValidationErrors().add("error", new LocalizableError("error.ingest"));
                    return this.getContext().getSourcePageResolution();
                }
    	        
    	        Osa.fedoraManager.createResidualObject(currentPid, destructionPlan);
    	        
    	        logContent += "[" + disposalTime + "] Setting object \""
                        + objName
                        + "\" [" + currentPid + "] deleted\n";
    	        
    	    } else {
    	        Osa.fedoraManager.deleteObject(currentPid);
    	        
    	        logContent += "[" + disposalTime + "] Deleting object \""
                        + objName
                        + "\" [" + currentPid + "]\n";
    	    }
    	}
    	
    	// create log
    	logDisposal(destructionPlan.getDisposeMethod(), logContent, logDir);
    	
        return new StreamingResolution(MIME_TEXT, "true");
    }
    
    @HandlesEvent("getDisposalListSize")
    public int getDisposalListSize(){
    	int listSize = Osa.dbManager.get("mongo").getDisposalListSize(getUserOrganizationName());
    	return listSize;
    }

    /**
     * Creates a list of to-be-deleted objects via ajax call
     * 
     * @return Objects that will be disposed/deleted, including possible child
     *         objects, in json format
     */
    @HandlesEvent("ajaxGetItems")
    public Resolution ajaxGetItems() {
        
        DestructionPlan destructionPlan = (DestructionPlan) this.getContext().getRequest().getSession().getAttribute("destructionPlan");
        boolean allowOrphans = destructionPlan.isAllowOrphans();
        User user = this.getContext().getUser();
        String jsonData = "";
        String name = "";
        String type = "";
        String method = destructionPlan.getDisposeMethod();
        ArrayList<HashMap<String, String>> childObjects = Osa.fedoraManager.getAllChildren(pid);

        // First check if object has any children
        // Id it does not have, construct json containing only the parent object
        // information
        if (childObjects.isEmpty()) {
            name = Osa.searchManager.getItemByPid(pid, user, getUserLocale()).replace("[", "").replace("]", "");
            type = pid.split("-")[0].split(":")[1];
            jsonData = "[{";
            jsonData += "\"method\":\"" + method + "\",";
            jsonData += "\"id\":\"" + pid + "\",";
            jsonData += "\"name\":\"" + name + "\",";
            jsonData += "\"deletionMsg\":\"noChildren\",";
            jsonData += "\"type\":\"" + type + "\"";
            jsonData += "}]";
        } else {
            // if has, check if recursiveDestruction is true, a.k.a delete all child objects
            if (destructionPlan.isRecursiveDestruction()) {
                name = Osa.searchManager.getItemByPid(pid, user, getUserLocale()).replace("[", "").replace("]", "");
                type = pid.split("-")[0].split(":")[1];
                // Parent element
                jsonData = "[";
                jsonData += "{\"method\":\"" + method + "\",";
                jsonData += "\"id\":\"" + pid + "\",";
                jsonData += "\"name\":\"" + name + "\",";
                jsonData += "\"deletionMsg\":\"deleteChildren\",";               
                
                jsonData += "\"type\":\"" + type + "\"},";
                for (HashMap<String, String> childObject : childObjects) {
                    // Child elements
                    String child = Osa.fedoraManager.cleanEntry(childObject.get("child"));
                    String childname = Osa.searchManager.getItemByPid(child, user, getUserLocale()).replace("[", "").replace("]", "");
                    String childtype = child.split("-")[0].split(":")[1];
                    jsonData += "{\"id\":\"" + child + "\",";
                    jsonData += "\"name\":\"" + childname + "\",";
                    jsonData += "\"type\":\"" + childtype + "\"},";
                }
               
                jsonData = jsonData.substring(0, jsonData.length() - 1);
                jsonData += "]";
            } else {
                
                // if false, only delete parent object
                name = Osa.searchManager.getItemByPid(pid, user, getUserLocale()).replace("[", "").replace("]", "");
                type = pid.split("-")[0].split(":")[1];
                jsonData = "[{";
                jsonData += "\"method\":\"" + method + "\",";
                jsonData += "\"id\":\"" + pid + "\",";
                jsonData += "\"name\":\"" + name + "\",";
                
                if (allowOrphans){
                    jsonData += "\"deletionMsg\":\"deleteParent_leaveOrphanes\",";
                }
                else {
                    jsonData += "\"deletionMsg\":\"deleteParent_notAllowed\",";
                }
                jsonData += "\"type\":\"" + type + "\"";
                jsonData += "}]"; //"},";
                jsonData = jsonData.substring(0, jsonData.length() - 1);
                jsonData += "]";
               
            }
        }

        return new StreamingResolution(MIME_JS, jsonData);
    } 
    
    /**
     * Deletes / disposes object via ajax call and logs process to log file.
     * If dispose method is delete, object(s) will be deleted.
     * If dispose method is dispose, object(s) will be set inactive in fedora.
     * 
     * @return String message of disposal
     */
    @HandlesEvent("ajaxDeleteItems")
    public Resolution ajaxDeleteItems() {
        
        DestructionPlan destructionPlan = (DestructionPlan) this.getContext().getRequest().getSession().getAttribute("destructionPlan");
       
        String organization = getUserOrganizationName().toUpperCase();
        String disposeMethod = destructionPlan.getDisposeMethod();
        String logDir = this.getLogDirectoryForOrganization();
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String disposalTime = dateFormat.format(date);
        
        String logContent = "";

        // TODO: check unmodifiable objects
        // Check if delete allowed
        boolean notAllowed = false;
        for (String childrenPid : childrenPids) {
            if (childrenPid.contains(RepositoryManager.PID_COLLECTION_ACTIONS)
                    || childrenPid.contains(RepositoryManager.PID_COLLECTION_AGENTS)
                    || childrenPid.contains(RepositoryManager.PID_COLLECTION_CONTEXTS)
                    || childrenPid.contains(RepositoryManager.PID_COLLECTION_EVENTS)
                    || childrenPid.contains(RepositoryManager.PID_COLLECTION_PLACES)
                    || childrenPid.contains(RepositoryManager.PID_COLLECTION_LYACTIONS)
                    || childrenPid.contains(RepositoryManager.PID_COLLECTION_TOL2008ACTIONS)
                    || childrenPid.contains(":F-tol2008action")
                    || childrenPid.contains(":F-lyaction"))
            {
                notAllowed = true;
                break;
            }
        }
        
        if (notAllowed) {
            // Delete not allowed
            return new StreamingResolution(MIME_TEXT, "denied");
        }

        if ("dispose".equalsIgnoreCase(disposeMethod)) {

            for (String childrenPid : childrenPids) {
                logContent += "[" + disposalTime + "] Setting object \""
                        + this.getObjName(childrenPid, organization)
                        + "\" [" + childrenPid + "] inactive\n";

                // add to mongodb
                boolean returnValue = Osa.dbManager.get("mongo").insertToDisposalList(this.getContext().getUser(), childrenPid, disposalTime, null);
                if (returnValue) {
                    Osa.fedoraManager.setObjectState(childrenPid, State.I);
                    Osa.dbManager.get("mongo").syncBaskets(childrenPid, MongoManager.EVENT_REMOVE, null);
                }
            }
            
        } else if (destructionPlan.isRecursiveDestruction()){
            /*
             * Deletes target and all its children. Doesn't leave orphans
             */         
            for (int i = childrenPids.size()-1; i >= 0; i--) {
                logContent += "[" + disposalTime + "] Deleting object \""
                        + this.getObjName(childrenPids.get(i), organization)
                        + "\" [" + childrenPids.get(i) + "]\n";
                Osa.fedoraManager.deleteObject(childrenPids.get(i));       
                Osa.dbManager.get("mongo").syncBaskets(childrenPids.get(i), MongoManager.EVENT_REMOVE, null);
            }
            
        } else if (!destructionPlan.isRecursiveDestruction()) {
            
            logContent += "[" + disposalTime + "] Deleting object \""
                    + this.getObjName(childrenPids.get(0), organization)
                    + "\" [" + childrenPids.get(0) + "]\n";
             Osa.fedoraManager.deleteObject(childrenPids.get(0));  
             Osa.dbManager.get("mongo").syncBaskets(childrenPids.get(0), MongoManager.EVENT_REMOVE, null);
            
        }       
        
        // Create log's content and set log's type
        logDisposal(disposeMethod, logContent, logDir);
       
        return new StreamingResolution(MIME_TEXT, disposeMethod + "d");
    }
    
    /**
     * Creates a log file when disposing objects.
     * Logs the time object was disposed and the object that was disposed.
     * 
     * @param logType     Disposal method [delete | dispose]
     * @param logContent  log text
     */
    public static void logDisposal(String logType, String logContent, String logDirectory) {
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String logDir = logDirectory + "/disposelogs/";
            
            File file = new File(logDir + logType + "/" + logType + "-" + sdf.format(new Date()) + ".log");
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file, true);
            PrintWriter printer = new PrintWriter(writer);

            if (!file.exists() || file.length() == 0) {
                file.createNewFile();
                printer.println("#### LOG FILE FOR " + logType.toUpperCase() + " ####");
                printer.println("");
            } else {
                printer.println("");
            }
            printer.print(logContent);
            printer.close();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

	public Vector<String> getPidArray() {
		return pidArray;
	}

	public void setPidArray(Vector<String> pidArray) {
		this.pidArray = pidArray;
	}
	
    public ArrayList<String> getChildrenPids() {
        return childrenPids;
    }

    public void setChildrenPids(ArrayList<String> childrenPids) {
        this.childrenPids = childrenPids;
    }
}
