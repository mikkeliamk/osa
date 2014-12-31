package fi.mamk.osa.bean;

import org.apache.log4j.Logger;
import fi.mamk.osa.bean.MetaDataElement.MetaDataType;

/**
 * Class for capture datastream
 * 
 * lhmElements          contains capture data, that is determined in content models
 */
public class CaptureBean extends DataStream {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(CaptureBean.class);
    
    public static final String CAPTURE_DSID 			= "CAPTURE";
    public static final String CAPTURE_LABEL 			= "Capture Record";
    
    public static final String RELATION_ConformsTo 		= "conformsTo";
    public static final String RELATION_HasAction 		= "hasAction";    
    public static final String RELATION_HasAgent 		= "hasAgent";
    public static final String RELATION_HasEvent 		= "hasEvent";
    public static final String RELATION_HasFormat 		= "hasFormat";
    public static final String RELATION_HasPart 		= "hasPart";
    public static final String RELATION_HasPlace 		= "hasPlace";
    public static final String RELATION_HasVersion 		= "hasVersion";
    public static final String RELATION_IsFormatOf 		= "isFormatOf";
    public static final String RELATION_IsChildOf 		= "isChildOf";
    public static final String RELATION_IsParentOf 		= "isParentOf";
    public static final String RELATION_IsPartOf 		= "isPartOf";
    public static final String RELATION_IsReferencedBy 	= "isReferencedBy";
    public static final String RELATION_IsReplacedBy 	= "isReplacedBy";
    public static final String RELATION_IsRequiredBy 	= "isRequiredBy";
    public static final String RELATION_IsVersionOf 	= "isVersionOf";
    public static final String RELATION_References 		= "references";
    public static final String RELATION_Replaces 		= "replaces";
    public static final String RELATION_Requires 		= "requires";
    public static final String RELATION_Source 			= "source";
    public static final String RELATION_HasRedaction 	= "hasRedaction";
    public static final String RELATION_IsRedactionOf 	= "isRedactionOf";
    public static final String RELATION_IsMemberOf 		= "isMemberOf";
    
    public static final String PUBLICITY_LEVEL          = "accessRights";
    public static final String IDENTIFIER               = "id";
    public static final String PREFERREDNAME            = "preferredName";
    public static final String TITLE                    = "title";
    public static final String TYPE 			        = "type";
    
    // Constructor
    public CaptureBean(String PID) {
        super(PID);
        dsID = CaptureBean.CAPTURE_DSID;
    }
    
    /**
     * Returns the value of isPartOf-relation
     * @return
     */
    public String getParent() {
        String parent = null;
        MetaDataElement element = this.getMetaDataElements().get(CaptureBean.RELATION_IsPartOf);
        
        if (element != null) {
            parent = element.getValue();
        }
        return parent;
    }
    
    public String getPublicityLevel() {
        String parent = null;
        MetaDataElement element = this.getMetaDataElements().get(CaptureBean.PUBLICITY_LEVEL);
        
        if (element != null) {
            parent = element.getValue();
        }
        return parent;
    }
        
    /**
     * set metadata to capture datastream
     * @param name      name of the relation
     * @param value     value  of the relation
     * @param eType     type of metadata 
     */
    public void setMetaDataElement(String name, String visibleName, String value, MetaDataType eType) {
        
        if (name.equalsIgnoreCase(CaptureBean.RELATION_ConformsTo)
            || name.equalsIgnoreCase(CaptureBean.RELATION_HasFormat)
            || name.equalsIgnoreCase(CaptureBean.RELATION_HasPart)
            || name.equalsIgnoreCase(CaptureBean.RELATION_HasRedaction)
            || name.equalsIgnoreCase(CaptureBean.RELATION_HasVersion)
            || name.equalsIgnoreCase(CaptureBean.RELATION_IsChildOf)
            || name.equalsIgnoreCase(CaptureBean.RELATION_IsFormatOf)
            || name.equalsIgnoreCase(CaptureBean.RELATION_IsMemberOf)
            || name.equalsIgnoreCase(CaptureBean.RELATION_IsParentOf)
            || name.equalsIgnoreCase(CaptureBean.RELATION_IsPartOf)
            || name.equalsIgnoreCase(CaptureBean.RELATION_IsRedactionOf)
            || name.equalsIgnoreCase(CaptureBean.RELATION_IsReferencedBy)
            || name.equalsIgnoreCase(CaptureBean.RELATION_IsReplacedBy)
            || name.equalsIgnoreCase(CaptureBean.RELATION_IsRequiredBy)
            || name.equalsIgnoreCase(CaptureBean.RELATION_IsVersionOf)
            || name.equalsIgnoreCase(CaptureBean.RELATION_References)
            || name.equalsIgnoreCase(CaptureBean.RELATION_Replaces)
            || name.equalsIgnoreCase(CaptureBean.RELATION_Requires)
            || name.equalsIgnoreCase(CaptureBean.RELATION_Source)
                )
            {
                super.setMetaDataElement(name, visibleName, value, MetaDataElement.MetaDataType.relation);
            } else {
                super.setMetaDataElement(name, visibleName, value, eType);
            }
    }
    
}
