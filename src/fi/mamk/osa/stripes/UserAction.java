package fi.mamk.osa.stripes;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import fi.mamk.osa.core.Osa;
import fi.mamk.osa.auth.Organization;
import fi.mamk.osa.auth.Role;
import fi.mamk.osa.auth.User;
import fi.mamk.osa.auth.LdapManager.EntryType;
import fi.mamk.osa.utils.RoleComparator;
import fi.mamk.osa.utils.UserComparator;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.LocalizableMessage;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import org.apache.log4j.Logger;

/**
 * User Action
 */
@UrlBinding("/User.action")
public class UserAction extends OsaBaseActionBean implements Serializable {
    
    private static final long serialVersionUID = 2200169840795011026L;
    private static final Logger logger = Logger.getLogger(UserAction.class);

    private String fname;
    private String lname;
    private String mail;
    private String preferredLanguage;
    private String password;
    private String repeatPassword;
    private String messageAddSuccess;
    private String messageAddFail;
    private String userOrganization;
    private String userGroup;
    private String userDn;
    private String modJson;
    private String sEcho;
    private Vector<String> entryDns;
    private Vector<String> usrFedoraRoles = new Vector<String>();
    
	@HandlesEvent("createUser")
    public Resolution createUser() {
		String error = "";
		boolean added = false;
		try {
            if (password.equals(repeatPassword)) {
            	added = Osa.authManager.createNewUser(fname, lname, mail, preferredLanguage, password, userOrganization, userGroup, usrFedoraRoles);
            	
            } else {
            	error = "Passwords don't match";
            }
        } catch (NullPointerException npex) {
            logger.error("NPE @ UserAction");
        } catch (Exception ex) {
            logger.error("E @ UserAction");
        }
	
		if (!added) {
			if (!error.equals("Passwords don't match")) {
				error = "User creation failed";
			}
    		return new StreamingResolution(MIME_TEXT, error);
    		
    	} else {   		
    		return new StreamingResolution(MIME_TEXT, "");
    	}
    	
    }

    public Vector<String> findOrganizations() {
    	Vector<String> values = Osa.authManager.getOrganizations();
    	return values;
    }
    
    public Resolution findRoles() {
        Vector<String> values = Osa.authManager.getOrganizationRoles(userOrganization);
        String jsonResult = new flexjson.JSONSerializer().serialize(values);
        return new StreamingResolution(MIME_JS,jsonResult);
    }

    public Resolution findGroups() {
    	Vector<String> values = Osa.authManager.getGroups(userOrganization);
    	String jsonResult = new flexjson.JSONSerializer().serialize(values);
    	return new StreamingResolution(MIME_JS,jsonResult);
    }
    
    public Resolution getAllAttributes() {

        String jsonResult = null;
        
        try {
            userDn = cleanUtf8Encoded(userDn);
            
            HashMap<String,String> datatable = Osa.authManager.getAttributes(userDn);
            jsonResult = new flexjson.JSONSerializer().serialize(datatable);
            
        } catch (UnsupportedEncodingException e) {
            logger.error("LDAP error in function getAllAttributes() "+e);
        }
    	return new StreamingResolution(MIME_JS,jsonResult);
    }
    
    public Resolution modifyAttributes() {
        
        String result = "";
    	try {
            userDn = cleanUtf8Encoded(userDn);
            modJson = cleanUtf8Encoded(modJson);
            
            result = Osa.authManager.modifyAttributes(userDn, modJson);
             
        } catch (UnsupportedEncodingException e) {
            logger.error("LDAP error in function modifyAttributes() "+e);
        }
    	
    	return new StreamingResolution("text/javascript",result);
    }
    
