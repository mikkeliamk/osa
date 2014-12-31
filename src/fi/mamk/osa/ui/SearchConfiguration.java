package fi.mamk.osa.ui;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Vector;

import fi.mamk.osa.solr.SolrField;


public class SearchConfiguration {

	private LinkedHashMap<String, SearchFormField> searchForm = new LinkedHashMap<String, SearchFormField>();
	private Vector<SolrField> freetextField = new Vector<SolrField>();
	private int searchHistoryLength = 0;
	private LinkedHashMap<SearchResultConfiguration.Type, SearchResultConfiguration> resultLayouts = new LinkedHashMap<SearchResultConfiguration.Type, SearchResultConfiguration>();
	private Vector<String> facetFields = new Vector<String>();
	private Vector<String> advBrowsefacetFields = new Vector<String>();
	private Date startDate, endDate;
	
	public Collection<SearchFormField> getFieldValues() {
	    return this.searchForm.values();
	}
	
	/**
	 * @return the searchForm
	 */
	public LinkedHashMap<String, SearchFormField> getSearchForm() {
		return searchForm;
	}
	/**
	 * @param searchForm the searchForm to set
	 */
	public void setSearchForm(LinkedHashMap<String, SearchFormField> searchForm) {
		this.searchForm = searchForm;
	}
	/**
	 * @return the searchHistoryLength
	 */
	public int getSearchHistoryLength() {
		return searchHistoryLength;
	}
	/**
	 * @param searchHistoryLength the searchHistoryLength to set
	 */
	public void setSearchHistoryLength(int searchHistoryLength) {
		this.searchHistoryLength = searchHistoryLength;
	}
	
	public LinkedHashMap<SearchResultConfiguration.Type, SearchResultConfiguration> getResultLayouts() {
		return resultLayouts;
	}

	public void setResultLayouts(LinkedHashMap<SearchResultConfiguration.Type, SearchResultConfiguration> resultLayouts) {
		this.resultLayouts = resultLayouts;
	}

	public Vector<String> getFacetFields() {
		return facetFields;
	}

	public void setFacetFields(Vector<String> facetFields) {
		this.facetFields = facetFields;
	}
	
	public Vector<String> getAdvBrowseFacetFields() {
		return advBrowsefacetFields;
	}

	public void setAdvBrowseFacetFields(Vector<String> advBrowsefacetFields) {
		this.advBrowsefacetFields = advBrowsefacetFields;
	}

	public Vector<SolrField> getFreetextField() {
		return freetextField;
	}

	public void setFreetextField(Vector<SolrField> freetextField) {
		this.freetextField = freetextField;
	}
	
	public Date getStartDate(){
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

}