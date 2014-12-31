package fi.mamk.osa.stripes;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;
import java.util.Vector;
import org.apache.commons.lang.StringUtils;
import fi.mamk.osa.auth.AccessRight;
import fi.mamk.osa.auth.Role;
import fi.mamk.osa.auth.User;
import fi.mamk.osa.core.Osa;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.HttpCache;

@HttpCache(allow=false)
public class OsaBaseActionBean implements ActionBean {

	protected static final String ADMIN	   	= "/admin.jsp";
	protected static final String BROWSE   	= "/browse.jsp";
	protected static final String INDEX    	= "/index.jsp";
	protected static final String _INDEX   	= "/";
	protected static final String INGEST   	= "/ingest.jsp";
	protected static final String LOGIN    	= "/login.jsp";
	protected static final String LOGOUT   	= "/logout.jsp";
	protected static final String SEARCH   	= "/search.jsp";
	protected static final String VIEW   	= "/view.jsp";
	protected static final String WORKSPACE	= "/workspace.jsp";
	
	protected static final String ERROR_404	= "/404.jsp";
	protected static final String ERROR_500	= "/error.jsp";

	protected static final String MIME_JSON	= "application/json";
	protected static final String MIME_JS	= "application/javascript";
	protected static final String MIME_PNG	= "image/png";
	protected static final String MIME_JPEG	= "image/jpeg";
	protected static final String MIME_GIF	= "image/gif";
	protected static final String MIME_TEXT	= "text/plain";
	protected static final String MIME_HTML	= "text/html";
	
	private OsaActionBeanContext context;
	
	public boolean fromSearch;
	/** Interface method from ActionBean. */
    public void setContext(ActionBeanContext context) {
        this.context = (OsaActionBeanContext) context;
    }

    /** Interface method from ActionBean, using a co-variant return type! */
    public OsaActionBeanContext getContext() {
        return this.context;
    }
    
    public boolean isFromSearch() {
        //check if comes from one of related pages and searched PID is the same
        if ((this.getContext().getRequest().getHeader("Referer").contains("view"))
           || (this.getContext().getRequest().getHeader("Referer").contains("search")) 
           || (this.getContext().getRequest().getHeader("Referer").contains("plainView"))) 
        {  
        	if (this.getContext().getRequest().getSession().getAttribute("fromSearch") != null) {        	
        		return (boolean) this.getContext().getRequest().getSession().getAttribute("fromSearch");
        	} else {
        		return false;
        	}
        } else {
            this.getContext().getRequest().getSession().setAttribute("fromSearch", false);
            return false;
        }
    }

	public void setFromSearch(boolean fromSearch) {
		this.getContext().getRequest().getSession().setAttribute("fromSearch", true);
	}

    public boolean getPreviousViewUrl() {
        String referer = this.getContext().getRequest().getHeader("Referer");
        if (referer != null) {
            return (referer.toLowerCase().contains("pid=") && referer.toLowerCase().contains("view="));
        } else {
            return false;
        }
    }
    
    public boolean getPreviousSearchUrl() {
        String referer = this.getContext().getRequest().getHeader("Referer");
        if (referer != null) {
        	return referer.toLowerCase().contains("search");
        } else {
        	return false;
        }
    }
    
    public int getAccessRightForObject(String path, String publicityLevel) {

        if (this.getContext() == null || this.getContext().getUser() == null) {
            return AccessRight.ACCESSRIGHTLEVEL_DENY_META;
        }
       
        Vector<Role> roles = this.getContext().getUser().getFedoraRoles();
        return Role.getAccessRightForObject(roles, path, publicityLevel);
    }
    
    public boolean hasRights(String path, String publicityLevel, int requiredLevel) {
        
        if (this.getContext() == null || this.getContext().getUser() == null) {
            return false;
        }
        
        Vector<Role> roles = this.getContext().getUser().getFedoraRoles();
        return Role.hasRights(roles, path, publicityLevel, requiredLevel);
    }
    
    /** Return current user's organization's name as a string
     * 
     * @return	Organization name as a string
     */
    public String getUserOrganizationName() {
    	return this.getContext().getUser().getOrganization().getName();
    }
    
    /**	Returns current user's name (cn)
     * 
     * @return	current user's name (cn)
     */
    public String getUserName() {
    	return this.getContext().getUser().getCn();
    }
    
    /** Return current user's locale. Reduces usage of this.getContext().getUser()...
     * 
     * @return	Current user's locale
     */
    public Locale getUserLocale(){
    	return this.getContext().getUser().getLocale();
    }
 
    /** Cleans UTF8 encoded string. Does:<br/>
     *  cleanedStr = URLDecoder.decode(encodedString, "UTF-8");<br/>
     *  cleanedStr = cleanedStr.replaceAll("^\"|\"$", "");
     * 
     * @param encodedString 	UTF8 encoded string
     * @return Cleaned string
     * @throws UnsupportedEncodingException
     */
    public String cleanUtf8Encoded(String encodedString) throws UnsupportedEncodingException {
    	String cleanedStr 	= "";
    	if (encodedString != null) {
        	cleanedStr 		= URLDecoder.decode(encodedString, "UTF-8");
        	cleanedStr 		= cleanedStr.replaceAll("^\"|\"$", "");
    	}
    	return cleanedStr;
    }

