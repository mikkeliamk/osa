package fi.mamk.osa.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Map;
import java.util.Vector;
import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import org.apache.log4j.Logger;
import fi.mamk.osa.core.Osa;
import flexjson.JSONDeserializer;

/**
 * Class to check passwords and other information about users in the LDAP.
 *
 */
public class LdapManager extends AuthenticationManager {
    
    private static final Logger logger = Logger.getLogger(LdapManager.class);
    public static enum EntryType {
    	USER,
    	ROLE
    }
    
    // Ldap attributes for organization
    public static final String ORG_NAME        = "o";
    public static final String ORG_VISIBLENAME = "displayName";
    public static final String ORG_DESC        = "description";
    // Ldap attributes for group
    public static final String GROUP_NAME = "ou";
    // Ldap attributes for person
    public static final String PERSON_FIRSTNAME      = "givenName";
    public static final String PERSON_LASTNAME       = "sn";
    public static final String PERSON_COMMONNAME     = "cn";
    public static final String PERSON_PASSWORD       = "userPassword";
    public static final String PERSON_EMAIL          = "mail";
    public static final String PERSON_LANGUAGE       = "preferredLanguage";
    // Ldap attributes for role
    public static final String ROLE_NAME             = "cn";
    public static final String ROLE_ACCESSRIGHTPATH  = "destinationIndicator";
    public static final String ROLE_ACCESSRIGHTLEVEL = "l";
    public static final String ROLE_ACCESSRIGHTPUBLICITYLEVEL = "ou";
    public static final String ROLE_USER             = "roleOccupant";
    
    /** LDAPManager status */
    public boolean status = false;
    
    /** Host:port where connected to */
    private String host;

    /** connection to LDAP */
    private DirContext context;
    private String admindn;
    private String adminpw;
    // domain components (dc=xx,dc=yy)
    private String domainComponents;
    // dn for instance admin
    private String instanceAdminDn;
    
    /**
     * Constructor
     */
    public LdapManager(String host, String AdminDn, String Adminpw, String baseDn, String adminDn) {
        this.host = host;
        this.admindn = AdminDn;
        this.adminpw = Adminpw;
        setDomainComponents(baseDn);
        setInstanceAdminDn(adminDn);
        context = getContext();
        // save host if connected
        if (context != null) {
            this.status = true;
            
            try {
                context.close();
            } catch (NamingException e) {
                logger.error("LdapManager initialization error.");
            }
        }
    }
    
    public static String hash256(String data) {
    	MessageDigest md = null;
    	
    	try {
    		
    		md = MessageDigest.getInstance("SHA-256");
    		md.update(data.getBytes());
    	
    	} catch (NoSuchAlgorithmException e){
    		logger.error("LdapManager error: Hashing error");
    	}
    	
    	return bytesToHex(md.digest());
    }
    
    public static String bytesToHex(byte[] bytes) {
    	StringBuffer result = new StringBuffer();
    	for (byte byt : bytes) result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
    	return result.toString();
    }
    
    public String getDefaultDn(String defaultOrg) {
        return "o="+defaultOrg+","+getDomainComponents();
    }
    
    /**
     * Check user
     * @param username      email-address
     * @param password      password
     * @return
     */
    public String isValidUser(String username, String password, String sessionid) {
        
    	DirContext authContext = null;
        boolean status = false;
        String usrdn = "";
        String cnValue = "";
        String hashedPw = hash256(password);
        String retValue = "";
        
        try {
        	authContext = getContext();
        	SearchControls ctls = new SearchControls();
        	
        	// Check e-mail and password
        	String filter = "(&(mail="+username+")(userPassword="+hashedPw+"))";
        	
          	NamingEnumeration<SearchResult> answer3 = authContext.search(getDomainComponents(), filter, ctls);
           	if (answer3.hasMore()) {
           	    status = true;

           	    SearchResult result = answer3.next();
                cnValue = result.getAttributes().get("cn").get().toString();
                usrdn = "cn="+cnValue+","+getDomainComponents();
           	}
           	
        	NamingEnumeration<NameClassPair> e = authContext.list(getDomainComponents());
        	while (e.hasMore()) {
        		NameClassPair nc = e.next();
        		String organization = nc.getName();
        		
              	NamingEnumeration<SearchResult> answer = authContext.search(organization+","+getDomainComponents(), filter, ctls);
	           	if (answer.hasMore()) {
	           	    status = true;
	           	    
                    SearchResult result = answer.next();
                    cnValue = result.getAttributes().get("cn").get().toString();
                    usrdn = "cn="+cnValue+","+organization+","+getDomainComponents();
	           	}
	           	
        		NamingEnumeration<NameClassPair> ae = authContext.list(organization +","+getDomainComponents());
        		while (ae.hasMore()) {
                    NameClassPair ne = ae.next();
                    String group = ne.getName();
                    NamingEnumeration<SearchResult> answer2 = authContext.search(group+","+organization+","+getDomainComponents(), filter, ctls);
                    if (answer2.hasMore()) {
                        status = true;
                        
                        SearchResult result = answer2.next();
                        cnValue = result.getAttributes().get("cn").get().toString();
                        usrdn = "cn="+cnValue+","+group+","+organization+","+getDomainComponents();
                    }
        		}        		
        	}
        	
        	authContext.close();
        	
        } catch (Exception e) {
        	logger.error("LdapManager error: user check failed, "+e);
        }
        
        if (usrdn != "") {
            // check if user already logged in        
            status = Osa.dbManager.get("sql").setCurrentuser(usrdn, sessionid);
            if (status == false) {
                retValue = "alreadyloggedin;"+usrdn;
            }
            
        } else {
            retValue = "notfound";
        }
        
        if (status) {
        	return usrdn;      	
        } else {
        	return retValue;
        }
        
    }
    
