package fi.mamk.osa.stripes;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import fi.mamk.osa.auth.AccessRight;
import fi.mamk.osa.auth.User;
import fi.mamk.osa.core.Osa;
import fi.mamk.osa.fedora.RepositoryManager;
import fi.mamk.osa.search.Search;
import fi.mamk.osa.solr.SolrManager;
import fi.mamk.osa.tags.RelevanceTag;
import fi.mamk.osa.ui.SearchFormField;
import fi.mamk.osa.ui.SearchResult;
import fi.mamk.osa.ui.TreeNode;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.LocalizableMessage;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationErrorHandler;
import net.sourceforge.stripes.validation.ValidationErrors;
import org.apache.log4j.Logger;


@UrlBinding("/Search.action")
public class SearchAction extends OsaBaseActionBean implements ValidationErrorHandler {
    
    private static final Logger logger = Logger.getLogger(SearchAction.class);
        
    // Wrapper object for single search event
    private Search search;
    private String searchResultType;
    private String json;
    private boolean ajax = false;
    
    private String rootpid;
    
	private String sSearch;	
	private String sEcho;
	private String relationSearchTerm;
	private String facetName;
	
	private TreeNode  mainNode;
	
	//paginate
	private int iDisplayStart;
	private int iDisplayLength;

	//sort
	private int iSortCol_0;
	private String sSortDir_0;
    
	// facet
	private HashMap<String,String> filter = new HashMap<String, String>();
	private boolean removeFilter = false;
	private boolean removeAll = false;
	private Integer previousSearch;
	
    public static final Map<String, String> DATAICONS_MAP = 
            Collections.unmodifiableMap(new HashMap<String, String>() {
                private static final long serialVersionUID = 1L;
            { 
                put(RepositoryManager.ACTION, "cog_go.png");
                put(RepositoryManager.AGENT, "user_go.png");
                put(RepositoryManager.APPLICATION, "application.png");
                put(RepositoryManager.AUDIO, "music.png");
                put(RepositoryManager.COLLECTION, "folder.png");
                put(RepositoryManager.BASECOLLECTION, "folder.png");
                put(RepositoryManager.GROUPCOLLECTION, "folder.png");
                put(RepositoryManager.UNITCOLLECTION, "folder.png");
                put(RepositoryManager.DOCUMENT, "page.png");
                put(RepositoryManager.DRAWING, "pencil.png");
                put(RepositoryManager.EVENT, "time.png");
                put(RepositoryManager.IMAGE, "photo.png");
                put(RepositoryManager.MAP, "map.png");
                put(RepositoryManager.MOVINGIMAGE, "film.png");
                put(RepositoryManager.PHYSICALOBJECT, "box.png");
                put(RepositoryManager.PLACE, "world.png");
                put(RepositoryManager.DEFAULT_TYPE, "asterisk_yellow.png");
            }});
	

    @HandlesEvent("search_fromAdvBrowse")
    public Resolution fromAdvBrowse() {
    	//for 'back to search results button'
    	this.getContext().getRequest().getSession().setAttribute("fromSearch", true);
    	
    	User user = getContext().getUser();
    	
    	search = getContext().getSearch();
    	
		search.setRows(this.getContext().getGui().getSearchResults().getRows());
		search.setFacetFields(this.getContext().getGui().getSearchConfiguration().getFacetFields());
		
		if (this.getContext().getSearch() != null && this.previousSearch != null) {
			search = this.getContext().getSearch();
			
			search.setRows(this.getContext().getGui().getSearchResults().getRows());
			
			if (!filter.isEmpty()) {				
				// Append to existing filters
				for (String key : this.filter.keySet()) {
					if(removeFilter) {
						search.removeFacetFilter(key,this.filter.get(key));
					} else {
						search.addFacetFilter(key,this.filter.get(key));
					}
				}
				
			} else if (removeAll) {
				search.setFacetFilters(new HashMap<String, Vector<String>>());
			}
			this.setSearch(search);
		}
		
		Search newSearch = new Search();
		
        newSearch.setRows(this.search.getRows());
        newSearch.setStart(this.search.getStart());
        newSearch.setFreetext(this.search.getFreetext());
        newSearch.setFacetFilters(this.search.getFacetFilters());
        newSearch.setFacetFields(this.search.getFacetFields());
		
        for (String key : this.search.getFields().keySet()) {
        	SearchFormField formField = this.search.getFields().get(key);		
        	SearchFormField guiField = getContext().getGui().getSearchConfiguration().getSearchForm().get(key);		
        	guiField.setValue(formField.getValue());
        	newSearch.getFields().put(key, guiField);			
        }
        
        for (int z = 0;z<this.getContext().getGui().getSearchConfiguration().getFreetextField().size();z++) {
        	newSearch.getFreetextSolrFields().add(this.getContext().getGui().getSearchConfiguration().getFreetextField().get(z));
        }
        
        try {
        	if (this.getContext().getUser() != null) {
        		search = Osa.searchManager.search(newSearch, user, user.getOrganization());
        	}
        	
        } catch (Exception e) {
            logger.error(e);
        }
		
        this.getContext().setSearch(search);
        return new StreamingResolution(MIME_TEXT, "success");
    }
    
