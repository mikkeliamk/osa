package fi.mamk.osa.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import fi.mamk.osa.auth.LdapManager.EntryType;

public abstract class AuthenticationManager {

    public boolean status;
    private String server;
    
    public AuthenticationManager() { }
    
    public String isValidUser(String username, String password, String sessionid) {
        String status = "";
        return status;
    }
    public boolean createNewUser(String fname, String lname, String mail, String lang, String pw, String userOrganization, String userGroup, Vector<String> usrFedoraRoles){
    	boolean status = false;
    	return status;
    }
    public boolean updateUser(String dn, String organization, String userGroup, Vector<String> fedoraRoles) {
        return false;
    }
    public boolean resetUserPassword(String usrDn, String pw) {
        return false;
    }
    public boolean createGroup(String groupName, String groupOrganization){
    	boolean status = false;
    	return status;
    }
    public boolean createRole(String roleName, String roleOrganization, Vector<AccessRight> accessRights){
        boolean status = false;
        return status;
    }
    public boolean updateRole(String dn, String roleName, Vector<AccessRight> accessrights) {
        return false;
    }
    public boolean createOrganization(String oName, String oDesc, String confFile, String afName, String alName, String aMail, String contact, String displayName){
    	boolean status = false;
    	return status;  
    }
    public List<User> getUsers(String organization){
        return null;
    }
    public String modifyAttributes(String dn, String jsonmod){
    	String result = "";
    	return result;
    }
    public void deleteEntry(String dn, EntryType entryType) {
    	
    }
    public HashMap<String,String> getAttributes(String dn){
    	HashMap<String,String> values = new HashMap<String,String>();
    	return values;
    }
    public String createUserDatatable() {
    	return "";
    }
    public Vector<String> getOrganizations(){
    	Vector<String> values =  new Vector<String>();
    	return values;
    }
    public Vector<String>getGroups(String organization){
    	Vector<String> values =  new Vector<String>();
    	return values;
    }
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public Vector<String> getOrganizationRoles(String organization) {
	    Vector<String> values =  new Vector<String>();
        return values;
	}
	public Role getRole(String roleName, String organization) {
	    return null;
	}
	public Role getPublicRole(String organization) {
	    return null;
	}
	public List<Role> getRoles(String organization) {
        return null;
	}
	public Vector<Role> getUserRoles(String user, String organization) {
	    Vector<Role> values =  new Vector<Role>();
        return values;
	}
	public User getUser(String userDn) {
		User user = new User();
		return user;
	}
	public String getDefaultDn(String defaultOrg) {
        return "";
	}
	public String getDomainComponents() {
        return "";
    }
	public String getInstanceAdminDn() {
	    return "";
	}
}