    /**
     * Get the directory service interface, containing methods for examining and 
     * updating attributes associated with objects, 
     * and for searching the directory. 
     * @return
     */
    public DirContext getContext() {
        DirContext authContext = null;
        try {
        	
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            props.put(Context.PROVIDER_URL, this.host);
            props.put(Context.SECURITY_AUTHENTICATION, "simple");
            props.put(Context.SECURITY_PRINCIPAL, this.admindn);
            props.put(Context.SECURITY_CREDENTIALS, this.adminpw);
                
            authContext = new InitialDirContext(props);
            
        } catch (Exception e) {
            logger.error("LdapManager error: "+e);
        }
        
        return authContext;
    	
    }
    
    /**
     * Creates new user and adds user to specified roles
     */
    public boolean createNewUser(String fname, 
                                 String lname, 
                                 String mail, 
                                 String lang, 
                                 String pw, 
                                 String userOrganization, 
                                 String userGroup, 
                                 Vector<String> fedoraRoles) {
    	
    	DirContext context = null;
    	boolean status = false;
    	
    	String newdn = "cn="+fname+" "+lname+",ou="+userGroup+",o="+userOrganization+","+getDomainComponents();
    	
    	Attribute cn = new BasicAttribute(LdapManager.PERSON_COMMONNAME, fname+" "+lname);
    	Attribute gn = new BasicAttribute(LdapManager.PERSON_FIRSTNAME, fname);
    	Attribute sn = new BasicAttribute(LdapManager.PERSON_LASTNAME, lname);
    	Attribute email = new BasicAttribute(LdapManager.PERSON_EMAIL, mail);
    	Attribute preferredLanguage = new BasicAttribute(LdapManager.PERSON_LANGUAGE, lang);
    	Attribute userPassword = new BasicAttribute(LdapManager.PERSON_PASSWORD, hash256(pw));
    	
    	Attribute oc = new BasicAttribute("objectClass");
    	oc.add("person");
    	oc.add("inetOrgPerson");
    	    	
    	try {
    		context = getContext();
    		Attributes entry = new BasicAttributes();
    		entry.put(cn);
    		entry.put(gn);
    		entry.put(sn);
    		entry.put(email);
    		entry.put(preferredLanguage);
    		entry.put(userPassword);
    		entry.put(oc);
    		
    		context.createSubcontext(newdn, entry);
    		logger.debug("LdapManager: Created user: " + newdn);
    		
    		// Add default role to user (access to public data)
    		addUserToRole(newdn, userOrganization, Role.ROLE_PUBLIC);
    		
    		// Add fedora role(s) to user
    		for (String fedoraRole : fedoraRoles) {
    			if (fedoraRole != null) { // Null-check in case vector has null indexes
    				addUserToRole(newdn, userOrganization, fedoraRole);
    			}
    		}
    		context.close();
    		
    	} catch (NamingException e) {
    		logger.error("LdapManager error: Error creating user, "+e);
    		return status;
    	}

    	status = true;
		return status;
    }
    
    /**
     * Updates user related data
     */
    public boolean updateUser(String usrDn, 
                              String organization, 
                              String userGroup, 
                              Vector<String> fedoraRoles) {
        
        DirContext context = null;
        String newDn = null;
        
        try {
            
            context = getContext();
            
            // get userGroup
            String currentUserGroup = "";
            String[] parts = usrDn.split(",");
            for (int i=0; i<parts.length; i++) {
                if (parts[i].startsWith("ou=")) {
                    currentUserGroup = parts[i].replaceFirst("ou=", "");
                    break;
                }
            }
            // check userGroup if changed
            if (!currentUserGroup.equals(userGroup)) {
                newDn = usrDn.replace("ou="+currentUserGroup, "ou="+userGroup);
                // change dn 
                context.rename(usrDn, newDn);
            }
            context.close();
            
            // Handle fedora roles, remove old roles
            this.removeUserFromRole(usrDn);
            
            // use newDn if changed
            if (newDn != null) {
                usrDn = newDn;
            }
            
            // Handle fedora roles, add modified
            for (String fedoraRole : fedoraRoles) {
                addUserToRole(usrDn, organization, fedoraRole);
            }
            
            context.close();
            
        } catch (NamingException e) {
            logger.error("LdapManager error: Error updating user, "+e);
            return false;
        }
        return true;
    }
    
    /**
     * Function for instance_admin to reset user password 
     */
    public boolean resetUserPassword(String usrDn, String pw) {
        DirContext context = null;
        
        try {
            context = getContext();
            Attribute userPassword = new BasicAttribute(LdapManager.PERSON_PASSWORD, hash256(pw));
            ModificationItem[] mods = new ModificationItem[1];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, userPassword);
            context.modifyAttributes(usrDn, mods);
            logger.info("LdapManager info: User '"+usrDn+"' password reseted by instance_admin.");
            context.close();
            
        } catch (NamingException e) {
            logger.error("LdapManager error: Error reseting user password, "+e);
            return false;
        }
        
