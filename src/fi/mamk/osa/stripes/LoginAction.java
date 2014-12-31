package fi.mamk.osa.stripes;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import fi.mamk.osa.auth.User;
import fi.mamk.osa.core.DestructionPlan;
import fi.mamk.osa.core.Osa;
import fi.mamk.osa.core.PreservationPlan;
import fi.mamk.osa.fedora.RepositoryManager;
import fi.mamk.osa.ui.Gui;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.OnwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.LocalizableError;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;
import org.apache.log4j.Logger;


@UrlBinding("/Login.action")
public class LoginAction extends OsaBaseActionBean implements HttpSessionListener {
    
    private static final Logger logger = Logger.getLogger(LoginAction.class);
    private static Map<String, HttpSession> sessions = new HashMap<String, HttpSession>();
    
    private String username;
    private String password;
    private String targetUrl;
    private String usrdn;
	private String getType;
	private boolean alreadyLogged = false;
	    
    @ValidationMethod(on="login")
    public void handleLoginErrors(ValidationErrors errors) {
    	HttpSession session = this.getContext().getRequest().getSession();

    	User previousUser = (User) session.getAttribute("user");
    	
    	// Check if user is not null and user is not anonymous. I.e. when logging in as registered user when previous user was anonymous.
    	// This is to prevent already logged user from running the login & login validation again.
    	if (previousUser != null && previousUser.isAnonymous()) {
	        if (username == null || username.equals("")){
	        	errors.add("username" , new LocalizableError("error.emptyuser"));
	        	
	        } else if (password == null || password.equals("")) {
	        	errors.add("username" , new LocalizableError("error.emptypassword"));
	        	
	        } else if (username != null && password != null) {
	            
		        usrdn = Osa.authManager.isValidUser(username, password, session.getId());
		        
		        if (!usrdn.isEmpty()) {
		            
		            if (usrdn.equalsIgnoreCase("notfound")) {
	    	            errors.add("username" , new LocalizableError("error.wronglogin"));
	    	        } else if (usrdn.startsWith("alreadyloggedin")) {
	    	            String[] parts = usrdn.split(";");
	    	            // Get user dn, trying to log in
	                    usrdn = parts[1];
	                    session.setAttribute("usrdn", usrdn);
	                    
	    	            errors.add("username" , new LocalizableError("error.duplicatelogin"));
	    	        }
		        }
	        }
	        // If already logged in, set alreadyLogged to true
    	} else {
    		setAlreadyLogged(true);
    	}
    }
    
    @HandlesEvent("login")
    @DefaultHandler
    public Resolution login() {
        
        HttpSession session = this.getContext().getRequest().getSession();
        
        // If user that is already logged in invokes this method, only do redirect.
        if (!alreadyLogged) {
	        User user = new User(usrdn, null);
	        session.setAttribute("user", user);
	        session.setAttribute("userLocale", user.getLocale());
	        
	        File configuration = null;
	        
	        if (user.getOrganization() != null) {
	            configuration = new File(this.getContext().getServletContext().getRealPath("/WEB-INF/config/orgs/"+user.getOrganization().getName()+".xml"));
	            
	            if (!configuration.canRead()) {
	                logger.error("Cannot find organization config");
	                configuration = new File(this.getContext().getServletContext().getRealPath("/") + Osa.defaultConfiguration);  
	            }
	            
	        } else {
	            configuration = new File(this.getContext().getServletContext().getRealPath("/") + Osa.defaultConfiguration);
	        }
	        
	        // Set session id
	        sessions.put(usrdn, session);
	        
	        session.setAttribute("gui",              new Gui(configuration, this.getContext()));
	        session.setAttribute("preservationPlan", new PreservationPlan(configuration));
	        session.setAttribute("destructionPlan",  new DestructionPlan(configuration));
        }
        
        // return current page if available
        if (this.getContext().getSourcePage() != null && this.getContext() != null && this.getContext().getSourcePageResolution() != null) {

            // return current page if available
        	if(this.getContext().getSearch() != null){
        	    
        	    if (this.getPreviousViewUrl()) {
                    // return current page, if object (pid) selected to view
                    String referer = this.getContext().getRequest().getHeader("Referer");
                    String target = referer.substring(referer.indexOf("/Ingest.action"), referer.length());
                    return new RedirectResolution(target);
                    
                } else {
                    return ((OnwardResolution<ForwardResolution>) this.getContext().getSourcePageResolution()).addParameter("src", "login");
                }
        	}
        	else{
        		return ((OnwardResolution<ForwardResolution>) this.getContext().getSourcePageResolution());
        	}        	
        	
        } else {
        	return new RedirectResolution(_INDEX);
        }
    }
    
    @HandlesEvent("forceLogin")
    public void forceLogin() {
        boolean status = false;
        String userSessionId = "";
        HttpSession session = this.getContext().getRequest().getSession();
        usrdn = session.getAttribute("usrdn").toString();
        
        if (usrdn != null) {
            
            // Remove previous login
            userSessionId = Osa.dbManager.get("sql").removeCurrentuser(usrdn);
            
            if (userSessionId != "" 
                && sessions.get(usrdn) != null 
                && userSessionId.equals(sessions.get(usrdn).getId())) {
                
                HttpSession oldSession = sessions.get(usrdn);
                oldSession.invalidate();
                sessions.remove(usrdn);
            }
            
            // Add user
            status = Osa.dbManager.get("sql").setCurrentuser(usrdn, session.getId());
            
            session.removeAttribute("usrdn");
        }
        
        if (status) {
            this.login();
        }
    }
    
