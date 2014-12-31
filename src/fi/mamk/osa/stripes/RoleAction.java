package fi.mamk.osa.stripes;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import fi.mamk.osa.auth.AccessRight;
import fi.mamk.osa.auth.LdapManager;
import fi.mamk.osa.auth.Role;
import fi.mamk.osa.auth.LdapManager.EntryType;
import fi.mamk.osa.auth.User;
import fi.mamk.osa.core.Osa;
import fi.mamk.osa.utils.RoleComparator;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.LocalizableMessage;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.LocalizableError;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Role Action
 */
@UrlBinding("/Role.action")
public class RoleAction extends OsaBaseActionBean
{
    private static final Logger logger = Logger.getLogger(RoleAction.class);
    
    private String roleName;
	private String roleOrganization;
    private String messageAddSuccess;
    private String messageAddFail;
    private String sEcho;
    private String roleDn;
    private Vector<String> entryDns;
    Vector<AccessRight> accessrights = new Vector<AccessRight>();
    
    @HandlesEvent("addRole")
    public Resolution addRole() {
        // get complete path for pids
	    handleAccessrightPaths();

        boolean added = Osa.authManager.createRole(roleName, roleOrganization, accessrights);

        if (!added) {
            return new StreamingResolution(MIME_TEXT, "Role creation failed");
        } else {
        	return new StreamingResolution(MIME_TEXT, "");
        }        
    }

    public Resolution updateRole() throws UnsupportedEncodingException {
    	boolean success		= false;
    	String dn			= "";
    	dn 					= cleanUtf8Encoded(roleDn);
    	// get complete path for pids
    	handleAccessrightPaths();
    	if (accessrights != null && accessrights.size() > 0) {
    		success = Osa.authManager.updateRole(dn, roleName, accessrights);
    	}
    	if (success) {
    		this.getContext().getMessages().add(new LocalizableMessage("message.role.updatesuccess"));
    	}
    	return new RedirectResolution(ADMIN);
    }
    
    @HandlesEvent("getRoleForUpdating")
    public Resolution getRoleForUpdating() throws UnsupportedEncodingException {
    	HashMap<String, Object> roleInfo 	= new HashMap<String, Object>();
    	User user = this.getContext().getUser();
    	
    	String[] dnParts 	= null;
    	String organization = "";
    	String roleName		= "";
    	String dn			= "";
    	dn 					= cleanUtf8Encoded(roleDn);
		
		dnParts = dn.split(",");
		for (String dnPart : dnParts) {
			if (dnPart.startsWith("cn=")) {
				roleName = dnPart.split("=")[1];
			}
			if (dnPart.startsWith("o=")) {
				organization = dnPart.split("=")[1];
			}
		}
		
    	Role role 			= new Role();
    	role				= Osa.authManager.getRole(roleName, organization);
    	
    	roleInfo.put("name", role.getName());
    	roleInfo.put("organization", organization);
    	roleInfo.put("dn", dn+","+Osa.authManager.getDomainComponents());
    	
    	Vector<Object> accessrights	= new Vector<Object>();
    	for (AccessRight ac : role.getRoleAccessrights()) {
    		HashMap<String, Object> accessright = new HashMap<String, Object>();
    		accessright.put("accessRightLevel", ac.getAccessRightLevel());
    		
    		String pathLocal 	= "";
    		String[] pathParts 	= ac.getAccessRightPath().split("/");
    		int index 			= (ac.getAccessRightPath().endsWith("+")) ? 2 : 1; // If path ends with recursive rule, get 2nd-to-last index, otherwise last
    		
    		// If remaining path is organization root, put organization name as visible name
    		if (pathParts[pathParts.length-index].equalsIgnoreCase(organization+":root")) {
    			pathLocal = StringUtils.capitalize(organization);
    		} else {
    			// Otherwise get name from Solr
    			pathLocal = Osa.searchManager.getItemByPid(pathParts[pathParts.length-index], user, getUserLocale());
    		}
    		
    		accessright.put("accessRightPath", pathParts[pathParts.length-index]);
    		accessright.put("accessRightPathLocal", pathLocal);
    		accessright.put("publicityLevel", ac.getPublicityLevel());
    		accessright.put("recursiveRule", String.valueOf(ac.isRecursiveRule()));
    		accessrights.add(accessright);
    	}
    	roleInfo.put("accessrights", accessrights);
    	return new StreamingResolution(MIME_JS, new flexjson.JSONSerializer().deepSerialize(roleInfo));
    }
    