    @HandlesEvent("getUser")
    public Resolution getUser() throws UnsupportedEncodingException {
    	if (this.getContext().getUser().getHighestAccessRight() < 100) {
    		return null;
    	}
    	
    	String organization					= getUserOrganizationName();
    	HashMap<String, Object> userInfo 	= new HashMap<String, Object>();
    	Vector<String> roles 				= new Vector<String>();
    	
    	String dn							= "";
    	dn 									= cleanUtf8Encoded(userDn);
		
		User user 							= new User();
    	user 								= Osa.authManager.getUser(dn);
    	Vector<String> orgRoles				= Osa.authManager.getOrganizationRoles(organization);
    	Vector<String> orgGroups			= Osa.authManager.getGroups(organization);
    	
    	userInfo.put("cn", user.getCn());
    	userInfo.put("dn", user.getDn());
    	userInfo.put("firstname", user.getFirstName());
    	userInfo.put("lastname", user.getLastName());
    	userInfo.put("mail", user.getMail());
    	userInfo.put("group", user.getGroup());
    	userInfo.put("language", new LocalizableMessage("capture.language."+user.getPreferredLanguage()).getMessage(getUserLocale()));
    	
    	for (Role role : user.getFedoraRoles()) {
    		roles.add(role.getName());
    	}
    	userInfo.put("contentroles", roles);	// Content roles that the selected user has
    	userInfo.put("orgroles", orgRoles);		// All roles for content ("fedora roles") that this organization has
    	userInfo.put("orggroups", orgGroups);	// All groups that this organization has
    	
    	return new StreamingResolution(MIME_JS, new flexjson.JSONSerializer().deepSerialize(userInfo));
    }
    
