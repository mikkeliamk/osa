package fi.mamk.osa.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;


public class SearchResult implements java.io.Serializable, Comparable<SearchResult> {

    static Logger                   logger              = Logger.getLogger(SearchResult.class);
    private static final long       serialVersionUID    = 1L;
    private HashMap<String, Object> metadataFields      = new HashMap<String, Object>();
    private float                   score;
    private String                  ancestorTitle       = "";           

    public SearchResult() {

    }

    public SearchResult(Map<String, Object> row) {

        for (String key : row.keySet()) {
            try {
                metadataFields.put(key, row.get(key));
            } catch (Exception e) {
                logger.error("Error creating items from search results. ", e);
            }
        }
    }

    @Override
    public int compareTo(SearchResult arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Collection<String> getFields() {
        return this.metadataFields.keySet();
    }
    
    /**
     * @return the metadataFields
     */
    public HashMap<String, Object> getMetadataFields() {
        return metadataFields;
    }

    /**
     * @param metadataFields the metadataFields to set
     */
    public void setMetadataFields(HashMap<String, Object> metadataFields) {
        this.metadataFields = metadataFields;
    }

    public String getAncestorTitle() {
        return ancestorTitle;
    }

    public void setAncestorTitle(String ancestorTitle) {
        this.ancestorTitle = ancestorTitle;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

}