    @HandlesEvent("setdefaultgui")
    public void setDefaultGui() {
    	HttpSession session = this.getContext().getRequest().getSession();
    	File xmlGui = new File(this.getContext().getServletContext().getRealPath("/") + Osa.defaultConfiguration);
        Gui gui = new Gui(xmlGui, this.getContext());
        session.setAttribute("gui", gui);
        
        if (session.getAttribute("user") == null) {
            setDefaultUser();
        }
    }
    
    @HandlesEvent("setdefaultuser")
    public void setDefaultUser() {
        HttpSession session = this.getContext().getRequest().getSession();
        String defaultdn = Osa.authManager.getDefaultDn(Osa.defaultRoot);
        Locale language = this.getContext().getRequest().getLocale();

        // Set default user
        User user = new User(defaultdn, language);
        session.setAttribute("user", user);
        session.setAttribute("userLocale", user.getLocale());
    }
    
    // Build add-menu from existing content models
    // TODO this should be done in the layout instead
    public List<String> listContentModels() {
        return this.getContext().getGui().getAllObjects();
    }
    
    @HandlesEvent("listContentModelsJson")
    public Resolution listContentModelsJson() {
    	Vector<String> collections = new Vector<String>();
    	Vector<String> contextObjects = new Vector<String>();
    	Vector<String> documents = new Vector<String>();
    	String json = "";
    	json = "{";
    	List<String> cm = this.getContext().getGui().getAllObjects();
    	
    	for (int i = 0; i < cm.size(); i++) {
    		switch(getType) {
	    		case "all":
		    		if (cm.get(i).contains("collection")) {
		    			collections.add(cm.get(i).replace(RepositoryManager.CONTENTMODEL, ""));
		    		}
		    		else if (cm.get(i).endsWith("event") ||
		    				cm.get(i).endsWith("place") ||
		    				cm.get(i).endsWith("agent") ||
		    				cm.get(i).endsWith("action")) {
		    			contextObjects.add(cm.get(i).replace(RepositoryManager.CONTENTMODEL, ""));
		    		} else {
		    			documents.add(cm.get(i).replace(RepositoryManager.CONTENTMODEL, ""));
		    		}
		    		break;
		    		
	    		case "collections":
	    			if (cm.get(i).contains("collection")) {
		    			collections.add(cm.get(i).replace(RepositoryManager.CONTENTMODEL, ""));
		    		}
	    			break;
	    			
	    		case "contexts":
	    			if (cm.get(i).endsWith("event") ||
		    				cm.get(i).endsWith("place") ||
		    				cm.get(i).endsWith("agent") ||
		    				cm.get(i).endsWith("action")) {
		    			contextObjects.add(cm.get(i).replace(RepositoryManager.CONTENTMODEL, ""));
	    			}
	    			break;
	    			
				case "documents":
					if (!cm.get(i).endsWith("event") &&
    				!cm.get(i).endsWith("place") &&
    				!cm.get(i).endsWith("agent") &&
    				!cm.get(i).endsWith("action") && 
    				!cm.get(i).contains("collection")) {
						documents.add(cm.get(i).replace(RepositoryManager.CONTENTMODEL, ""));
					}
					break;
			}
    	}
    	
    	// Creates the actual JSON object according to given type
    	switch (getType) {
	    	case "all":
		    	json += "\"collection\":"+new flexjson.JSONSerializer().serialize(collections)+",";
		    	json += "\"document\":"+new flexjson.JSONSerializer().serialize(documents)+",";
		    	json += "\"contextobject\":"+new flexjson.JSONSerializer().serialize(contextObjects);
		    	break;
	    	case "collections":
	    		json += "\"collection\":"+new flexjson.JSONSerializer().serialize(collections);
	    		break;
	    	case "contexts": 
	    		json += "\"contextobject\":"+new flexjson.JSONSerializer().serialize(contextObjects);
	    		break;
	    	case "documents":
	    		json += "\"document\":"+new flexjson.JSONSerializer().serialize(documents);
	    		break;
    	}
    	json += "}";
    	
    	return new StreamingResolution(MIME_JS, json);
    }
        
    public void setUsername(String username) { 
        this.username = username; 
    }
    
    public String getUsername() { 
        return username; 
    }

    public void setPassword(String password) { 
        this.password = password; 
    }
    
    public String getPassword() { 
        return password; 
    }

    public String getTargetUrl() { 
        return targetUrl; 
    }
    
    public void setTargetUrl(String targetUrl) { 
        this.targetUrl = targetUrl; 
    }
    
    /**
     * Notification that a session was created.
     */
    public void sessionCreated(HttpSessionEvent e) {
//        HttpSession session = e.getSession();
    }
    
    /**
     * Notification that a session was about to be invalidated.
     */
    public void sessionDestroyed(HttpSessionEvent e) {
        HttpSession session = e.getSession();
        User user = (User) session.getAttribute("user");

        if (user != null) {
            // Remove inactive user from database
            Osa.dbManager.get("sql").removeCurrentuser(user.getDn());
        }
    }

	public String getGetType() {
		return getType;
	}

	public void setGetType(String getType) {
		this.getType = getType;
	}

	public boolean isAlreadyLogged() {
		return alreadyLogged;
	}

	public void setAlreadyLogged(boolean alreadyLogged) {
		this.alreadyLogged = alreadyLogged;
	}
}