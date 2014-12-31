package fi.mamk.osa.auth;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;

import fi.mamk.osa.core.Osa;

/**
 * Class for user creation
 * 
 * fedoraRoles  contains all roles, that has been set to user
 */
public class User implements Serializable {
    
    private static final long serialVersionUID = -9105054849954016390L;
    private static final Logger logger = Logger.getLogger(User.class);
    
	/** Distinguished name */
	private String dn = null;
	/** Common name */
	private String cn = null;
	/** Password of the User. Could be cleartext, encoded or hash depending on implementation. */
    private String password = null;
    /** Unique identifier */
    private String uid = null;
    
    /** First name (given name) */ 
    private String firstName = null;
    /** Last name (surname) */
    private String lastName = null;
    /** Description of the User */
    private String description = null;
    /** e-mail address of the User */
    private String mail = null;
    /** Preferred language of the User */
    private String preferredLanguage = null;
    /** Title  of the User */
    private String title = null;
    /** Employee number  of the User */
    private String employeeNumber = null;
    /** Jpeg photo  of the User. Could be URI or file address depenging on implementation */
	private String jpegPhoto = null;
	/** Role for fedora commons repository */
	private Vector<Role> fedoraRoles = null;
	/** User's organization */
    private Organization organization = null;
    /** User's group in organization */
    private String group = null;
	/** Locale of the User */
	private Locale locale = null;
	/** User is anonymous */
	private boolean anonymous = false;
	/** User is instance admin */
	private boolean instance_admin = false;
	
	private int highestAccessRight = 0;
	
	/**
     * Constructor
     */
	public User() {
	    
	}
	
	/**
     * Constructor
     */
	public User(String dn, Locale language) {
	    
        this.setDn(dn);
        
        // get user attributes from ldap
	    HashMap<String,String> usrAttrs = Osa.authManager.getAttributes(dn);
	   
	    if (dn.equals(Osa.authManager.getInstanceAdminDn())) {
	        this.setInstanceAdmin(true);
	    } 
	    
	    if (usrAttrs.containsKey("cn")) {
            this.setCn(usrAttrs.get("cn"));
            this.setAnonymous(false);
            logger.info("logged in with dn: "+dn);
        } else {
            this.setAnonymous(true);
            logger.info("logged in as anonymous user with dn: "+dn);
        }
	    
        if (usrAttrs.containsKey("preferredLanguage")) {
            String[] localeParts = usrAttrs.get("preferredLanguage").split("_");
            this.locale = new Locale(localeParts[0],localeParts[1]);
        }
        
        // TODO: find better way to do this
        // parse organization name from dn
	    String[] parts = dn.split(",");
	    String orgname = "";
	    for (int i=0; i<parts.length; i++) {
	    	if (parts[i].indexOf("o=") == 0) {
	    		orgname = parts[i].replace("o=", "");
	    		break;
	    	}
	    }
	    
	    Organization org = new Organization(dn.replaceAll("(cn=.*?,)", ""), orgname, null);
	    this.setOrganization(org);

	    if (usrAttrs.containsKey("mail")) {
	        this.setMail(usrAttrs.get("mail"));
	    }
	    
	    if (anonymous) {
	        Role anonymousRole = Osa.authManager.getPublicRole(orgname);
	        Vector<Role> anonymousRoles = new Vector<Role>();
	        anonymousRoles.add(anonymousRole);
	        this.setFedoraRoles(anonymousRoles);
	        logger.info("User role in Fedora: "+anonymousRole.name);
	        
	        if (language != null) {
	            this.locale = language;
	        }
	        
	    } else if (!this.isInstanceAdmin()) {
	        this.setFedoraRoles(Osa.authManager.getUserRoles(dn, org.getName()));
	        for (Role role : this.getFedoraRoles()) {
	            logger.info("User role in Fedora: "+role.name);
	        }
	    }
	    
	    logger.info("User rolelevel: "+this.getHighestAccessRight());	   
	    logger.info("User locale: "+this.locale);
	    logger.info("User organization: "+this.organization.getName());
	}
	
	/**
     * @return the application role
     */
	public String getRole() {
	    int acRight = this.getHighestAccessRight();
	    String role = "";
	    
	    if (this.isInstanceAdmin()) {
	        role = "instance_admin";
	    } else if (acRight == AccessRight.ACCESSRIGHTLEVEL_ADMIN) {
	        role = "admin";
	    } else if (acRight == AccessRight.ACCESSRIGHTLEVEL_RECORDSMANAGEMENT) {
	        role = "manager";
	    } else {
	        role = "reader";
	    }
	    return role;
	}

	
	/**
	 * @return the dn
	 */
	public String getDn() {
		return dn;
	}
	/**
	 * @param dn the dn to set
	 */
	public void setDn(String dn) {
		this.dn = dn;
	}
	/**
	 * @return the cn
	 */
	public String getCn() {
		return cn;
	}
	/**
	 * @param cn the cn to set
	 */
	public void setCn(String cn) {
		this.cn = cn;
	}
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
	/**
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}
	/**
	 * @param uid the uid to set
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}
	/**
	 * @return the firstName
	 */
	public String getFirstName() {
		return firstName;
	}
	/**
	 * @param firstName the firstName to set
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	/**
	 * @return the lastName
	 */
	public String getLastName() {
		return lastName;
	}
	/**
	 * @param lastName the lastName to set
	 */
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the mail
	 */
	public String getMail() {
		return mail;
	}
	/**
	 * @param mail the mail to set
	 */
	public void setMail(String mail) {
		this.mail = mail;
	}
	/**
	 * @return the preferredLanguage
	 */
	public String getPreferredLanguage() {
		return preferredLanguage;
	}
	/**
	 * @param preferredLanguage the preferredLanguage to set
	 */
	public void setPreferredLanguage(String preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
	}
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	/**
	 * @return the employeeNumber
	 */
	public String getEmployeeNumber() {
		return employeeNumber;
	}
	/**
	 * @param employeeNumber the employeeNumber to set
	 */
	public void setEmployeeNumber(String employeeNumber) {
		this.employeeNumber = employeeNumber;
	}
	/**
	 * @return the jpegPhoto
	 */
	public String getJpegPhoto() {
		return jpegPhoto;
	}
	
