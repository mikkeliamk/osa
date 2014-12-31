package fi.mamk.osa.stripes;

import java.util.HashMap;

import org.apache.log4j.Logger;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import fi.mamk.osa.auth.User;
import fi.mamk.osa.core.Osa;
import fi.mamk.osa.search.Search;

@UrlBinding("/AdvancedBrowse.action")
public class AdvancedBrowseAction extends OsaBaseActionBean {
    
    private static final Logger logger = Logger.getLogger(AdvancedBrowseAction.class);
	
    //variables for facet search
    private String jsonFacets,  jsonOptions, jsonFacetItems;
    private String selectedFacet, selectedValue;
    private HashMap<String, Object> jsonFacetValues;	
    //for making filters
    private HashMap<String, String> filters;
    private Search search;
    private String PATH = "/advancedbrowse.jsp";
    
    static private boolean keepSearch;
	
	@DefaultHandler
    public Resolution showBrowse() {
		return new ForwardResolution(PATH);
    }
	
	@HandlesEvent("keepSearch")
    public Resolution keepSearch() {	
		setKeepSearch(true);
		return new ForwardResolution(PATH);
    }
	
	//load all facet fields
	@HandlesEvent("getAjaxBrowseFacets")
    public Resolution facetResult() throws Exception {	
		HashMap<String, Object> json = new HashMap<String, Object>();
		if ((isKeepSearch()) && (this.getContext().getSearch() != null)) {	
			keepSearch = false;
			search = this.getContext().getSearch();
			//set facet vector session from GUI
			search.setFacetFields(getContext().getGui().getSearchConfiguration().getAdvBrowseFacetFields()); 
		}
		else if ((this.getContext().getSearch() == null) || (isKeepSearch()==false)) {
			search = new Search();  
			//set facet vector session from GUI
			search.setFacetFields(getContext().getGui().getSearchConfiguration().getAdvBrowseFacetFields()); 			
			try{    
				search = Osa.searchManager.search(search, 
	        				getContext().getUser(),
	        				getContext().getUser().getOrganization());					
			} catch (Exception e) {
				logger.error(e);
				return null;
			}
		}
		
		json.put("FacetNames", search.getFacetFields());
    	json.put("FacetNumber", search.getFacetFields().size()); 
    	this.getContext().setSearch(search);    	
    	jsonFacets = new flexjson.JSONSerializer().deepSerialize(json);  
    	return new StreamingResolution("application/json", jsonFacets);
    }
	
	//load all facet fields for selected facet
	@HandlesEvent("getAjaxBrowseOptions")
	public Resolution facetOptions() throws Exception {
		search = getContext().getSearch();
		try {
			search = Osa.searchManager.search(search, this.getContext().getUser(), this.getContext().getUser().getOrganization());
			jsonFacetValues = new HashMap<String, Object>();
			for (String key : search.getFacets().keySet()) {	
				jsonFacetValues.put(key, search.getFacets().get(key).toString());
			}  
			
			if (search.getFacetFilters() != null) {				
				jsonFacetValues.put("getFacetFilters", new flexjson.JSONSerializer().deepSerialize(search.getFacetFilters().entrySet().toString()));
			}
			jsonFacetValues.put("NumberOfResults", search.getTotalSize());
			jsonOptions = new flexjson.JSONSerializer().deepSerialize(jsonFacetValues); 		
			return new StreamingResolution("application/json", jsonOptions);
			
		} catch (Exception e) {
		    logger.error(e);
			return null;
		}
	}
	
	//get facet fields for selected filters
	
	@HandlesEvent("getAjaxUpdatedOptions")
	public Resolution facetUpdatedOptionsResult() {
		search = getContext().getSearch();
		filters = new HashMap<>();
		
		User user = getContext().getUser();		
		if (getContext().getRequest().getParameter("action").equalsIgnoreCase("addFilter")){
			search.addFacetFilter(this.getContext().getRequest().getParameter("selectedFacet"),this.getContext().getRequest().getParameter("selectedValue"));	
			if (!this.getContext().getRequest().getParameter("selectedValue").equalsIgnoreCase("empty")) {
				filters.put(this.getContext().getRequest().getParameter("selectedFacet"), this.getContext().getRequest().getParameter("selectedValue"));
			}
		} else {				
			search.removeFacetFilter(this.getContext().getRequest().getParameter("selectedFacet"),this.getContext().getRequest().getParameter("selectedValue"));		
			if (!this.getContext().getRequest().getParameter("selectedValue").equalsIgnoreCase("empty")) {
				filters.remove(this.getContext().getRequest().getParameter("selectedFacet"));
			}
			
		}	
		search.setFilters(filters);			
		try {			
			search = Osa.searchManager.search(search, user, user.getOrganization());
			getContext().setSearch(search);	
			HashMap<String, Object> updatedOptions = new HashMap<String, Object>();		
			for (String key : search.getFacets().keySet()){					
				updatedOptions.put(key, search.getFacets().get(key).toString());
			}			
			if (search.getFacetFilters() != null) {			
				updatedOptions.put("getFacetFilters", new flexjson.JSONSerializer().deepSerialize(search.getFacetFilters().entrySet().toString()));
			}
			updatedOptions.put("NumberOfResults", search.getTotalSize());
			String jsonKeys = new flexjson.JSONSerializer().deepSerialize(updatedOptions);			
			return new StreamingResolution("application/json", jsonKeys);
			
		} catch (Exception e) {
		    logger.error(e);	
			return null;
		}			
	}
	
	//clear current search
    @HandlesEvent("clearSearch")
    public Resolution clearSearch() throws Exception {			
    	this.getContext().setSearch(null);				
    	return new ForwardResolution(PATH);
    }
		
	public String getJsonFacets() {
		return jsonFacets;
	}

	public void setJsonFacets(String jsonFacets) {
		this.jsonFacets = jsonFacets;
	}

	public String getJsonOptions() {
		return jsonOptions;
	}

	public void setJsonOptions(String jsonOptions) {
		this.jsonOptions = jsonOptions;
	}

	public String getJsonFacetItems() {
		return jsonFacetItems;
	}

	public void setJsonFacetItems(String jsonFacetItems) {
		this.jsonFacetItems = jsonFacetItems;
	}

	public String getSelectedFacet() {
		return selectedFacet;
	}

	public void setSelectedFacet(String selectedFacet) {
		this.selectedFacet = selectedFacet;
	}

	public String getSelectedValue() {
		return selectedValue;
	}

	public void setSelectedValue(String selectedValue) {
		this.selectedValue = selectedValue;
	}

	public HashMap<String, Object> getJsonFacetValues() {
		return jsonFacetValues;
	}

	public void setJsonFacetValues(HashMap<String, Object> jsonFacetValues) {
		this.jsonFacetValues = jsonFacetValues;
	}
	
	public Search getSearch() {
		return search;
	}

	public void setSearch(Search search) {
		this.search = search;
	}

	public HashMap<String, String> getFilters() {
		return filters;
	}

	public void setFilters(HashMap<String, String> filters) {
		this.filters = filters;
	}

	public static boolean isKeepSearch() {
		return keepSearch;
	}

	public static void setKeepSearch(boolean keepSearch) {
		AdvancedBrowseAction.keepSearch = keepSearch;
	}
}
