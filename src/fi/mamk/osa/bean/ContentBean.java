package fi.mamk.osa.bean;

import org.apache.log4j.Logger;

import fi.mamk.osa.bean.MetaDataElement.MetaDataType;

/**
 * Class for content datastream
 */
public class ContentBean extends DataStream {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(ContentBean.class);
    
    public static final String CONTENT_DSID = "CONTENT";
    public static final String CONTENT_LABEL = "Content Record";
    private String strContent = "content";
       
    // Constructor
    public ContentBean(String PID) {
        super(PID);
        dsID = ContentBean.CONTENT_DSID;
    }
    
    public void setContent(String value) {
        this.setMetaDataElement(strContent, strContent, value, MetaDataType.string);
    }
    
    public String getContent() {
        return this.getMetaDataElements().get(strContent).getValue();
    }
}