        return true;
    }
    
    /**
     * Creates new group to the organization
     */
    public boolean createGroup(String groupName, String groupOrganization) {
    	
    	DirContext context = null;
    	boolean status = false;
    	
    	String newdn = "ou="+ groupName +",o="+ groupOrganization + "," + getDomainComponents();
    	
    	Attribute ou = new BasicAttribute(LdapManager.GROUP_NAME, groupName);
    	Attribute oc = new BasicAttribute("objectClass");
    	oc.add("organizationalUnit");
    	
    	try {
    		
    		context = getContext();
    		Attributes entry = new BasicAttributes();
    		entry.put(ou);
    		entry.put(oc);
    		
    		context.createSubcontext(newdn, entry);
    		logger.debug("LdapManager: Created group: "+newdn);
    		context.close();
    		
    	} catch (NamingException e) {
    		logger.error("LdapManager error: Error creating group, "+e);
    		return status;
    	}
    	
    	status = true;
		return status;
    	
    }
    
    /**
     * Creates new organization
     */
    public boolean createOrganization(String oName, String oDesc, String confFile, String afName, String alName, String aMail, String contact, String displayName) {
    	
    	DirContext context = null;
    	boolean status = false;
    	Attribute description = null;
    	Attribute dN = null;
    	
    	String newdn = "o=" + oName + ","+ getDomainComponents();
    	
    	Attribute o = new BasicAttribute(LdapManager.ORG_NAME, oName);
    	if (oDesc != null) {
    	    description = new BasicAttribute(LdapManager.ORG_DESC, oDesc);
    	}
    	if (displayName != null) {
    	    dN = new BasicAttribute(LdapManager.ORG_VISIBLENAME, displayName);
    	}
    	Attribute oc = new BasicAttribute("objectClass");
    	oc.add("OpenLDAPorg");
    	
    	try {
    		
    		context = getContext();
    		Attributes entry = new BasicAttributes();
    		entry.put(o);
    		if (dN != null) {
    		    entry.put(dN);
    		}
    		if (description != null) {
    		    entry.put(description);
    		}
    		entry.put(oc);
    		context.createSubcontext(newdn, entry);
    		logger.debug("LdapManager: Created organization: "+newdn);
    		context.close();
    		
    	} catch (NamingException e) {
    		logger.error("LdapManager error: Error creating organization, "+e);
    		return status;
    	}
    	
    	status = true;
		return status;
    	
    }

    /**
     * Creates role to selected organization
     * @param roleName              role name
     * @param roleOrganization      role organization
     * @param roleAccessRightLevel  public/restricted/confidential
     * @param roleDirectories       path to directory or document, where role has in level roleAccessRightLevel
     */
    public boolean createRole(String roleName, String roleOrganization, Vector<AccessRight> accessrights) {
        
        DirContext context = null;
        boolean status = false;
        String newdn = "cn="+roleName+",o="+roleOrganization+","+getDomainComponents();
        
        try {

        	Attributes entry = new BasicAttributes();
            Attribute oc = new BasicAttribute("objectClass", "organizationalRole");
            Attribute cn = new BasicAttribute(LdapManager.ROLE_NAME, roleName);
            Attribute ou = new BasicAttribute(LdapManager.ROLE_ACCESSRIGHTPUBLICITYLEVEL);
            Attribute l = new BasicAttribute(LdapManager.ROLE_ACCESSRIGHTLEVEL);
            Attribute di = new BasicAttribute(LdapManager.ROLE_ACCESSRIGHTPATH);
            entry.put(oc);
            entry.put(cn);
            int i=0;
            
            for (AccessRight accessRight : accessrights) {
            	if (accessRight != null) {
	                String premark = Integer.toString(i)+"-";
	            	ou.add(premark+accessRight.getPublicityLevel());
	            	l.add(premark+Integer.toString(accessRight.getAccessRightLevel()));
	            	di.add(premark+accessRight.getAccessRightPath());
	            	entry.put(ou);
	            	entry.put(l);
	                entry.put(di);
	                i++;
            	}
            }
            
            context = getContext();
            context.createSubcontext(newdn, entry);
            context.close();
            logger.debug("LdapManager: Created role: "+newdn);
            
        } catch (NamingException e) {
            logger.error("LdapManager error: Error creating role, "+e);
            return status;
        }
        
        status = true;
        return status;
    }
    
    /**
     * Updates role related data
     */
    public boolean updateRole(String roleDn, String roleName, Vector<AccessRight> accessrights) {
        
        //role: cn=name,o=organization,dc=nn,dc=fi
        DirContext context = null;
        String newDn = null;
        
        try {
            
            context = getContext();
            
            // get name
            String currentRoleName = "";
            String[] parts = roleDn.split(",");
            for (int i=0; i<parts.length; i++) {
                if (parts[i].startsWith("cn=")) {
                    currentRoleName = parts[i].replaceFirst("cn=", "");
                    break;
                }
            }
            
            // modify role, remove accessrights
            ModificationItem[] mods = new ModificationItem[3];
            mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(LdapManager.ROLE_ACCESSRIGHTPUBLICITYLEVEL));
            mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(LdapManager.ROLE_ACCESSRIGHTLEVEL));
            mods[2] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(LdapManager.ROLE_ACCESSRIGHTPATH));
            context.modifyAttributes(roleDn, mods);

            // check if roleName changed
            if (roleName != null && !currentRoleName.equals(roleName)) {
                newDn = roleDn.replace("cn="+currentRoleName, "cn="+roleName);
                // change dn 
                context.rename(roleDn, newDn);
                roleDn = newDn;
            }
            
            // modify role, add new accessrights
            int i=0;
            
            for (AccessRight accessRight : accessrights) {
                mods = new ModificationItem[3];
                if (accessRight != null) {
                    String premark = Integer.toString(i)+"-";
                    String publicityLevel = premark+accessRight.getPublicityLevel();
                    String acLevel = premark+Integer.toString(accessRight.getAccessRightLevel());
                    String di = premark+accessRight.getAccessRightPath();
                    
                    mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(LdapManager.ROLE_ACCESSRIGHTPUBLICITYLEVEL, publicityLevel));
                    mods[1] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(LdapManager.ROLE_ACCESSRIGHTLEVEL, acLevel));
                    mods[2] = new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(LdapManager.ROLE_ACCESSRIGHTPATH, di));
                    
                    context.modifyAttributes(roleDn, mods);
                    i++;
                }
            }

            logger.debug("LdapManager:role '"+roleDn+"' updated.");
            context.close();
            
        } catch (NamingException e) {
            logger.error("LdapManager error: Error updating role, "+e);
            return false;
        }
        
        return true;
    }
    
    public Vector<String> getOrganizations() {
    	
    	DirContext context = null;
    	Vector<String> values = new Vector<String>();
    	
    	try {
    		context = getContext();
            SearchControls ctls = new SearchControls();
            String[] attrIDs = {"o"};
            ctls.setReturningAttributes(attrIDs);
            ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);

            NamingEnumeration<SearchResult> answer = context.search( getDomainComponents(), "(objectclass=OpenLDAPorg)",ctls );
            while (answer.hasMore()) {
                SearchResult rslt = answer.next();
                Attributes attrs = rslt.getAttributes();
                
                if (attrs.get("o") != null) {
                	String test = attrs.get("o").toString();
                	test = test.replace("o: ","");
                	values.addElement(test);
                }
            }
            
            context.close();

        } catch (NamingException e) {
        	logger.error("LdapManager error: Error searching organizations, "+e);
        }
    	
    	return values;
    }
    
    public Vector<String> getGroups(String organization) {
    	
    	Vector<String> values = new Vector<String>();
    	if (organization == null) {
    		return values;
    	}
    	DirContext context = null;
    	String usersContainer = "o="+ organization +","+getDomainComponents();
    	
    	try {
    		context = getContext();
            SearchControls ctls = new SearchControls();
            String[] attrIDs = {"ou"};
            ctls.setReturningAttributes(attrIDs);
            ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);

            NamingEnumeration<SearchResult> answer = context.search( usersContainer, "(objectclass=organizationalUnit)",ctls );
            while (answer.hasMore()) {
                SearchResult rslt = answer.next();
                Attributes attrs = rslt.getAttributes();
                
                if (attrs.get("ou") != null) {
                	String test = attrs.get("ou").toString();
                	test = test.replace("ou: ","");
                	values.addElement(test);
                }
            }
            
            context.close();

        } catch (NamingException e) {
        	logger.error("LdapManager error: Error searching organizations, "+e);
        }
    	
    	return values;
    }
    
    /**
     * Returns all users for organization
     */
    public List<User> getUsers(String organization) {
        List<User> users = new ArrayList<User>();
        DirContext context = null;

        if (organization == null || organization == "") {
            return users;
        }

        String GROUP_CTX = "o="+organization+","+getDomainComponents();
        String ORGANIZATION_ROLES_QUERY = "(&(objectClass=organizationalUnit))";
        String ORGANIZATION_USERS_QUERY = "(&(objectClass=inetOrgPerson))";
        User user = null;
        String group = "";
        SearchControls ctls = new SearchControls();
        
        try {
            context = getContext();
            NamingEnumeration<SearchResult> groupResults = context.search(GROUP_CTX, ORGANIZATION_ROLES_QUERY, ctls);
            // groups
            while (groupResults.hasMore()) {
                SearchResult result = groupResults.next();
                group = result.getAttributes().get("ou").get().toString();
                String USER_CTX = "ou="+group+","+GROUP_CTX;
                NamingEnumeration<SearchResult> userResults = context.search(USER_CTX, ORGANIZATION_USERS_QUERY, ctls);
                // users
                while (userResults.hasMore()) {
                    SearchResult userResult = userResults.next();
                    user = new User();
                    user.setGroup(group);
                    
                    if (userResult.getAttributes().get(LdapManager.PERSON_COMMONNAME) != null) {
                        user.setCn(userResult.getAttributes().get(LdapManager.PERSON_COMMONNAME).get().toString());
                        user.setDn("cn="+user.getCn()+","+USER_CTX);
                    }
                    if (userResult.getAttributes().get(LdapManager.PERSON_FIRSTNAME) != null) {
                        user.setFirstName(userResult.getAttributes().get(LdapManager.PERSON_FIRSTNAME).get().toString());
                    }
                    if (userResult.getAttributes().get(LdapManager.PERSON_LASTNAME) != null) {
                        user.setLastName(userResult.getAttributes().get(LdapManager.PERSON_LASTNAME).get().toString());
                    }
                    if (userResult.getAttributes().get(LdapManager.PERSON_EMAIL) != null) {
                        user.setMail(userResult.getAttributes().get(LdapManager.PERSON_EMAIL).get().toString());
                    }
                    if (userResult.getAttributes().get(LdapManager.PERSON_LANGUAGE) != null) {
                        user.setPreferredLanguage(userResult.getAttributes().get(LdapManager.PERSON_LANGUAGE).get().toString());
                    }
                    users.add(user);
                    
                }
                userResults.close();
            }
            groupResults.close();
            
            // Find Fedora roles from ldap and set roles to users
            for (User currentUser : users) {
                currentUser.setFedoraRoles(Osa.authManager.getUserRoles(currentUser.getDn(), organization));
            }
            
            context.close();
            
        } catch (NamingException e) {
            logger.error("LdapManager error: User query failed, "+e);
        }
        
        return users;
    }
    
    /** Get user's info from LDAP
     * 
     * @param dn	User dn
     * @return		User object
     */
    public User getUser(String userDn) {
    	DirContext context 	= null;
    	User userInfo 		= new User();
    	String dn 			= "";
    	String org			= "";
    	String ou			= "";
    	
    	if (!userDn.endsWith(getDomainComponents())) {
    		dn = userDn +","+ getDomainComponents();
    	} else {
    		dn = userDn;
    	}
    	
    	try {
    		String[] parts = dn.split(",");
    		for (int i = 0; i < parts.length; i++) {
    			if (parts[i].startsWith("ou=")) {
    				ou = parts[i].split("=")[1];
    			}
    			if (parts[i].startsWith("o=")) {
    				org = parts[i].split("=")[1];
    			}
    		}
    		userInfo.setDn(dn);
    		userInfo.setGroup(ou);
    		
    		context = getContext();
    		
    		Attributes attrs = context.getAttributes(dn);
    		NamingEnumeration<?> i = attrs.getAll();
    		while (i.hasMore()) {
    			Attribute attr = (Attribute) i.next();
    			String values = "";
    			
    			NamingEnumeration<?> e = attr.getAll();

    			while (e != null && e.hasMore()) {
    			    String value = e.next().toString();
    			    if (attr.getID().equals(LdapManager.ROLE_ACCESSRIGHTLEVEL) 
    			        || attr.getID().equals(LdapManager.ROLE_ACCESSRIGHTPATH) 
    			        || attr.getID().equals(LdapManager.ROLE_ACCESSRIGHTPUBLICITYLEVEL)) {

    			        if (value.contains("-")) {
    			            value = value.substring(value.indexOf("-")+1);
    			        }
    			    } 
    			    
    			    if (values == "") {
    			        values += value;
    			    } else {
    			        // separate values
    			        values = values+";"+value;
    			    }
    			}
    			
    			if (LdapManager.PERSON_COMMONNAME.equals(attr.getID())) {
    				if (!values.isEmpty()) {userInfo.setCn(values);}
    			}
    			if (LdapManager.PERSON_FIRSTNAME.equals(attr.getID())) {
    				if (!values.isEmpty()) {userInfo.setFirstName(values);}
    			}
    			if (LdapManager.PERSON_LASTNAME.equals(attr.getID())) {
    				if (!values.isEmpty()) {userInfo.setLastName(values);}
    			}
    			if (LdapManager.PERSON_EMAIL.equals(attr.getID())) {
    				if (!values.isEmpty()) {userInfo.setMail(values);}
    			}
    			if (LdapManager.PERSON_LANGUAGE.equals(attr.getID())) {
    				if (!values.isEmpty()) {userInfo.setPreferredLanguage(values);}
    			}
    		}
    		
    		userInfo.setFedoraRoles(Osa.authManager.getUserRoles(userInfo.getDn(), org));
    		context.close();
    		
    	} catch (Exception e) {
            logger.error("LdapManager error: user query failed, "+e);
    	}
    	
    	return userInfo;
    }
    
    /**
     * Returns public role (f.ex. anonymous user)
     */
    public Role getPublicRole(String organization) {
        Role role = getRole(Role.ROLE_PUBLIC, organization);
        return role;
    }
    
    /**
     * Returns all roles for organization
     */
    public List<Role> getRoles(String organization) {
        List<Role> roles = new ArrayList<Role>();
        DirContext context = getContext();
        SearchControls ctls = new SearchControls();
        Role role = null;

        if (organization == null || organization == "") {
            return roles;
        }
        
        String CURRENT_CTX = "o="+organization+","+getDomainComponents();
        String ORGANIZATION_ROLE_QUERY = "(&(objectClass=organizationalRole))";
        
        try {
            NamingEnumeration<SearchResult> results = context.search(CURRENT_CTX, ORGANIZATION_ROLE_QUERY, ctls);
            while (results.hasMore()) {
                role = new Role();
                int groups = 0;
                int roleOccupants = 0;
                
                SearchResult result = results.next();
                
                if (result.getAttributes().get(LdapManager.ROLE_NAME) != null) {
                    role.setName(result.getAttributes().get(LdapManager.ROLE_NAME).get().toString());
                }
                
                // Handle several attribute-groups (publicityLevel-accessRightLevel-destination)
                if (result.getAttributes().get(LdapManager.ROLE_ACCESSRIGHTPUBLICITYLEVEL) != null) {
                    groups = result.getAttributes().get(LdapManager.ROLE_ACCESSRIGHTPUBLICITYLEVEL).size();
                }
                
                for (int i=0; i<groups; i++) {
                    String premark = Integer.toString(i)+"-";
                    String publicityLevel = result.getAttributes().get(LdapManager.ROLE_ACCESSRIGHTPUBLICITYLEVEL).get(i).toString();
                    String accessRightLevel = result.getAttributes().get(LdapManager.ROLE_ACCESSRIGHTLEVEL).get(i).toString();
                    String destinationIndicator = result.getAttributes().get(LdapManager.ROLE_ACCESSRIGHTPATH).get(i).toString();
                    
                    publicityLevel = publicityLevel.replaceFirst(premark, "");
                    accessRightLevel = accessRightLevel.replaceFirst(premark, "");
                    destinationIndicator = destinationIndicator.replaceFirst(premark, "");
                    
                    AccessRight right = new AccessRight(publicityLevel, Integer.parseInt(accessRightLevel), destinationIndicator);
                    role.setRoleAccessright(right);
                }
                
                if (result.getAttributes().get(LdapManager.ROLE_USER) != null) {
                    roleOccupants = result.getAttributes().get(LdapManager.ROLE_USER).size();
                }

                for (int i=0; i<roleOccupants; i++) {
                    // userdns, who has this role
                    role.setRoleOccupant(result.getAttributes().get(LdapManager.ROLE_USER).get(i).toString());
                }
                
                roles.add(role);
            }
            results.close();
            context.close();
            
        } catch (NamingException e) {
            logger.error("LdapManager error: Role query failed, "+e);
        }
        
        return roles;
    }
    
    public HashMap<String,String> getAttributes(String objectdn) {
    	DirContext context = null;
    	HashMap<String,String> datatable = new HashMap<String,String>();
    	
    	String dn = "";
    	
    	if (!objectdn.endsWith(getDomainComponents())) {
    		dn = objectdn +","+ getDomainComponents();
    	} else {
    		dn = objectdn;
    	}
    	
    	try {

    		context = getContext();    		
    		Attributes attrs = context.getAttributes(dn);
    		
    		NamingEnumeration<?> i = attrs.getAll();
    		
    		while (i.hasMore()) {
    			Attribute attr = (Attribute) i.next();
    			String values = "";
    			NamingEnumeration<?> e = attr.getAll();

    			while (e != null && e.hasMore()) {
    			    String value = e.next().toString();
    			    
    			    if (attr.getID().equals(LdapManager.ROLE_ACCESSRIGHTLEVEL) 
    			        || attr.getID().equals(LdapManager.ROLE_ACCESSRIGHTPATH) 
    			        || attr.getID().equals(LdapManager.ROLE_ACCESSRIGHTPUBLICITYLEVEL)) {

    			        if (value.contains("-")) {
    			            value = value.substring(value.indexOf("-")+1);
    			        }
    			    } 
    			    
    			    if (values == "") {
    			        values += value;
    			    } else {
    			        // separate values
    			        values = values+";"+value;
    			    }
    			}
    			datatable.put(attr.getID(), values);
    		}
    		
    		context.close();
    		
    	} catch (Exception e) {
            logger.error("LdapManager error: attributes not found for "+dn+", "+e);
    	}
    	
    	return datatable;
    	
    }
    
    public Vector<String> getOrganizationRoles(String organization) {
        
        if (organization == null) {
            return null;
        }
        
        String CURRENT_CTX = "o="+organization+","+getDomainComponents();
        String ORGANIZATION_ROLE_QUERY = "(&(objectClass=organizationalRole))";
        DirContext context = getContext();
        SearchControls ctls = new SearchControls();
        Vector<String> roles = new Vector<String>();
        
        try {
            
            NamingEnumeration<SearchResult> results = context.search(CURRENT_CTX, ORGANIZATION_ROLE_QUERY, ctls);
            while (results.hasMore()) {
                SearchResult result = results.next();
                String roleName = result.getAttributes().get(LdapManager.ROLE_NAME).get().toString();
                roles.add(roleName);
            }
            results.close();
            context.close();
            
        } catch(NamingException e) {
            logger.error("LdapManager error: Role query failed, "+e);
        }

        return roles;
    }
    
    /**
     * Get role details
     */
    public Role getRole(String roleName, String organization) {
        if (organization == null) {
            return null;
        }
        
        String CURRENT_CTX 	= "o="+organization+","+getDomainComponents();
        String ROLE_QUERY 	= "(&(objectClass=organizationalRole)(cn="+roleName+"))";
        DirContext context 	= getContext();
        SearchControls ctls = new SearchControls();
        Role role 			= null;
        
        try {
            
            NamingEnumeration<SearchResult> results = context.search(CURRENT_CTX, ROLE_QUERY, ctls);
            while (results.hasMore()) {
                SearchResult result = results.next();
                String name = result.getAttributes().get(LdapManager.ROLE_NAME).get().toString();
                role = new Role(name);
                // Handle several attribute-groups (publicityLevel-accessRightLevel-destination)
                int groups = 0;
                int roleOccupants = 0;
                
                if (result.getAttributes().get(LdapManager.ROLE_ACCESSRIGHTPUBLICITYLEVEL) != null) {
                    groups = result.getAttributes().get(LdapManager.ROLE_ACCESSRIGHTPUBLICITYLEVEL).size();
                }
                
                for (int i=0; i<groups; i++) {
                    String premark = Integer.toString(i)+"-";
                    String publicityLevel = result.getAttributes().get(LdapManager.ROLE_ACCESSRIGHTPUBLICITYLEVEL).get(i).toString();
                    String accessRightLevel = result.getAttributes().get(LdapManager.ROLE_ACCESSRIGHTLEVEL).get(i).toString();
                    String destinationIndicator = result.getAttributes().get(LdapManager.ROLE_ACCESSRIGHTPATH).get(i).toString();
                    
                    publicityLevel = publicityLevel.replaceFirst(premark, "");
                    accessRightLevel = accessRightLevel.replaceFirst(premark, "");
                    destinationIndicator = destinationIndicator.replaceFirst(premark, "");
                    
                    AccessRight right = new AccessRight(publicityLevel, Integer.parseInt(accessRightLevel), destinationIndicator);
                    role.setRoleAccessright(right);
                }
                
                if (result.getAttributes().get(LdapManager.ROLE_USER) != null) {
                    roleOccupants = result.getAttributes().get(LdapManager.ROLE_USER).size();
                }

                for (int i=0; i<roleOccupants; i++) {
                    // userdns, who has this role
                    role.setRoleOccupant(result.getAttributes().get(LdapManager.ROLE_USER).get(i).toString());
                }
            }
            results.close();
            context.close();
            
        } catch(NamingException e) {
            logger.error("LdapManager error: Role query failed, "+e);
        }
        
        return role;
    }
    
    /**
     * Get roles for user according to dn and organization name
     * @param user          user dn (f.ex. "cn=name,ou=group,o=organization,dc=nn,dc=fi")
     * @param organization  name of user organization
     */
    public Vector<Role> getUserRoles(String user, String organization) {
        if (organization == null) {
            return null;
        }
        
        String CURRENT_CTX 		= "o="+organization+","+getDomainComponents();        
        String USER_ROLE_QUERY 	= "(&(objectClass=organizationalRole)(roleOccupant={0}))";
        DirContext context 		= getContext();
        SearchControls ctls 	= new SearchControls();
        Vector<Role> roles 		= new Vector<Role>();
        
        try {
            
            NamingEnumeration<SearchResult> results = context.search(CURRENT_CTX, USER_ROLE_QUERY, new Object[] { user }, ctls);
            while (results.hasMore()) {
                SearchResult result = results.next();
                String name = result.getAttributes().get(LdapManager.ROLE_NAME).get().toString();
                Role role = new Role(name);
                // Handle several attribute-groups (publicityLevel-accessRightLevel-destination)
                int groups = result.getAttributes().get(LdapManager.ROLE_ACCESSRIGHTPUBLICITYLEVEL).size();
                
                for (int i=0; i<groups; i++) {
                    String premark = Integer.toString(i)+"-";
                    String publicityLevel = result.getAttributes().get(LdapManager.ROLE_ACCESSRIGHTPUBLICITYLEVEL).get(i).toString();
                    String accessRightLevel = result.getAttributes().get(LdapManager.ROLE_ACCESSRIGHTLEVEL).get(i).toString();
                    String destinationIndicator = result.getAttributes().get(LdapManager.ROLE_ACCESSRIGHTPATH).get(i).toString();
                    publicityLevel = publicityLevel.replaceFirst(premark, "");
                    accessRightLevel = accessRightLevel.replaceFirst(premark, "");
                    destinationIndicator = destinationIndicator.replaceFirst(premark, "");
                    
                    AccessRight right = new AccessRight(publicityLevel, Integer.parseInt(accessRightLevel), destinationIndicator);
                    role.setRoleAccessright(right);
                }
                
                roles.add(role);
            }
            results.close();
            context.close();
            
        } catch (NamingException e) {
            logger.error("LdapManager error: Role query for user failed, "+e);
        }

        return roles;
    }
    
    /**
     * Add user to ldap role
     * @param userdn
     * @param organization
     * @param role
     */
    public void addUserToRole(String userdn, String organization, String role) {
        
        DirContext context = null;
        //role: cn=name,o=organization,dc=nn,dc=fi
        String ROLE_CTX = "cn="+role+",o="+organization+","+getDomainComponents();
        
        if (role.equals(Role.ROLE_PUBLIC)) {
            Vector<String> orgRoles = getOrganizationRoles(organization);
            // check if public role exists
            if (orgRoles == null || orgRoles.isEmpty() || !orgRoles.contains(Role.ROLE_PUBLIC)) {
                // create public role
                this.createRole(Role.ROLE_PUBLIC, organization, Role.getPublicRoleAccessrights(organization));
                logger.info("LdapManager: "+Role.ROLE_PUBLIC+" role created");
            }
        }
        
        try {
            
            context = getContext();
            
            ModificationItem[] mods = new ModificationItem[1];
            //user: cn=name,ou=group,o=organization,dc=nn,dc=fi
            Attribute mod0 = new BasicAttribute(LdapManager.ROLE_USER, userdn);
            mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, mod0);
            
            context.modifyAttributes(ROLE_CTX, mods);
            logger.debug("LdapManager: role '"+role+"' modified.");
            context.close();
                        
        } catch (NamingException e) {
            logger.error("LdapManager error: Error adding user to role, "+e);
        }
    }
    
    public void removeUserFromRole(String userdn) {
        try {
            context = getContext();

            String organization = "";
            // get organization
            String[] parts = userdn.split(",");
            for (int i=0; i<parts.length; i++) {
                if (parts[i].startsWith("o=")) {
                    organization = parts[i].substring(2, parts[i].length());
                    break;
                }
            }
            
            Vector<Role> roles = getUserRoles(userdn, organization);
            
            for (int i=0; i<roles.size(); i++) {
                String roleName = roles.get(i).name;
                String role_ctx = "cn="+roleName+",o="+organization+","+getDomainComponents();
                
                ModificationItem[] mods = new ModificationItem[1];
                Attribute mod0 = new BasicAttribute(LdapManager.ROLE_USER, userdn);
                mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, mod0);

                context.modifyAttributes(role_ctx, mods);
            }
            context.close();
            
        } catch (NamingException e) {
            logger.error("LdapManager error: Error removing user from role, "+e);
        }
    }
    
    public String modifyAttributes(String dn, String jsonMod) {
    	
    	String usrDn = "";
    	
    	if (!dn.endsWith(getDomainComponents())) {
    		usrDn = dn +","+ getDomainComponents();
    	} else {
    		usrDn = dn;
    	}
    	String newDn = usrDn;
    	
    	DirContext context = null;
    	HashMap values = (HashMap) new JSONDeserializer().deserialize(jsonMod);
    	HashMap modifiers = new HashMap();
    	
    	Iterator iterEntries = values.entrySet().iterator();
    	int i = 0;
    	
    	try {
    		context = getContext();
    		
	    	while (iterEntries.hasNext()) {
	    	    Map.Entry entry = (Map.Entry) iterEntries.next();
	    		String attr = entry.getKey().toString();
	    		String value = "";
	    		
	    		if (attr.equalsIgnoreCase(LdapManager.PERSON_PASSWORD)) {
	    		    value = hash256(entry.getValue().toString());
	    		} else {
	    		    value = entry.getValue().toString();
	    		}
	    		    		
	    		if (attr.equals("cn")) {
	    			String[] parts = newDn.split(",");
	    			int j = 0;
	    			while (parts[j].indexOf("cn=") != 0) {
	    				j++;
	    			}
	    			parts[j] = "cn="+entry.getValue();
	    			
	    			newDn = "";
	    			for (int n = 0;n < parts.length;n++) {
	    				if (n == 0) {
	    					newDn += parts[n];
	    				} else {
	    					newDn += ","+parts[n];
	    				}
	    			}	
	    			
	    		} else {
	    			modifiers.put(attr, value);
	    		}

	    	}
	    	
	    	if (!newDn.equals(usrDn)) {
	    		context.rename(usrDn, newDn);
	    	}
	    	
	    	ModificationItem[] mods = new ModificationItem[modifiers.size()];
	    	Iterator iterModentries = modifiers.entrySet().iterator();
	    	int ae = 0;
	    	while (iterModentries.hasNext()) {
	    		Map.Entry modentry = (Map.Entry) iterModentries.next();
	    		
	    		if ((modentry.getKey().toString().equals(LdapManager.ROLE_ACCESSRIGHTPATH) || modentry.getKey().toString().equals(LdapManager.ROLE_USER))
	    		    && modentry.getValue().toString().contains("[")) {
	    		    
	    		    // handle multivalues
    		        String multipleValues = modentry.getValue().toString();
    		        multipleValues = multipleValues.substring(1, multipleValues.length()-1);
    		        String[] arrayValues = multipleValues.split(", ");
    		        
    		        BasicAttribute attribute = new BasicAttribute(modentry.getKey().toString());
    		        for (int index=0; index<arrayValues.length; index++) {
    		            attribute.add(arrayValues[index]);
    		        }
    		        
    		        mods[ae] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute);
    		        
	    		} else {
	    		
	    		    mods[ae] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, new BasicAttribute(modentry.getKey().toString(), modentry.getValue().toString()));
	    		}
	    		ae++;
	    	} 		
    		context.modifyAttributes(newDn, mods);
    		context.close();
    		
    	} catch (Exception e) {
    	    logger.error("LdapManager error: modify attributes failed, "+e);
    	}
    	
    	String result = "fail";
    	return result;
    }
    
    public void deleteEntry(String dn, EntryType entryType) {
    	DirContext context = null;
    	String usrDn = dn+","+getDomainComponents();
    	
    	try {
    		context = getContext();
    		context.destroySubcontext(usrDn);
    		
    		// Delete user from role
    		if (entryType.equals(EntryType.USER)) {
    			removeUserFromRole(usrDn);
    		}
    		
    		context.close();
    		
    	} catch (Exception e) {
    	    logger.error("LdapManager error: deleteEntry failed, "+e);
    	}	
    }
    
    private void setDomainComponents(String baseDomainComponents) {
        this.domainComponents = baseDomainComponents;
    }
    
    public String getDomainComponents() {
        return this.domainComponents;
    }
    
    private void setInstanceAdminDn(String instanceAdmin) {
        // Dn for instance admin
        if (!this.domainComponents.isEmpty() && instanceAdmin.endsWith(this.domainComponents)) {
            this.instanceAdminDn = instanceAdmin;
        }
    }
    
    public String getInstanceAdminDn() {
        return this.instanceAdminDn;
    }

}