    @HandlesEvent("deleteUser")
    public Resolution deleteUser() {
    	String dn = "";
    	String result = "";
    	
    	for (String usrDn : entryDns) {
    		try {
				dn = cleanUtf8Encoded(usrDn);
				Osa.authManager.deleteEntry(dn, EntryType.USER);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
    		logger.info("deleted entry "+dn);
		}
    	result = "success";
    	return new StreamingResolution(MIME_TEXT,result);
    }

    @HandlesEvent("updateUser")
    public Resolution updateUser() throws UnsupportedEncodingException {
    	String dn 	= "";
    	dn			= cleanUtf8Encoded(userDn);
    	boolean updated = Osa.authManager.updateUser(dn, userOrganization, userGroup, usrFedoraRoles);
    	
    	if (updated) {
    		this.getContext().getMessages().add(new LocalizableMessage("message.user.updatesuccess"));
    	}
    	return new RedirectResolution(ADMIN);
    }
    
    @HandlesEvent("resetUserPassword")
    public Resolution resetUserPassword() {
        Osa.authManager.resetUserPassword("", "");
        return new RedirectResolution(ADMIN);
    }
    
    @HandlesEvent("createUserTable")
    public Resolution createUserDatatable() {
    	String json = "";
    	String org = getUserOrganizationName();
    	List<User> userList = Osa.authManager.getUsers(org);
    	Collections.sort(userList, new UserComparator(UserComparator.NAME, false));
    	json = "{" +
			    "\"sEcho\": "+sEcho+"," +
			    "\"iTotalRecords\": "+userList.size()+"," +
			    "\"iTotalDisplayRecords\": " +userList.size()+",";
    	
    	json += "\"aaData\": [";
    	for (User user : userList) {
    		json += "[";
    		// #1 col
    		json += "\"<div class='relative-td-fix'>" +
    					"<input type='checkbox' class='select-user floatLeft' " +
    						"data-dn='cn="+user.getCn()+",ou="+user.getGroup()+",o="+org+"' " +
    						"data-role='"+new LocalizableMessage("user.role."+user.getRole()).getMessage(getUserLocale())+"'>" +
    					"<img src='img/icons/user-icon-transparent.png' class='usericon'/>" +
						"<div>" +
		    				"<span class='username edit-user pointerCursor' title='"+new LocalizableMessage("user.editinfo").getMessage(getUserLocale())+"'>"+user.getCn()+"</span><br/>" + 
		    				"<span class='info-small gray'>"+user.getMail()+"</span>" +
						"</div>" +
						"<span class='delete-user pointerCursor blue info-small' " +
	    					"data-dn='cn="+user.getCn()+",ou="+user.getGroup()+",o="+org+"' " +
							"data-role='"+new LocalizableMessage("user.role."+user.getRole()).getMessage(getUserLocale())+"'>" +
								new LocalizableMessage("button.delete.user").getMessage(getUserLocale())+
						"</span>"+
						"<div class='clearfix'></div>"+
					  "</div>\","; 
    		// #2 col
    		json += "\""+user.getGroup()+"\","; 

    		Vector<Role> fedoraRoles = user.getFedoraRoles();
    		Collections.sort(fedoraRoles, new RoleComparator(RoleComparator.NAME, false));
    		String rolesString = "";
    		if (!fedoraRoles.isEmpty()) {
    			for (Role fedoraRole : fedoraRoles) {
    				rolesString += "<span class='info-small blue'>"+fedoraRole.getName()+"</span><br/>";
    			}
    		}
    		json += "\""+rolesString+"\"";
    		json +=	"],";
    	} // for
    	
    	if (userList.size() > 0) {
    		json = json.substring(0, json.length()-1);
    	}
    	json += "]}";
    	return new StreamingResolution(MIME_JS, json);
    }
       
    public Resolution setOrganization() {
    	Organization org = new Organization("o="+userOrganization+","+Osa.authManager.getDomainComponents(), userOrganization, null);
    	this.getContext().getUser().setOrganization(org);
    	logger.info("Current Organization:"+this.getContext().getUser().getOrganization().getName());
    	return new StreamingResolution(MIME_JS, "success");
    }
    
    @HandlesEvent("getLatestEdited")
	public Resolution getLatestEdited() {
		User user = this.getContext().getUser();
		String json = "";
		if(user != null && !user.isAnonymous()) {
			json = Osa.dbManager.get("mongo").getLatestEdited(user);
		}
		return new StreamingResolution(MIME_JS, json);
	}
    
    public String getPreferredLanguage() {
    	return preferredLanguage;
    }
    public void setPreferredLanguage(String preferredLanguage) {
    	this.preferredLanguage = preferredLanguage;
    }
    public String getFname() {
    	return fname;
    }
    public void setFname(String fname) {
    	this.fname = fname;
    }
    public String getLname() {
    	return lname;
    }
    public void setLname(String lname) {
    	this.lname = lname;
    }
    public String getMail() {
    	return mail;
    }
    public void setMail(String mail) {
    	this.mail = mail;
    }
    public String getPassword() {
    	return password;
    }
    public void setPassword(String password) {
    	this.password = password;
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
    public String getUserOrganization() {
    	return userOrganization;
    }
    public void setUserOrganization(String userOrganization) {
    	this.userOrganization = userOrganization;
    }
    public String getUserGroup() {
    	return userGroup;
    }
    public void setUserGroup(String userGroup) {
    	this.userGroup = userGroup;
    }
    public String getUserDn() {
    	return userDn;
    }
    public void setUserDn(String userDn) {
    	this.userDn = userDn;
    }
    public String getModJson() {
    	return modJson;
    }
    public void setModJson(String modJson) {
    	this.modJson = modJson;
    }
    public Vector<String> getUsrFedoraRoles() {
        return usrFedoraRoles;
    }
    public void setUsrFedoraRoles(Vector<String> usrFedoraRoles) {
        this.usrFedoraRoles = usrFedoraRoles;
    }
    public String getRepeatPassword() {
    	return repeatPassword;
    }
    public void setRepeatPassword(String repeatPassword) {
    	this.repeatPassword = repeatPassword;
    }
    public String getsEcho() {
    	return sEcho;
    }
    public void setsEcho(String sEcho) {
    	this.sEcho = sEcho;
    }
    public Vector<String> getEntryDns() {
    	return entryDns;
    }
    public void setEntryDns(Vector<String> entryDns) {
    	this.entryDns = entryDns;
    }
}