    @HandlesEvent("search")
    public Resolution search() {    
    	//for 'back to search results button'
    	this.getContext().getRequest().getSession().setAttribute("fromSearch", true);
    	
    	User user = getContext().getUser();
    	
		if (search == null) {
			search = new Search();
		}
		
		search.setRows(this.getContext().getGui().getSearchResults().getRows());
		search.setFacetFields(this.getContext().getGui().getSearchConfiguration().getFacetFields());
		
		if (this.getContext().getSearch() != null && this.previousSearch != null) {
			search = this.getContext().getSearch();			
			search.setRows(this.getContext().getGui().getSearchResults().getRows());
			
			if (!filter.isEmpty()) {				
				// Append to existing filters
				for (String key : this.filter.keySet()) {
					if(removeFilter) {
						search.removeFacetFilter(key,this.filter.get(key));
					} else {
						search.addFacetFilter(key,this.filter.get(key));
						
					}
				}
				
			} else if (removeAll) {
				search.setFacetFilters(new HashMap<String, Vector<String>>());
			}
			this.setSearch(search);
		}
		
		Search newSearch = new Search();	
		
		newSearch.setRows(this.search.getRows());
		newSearch.setStart(this.search.getStart());
		newSearch.setFreetext(this.search.getFreetext());
		newSearch.setFacetFilters(this.search.getFacetFilters());
		newSearch.setFacetFields(this.search.getFacetFields());
		
		for (String key : this.search.getFields().keySet()) {
			SearchFormField formField = this.search.getFields().get(key);				
			SearchFormField guiField = getContext().getGui().getSearchConfiguration().getSearchForm().get(key);				
			guiField.setValue(formField.getValue());
			newSearch.getFields().put(key, guiField);	
		}
		
		for (int z = 0;z<this.getContext().getGui().getSearchConfiguration().getFreetextField().size();z++) {
			newSearch.getFreetextSolrFields().add(this.getContext().getGui().getSearchConfiguration().getFreetextField().get(z));
		}
		
		// TODO: Handle facet events
		// TODO: Handle search history events
		
		try {
			if (this.getContext().getUser() != null) {
				search = Osa.searchManager.search(newSearch, user, user.getOrganization());		
			}
			
		} catch (Exception e) {
            logger.error(e);
        }
		
		this.getContext().setSearch(search);
		// If used by AJAX call, return plain text, otherwise redirect
		if (ajax) {
			return new StreamingResolution(MIME_TEXT, "success");
		} else {
			return new ForwardResolution(SEARCH).addParameter("src", "main");
		}
    }
    