	/**
	 * Get all access right filters from user roles
	 * @return
	 */
	public Vector< HashMap<String, String> > getAccessRightFilters() {
	    // Collect filters per role to vector
	    Vector< HashMap<String, String> > filters = new Vector< HashMap<String, String> >();
	    // vector to store paths, where access is denied
        Vector<String> nonAccessiblePaths = new Vector<String>();
        
        if (this.isInstanceAdmin()) {
            return filters;
        }
        
        // Check if access is denied
        for (Role role : this.getFedoraRoles()) {
            for (AccessRight acRight : role.getRoleAccessrights()) {
                int accessRightLevel = acRight.getAccessRightLevel();
                if (accessRightLevel == AccessRight.ACCESSRIGHTLEVEL_DENY_META) {
                    // access denied to this path
                    String accessRightPath = "("+acRight.getAccessRightPath()+"";
                    // replace '+' char with '*' for Solr. Query syntax: ("publicityLevel AND accessRightPath/"*)
                    if (accessRightPath.contains("+")) accessRightPath = accessRightPath.replace("+", "") + "*";
                    accessRightPath += ")";                    
                    nonAccessiblePaths.add(accessRightPath);
                }
            }
        }
        
        // Go through all roles
	    for (Role role : this.getFedoraRoles()) {
            // Go through all AccessRights of the Role
            for (AccessRight acRight : role.getRoleAccessrights()) {
                String publicityLevels = "(" + acRight.getPublicityLevels() + ")";
                String accessRightPath = "(" + acRight.getAccessRightPath() + "";
                // replace '+' char with '*' for Solr. Query syntax: ("publicityLevel AND accessRightPath/"*)
                if (accessRightPath.contains("+")) accessRightPath = accessRightPath.replace("+", "") + "*";
                accessRightPath += ")";
                int accessRightLevel = acRight.getAccessRightLevel();
                HashMap<String, String> filter = new HashMap<String, String>();

                if (accessRightLevel != AccessRight.ACCESSRIGHTLEVEL_DENY_META) {
                    filter.put("c.accessRights", publicityLevels);
                    filter.put("m.objectPath", accessRightPath);
                    
                    // add paths, where access is denied, to filter
                    for (String path : nonAccessiblePaths) {
                        filter.put("-m.objectPath", path);
                    }
                    filters.add(filter);
                }
            }
        }
        return filters;
    }
	
    /**
     * @param User's highest accessright level to set
     */
    public void setHighestAccessRight(int value) {
        this.highestAccessRight = value;
    }
    
	/**
	 * @return User's highest accessright
	 */
    public int getHighestAccessRight() {
        int value = 0;
        if (this.highestAccessRight == 0) {
            if (this.isInstanceAdmin()) {
                value = AccessRight.ACCESSRIGHTLEVEL_INSTANCEADMIN;
            } else  {
                for (Role role : this.getFedoraRoles()) {
                    for (AccessRight acRight : role.getRoleAccessrights()) {
                        if (acRight.getAccessRightLevel() > value) {
                            value = acRight.getAccessRightLevel();
                        }
                    }
                }
            }
            setHighestAccessRight(value);
        }
        return this.highestAccessRight;
    }
	
    /**
     * @return the roles determined to fedora
     */
    public Vector<Role> getFedoraRoles() {
    	return this.fedoraRoles;
    }
    /**
     * @param roles to the roles set
     */
    public void setFedoraRoles(Vector<Role> roles) {
    	this.fedoraRoles = roles;
    }
	/**
	 * @param jpegPhoto the jpegPhoto to set
	 */
	public void setJpegPhoto(String jpegPhoto) {
		this.jpegPhoto = jpegPhoto;
	}
	/**
	 * @return the organization
	 */
	public Organization getOrganization() {
		return organization;
	}
	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
	/**
     * @return group
     */
	public String getGroup() {
        return this.group;
    }
	/**
     * @param group the group to set
     */
	public void setGroup(String group) {
	    this.group = group;
	}
	/**
	 * @return the locale
	 */
	public Locale getLocale() {
		return locale;
	}
	/**
	 * @param locale the locale to set
	 */
	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public String toString() {    
        ReflectionToStringBuilder tsb = 
        	new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return tsb.toString();
    }

	public void setAnonymous(boolean anonymous) {
		this.anonymous = anonymous;
	}

	public boolean isAnonymous() {
		return anonymous;
	}
	
	public void setInstanceAdmin(boolean value) {
	    this.instance_admin = value;
	}
	
	public boolean isInstanceAdmin() {
        return this.instance_admin;
    }
	
	public boolean isAdmin() {
	    return this.getHighestAccessRight() >= AccessRight.ACCESSRIGHTLEVEL_ADMIN;
    }

}
