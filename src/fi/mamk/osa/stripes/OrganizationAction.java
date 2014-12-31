package fi.mamk.osa.stripes;

import fi.mamk.osa.core.Osa;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import org.apache.log4j.Logger;

/**
 * Organization Action
 */
@UrlBinding("/Organization.action")
public class OrganizationAction extends OsaBaseActionBean {
    
    private static final Logger logger = Logger.getLogger(OrganizationAction.class);
    
    private String organizationName;
    private String displayName;
    private String organizationDescription;
    private String confFile;
    private String adminFirstname;
    private String adminLastName;
    private String adminMail;
    private String contact;
    private String messageAddSuccess;
    private String messageAddFail;
    
	public String getUserOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(String organizationName) {
		this.organizationName = organizationName;
	}

	public String getOrganizationDescription() {
		return organizationDescription;
	}

	public void setOrganizationDescription(String organizationDescription) {
		this.organizationDescription = organizationDescription;
	}

	public String getConfFile() {
		return confFile;
	}

	public void setConfFile(String confFile) {
		this.confFile = confFile;
	}

	public String getAdminFirstname() {
		return adminFirstname;
	}

	public void setAdminFirstname(String adminFirstname) {
		this.adminFirstname = adminFirstname;
	}

	public String getAdminLastName() {
		return adminLastName;
	}

	public void setAdminLastName(String adminLastName) {
		this.adminLastName = adminLastName;
	}

	public String getAdminMail() {
		return adminMail;
	}

	public void setAdminMail(String adminMail) {
		this.adminMail = adminMail;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
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
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
     
    @HandlesEvent("addOrganization")
    public Resolution createOrganization(){
    	boolean added = Osa.authManager.createOrganization(organizationName, 
    	                                                   organizationDescription, 
    	                                                   confFile, 
    	                                                   adminFirstname, 
    	                                                   adminLastName, 
    	                                                   adminMail, 
    	                                                   contact, 
    	                                                   displayName);    	
    	return new RedirectResolution(ADMIN);
    }

}