    /** Checks if user logged in is admin
     * 
     * @return true if admin (AccessRight.ACCESSRIGHTLEVEL_ADMIN)
     */
    public boolean isAdmin() {
        if (this.getContext() == null || this.getContext().getUser() == null) {
            return false;
        }
        return getContext().getUser().getHighestAccessRight() >= AccessRight.ACCESSRIGHTLEVEL_ADMIN;
    }
    
    /** Checks if user logged in is records manager or higher
     * 
     * @return true if records manager (AccessRight.ACCESSRIGHTLEVEL_RECORDSMANAGEMENT)
     */
    public boolean isRecordsManager() {
        if (this.getContext() == null || this.getContext().getUser() == null) {
            return false;
        }
        return getContext().getUser().getHighestAccessRight() >= AccessRight.ACCESSRIGHTLEVEL_RECORDSMANAGEMENT;
    }
    
    public boolean isDocumentAdder() {
        if (this.getContext() == null || this.getContext().getUser() == null) {
            return false;
        }
        return getContext().getUser().getHighestAccessRight() >= AccessRight.ACCESSRIGHTLEVEL_ADD_DOC;
    }
    
    /**
     * @param appendOrgAndUser	Boolean flag to indicate whether to append organization and username to directory path
     * @return Osa.dataDirectory + Osa.ingestDirectory + "/" + organization + "/" + username + "/"
     */
    public String getIngestDirectory(boolean appendOrgAndUser) {
        String queueDir = Osa.dataDirectory + Osa.ingestDirectory;
        if (!appendOrgAndUser) {
            checkIfDirectoryExists(queueDir);
        	return queueDir;
        }
        String organization = getUserOrganizationName();
        String username = getUserName().replaceAll(" ", "");
        String dir = queueDir + "/" + organization + "/" + username + "/";
        checkIfDirectoryExists(dir);
        return dir;
    }
    
    /**
     * @param appendOrgAndUser	Boolean flag to indicate whether to append organization and username to directory path
     * @return Osa.dataDirectory + Osa.importDirectory + "/" + organization + "/" + username + "/"
     */
    public String getImportDirectory(boolean appendOrgAndUser) {
        String watchedDir = Osa.dataDirectory + Osa.importDirectory;
        if (!appendOrgAndUser) {
            checkIfDirectoryExists(watchedDir);
        	return watchedDir;
        }
        String organization = getUserOrganizationName();
        String username = getUserName().replaceAll(" ", "");
        String dir = watchedDir + "/" + organization + "/" + username + "/";
        checkIfDirectoryExists(dir);
        return dir;
    }
    
    /**
     * @param appendOrgAndUser	Boolean flag to indicate whether to append organization and username to directory path
     * @return Osa.dataDirectory + Osa.uploadDirectory + "/" + organization + "/" + username + "/"
     */
    public String getUploadDirectory(boolean appendOrgAndUser) {
        String uploadDir = Osa.dataDirectory + Osa.uploadDirectory;
        if (!appendOrgAndUser) {
            checkIfDirectoryExists(uploadDir);
        	return uploadDir;
        }
        String organization = getUserOrganizationName();
        String username = getUserName().replaceAll(" ", "");
        String dir = uploadDir + "/" + organization + "/" + username + "/";
        checkIfDirectoryExists(dir);
        return dir;
    }
    
    /**
     * 
     * @param logType				String indicating type of log, f.ex microservice. Leave null or empty if you want just the root of log directory
     * @param appendOrgAndUser		Boolean flag to indicate whether append organization and user names to path
     * @return
     */
    public String getLogDirectory (String logType, boolean appendOrgAndUser) {
    	
        String logDir = Osa.logDirectory;
        if (!appendOrgAndUser) {
        	return logDir;
        }
        String organization = getUserOrganizationName();
        String username = getUserName().replaceAll(" ", "");
        String dir = logDir + "/" + organization + "/" + username;
        
        if (logType != null && StringUtils.isNotEmpty(logType)) {
            dir += "/" + logType + "/";
        }
        return dir;
    }
    
    public String getLogDirectoryForOrganization() {
        String logDir = Osa.logDirectory;
        String organization = getUserOrganizationName();
        String dir = logDir + "/" + organization;
        return dir;
    }
    
    public String getFailedDirectory() {
        String dir = Osa.dataDirectory + Osa.failedDirectory + "/";
        checkIfDirectoryExists(dir);
        return dir;
    }
    
    public void checkIfDirectoryExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.setWritable(true, true);
            dir.mkdirs();
        }
    }
    
    /**
     * Gets object's name from solr. If object is root object return
     * organization name
     * 
     * @param pid
     *            Object whose name to get
     * @param organization
     *            User's organization
     * @return Object's name / error message if name not available
     */
    public String getObjName(String pid, String organization) {
        String identifier = "";
        User user = this.getContext().getUser();

        if (pid.contains("info:fedora/")) {
            identifier = pid.replace("info:fedora/", "");
        } else {
            identifier = pid;
        }

        if (identifier.equalsIgnoreCase(organization + ":root")) {
            return organization;
        } else {
            String test = Osa.searchManager.getItemByPid(identifier.replace(":", "\\:"), user, getUserLocale());

            if (test != null) {
                return test.replace("[", "").replace("]", "");
            } else {
                return "Label not found for pid: " + identifier;
            }
        }
    }
}