package fi.mamk.osa.bean;

import org.apache.log4j.Logger;

/**
 * Class for original datastream
 * 
 * lhmElements          contains url and content for original object
 */
public class OriginalBean extends DataStream {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(OriginalBean.class);
    
    public static final String ORIGINAL_DSID = "original";
    
    // Constructor
    public OriginalBean(String PID) {
        super(PID);
        dsID = OriginalBean.ORIGINAL_DSID;
    }
    
}
