package fi.mamk.osa.stripes;

import java.util.Collections;
import java.util.Vector;
import fi.mamk.osa.core.Osa;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.LocalizableMessage;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.LocalizableError;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;
import org.apache.log4j.Logger;


/**
 * Group Action
 */
@UrlBinding("/Group.action")
public class GroupAction extends OsaBaseActionBean {
    
    private static final Logger logger = Logger.getLogger(GroupAction.class);
    
    private String groupName;
    private String groupOrganization;
    private String messageAddSuccess;
    private String messageAddFail;
	private String sEcho;
    	
	@HandlesEvent("createGroupAjax")
    public Resolution createGroupAjax() {
		
    	boolean added = Osa.authManager.createGroup(groupName, groupOrganization);
    	
    	if (added) {
    		return new StreamingResolution(MIME_TEXT, "");
    	} else {
    		return new StreamingResolution(MIME_TEXT, "Group not added");
    	}
    }
	
    @ValidationMethod(on="createGroup")
    public void handleGroupErrors(ValidationErrors errors) {
        boolean added = Osa.authManager.createGroup(groupName, groupOrganization);
        if (!added) {
            errors.add("fname", new LocalizableError("add.error.failed"));	 
        }
    }

	@HandlesEvent("createGroup")
    public Resolution createGroup(){
        this.getContext().getMessages().add(new LocalizableMessage("message.role.addsuccess"));
        return new RedirectResolution(ADMIN);
    }
	
	@HandlesEvent("createGroupTable")
	public Resolution createGroupTable() {
		Vector<String> groups = Osa.authManager.getGroups(getUserOrganizationName());
		Collections.sort(groups);
		
		String json = "";
		json = "{" +
			    "\"sEcho\": "+sEcho+"," +
			    "\"iTotalRecords\": "+groups.size()+"," +
			    "\"iTotalDisplayRecords\": " +groups.size()+",";
    	
    	json += "\"aaData\": [";
    	for (String group : groups) {
    		json += "[";
			json += "\""+group+"\"";
    		json += "],";
    	}
		
    	if (groups.size() > 0) {
    		json = json.substring(0, json.length()-1);
    	}
    	json += "]}";
		return new StreamingResolution(MIME_JS, json);
	}
	
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public String getGroupOrganization() {
		return groupOrganization;
	}
	public void setGroupOrganization(String groupOrganization) {
		this.groupOrganization = groupOrganization;
	}
	public String getMessageAddSuccess() {
		return messageAddSuccess;
	}
	public void setMessageAddSuccess(String messageAddSuccess) {
		this.messageAddSuccess = messageAddSuccess;
	}
	public String getMessageAddFail() {
		return messageAddFail;
	}
	public void setMessageAddFail(String messageAddFail) {
		this.messageAddFail = messageAddFail;
	}
	public String getsEcho() {
		return sEcho;
	}
	public void setsEcho(String sEcho) {
		this.sEcho = sEcho;
	}
}