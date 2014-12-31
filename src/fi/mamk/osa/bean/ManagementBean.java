package fi.mamk.osa.bean;

import org.apache.log4j.Logger;

/**
 * Class for management datastream
 * contains ancestor data and path, where object is stored
 */
public class ManagementBean extends DataStream {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(ManagementBean.class);
    
    public static final String MANAGEMENT_DSID = "MANAGEMENT";
    public static final String MANAGEMENT_LABEL = "Management Record";
    
    public static final String DATA_isAncestor = "isAncestor";
    public static final String DATA_objectPath = "objectPath";
    
    // Constructor
    public ManagementBean(String PID) {
        super(PID);
        dsID = ManagementBean.MANAGEMENT_DSID;
    }

    /**
     * Returns the value of isAncestor
     * @return
     */
    public String getAncestor() {
        String parent = null;
        MetaDataElement element = (MetaDataElement)this.getMetaDataElements().get(DATA_isAncestor);
        if (element != null) {
            parent = element.getValue();
        }
        return parent;
    }
    
    /**
     * Set isAncestor value (ancestor = next to root object)
     * @param pid
     */
    public void setAncestor(String pid) {
        boolean newValue = true;
        
        MetaDataElement element = (MetaDataElement)this.getMetaDataElements().get(DATA_isAncestor);
        if (element != null) {
            element.removeValue();
            element.setValue(pid);
            newValue = false;
        }
        
        // Metadata value not found, create new
        if (newValue) {
            this.getMetaDataElements().put(ManagementBean.DATA_isAncestor,
                                           new MetaDataElement(ManagementBean.DATA_isAncestor, 
                                                               ManagementBean.DATA_isAncestor, 
                                                               pid, 
                                                               MetaDataElement.MetaDataType.getEnum("string")));
        }
    }
    
    /**
     * Set path value
     * @param path      object location in hierarchy
     */
    public void setPath(String path) {
        boolean newValue = true;
        
        if (!path.endsWith("/")) {
            path = path.concat("/");
        }
        
        MetaDataElement element = (MetaDataElement)this.getMetaDataElements().get(DATA_objectPath);
        if (element != null) {
            element.removeValue();
            element.setValue(path);
            newValue = false;
        }
        
        if (path != null && path != "") {
            // Metadata value not found, create new
            if (newValue) {
                this.getMetaDataElements().put(ManagementBean.DATA_objectPath,
                                               new MetaDataElement(ManagementBean.DATA_objectPath, 
                                                                   ManagementBean.DATA_objectPath, 
                                                                   path, 
                                                                   MetaDataElement.MetaDataType.getEnum("string")));
            }
        }
    }
    
    public String getPath() {
        String path = "";
        MetaDataElement element = (MetaDataElement)this.getMetaDataElements().get(DATA_objectPath);
        if (element != null) {
            path = element.getValue();
        }
        return path;
    }
    
}