    @HandlesEvent("manage")
    public Resolution manageSort() {
        
    	User user = getContext().getUser();
		String sorting = "";
		search = this.getContext().getSearch();

		Vector<Vector<String>> headerkeys = getContext().getGui().getSearchResults().getHeaderKeys();
		
		if (search != null && search.getResults().size() > 0 && search.getResults() != null && user != null) {
			try {
				sorting = headerkeys.get(iSortCol_0).get(0);
				if (sSortDir_0 != null) {
				    // Use field "alphaNameSort" for sorting titles and preferredNames, 
				    // because sorting doesn't work well on multivalued and tokenized fields.
				    if (sorting.equals(SolrManager.SOLRFIELD_Title)) {
				        sorting = "alphaNameSort "+sSortDir_0;
				    } else {
					    sorting += " "+sSortDir_0;
				    }
				} else {
					sorting = "score desc";
				}
				
                if (!sorting.equalsIgnoreCase(search.getSorting()) || iDisplayStart != search.getStart() || iDisplayLength != search.getRows() ) {
                	search.setSorting(sorting);
                	search.setStart(iDisplayStart);
                	search.setRows(iDisplayLength);
                	search.setResults(new Vector<SearchResult>());
                	search = Osa.searchManager.search(search, user, user.getOrganization());
                }
	
    			json = "{" +
    				    "\"sEcho\": "+sEcho+"," +
    				    "\"iTotalRecords\": "+search.getTotalSize()+"," +
    				    "\"iTotalDisplayRecords\": " + search.getTotalSize()+",";
	    	
    			json += "\"aaData\": [";
    			for (SearchResult results : search.getResults()) {
    				HashMap<String, Object> resultEntry = results.getMetadataFields();
    				Object resultEntryPid				= resultEntry.get(SolrManager.SOLRFIELD_Pid);
    				
    				json += "[";
    				for (int i = 0; i < headerkeys.size(); i++) {
    					String currentHeaderkey = headerkeys.get(i).get(0);
    					
    					// Columns that have data indexed in Solr 
    					if (resultEntry.get(currentHeaderkey) != null) {
    						
    						if (currentHeaderkey.equals(SolrManager.SOLRFIELD_Title) || currentHeaderkey.equals(SolrManager.SOLRFIELD_PreferredName)) {
    						    
    							// If object is ancestor itself, display root as ancestor
    							if (resultEntryPid.equals(resultEntry.get("m.isAncestor"))) {
    								json += "\"<a href='Ingest.action?pid="+resultEntryPid+"&view='>"+resultEntry.get(currentHeaderkey)+"</a><br/>" +
    											"<span class='ancestor-title'>-</span>\",";
    							} else {
    							    String ancestorpid = resultEntry.get("m.isAncestor").toString();
    							    String ancestortitle = results.getAncestorTitle();
    								json += "\"<a href='Ingest.action?pid="+resultEntryPid+"&view='>"+resultEntry.get(currentHeaderkey)+"</a><br/>" +
    											"<span class='ancestor-title'>" +
    												"<a href='Ingest.action?pid="+ancestorpid+"&view='>"+ancestortitle+"</a>" +
    											"</span>\",";
    							}
    							
    						} else if (currentHeaderkey.equalsIgnoreCase(SolrManager.SOLRFIELD_Type)) {
    							String dataicontype 		= resultEntry.get(currentHeaderkey).toString().toLowerCase().replace("[", "").replace("]", "");
    							String dataicontypeLocal 	= new LocalizableMessage("link.add."+dataicontype).getMessage(getUserLocale()); 
    							json += "\"<img src='img/icons/silk/"+SearchAction.DATAICONS_MAP.get(dataicontype)+"' title='"+dataicontypeLocal+"' alt='"+dataicontypeLocal+"'/>\",";
    						
    						} else if (currentHeaderkey.equalsIgnoreCase("score")) {
    							Object field = resultEntry.get(currentHeaderkey);
    							json += "\""+RelevanceTag.getRelevanceIcon(Float.parseFloat(field.toString()))+"\",";
    						
    						} else if (currentHeaderkey.equalsIgnoreCase(SolrManager.SOLRFIELD_Id)) {
    							json += "\""+SolrManager.convertToString(resultEntry.get(currentHeaderkey), "c.id")+"\",";
    						} else {
    							json += "\""+resultEntry.get(currentHeaderkey)+"\",";
    						}
    						
    					// Columns whose data is not indexed in Solr
    					} else {
    					    
    						if ((currentHeaderkey.equalsIgnoreCase(SolrManager.SOLRFIELD_Title) && resultEntry.get(SolrManager.SOLRFIELD_Title) != null)
    						    || (currentHeaderkey.equals(SolrManager.SOLRFIELD_PreferredName) && resultEntry.get(SolrManager.SOLRFIELD_PreferredName) != null)) {
    						    String ancestortitle = results.getAncestorTitle();
    							
    							if (ancestortitle == null || ancestortitle.equalsIgnoreCase(new LocalizableMessage("header.notitle").getMessage(getUserLocale()))) {
    								ancestortitle = new LocalizableMessage("header.notitle").getMessage(getUserLocale());
    							}
    							json += "\"<a href='Ingest.action?pid="+resultEntryPid+"&view='>"+new LocalizableMessage("header.notitle").getMessage(getUserLocale())+"</a><br/><span class='ancestor-title'>"+ancestortitle+"</span>\",";
    							
    						} else if (currentHeaderkey.equalsIgnoreCase("download")) {
    							String titlemsg = new LocalizableMessage("message.downloadtargz").getMessage(getUserLocale());
    							
    							String path = SolrManager.convertToString(resultEntry.get("m.objectPath"), "m.objectPath");
    							String publicityLvl = SolrManager.convertToString(resultEntry.get("c.accessRights"), "c.accessRights");
    							int accessLevel = this.getAccessRightForObject(path, publicityLvl);
    							
    							if (accessLevel >= AccessRight.ACCESSRIGHTLEVEL_READ_DOC) {
    								json += "\"<a href='Workflow.action?pid="+resultEntryPid+"&downloadObject='><img src='img/icons/silk/compress.png' alt='tar.gz' title='"+titlemsg+"'/></a>\",";
    							} else {
    								json += "\"-\",";
    							}
    						
    						} else if (currentHeaderkey.equalsIgnoreCase("basket")) {
    							String addtobasket = new LocalizableMessage("message.addtobasket").getMessage(getUserLocale());
    							String removeFromBasket = new LocalizableMessage("message.removefrombasket").getMessage(getUserLocale());
    							
    							if (!user.isAnonymous()) {
    								boolean exists = Osa.dbManager.get("mongo").checkIfExists(user, resultEntryPid.toString());
    								if(exists) {
    									json += "\"<img src='img/icons/silk/basket_delete.png' class='basketaction pointerCursor' data-action='deleteFromBasket' id='"+resultEntryPid+"' title='"+removeFromBasket+"' alt='[-]'/>\",";
    								} else {
    									json += "\"<img src='img/icons/silk/basket_put.png' class='basketaction pointerCursor' data-action='addToBasket' id='"+resultEntryPid+"' title='"+addtobasket+"' alt='[+]'/>\",";
    								}
    							} else {
    								String basketNotAvailable = new LocalizableMessage("basket.notavailable.anon").getMessage(getUserLocale());
    								json += "\"<img src='img/icons/silk/basket_error.png' title='"+basketNotAvailable+"' alt='-'/>\",";
    							}
    						} else if (currentHeaderkey.equalsIgnoreCase(SolrManager.SOLRFIELD_Id)) {
    							json += "\"\",";
    						
    						} else {
    							String ancestorpid = resultEntry.get("m.isAncestor").toString();
    						    String ancestortitle = results.getAncestorTitle();
    							json += "\"<a href='Ingest.action?pid="+resultEntryPid+"&view='>"+resultEntryPid+"</a><br/><span class='ancestor-title'><a href='Ingest.action?pid="+ancestorpid+"&view='>"+ancestortitle+"</a></span>\",";
    						}
    					}
    				}
    				if (search.getTotalSize() > 0) {
    					json = json.substring(0, json.length()-1);		//strip last colon
    				}
    				
    				json += "],";
    			}
	    	
    			if (search.getTotalSize() > 0) {
    				json = json.substring(0, json.length()-1);		//strip last colon
    			}
    			
    			json += "]}";
    			
			} catch (Exception e) {
				logger.error("ERROR in manageSort(): " + e.getMessage());
			}
    	} else { // If no results found
    		long searchTotalSize = 0;
    		json = "{" +
    				"\"sEcho\": "+sEcho+"," +
    				"\"iTotalRecords\": "+searchTotalSize+"," +
    				"\"iTotalDisplayRecords\": " + searchTotalSize+",";
    		json += "\"aaData\": [";
    		// If user is not logged in, display "Log in to make searches"-message
    		// Otherwise return empty 'aaData' array
    		if (user == null) {
    			json += "[";
    			for (int i=0; i<headerkeys.size(); i++) {
    				json += "\"Log in to make searches\",";
    			}
    			json = json.substring(0, json.length()-1);
    			json += "]";
    		}
    		json += "]}";
    	}
		
		return new StreamingResolution("application/json", json);
    }
    
