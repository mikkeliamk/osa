package fi.mamk.osa.auth;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Class for setting roles and permissions
 * publicityLevel       access to public/restricted/confidential data
 * accessRightLevel     describes access to content
 * accessRightPath      paths to directories or objects, where this role has access rights
 */
public class AccessRight implements Serializable {
	
    private static final long serialVersionUID = -8302724810061640050L;

    public static final int ACCESSRIGHTLEVEL_DENY_META         = -1;
    public static final int ACCESSRIGHTLEVEL_READ_META         = 10;
    public static final int ACCESSRIGHTLEVEL_READ_DOC          = 20;
    public static final int ACCESSRIGHTLEVEL_WRITE_META        = 30;
    public static final int ACCESSRIGHTLEVEL_ADD_DOC           = 40;
    public static final int ACCESSRIGHTLEVEL_RECORDSMANAGEMENT = 50;
    public static final int ACCESSRIGHTLEVEL_ADMIN             = 100;
    public static final int ACCESSRIGHTLEVEL_INSTANCEADMIN     = 200;

    public static final String PUBLICITYLEVEL_Confidential = "confidential";
    public static final String PUBLICITYLEVEL_Restricted   = "restricted";
    public static final String PUBLICITYLEVEL_Public       = "public";
    
    /**
     * publicityLevel
     * 0: access to public data
     * 1: access to restricted data
     * 2: access to confidential data
     */
    public String publicityLevel;
    private int accessRightLevel;
    private String accessRightPath;
    private boolean recursiveRule;
    
    // publicity level hierarchy
    public static final List<String> PUBLICITYLEVEL_LIST = 
    Collections.unmodifiableList(new Vector<String>() {
        private static final long serialVersionUID = 1L;
    { 
        add("public");
        add("restricted");
        add("confidential");
    }});
	
    /**
     * Constructor
     */
	public AccessRight() {
		this.publicityLevel = PUBLICITYLEVEL_Public;
		this.accessRightLevel = ACCESSRIGHTLEVEL_READ_META;
		this.accessRightPath = null;
		this.recursiveRule = false;
	}
	
	/**
     * Constructor
     */
    public AccessRight(String publicityLevel, int accessRightLevel, String accessRightPath) {
		this.publicityLevel = publicityLevel;
		this.accessRightLevel = accessRightLevel;
		this.accessRightPath = accessRightPath;
		
		if (this.accessRightPath.endsWith("+")) {
		    this.recursiveRule = true;
		} else {
		    this.recursiveRule = false;
		}
	}
    
    /**
     * Get all publicity levels below determined level
     * (public, restricted, confidential)
     * @return
     */
    public String getPublicityLevels() {

        String levels = "";
        int index = PUBLICITYLEVEL_LIST.indexOf(this.publicityLevel);
        
        if (this.getAccessRightLevel() == AccessRight.ACCESSRIGHTLEVEL_DENY_META) {
            for (int i=0; i<PUBLICITYLEVEL_LIST.size(); i++) {
                if (i >= index) {
                    if (levels != "") {
                        levels += " ";
                    }
                    levels += PUBLICITYLEVEL_LIST.get(i);
                }
            }
            
        } else {
            for (int i=0; i<PUBLICITYLEVEL_LIST.size(); i++) {
                if (i <= index) {
                    if (levels != "") {
                        levels += " ";
                    }
                    levels += PUBLICITYLEVEL_LIST.get(i);
                }
            }
        }
        return levels;
    }
    
    public String getPublicityLevel() {
		return publicityLevel;
	}
	public void setPublicityLevel(String publicityLevel) {
		this.publicityLevel = publicityLevel;
	}
	public String getAccessRightPath() {
		return accessRightPath;
	}
	public void setAccessRightPath(String accessRightPath) {
		this.accessRightPath = accessRightPath;
	}
	public int getAccessRightLevel() {
		return accessRightLevel;
	}
	public void setAccessRightLevel(int accessRightLevel) {
		this.accessRightLevel = accessRightLevel;
	}
    public boolean isRecursiveRule() {
        return recursiveRule;
    }
    public void setRecursiveRule(boolean recursiveRule) {
        this.recursiveRule = recursiveRule;
    }

}
