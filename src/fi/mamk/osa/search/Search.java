package fi.mamk.osa.search;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import fi.mamk.osa.solr.SolrField;
import fi.mamk.osa.ui.SearchFormField;
import fi.mamk.osa.ui.SearchResult;


// Represents a single search
// contains all information about performed search and result set it yielded.
public class Search implements Serializable {

	private static final long serialVersionUID = -1349385738557539687L;
	
	private static final Logger logger = Logger.getLogger(Search.class);

	// Submitted search form
	private String freetext 								= null;
	private boolean searchInsideFiles 						= false;
	private boolean searchNgrams 							= false;	
	private LinkedHashMap<String, SearchFormField> fields 	= new LinkedHashMap<String, SearchFormField>();
	private Vector<SolrField> freetextSolrFields			= new Vector<SolrField>();
	// Results
	private Vector<SearchResult> results 					= new Vector<SearchResult>();	
	private int queryTime;
	private long totalSize;
	
	// Facets
	private HashMap<String, LinkedHashMap<String, Integer>> facets 	= new HashMap<String, LinkedHashMap<String,Integer>>();
	private HashMap<String, Vector<String>> facetFilters 			= new HashMap<String, Vector<String>>();
	private Vector<String> facetFields 								= new Vector<String>(); // Parameter
	private HashMap<String, String> filters 						= new HashMap<String, String>();
	
	// Paging
	private int rows 			= 250;
	private int start 			= 0;
	private String sorting 		= "score desc";
	
	private String forwardPage 	= "/results.jsp";
	
	public Search() {}
	public Search(String stripesbughere) {}
		
	public void addFacetFilter(String facetName, String value) {
		if(this.facetFilters.get(facetName) == null) {
			this.facetFilters.put(facetName, new Vector<String>());
		}		
		Vector<String> filterValues = this.facetFilters.get(facetName);
		if(!filterValues.contains(value)) {
			filterValues.add(value);
		}
	}
	
	public void removeFacetFilter(String facetName, String value) {		
		Vector<String> filterValues = this.facetFilters.get(facetName);
		if(filterValues != null) {			
		
			filterValues.remove(value);
		} 
		if(filterValues.isEmpty()) {
			this.facetFilters.remove(facetName);
		}
	}
	
	public Collection<SearchFormField> getFieldValues() {
		return this.fields.values();
	}
	
	public int getResultCount() {
		return this.results.size();		
	}

	public String getFreetext() {
		return freetext;
	}

	public void setFreetext(String freetext) {
		this.freetext = freetext;
	}

	public boolean isSearchInsideFiles() {
		return searchInsideFiles;
	}

	public void setSearchInsideFiles(boolean searchInsideFiles) {
		this.searchInsideFiles = searchInsideFiles;
	}

	public boolean isSearchNgrams() {
		return searchNgrams;
	}

	public void setSearchNgrams(boolean searchNgrams) {
		this.searchNgrams = searchNgrams;
	}

	public LinkedHashMap<String, SearchFormField> getFields() {
		return fields;
	}

	public void setFields(LinkedHashMap<String, SearchFormField> fields) {
		this.fields = fields;
	}

	public Vector<SearchResult> getResults() {
		return results;
	}

	public String getResultsJson(){
		String JsonResult = new flexjson.JSONSerializer().serialize(results);
		return JsonResult;
	}
	
	public void setResults(Vector<SearchResult> results) {
		this.results = results;
	}

	public int getQueryTime() {
		return queryTime;
	}

	public void setQueryTime(int queryTime) {
		this.queryTime = queryTime;
	}

	public long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public String getForwardPage() {
		return forwardPage;
	}

	public void setForwardPage(String forwardPage) {
		this.forwardPage = forwardPage;
	}

    
    public String toString() {
        ReflectionToStringBuilder tsb = 
       	new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
       return tsb.toString();
	}

	/**
	 * @return the sorting
	 */
	public String getSorting() {
		return sorting;
	}

	/**
	 * @param sorting the sorting to set
	 */
	public void setSorting(String sorting) {
		this.sorting = sorting;
	}

	/**
	 * @return the facets
	 */
	public HashMap<String, LinkedHashMap<String, Integer>> getFacets() {
		return facets;
	}

	/**
	 * @param facets the facets to set
	 */
	public void setFacets(HashMap<String, LinkedHashMap<String, Integer>> facets) {
		this.facets = facets;
	}

	public HashMap<String, Vector<String>> getFacetFilters() {
		return facetFilters;
	}

	public void setFacetFilters(HashMap<String, Vector<String>> facetFilters) {
		this.facetFilters = facetFilters;
	}

	public Vector<String> getFacetFields() {
		return facetFields;
	}

	public void setFacetFields(Vector<String> facetFields) {
		this.facetFields = facetFields;
	}

	public Vector<SolrField> getFreetextSolrFields() {
		return freetextSolrFields;
	}

	public void setFreetextSolrFields(Vector<SolrField> freetextSolrFields) {
		this.freetextSolrFields = freetextSolrFields;
	}
	public HashMap<String, String> getFilters() {
		return filters;
	}
	public void setFilters(HashMap<String, String> filters) {
		this.filters = filters;
	}
	
}