    /**
     * ValidationErrorHandler
     */
    public Resolution handleValidationErrors(ValidationErrors errors) {
        if (errors.hasFieldErrors()) {
            errors.addGlobalError(new SimpleError("Check fields.\n"));
        }
        return null;
    }    

    public Resolution setType() {
    	this.getContext().getGui().setResultType(searchResultType);
    	this.getContext().getGui().setSearchResultType();
    	return new StreamingResolution("text/javascript","asd");
    }
    
    /** Gets content types that are currently indexed in solr. 
     * 	This is that only types with actual content in solr are displayed to user when making searches.
     * 
     * @return	Content types from facet list grouped by category
     */
    public LinkedHashMap<String, LinkedHashMap<String, Integer>> getContentTypes() {
    	Search typefacetsearch = new Search();
    	LinkedHashMap<String, Integer> collections = new LinkedHashMap<String, Integer>();
    	LinkedHashMap<String, Integer> contents = new LinkedHashMap<String, Integer>();
    	LinkedHashMap<String, Integer> contextObjects = new LinkedHashMap<String, Integer>();
    	LinkedHashMap<String, LinkedHashMap<String, Integer>> contentTypeMap = new LinkedHashMap<String, LinkedHashMap<String, Integer>>();
    	
    	typefacetsearch.setRows(0);
    	typefacetsearch.setStart(0);
    	typefacetsearch.setFreetext("");
    	typefacetsearch.getFacetFields().add("c.type");
    	User user = this.getContext().getUser();
    	
    	try {
			typefacetsearch = Osa.searchManager.search(typefacetsearch, user, user.getOrganization());
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (LinkedHashMap<String, Integer> facet : typefacetsearch.getFacets().values()) {
			for (String key : facet.keySet()) {
				// Map key = facet name, map value = facet count
				if (key.endsWith("collection")) {
					collections.put(key.toLowerCase(), facet.get(key));
				} else if (key.equalsIgnoreCase("action") || 
                           key.equalsIgnoreCase("agent") || 
                           key.equalsIgnoreCase("event") || 
                           key.equalsIgnoreCase("place")) {
					contextObjects.put(key.toLowerCase(), facet.get(key));
				} else {
					contents.put(key.toLowerCase(), facet.get(key));
				}
			}
		}
		contentTypeMap.put("document", contents);
		contentTypeMap.put("collection", collections);
		contentTypeMap.put("contextobject", contextObjects);
    	return contentTypeMap;
    }
    
    @HandlesEvent("getAjaxSearchFacets")
    public Resolution getAjaxSearchFacets() {

    	Search ajaxFacets = this.getContext().getSearch();
    	HashMap<String, Object> json = new HashMap<String, Object>(); 

    	HashMap<String, Vector<String>> fields = new HashMap<String, Vector<String>>();
    	HashMap<String, Vector<String>> freetext = new HashMap<String, Vector<String>>();
    	Vector<String> text = new Vector<String>();    	
    	
        if (search == null) {
        	search = new Search();
        } 
        
        if (this.previousSearch != null) {
        	search = this.getContext().getSearch();
        	if (!filter.isEmpty()) {					
        		// Append to existing filters
        		for (String key : this.filter.keySet()) {
        			if (removeFilter) {
        				search.removeFacetFilter(key,this.filter.get(key));
        			} else {
        				search.addFacetFilter(key,this.filter.get(key));
        			}
        		}
        	} else if (removeAll) {
        		search.setFacetFilters(new HashMap<String, Vector<String>>());
        	}
        }
        
        // To make all objects consistent
        if (ajaxFacets.getFreetext() != null) {
        	text.add(ajaxFacets.getFreetext());
        	freetext.put("freetext", text);
        }
        // To make all objects consistent
        for (SearchFormField f : ajaxFacets.getFields().values()) {
        	fields.put(f.getName(), f.getValue());
        }
        
        getContext().setSearch(ajaxFacets);
        json.put("resultcount", ajaxFacets.getTotalSize());
        json.put("facets", ajaxFacets.getFacets());
        json.put("facetfilters", ajaxFacets.getFilters());
        json.put("freetext", freetext);
        json.put("fields", fields);
        String jsonstring = new flexjson.JSONSerializer().deepSerialize(json);
              	
        return new StreamingResolution(MIME_JS, jsonstring);
    }
        
    private TreeNode formMainNode(){
    	
		String rootPID= getUserOrganizationName().toUpperCase() + rootpid;	
			 
		HashMap<String, String> filters = null;
		filters = new HashMap<String, String>();
        filters.put("c.type", "*collection");
        List<TreeNode> elements = new ArrayList<TreeNode>();
         
        elements = Osa.searchManager.getChildren(rootPID,  RepositoryManager.DATATYPES_MAP_REVERSE.get(RepositoryManager.COLLECTION), this.getContext().getUser(), filters, null);
        for (TreeNode node : elements) {       	 	        	
            node.setChildren(Osa.searchManager.getChildren(node.getId(), RepositoryManager.DATATYPES_MAP_REVERSE.get(RepositoryManager.COLLECTION), this.getContext().getUser(), filters, null));
        }
        TreeNode tree = new TreeNode();
        tree.setId(rootPID);
        tree.setType(RepositoryManager.DATATYPES_MAP_REVERSE.get(RepositoryManager.COLLECTION));
        tree.setName(Osa.searchManager.getItemByPid(rootPID,this.getContext().getUser(), getUserLocale()));
        tree.setChildren(elements);
    
        this.setMainNode(tree);
	       
		return tree;
    	
    }
    
    @HandlesEvent("relationSearch")
    public Resolution relationSearch() throws UnsupportedEncodingException {    
    	String fullListJson = null;
    	Vector<String> facet =  new Vector<String>();
    	HashMap<String, String> filterQuery = new HashMap<String, String>();    
    	User user = this.getContext().getUser();    
    	HashMap<String, String> filters = null;
    	
    	if (this.getContext().getUser() != null) {
    		Search relationSearch = new Search();
    		// If doing filtering for isPartOf, show only collections
    		if(facetName.equalsIgnoreCase("isPartOf")) {
    			filterQuery.put("c.type", "*collection");
    			relationSearch.setFilters(filterQuery);
    		} else if(facetName.equalsIgnoreCase(RepositoryManager.ACTION+"Id") 
    					|| facetName.equalsIgnoreCase(RepositoryManager.AGENT+"Id") 
    					|| facetName.equalsIgnoreCase(RepositoryManager.EVENT+"Id") 
    					|| facetName.equalsIgnoreCase(RepositoryManager.PLACE+"Id")) {
    			facetName = facetName.replace("Id", "");
    			facet.add(facetName);
    			relationSearch.getFacetFilters().put("c.type", facet);
    		}   	
    		
    		relationSearch.setFreetext(URLDecoder.decode(this.getRelationSearchTerm(), "UTF-8")+"*");
    		relationSearch.setSorting("c.title asc, c.preferredName asc");
    		
            for (int z = 0;z < this.getContext().getGui().getSearchConfiguration().getFreetextField().size(); z++) {
                relationSearch.getFreetextSolrFields().add(this.getContext().getGui().getSearchConfiguration().getFreetextField().get(z));
            }
    		
            try {
            	search = Osa.searchManager.search(relationSearch, user, null);
           	} catch (Exception e) {
           	    logger.error(e);
            }
    		
    		TreeNode node = new TreeNode(); 	
        	node.setName(Osa.defaultRoot);
        	node.setType(RepositoryManager.DATATYPES_MAP_REVERSE.get(RepositoryManager.COLLECTION));
        	List<TreeNode> allNodes = new ArrayList<>();    

        	if (this.getMainNode() != null){
        		allNodes = this.getMainNode().getChildren();
        	} else {
        		allNodes = this.formMainNode().getChildren();
        	}			
        	
        	for (TreeNode midNode : allNodes) {		
        		for (SearchResult results : search.getResults()) {	
        			HashMap<String, Object> resultEntry = results.getMetadataFields();
        			String resultPID = (String) resultEntry.get(SolrManager.SOLRFIELD_Pid);
        			if (midNode.getId().equalsIgnoreCase(resultPID)){
        				TreeNode tree = new TreeNode();
                		tree.setId(resultPID); 
                		tree.setLoadOnDemand(false);
                		tree.setType(RepositoryManager.DATATYPES_MAP_REVERSE.get(RepositoryManager.COLLECTION));
                		tree.setName(Osa.searchManager.getItemByPid(resultPID, this.getContext().getUser(),getUserLocale()));
                		tree.setChildren(Osa.searchManager.getChildren(resultPID, RepositoryManager.DATATYPES_MAP_REVERSE.get(RepositoryManager.COLLECTION), this.getContext().getUser(), filters, null));
                		node.addChild(tree);
        			}
        			for (TreeNode lowNode : midNode.getChildren()) {
        				if (lowNode.getId().equalsIgnoreCase(resultPID)) {
        					midNode.setLoadOnDemand(false);
                    		node.addChild(midNode);
        				}
        			}
        		}
        	}
    		
    		fullListJson = new flexjson.JSONSerializer().deepSerialize(node);
    	}
    
    	return new StreamingResolution(MIME_JS, fullListJson);
    	
    }
    	
    // Generated getters & setters below

    public void setSearch(Search search) {
    	this.search = search;
    }
    
    public Search getSearch() {
    	return this.search;
    }

	public String getSearchResultType() {
		return searchResultType;
	}

	public void setSearchResultType(String searchResultType) {
		this.searchResultType = searchResultType;
	}

	public String getsSearch() {
		return sSearch;
	}

	public void setsSearch(String sSearch) {
		this.sSearch = sSearch;
	}

	public String getsEcho() {
		return sEcho;
	}

	public void setsEcho(String sEcho) {
		this.sEcho = sEcho;
	}

	public int getiDisplayStart() {
		return iDisplayStart;
	}

	public void setiDisplayStart(int iDisplayStart) {
		this.iDisplayStart = iDisplayStart;
	}

	public int getiDisplayLength() {
		return iDisplayLength;
	}

	public void setiDisplayLength(int iDisplayLength) {
		this.iDisplayLength = iDisplayLength;
	}

	public int getiSortCol_0() {
		return iSortCol_0;
	}

	public void setiSortCol_0(int iSortCol_0) {
		this.iSortCol_0 = iSortCol_0;
	}

	public String getsSortDir_0() {
		return sSortDir_0;
	}

	public void setsSortDir_0(String sSortDir_0) {
		this.sSortDir_0 = sSortDir_0;
	}

	public HashMap<String,String> getFilter() {
		return filter;
	}

	public void setFilter(HashMap<String,String> filter) {
		this.filter = filter;
	}

	public boolean isRemoveFilter() {
		return removeFilter;
	}

	public void setRemoveFilter(boolean removeFilter) {
		this.removeFilter = removeFilter;
	}

	public int getPreviousSearch() {
		return previousSearch;
	}

	public void setPreviousSearch(int previousSearch) {
		this.previousSearch = previousSearch;
	}

	public boolean isRemoveAll() {
		return removeAll;
	}

	public void setRemoveAll(boolean removeAll) {
		this.removeAll = removeAll;
	}

	public String getRelationSearchTerm() {
		return relationSearchTerm;
	}

	public void setRelationSearchTerm(String relationSearchTerm) {
		this.relationSearchTerm = relationSearchTerm;
	}

	public String getFacetName() {
		return facetName;
	}

	public void setFacetName(String facetName) {
		this.facetName = facetName;
	}

	public boolean isAjax() {
		return ajax;
	}

	public void setAjax(boolean ajax) {
		this.ajax = ajax;
	}

	public String getRootpid() {
	    return rootpid;
    }

    public void setRootpid(String rootpid) {
        this.rootpid = rootpid;
    }
    
    public void setMainNode(TreeNode mainNode) {
    	this.mainNode = mainNode;
    }
    
    public TreeNode getMainNode() {
    	return mainNode;
    }
}
