package fi.mamk.osa.solr;

import java.io.Serializable;
import java.util.Vector;

public class SolrConfiguration implements Serializable {
    
    private static final long serialVersionUID = 1728352989991748794L;
    private boolean enabled;
    private boolean logQueries;
    private Integer rowsToDisplay = 100;
    private Vector<SolrField> searchFields = new Vector<SolrField>();  
    
    public SolrConfiguration() {
        // Disable Solr by default
        this.enabled = false;
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isLogQueries() {
        return logQueries;
    }

    public Integer getRowsToDisplay() {
        return rowsToDisplay;
    }

    public Vector<SolrField> getSearchFields() {
        return searchFields;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setLogQueries(boolean logQueries) {
        this.logQueries = logQueries;
    }

    public void setRowsToDisplay(Integer rowsToDisplay) {
        this.rowsToDisplay = rowsToDisplay;
    }

    public void setSearchFields(Vector<SolrField> searchFields) {
        this.searchFields = searchFields;
    }   

}
