package fi.mamk.osa.solr;

import java.io.Serializable;

public class SolrField implements Serializable {
    
    private static final long serialVersionUID = 3391525129053148122L;
    private String name;
    private Integer boost;
    private Integer ngramBoost;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getBoost() {
        return boost;
    }
    
    public void setBoost(Integer boost) {
        this.boost = boost;
    }
    
    public Integer getNgramBoost() {
        return ngramBoost;
    }
    
    public void setNgramBoost(Integer ngramBoost) {
        this.ngramBoost = ngramBoost;
    }
    
}
