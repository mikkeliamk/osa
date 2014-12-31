package fi.mamk.osa.bean;

import org.apache.log4j.Logger;

/**
 * Class for relations datastream
 */
public class RelationsBean extends DataStream {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(RelationsBean.class);
    
    public static final String RELATIONS_DSID = "RELS_EXT";

    // Constructor
    public RelationsBean(String PID) {
        super(PID);
        dsID = RelationsBean.RELATIONS_DSID;
    }
}