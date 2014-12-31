package fi.mamk.osa.auth;

import java.io.Serializable;
import java.util.Collections;
import java.util.Vector;
import org.apache.log4j.Logger;
import fi.mamk.osa.auth.AccessRight;

/**
 * Class for setting roles and permissions
 */
public class Role implements Serializable {

    private static final long serialVersionUID = -6687757850396871383L;
    private static final Logger logger = Logger.getLogger(Role.class);
    public static final String ROLE_PUBLIC = "public";
    
    // role name
    public String name;
    // access rights
    private Vector<AccessRight> roleAccessrights = new Vector<AccessRight>();
    // user dns, who has this role
    private Vector<String> roleOccupants = new Vector<String>();
    
    /**
     * Constructor
     */
    public Role() {
        this.name = "";
    }
    
    /**
     * Constructor
     */
    public Role(String name) {
        this.name = name;
    }
    
    /**
     * Constructor
     */
    public Role(String name, Vector<AccessRight> roleAccessrights) {
        this.name = name;
        this.roleAccessrights = roleAccessrights;
    }
    
    /**
     * Constructor
     * @param isAnonymous       user not logged in
     * @param organization      organization name for public search        
     */
    public Role(boolean isAnonymous, String organization) {
        this.name = "";
        
        if (isAnonymous) {
            this.name = "anonymous";
            String path = organization.toUpperCase()+":root";
            AccessRight bublicRight = new AccessRight();
            bublicRight.setAccessRightLevel(AccessRight.ACCESSRIGHTLEVEL_READ_META);
            bublicRight.setAccessRightPath(path);
            this.setRoleAccessright(bublicRight);
        }
    }
      
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setRoleAccessright (AccessRight ac) {
        this.roleAccessrights.add(ac);
    }
    
    public Vector<AccessRight> getRoleAccessrights () {
        return this.roleAccessrights;
    }
    
    public void setRoleAccessrights (Vector<AccessRight> acrights) {
        this.roleAccessrights = acrights;
    }
    
    public void setRoleOccupant (String userdn) {
        this.roleOccupants.add(userdn);
    }
    
    public Vector<String> getRoleOccupants () {
        return this.roleOccupants;
    }
    
    public void setRoleOccupants (Vector<String> userdns) {
        this.roleOccupants = userdns;
    }
    
    /**
     * Access rights for public role
     * @param organization
     * @return
     */
    public static Vector<AccessRight> getPublicRoleAccessrights (String organization) {
        Vector<AccessRight> publicRoleAccessrights = new Vector<AccessRight>();
        AccessRight ac = new AccessRight();
        ac.setAccessRightPath(organization.toUpperCase()+":root/+");
        publicRoleAccessrights.add(ac);
        return publicRoleAccessrights;
    }
    
    /**
     * Return highest accessright for object based on its path and publicity level
     * @param roles             user roles
     * @param path              object path
     * @param publicityLevel    object publicity level
     * @return
     */
    public static int getAccessRightForObject(Vector<Role> roles, String path, String publicityLevel) {
        Vector<Integer> accessRightValues = new Vector<Integer>();
        int retValue = AccessRight.ACCESSRIGHTLEVEL_DENY_META;
        
        if (path == null || path == "" || roles == null) {
            return retValue;
        }
        
        for (Role role : roles) {
            Vector<AccessRight> acRights = role.getRoleAccessrights();
            for (AccessRight ac : acRights) {
                String acPath = ac.getAccessRightPath();
                int acLevel = ac.getAccessRightLevel();
                if (acPath.endsWith("+")) {
                    // recursive path
                    acPath = acPath.replace("+", "");
                    if (path.startsWith(acPath)) {
                        if (ac.getPublicityLevels().contains(publicityLevel)) {
                            accessRightValues.add(acLevel);
                        }
                    }
                    
                } else {
                    // exact path
                    if (path.equals(acPath)) {
                        if (ac.getPublicityLevels().contains(publicityLevel)) {
                            accessRightValues.add(acLevel);
                        }
                    }
                }
            }
        }
        
        if (!accessRightValues.isEmpty()) {
            
            if (accessRightValues.contains(AccessRight.ACCESSRIGHTLEVEL_DENY_META)) {
                // if denied
                retValue = AccessRight.ACCESSRIGHTLEVEL_DENY_META;
            } else {
                // set the max accessright
                retValue = Collections.max(accessRightValues);
            }
        }
        
        return retValue;
    }
    
    /**
     * Method for comparing users accessrights to the object based on its path and publicity level.
     * @param roles
     * @param path
     * @param publicityLevel
     * @param requiredLevel
     * @return
     */
    public static boolean hasRights(Vector<Role> roles, String path, String publicityLevel, int requiredLevel) {
        Vector<Integer> accessRightValues = new Vector<Integer>();
        boolean retValue = false;
        
        if (path == null || path == "" || roles == null) {
            return retValue;
        }
        
        for (Role role : roles) {
            Vector<AccessRight> acRights = role.getRoleAccessrights();
            for (AccessRight ac : acRights) {
                String acPath = ac.getAccessRightPath();
                int acLevel = ac.getAccessRightLevel();
                if (acPath.endsWith("+")) {
                    // recursive path
                    acPath = acPath.replace("+", "");
                    if (path.startsWith(acPath)) {
                        if (ac.getPublicityLevels().contains(publicityLevel)) {
                            accessRightValues.add(acLevel);
                        }
                    }
                    
                } else {
                    // exact path
                    if (path.equals(acPath)) {
                        if (ac.getPublicityLevels().contains(publicityLevel)) {
                            accessRightValues.add(acLevel);
                        }
                    }
                }
            }
        }
        
        if (!accessRightValues.isEmpty()) {
            
            if (accessRightValues.contains(AccessRight.ACCESSRIGHTLEVEL_DENY_META)) {
                // if denied
                retValue = false;
            } else if (Collections.max(accessRightValues) >= requiredLevel) {
                retValue = true;
            }
        }
        
        return retValue;
    }
    
    @Override
    public String toString() {
        return "role [name="+name+", roleAccessrights: "+roleAccessrights.size()+" kpl]";
    }
}
