package fi.mamk.osa.solr;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;

import fi.mamk.osa.core.Osa;

public class Solr implements Serializable {
	/*
	 * SolrJ implementation - Java API for Solr
	 * http://wiki.apache.org/solr/Solrj
	 */
    private static final long serialVersionUID = 4448520720873777807L;
    private static Logger logger 				= Logger.getLogger(Solr.class);
    
    private String url 			                = Osa.solrServerUrl;
    private SolrServer metadata                 = null;
    private SolrServer content                  = null;
    
    private HashMap<String, String> rawparams   = new HashMap<String, String>();
	
	public Solr() {
	    // TODO add sharding support
	    metadata = new HttpSolrServer(url + "/" + "metadata"); // fixed metadata core
	    content = new HttpSolrServer(url + "/" + "content");   // fixed content core 
	}
	
	// TODO add boost query parameters
    public QueryResponse query(String query, 
                               Vector<String> queryFields, 
                               HashMap<String, String> filters,
                               String accessRights, 
                               Vector<String> facetFields, 
                               int start, 
                               int rows, 
                               String sorting) {
        
        if (query == null || query == "") { query = "*:*"; }
        SolrQuery solrQuery = new SolrQuery().
                                  setParam("defType", "edismax").
                                  setParam("mm", "0").
                                  setParam("fl", "*,score,c.title:c.preferredName").
                                  setQuery(query);

        // Additional parameters
        // TODO refactor
		if(this.rawparams.size() > 0){
			for(String key : this.rawparams.keySet()){
				solrQuery = solrQuery.setParam(key, this.rawparams.get(key));
			}
			this.rawparams = new HashMap<String, String>();
		}
		
        // Paging
        if (start != -1) {
            solrQuery = solrQuery.setParam("start", Integer.toString(start)); 
        }
        if (rows != -1) {
            solrQuery = solrQuery.setParam("rows", Integer.toString(rows));
        }

        // Query fields
        String qfs = "";
        for (String qf : queryFields) {
            qfs += " " + qf;
        }
        qfs = qfs.replaceFirst(" ", "");
        solrQuery.setParam("qf", qfs);
        
        // Facets
        solrQuery.setParam("facet", "true").            // Faceting enabled/disabled
        setParam("facet.mincount", "1").                // Minimum count to include in facet results
        setParam("facet.sort", "count").                // Sort facet values by count
        setParam("facet.limit", "15").                  // Number of facet values returned
        setParam("facet.missing", "true");              // Count the amount of results where facet value is missing		
		for (String ff : facetFields) {
			solrQuery.add("facet.field", ff);			
		}
		
        // Sorting
        solrQuery.setParam("sort", sorting);			// Sorting by specific column with asc/desc. e.g. "score asc"
		
        // Filters				
        for (String key : filters.keySet()) {
            solrQuery.addFilterQuery(key + ":" + filters.get(key));
        }
        solrQuery.addFilterQuery(accessRights);
        // TODO add sharding. now searches only the metadata core.
        QueryResponse response = null;
        try {
        	response = metadata.query(solrQuery);

        } catch (SolrServerException e) {
            logger.error("Solr server error: " + e);
        }
		
        return response;
	}	
	
    public void addRawParam(String param, String value){
        this.rawparams.put(param, value);
    }
    
}