    @HandlesEvent("createRoleTable")
    public Resolution createRoleTable(){
    	String json = "";
    	User user = this.getContext().getUser();
    	String org 	= getUserOrganizationName();
    	List<Role> roleList = Osa.authManager.getRoles(org);
    	Collections.sort(roleList, new RoleComparator(RoleComparator.NAME, false));
    	
    	json = "{" +
			    "\"sEcho\": "+sEcho+"," +
			    "\"iTotalRecords\": "+roleList.size()+"," +
			    "\"iTotalDisplayRecords\": " +roleList.size()+",";
    	
    	json += "\"aaData\": [";
    	
    	for (Role role : roleList) {
    		json += "[";
    		// #1 col
    		json += "\"<input type='checkbox' class='select-role floatLeft' data-dn='cn="+role.getName()+",o="+org+"'/> " +
    				  "<span class='username edit-role pointerCursor' title='"+new LocalizableMessage("role.editinfo").getMessage(getUserLocale())+"'>"+role.getName()+"</span>\",";
    		// #2 col
    		json += "\"";
    		String accessRightString = "";
    		
    		for (AccessRight ar : role.getRoleAccessrights()) {
    			/*OSA:C-1407847655162-0*/
    			accessRightString = "";
    			String pids [] =  ar.getAccessRightPath().split("/");
    			
    			for( String pidNotFormated : pids){
    				if(pidNotFormated.length() >=4){
    					String objectName = Osa.searchManager.getItemByPid(pidNotFormated, user, this.getUserLocale());        	
        				if(accessRightString != ""){
        					accessRightString += ", "+pidNotFormated;
        				}
        				else {
        					accessRightString += pidNotFormated;
        				}    			
        				if (!objectName.contains("No title found")){
        					accessRightString += " ("+objectName+") ";
        				}   
    				} 				
    			}
    			accessRightString.replaceAll("\\+", "");
    			json += "<div class='accessright-container'>"+
							new LocalizableMessage("role.accessrightlevel."+ar.getAccessRightLevel()+".long").getMessage(getUserLocale())+
							new LocalizableMessage("role."+ar.getPublicityLevel()+".long").getMessage(getUserLocale())+".<br/>"+
							new LocalizableMessage("role.appliesto").getMessage(getUserLocale())+": <span class='info-small blue'>"+accessRightString+" </span>"+
    					"</div>";
    		}
    		json += "\",";
    		// #3 col
    		json += "\"";
    		String occupantCleaned = "";
    		//TODO Maybe change the cleaning logic
    		Vector<String> occupants = role.getRoleOccupants();
    		Collections.sort(occupants);
    		for (String occupant : occupants) {
    			occupantCleaned = occupant.split(",")[0].replace("cn=", "");
    			json += occupantCleaned+"<br/>";
    		}
    		json += "\"";
    		json +=	"],";
    	}
    	if (roleList.size() > 0) {
    		json = json.substring(0, json.length()-1);
    	}
    	json += "]}";
    	return new StreamingResolution(MIME_JS, json);
    }
    
    @HandlesEvent("deleteRole")
    public Resolution deleteRole () {
    	String dn = "";
    	String result = "";
    	for (String roleDn : entryDns) {
    		try {
				dn = cleanUtf8Encoded(roleDn);
				Osa.authManager.deleteEntry(dn, EntryType.ROLE);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
    		logger.info("deleted entry "+dn);
		}
    	result = "success";
    	return new StreamingResolution(MIME_TEXT,result);
    }
    
    /**
     * Sets complete path for pids
     */
    public void handleAccessrightPaths() {
        // get complete path for pids
        for (AccessRight acr : accessrights) {
            if (acr != null) {
                String directory = acr.getAccessRightPath();
                String path = Osa.fedoraManager.getPath(directory);
                if (path.endsWith("/")) {
                    acr.setAccessRightPath(path);
                } else {
                    acr.setAccessRightPath(path+ "/");
                }
                
                if (acr.isRecursiveRule() && !acr.getAccessRightPath().endsWith("+")) {
                    acr.setAccessRightPath(acr.getAccessRightPath() + "+");
                } else if (!acr.isRecursiveRule() && acr.getAccessRightPath().endsWith("+")) {
                    acr.setAccessRightPath(acr.getAccessRightPath().substring(0, acr.getAccessRightPath().length()-1)); 
                }           
            }
        }
    }
    
	public String getRoleName() {
	  return roleName;
	}
	public void setRoleName(String roleName) {
	  this.roleName = roleName;
	}
	public String getRoleOrganization() {
	  return roleOrganization;
	}
	public void setRoleOrganization(String roleOrganization) {
	  this.roleOrganization = roleOrganization;
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
	public Vector<AccessRight> getRoleAccessrights() {
		return accessrights;
	}
	public void setRoleAccessrights(Vector<AccessRight> roleAccessrights) {
		this.accessrights = roleAccessrights;
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
	public String getRoleDn() {
		return roleDn;
	}
	public void setRoleDn(String roleDn) {
		this.roleDn = roleDn;
	}
}
