package fi.mamk.osa.stripes;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.jersey.api.client.ClientHandlerException;

import fi.mamk.osa.auth.AccessRight;
import fi.mamk.osa.auth.User;
import fi.mamk.osa.bean.CaptureBean;
import fi.mamk.osa.bean.FedoraBean;
import fi.mamk.osa.bean.ManagementBean;
import fi.mamk.osa.bean.MetaDataElement;
import fi.mamk.osa.core.Osa;
import fi.mamk.osa.core.PreservationPlan;
import fi.mamk.osa.fedora.RepositoryManager;
import fi.mamk.osa.ui.Gui;
import fi.mamk.osa.workflow.WorkflowManager;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;

@UrlBinding("/Workflow.action")
public class WorkflowAction extends OsaBaseActionBean {
	
	private static final Logger logger = Logger.getLogger(WorkflowAction.class);
	private String clientAnswer;
	private String workFlowName;
	private String filename;
	private String pid;
		
	/**
	 * Executes Zip workflow
	 */
	public File getZip(String uploadDirectory, String name) throws IOException, ClientHandlerException {
		HashMap<String,Object> options = new HashMap<String,Object>();
		options.put(WorkflowManager.OPT_FILENAME, name);
		options.put(WorkflowManager.OPT_UPLOADDIR, uploadDirectory);

	    Osa.workflowManager.startWorkflow(WorkflowManager.WORKFLOW_ZIP, options);
		
		getContext().getRequest().getSession().setAttribute("Workflowoutput", uploadDirectory+name+".tar.gz");
		
		while (!new File((String) getContext().getRequest().getSession().getAttribute("Workflowoutput")).exists()) {
			//wait
			try {
			    Thread.sleep(100);
			} catch (InterruptedException e) {
			    logger.error("WorkflowAction:getZip error: "+e);
			}
		}
		
		// Delete created folders from server after compressing
		File newFile = new File(uploadDirectory+name);
		if (newFile.exists()) {
    		try {
    			FileUtils.deleteDirectory(newFile);
    		} catch (IOException e) {
    			logger.error("WorkflowAction:getZip error: "+e);
    		}
		}
		
		return null;
	}
	
    @HandlesEvent("downloadObject")
    public Resolution getObjectData() throws MalformedURLException, IOException {
    	FedoraBean fObject = Osa.fedoraManager.findObject(pid);
    	String path = ((ManagementBean)fObject.getDataStream(FedoraBean.DATASTREAM_MANAGEMENT)).getPath();
        String publicityLevel = ((CaptureBean)fObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE)).getPublicityLevel();
        
        // If user does not have sufficient rights to download requested object, redirect to error page
        if (this.getAccessRightForObject(path, publicityLevel) < AccessRight.ACCESSRIGHTLEVEL_READ_DOC) {
        	return new ForwardResolution(ERROR_404);
        }
        
    	List<String> existingDatastreams = Osa.fedoraManager.listDataStreams(pid);
    
    	String origFormat = fObject.getDataStream(FedoraBean.DATASTREAM_ORIGINAL).getMimeFormat().toString();
    	String origFileExt = origFormat.substring(origFormat.indexOf("/")+1, origFormat.length());
    	
    	if (origFileExt.isEmpty()) {origFileExt = null;}
    
    	logger.info("Started creation of compressed file for "+pid);
    	String uploadDirectory = this.getUploadDirectory(true);
    	
        for(int i = 0; i < existingDatastreams.size(); i++) {
            
        	Osa.fedoraManager.writeDatastreamToFile(uploadDirectory, pid, existingDatastreams.get(i), origFileExt);
        }
        
        try {
            // execute workflow
            this.getZip(uploadDirectory, pid);
            
        } catch (ClientHandlerException e) {
            logger.error(e);
            File newFile = new File(uploadDirectory+pid);
            if (newFile.exists()) {
                try {
                    FileUtils.deleteDirectory(newFile);
                } catch (IOException error) {
                    logger.error(error);
                }
            }
            return new ForwardResolution(ERROR_500);
        }
        
