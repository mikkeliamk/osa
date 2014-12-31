package fi.mamk.osa.bean;

import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

/**
 * Class for several attachment datastreams
 * 
 * lhmElements          contains datastream url and content (key=datastream name)
 * lhmElementProperties contains datastream properties (key=datastream name)
 */
public class AttachmentBean extends DataStream {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(AttachmentBean.class);
    
    public static final String ATTACHMENT_DSID = "attachment";
    
    // container for thumb-objects property data
    LinkedHashMap<String, DataStream> lhmElementProperties = new LinkedHashMap<String, DataStream>();
    
    // Constructor
    public AttachmentBean(String PID) 
    {
        super(PID);
        dsID = AttachmentBean.ATTACHMENT_DSID;
    }
    
    public LinkedHashMap<String, DataStream> getElementProperties() { return this.lhmElementProperties; }
    public void setElementProperties(LinkedHashMap<String, DataStream> elements) { this.lhmElementProperties = elements; }
    
    public void setElementProperty(String dataStream, String propertyName, String propertyValue) {
        if (this.lhmElementProperties.containsKey(dataStream)) {
            this.lhmElementProperties.get(dataStream).setProperty(propertyName, propertyValue);
        } else {
            DataStream ds = new AttachmentBean(dataStream);
            ds.setProperty(propertyName, propertyValue);
            this.lhmElementProperties.put(dataStream, ds);
        }
    }
}