    	return new StreamingResolution("application/zip") {
    	    public void stream(HttpServletResponse response) throws Exception {
    	    	File zipFile = new File((String) getContext().getRequest().getSession().getAttribute("Workflowoutput"));
    	    	getContext().getRequest().getSession().setAttribute("Workflowoutput", null);
    	    	byte[] fileData = new byte[(int)zipFile.length()];
    	    	DataInputStream dis = new DataInputStream((new FileInputStream(zipFile)));
    	    	dis.readFully(fileData);
    	    	dis.close();
    	        response.getOutputStream().write(fileData);
    	        zipFile.delete();
    	        logger.info("Successfully created compressed file for "+pid);
    	    }
    	}.setFilename(pid+".tar.gz");
    }
	
    @HandlesEvent("startIngestWorkflow")
    public Resolution startIngestWorkflow() {
        
        ArrayList<String> filenames = new flexjson.JSONDeserializer<ArrayList<String>>().deserialize(filename);
       
        User user = this.getContext().getUser();
        String username = this.getContext().getUser().getCn().replaceAll(" ", "");
        String organization = this.getContext().getUser().getOrganization().getName().toUpperCase();
        Gui userGui = this.getContext().getGui();
        
        for (String filenamepath : filenames) {
            // file is f.ex. osa/username/filename
            if (filenamepath.contains(username)) { 
                
                File file = null;
                ArrayList<File> attachmentList = new ArrayList<File>();
                String type = null;
                String objectType = null;
                String objParent = null;
                boolean isCollection = false;
                
                try {
    	            filenamepath = cleanUtf8Encoded(filenamepath);
    	            file = new File(Osa.dataDirectory + Osa.ingestDirectory + "/" + filenamepath);
                } catch (IOException e) {
                    logger.error("WorkflowAction:startIngestWorkflow() "+e);
                }
                
                String filename = FilenameUtils.getName(filenamepath);
                LinkedHashMap<String, MetaDataElement> captureElements = Osa.dbManager.get("mongo").getMetadataFile(user, filename);
                
                if (captureElements.containsKey(CaptureBean.RELATION_IsPartOf)) {
                    MetaDataElement relationElement = captureElements.get(CaptureBean.RELATION_IsPartOf);
                    objParent = relationElement.getValue();
                }
                
                if (captureElements.containsKey(CaptureBean.TYPE)) {
                    MetaDataElement typeElement = captureElements.get(CaptureBean.TYPE);
                    type = typeElement.getValue();
                    
                    if (type.endsWith(RepositoryManager.COLLECTION)) {
                        // collection itself has information in preservation plan
                        objectType = type;
                        isCollection = true;
                    } else {
                        // parent has information in preservation plan
                        objectType = Osa.searchManager.getObjectType(objParent, user);
                    }
                } else {
                    continue;
                }
    
                if (!captureElements.containsKey(CaptureBean.IDENTIFIER)) {
                    // check if id will be generated
                    boolean generated = userGui.getGUIElements(type).get(CaptureBean.IDENTIFIER).getGenerated();
                    MetaDataElement idElem = new MetaDataElement();
                    idElem.setName(CaptureBean.IDENTIFIER);
                    idElem.setGenerated(generated);
                    captureElements.put(CaptureBean.IDENTIFIER, idElem);
                }
                
                if (!captureElements.containsKey(CaptureBean.TITLE)) {
                    // set filename as title, if title does not exist
                    MetaDataElement titleElem = new MetaDataElement();
                    titleElem.setName(CaptureBean.TITLE);
                    titleElem.setValue(filename);
                    captureElements.put(CaptureBean.TITLE, titleElem);
                }
                
                // Check preservation plan
                PreservationPlan plan = (PreservationPlan) this.getContext().getRequest().getSession().getAttribute("preservationPlan");
                String validationText = Osa.fedoraManager.isObjectPermitted(plan, objectType, -1, objParent, isCollection);
                
                if (validationText != "") {
                    logger.error("WorkflowAction:startIngestWorkflow() preservationplan check failed.");
                    continue;
                }
                
                try {
                    Osa.fedoraManager.ingest(userGui, user, organization, file, attachmentList, type, null, captureElements, null);
                } catch (SAXParseException e) {
                    logger.error("WorkflowAction:startIngestWorkflow() schema validation errors: "+e);
                    continue;
                    
                } catch (SAXException e) {
                    logger.error("WorkflowAction:startIngestWorkflow() errors: "+e);
                    continue;
                }
                
                // remove from mongo
                Osa.dbManager.get("mongo").removeMetadataFile(user, filename);
                // remove file
                removeFromQueueFolder(filenamepath);
            }
        }
        
        return new StreamingResolution(MIME_TEXT, "true");
    }
	
    public void removeFromQueueFolder(String filename) {
        String queueDir = Osa.dataDirectory + Osa.ingestDirectory;
        File file = new File(queueDir + filename);
    
        if (file.canRead()) {
            file.delete();
        }
    }
    
    public String getClientAnswer() {
    	return clientAnswer;
    }
    
    public void setClientAnswer(String clientAnswer) {
    	this.clientAnswer = clientAnswer;
    }
    
    public String getWorkFlowName() {
    	return workFlowName;
    }
    
    public void setWorkFlowName(String workFlowName) {
    	this.workFlowName = workFlowName;
    }
    
    public String getFilename() {
    	return filename;
    }
    
    public void setFilename(String filename) {
    	this.filename = filename;
    }
    
    public String getPid() {
    	return pid;
    }
    
    public void setPid(String pid) {
    	this.pid = pid;
    }
}
