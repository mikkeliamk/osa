package fi.mamk.osa.solr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import net.sourceforge.stripes.action.LocalizableMessage;

import org.apache.commons.lang.text.*;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import fi.mamk.osa.auth.Organization;
import fi.mamk.osa.auth.User;
import fi.mamk.osa.fedora.RepositoryManager;
import fi.mamk.osa.search.Search;
import fi.mamk.osa.search.SearchManager;
import fi.mamk.osa.ui.SearchFormField;
import fi.mamk.osa.ui.SearchResult;
import fi.mamk.osa.ui.TreeNode;

public class SolrManager extends SearchManager
{
    private static final Logger logger = Logger.getLogger(SolrManager.class);

    public static final String SOLRFIELD_Pid           = "PID";
    public static final String SOLRFIELD_DestrDate     = "c.destructionDate";
    public static final String SOLRFIELD_Id            = "c.id";
    public static final String SOLRFIELD_IsPartOf      = "c.isPartOf";
    public static final String SOLRFIELD_PreferredName = "c.preferredName";
    public static final String SOLRFIELD_Title         = "c.title";
    public static final String SOLRFIELD_Type          = "c.type";

    private Solr solr;
    
    public SolrManager() {
    	this.solr = new Solr();
    }

    @Override
    public boolean testSearch() {
        boolean status = false;

        QueryResponse response = solr.query("*:*", new Vector<String>(), new HashMap<String, String>(), null, null, -1, -1, " ");
        if (response != null) {
            SolrDocumentList results = response.getResults();
            
            if (!results.isEmpty()) {
                status = true;
            }
        }
        
        return status;
    }    
    
    @Override
    public List<TreeNode> getChildren(String identifier, String pidType, User user, HashMap<String, String> filters, String accessRights) {
    	HashMap<String, String> searchFilters = (filters == null) ? new HashMap<String, String>() : filters;
    	String queryString = "\""+identifier+"\"";
        ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();
        
        if (accessRights == null || accessRights.isEmpty() || accessRights.equals("")) {
            accessRights = parseAccessRights(user);
        }
        
    	QueryResponse response = solr.query(SOLRFIELD_IsPartOf+":"+queryString, 
    	                                    new Vector<String>(), 
    	                                    searchFilters, 
    	                                    accessRights, 
    	                                    new Vector<String>(), 
    	                                    -1, 
    	                                    Integer.MAX_VALUE, 
    	                                    SOLRFIELD_Id+" asc");
    	
    	if (response != null) {

            SolrDocumentList results = response.getResults();
                        
            for (SolrDocument result : results) {

                TreeNode cNode = new TreeNode();
            	String pid = null;
            	
            	if (result.getFieldValue(SOLRFIELD_Pid) != null) {
                    pid = result.getFieldValue(SOLRFIELD_Pid).toString();
                }
            	
            	if (pid != null) {
    			    
                    if (pidType == null // show all
                        || (pidType.equals("C") && pid.contains(":"+pidType+"-")) // show collections
                        || (!pidType.equals("C") && (pid.contains(RepositoryManager.PID_COLLECTION_CONTEXTS) // show agents, actions, events, places
                                                     || pid.contains(RepositoryManager.PID_COLLECTION_ACTIONS)
                                                     || pid.contains(RepositoryManager.PID_COLLECTION_TOL2008ACTIONS)
                                                     || pid.contains(RepositoryManager.PID_COLLECTION_LYACTIONS)
                                                     || pid.contains(RepositoryManager.PID_COLLECTION_AGENTS)
                                                     || pid.contains(RepositoryManager.PID_COLLECTION_EVENTS)
                                                     || pid.contains(RepositoryManager.PID_COLLECTION_PLACES)
                                                     || pid.contains(":"+pidType)))
                        ) {
                        cNode.setId(result.getFieldValue(SOLRFIELD_Pid).toString());
                    } else {
                        // if pidType is determined (L/E/F/W), 
                        // only PIDs containing pidType value will be shown
                        continue;
                    }
                }

            	// if c.title is not available use c.preferredName
            	if (result.containsKey(SOLRFIELD_Title)) {
            	    String name = convertToString(result.get(SOLRFIELD_Title), SOLRFIELD_Title);
                    cNode.setName(name);
            	} else if (result.containsKey(SOLRFIELD_PreferredName)) {
            	    String name = convertToString(result.get(SOLRFIELD_PreferredName), SOLRFIELD_PreferredName);
                    cNode.setName(name);
                } else {
                    cNode.setName(new LocalizableMessage("header.notitlejs").getMessage(user.getLocale()));
                }
    			
            	if (result.containsKey(SOLRFIELD_Type)) {
            	    String type = convertToString(result.get(SOLRFIELD_Type), SOLRFIELD_Type);
    			    // use collection for all collection types
    			    if (type.contains(RepositoryManager.COLLECTION)) {
    			        type = RepositoryManager.COLLECTION;
    			    }
    			    
    			    String key = RepositoryManager.DATATYPES_MAP_REVERSE.get(type);
    			    cNode.setType(key);
    			}
    			                
                nodes.add(cNode);
            }
    	}  	
    	
    	return nodes;
    }
    
    @Override
    public HashMap<String, String> getDisposableItems(String dateTime) {
        
        HashMap<String, String> searchFilters = new HashMap<String, String>();
        String queryString = "\""+dateTime+"\"";
        // map for key:pid and value:title
        HashMap<String, String> items = new HashMap<String, String>();
        
        QueryResponse response = solr.query(SOLRFIELD_DestrDate+":"+queryString, 
                                            new Vector<String>(), 
                                            searchFilters, 
                                            "", 
                                            new Vector<String>(), 
                                            -1, 
                                            Integer.MAX_VALUE, 
                                            "");
        
        if (response != null) {

            SolrDocumentList results = response.getResults();
            
            for (int i=0; i<results.size(); i++) {

                String pid = null;
                String title = "";
                if (results.get(i).getFieldValue(SOLRFIELD_Pid) != null) {
                    pid = results.get(i).getFieldValue(SOLRFIELD_Pid).toString();
                }
                
                if (results.get(i).containsKey(SOLRFIELD_Title)) {
                    title = convertToString(results.get(i).get(SOLRFIELD_Title), SOLRFIELD_Title);
                } else if (results.get(i).containsKey(SOLRFIELD_PreferredName) && title.equals("")) {
                    title = convertToString(results.get(i).get(SOLRFIELD_PreferredName), SOLRFIELD_PreferredName);
                }
                
                items.put(pid, title);
            }
        }
        
        return items;
    }
    
    @Override
    public String getItemByPid(String identifier, User user, Locale locale){
    	String title = "";
    	
    	QueryResponse response = solr.query(SOLRFIELD_Pid+":"+identifier, 
    	                                    new Vector<String>(), 
    	                                    new HashMap<String, String>(), 
    	                                    parseAccessRights(user), 
    	                                    new Vector<String>(), 
    	                                    -1, -1, "score asc");
    	if (response != null) {
    		SolrDocumentList results = response.getResults();
    		
    		for (int i=0; i<results.size();i++) {
    		    if (results.get(i).containsKey(SOLRFIELD_Title)) {
    				title = convertToString(results.get(i).get(SOLRFIELD_Title), SOLRFIELD_Title);
    			} else if (results.get(i).containsKey(SOLRFIELD_PreferredName) && title.equals("")) {
    			    title = convertToString(results.get(i).get(SOLRFIELD_PreferredName), SOLRFIELD_PreferredName);
    			}
    		}
    	}
    	
    	if (title.isEmpty() || title.equals("")) {
    		title = new LocalizableMessage("header.notitlejs").getMessage(locale);
    	}
    	
    	return title;
    	
    }
        
    @Override
    public String getObjectPath(String identifier, User user, boolean viewOnly){
        
        String org = user.getOrganization().getName().toUpperCase();
    	String path = "<a href='browse.jsp' title='"+org+"'>"+org+"</a> &raquo; ";
    	String origPid = identifier;
    	ArrayList<String> names = new ArrayList<String>();
    	boolean isRoot = false;

    	while (!isRoot && identifier != "") {
    		QueryResponse response = solr.query(SOLRFIELD_Pid+":"+identifier, 
    		                                    new Vector<String>(), 
    		                                    new HashMap<String, String>(), 
    		                                    parseAccessRights(user), 
    		                                    new Vector<String>(), 
    		                                    -1, -1, "score asc");
    		if (response != null) {
        		SolrDocumentList results = response.getResults();
        		String isPartOf = "";
        		String title = "";
        		String pid = "";
        		
        		if (results.size() == 0) {
        		    identifier = isPartOf;
        		}
        		
        		for (int i=0; i<results.size(); i++) {
                    
        		    if (results.get(i).containsKey(SOLRFIELD_IsPartOf)) {
                        isPartOf = convertToString(results.get(i).get(SOLRFIELD_IsPartOf), SOLRFIELD_IsPartOf);
                        if (isPartOf.contains(":root") || user.isAnonymous()) {
                            // show anonymous user only name
                            isRoot = true;
                        }
                    }
        		    
        		    if (results.get(i).containsKey(SOLRFIELD_Title)) {
                        title = convertToString(results.get(i).get(SOLRFIELD_Title), SOLRFIELD_Title);
                    } else if (results.get(i).containsKey(SOLRFIELD_PreferredName)) {
                        title = convertToString(results.get(i).get(SOLRFIELD_PreferredName), SOLRFIELD_PreferredName);
                    }
                    
        		    if (results.get(i).containsKey(SOLRFIELD_Pid)) {
                        pid = results.get(i).getFieldValue(SOLRFIELD_Pid).toString();
                    }
                    
                    identifier = isPartOf;
                    
                    // If found pid equals object that user opened, do not create link or arrow
                    if (pid.equalsIgnoreCase(origPid)) {
                        names.add("<span class='italic'>"+title+"</span>");
                    } else {
                        names.add("<a href='Ingest.action?pid="+pid+"&plainView="+viewOnly+"&view=' title='"+title+"'>"+title+"</a> &raquo; ");
                    }
        		}
        	}
    	}
    	
    	for (int x =(names.size()-1); x>-1; x--) {
    		path += names.get(x);
    	}
    	
    	return path;
    }
    
    @Override
    public String getObjectType(String identifier, User user){
        String type = "";
        
        QueryResponse response = solr.query(SOLRFIELD_Pid+":"+identifier, 
                                            new Vector<String>(), 
                                            new HashMap<String, String>(), 
                                            parseAccessRights(user), 
                                            new Vector<String>(), 
                                            -1, -1, "score asc");
        if (response != null) {
            SolrDocumentList results = response.getResults();
            
            for (SolrDocument doc : results) {
            	type = convertToString(doc.get(SOLRFIELD_Type), SOLRFIELD_Type);
            }
        }
        
        if (type == null || type.isEmpty() || type.equals("")) {
            type = "null";
        }
        return type;
    }
    
    @Override
    /**
     * Get metadata value by object pid and metadata name
     */
    public String getObjectValue(String identifier, String mdName, User user){
        String mdValue = "";
        QueryResponse response = solr.query(SOLRFIELD_Pid+":"+identifier, 
                                            new Vector<String>(), 
                                            new HashMap<String, String>(), 
                                            parseAccessRights(user), 
                                            new Vector<String>(), 
                                            -1, -1, "score asc");
        
        if (response != null) {
            SolrDocumentList results = response.getResults();
            for (SolrDocument doc : results) {
                for (String key : doc.keySet()) {     
                    if (key.equals(mdName)) {
                        mdValue = convertToString(doc.get(key), key);
                        break;
                    }
                }
            }
        }
        
        if (mdValue == null || mdValue.isEmpty() || mdValue.equals("")) {
            mdValue = "";
        }
        return mdValue;
    }
    
    @Override
    public Search search(Search search, User user, Organization organization) throws Exception {
        if (search == null) {
            search = new Search();
        }
        
        if (user != null) {                 // Private search
            organization = user.getOrganization();
        } else if (organization != null) {  // Public search
            // do something
        } else {                            // No known organization or user
            throw new Exception("Search error. Unknown user and organization.");
        }
            
        // TODO Parse query
        String freetext = "";
        if (search.getFreetext() == null || search.getFreetext().equals("")) {
            freetext = "*:*";
        } else {
            freetext = search.getFreetext();
        }
        String query = freetext;

        for (SearchFormField searchFormField : search.getFieldValues()) {
          
            // Generate tokens from field values
        	if (searchFormField.getValue().get(0) != null){
        	
        		String searchTypes = "";
        		for (int i = 0; i < searchFormField.getValue().size(); i++) {
        	        if (i != 0) { searchTypes += " "; }
        	        searchTypes += searchFormField.getValue().get(i);
        	    }
        		
	            String[] tokens = new StrTokenizer(searchTypes, StrMatcher.spaceMatcher(), StrMatcher.quoteMatcher()).getTokenArray();
	          
	            // If there is more than one Solr field bound to form field
	            // include the term as AND (field:value OR field:value ...)
	            // otherwise just include it as simple AND statement.
	            
	            // Multiple Solr fields bound
	            if (searchFormField.getSolrFields().size() > 1) {
	                String innerQuery = " AND (";
	                for (SolrField solrField : searchFormField.getSolrFields()) {
	                    // Multiple tokens: add each token as separate OR-condition unless they're quoted
	                    if (tokens.length > 1) {
	                        for (String token : tokens) {
	                            innerQuery += " OR " + solrField.getName() + ":" + token;
	                        }
	                  
	                        // Single token 
	                    } else {
	                        innerQuery += " OR " + solrField.getName() + ":" + tokens[0];
	                    }
	                }
	                innerQuery = innerQuery.replaceFirst(" OR", "");
	                innerQuery += ")";
	                query += innerQuery;
	          
	                // Single Solr field bound
	            } else {
	              
	                // Multiple tokens: add each token as separate OR-condition unless they're quoted
	                if (tokens.length > 1 
	                    && searchFormField.getType() != SearchFormField.Type.date ) {
	                  
	                	switch (searchFormField.getName()) {
	                		case "archiveClass":
		                        String innerQuery = " AND " + searchFormField.getName() + ":("; 
		                        for (String token : tokens) {               
		                            innerQuery += " " + token;
		                        } 
		                        innerQuery += ")";
		                        query += innerQuery;	                			
	                			break; 
	                			
	                		case SOLRFIELD_Type:
	                			query += " AND (";
	                			int tempCounter = 0;
		                        for (String token : tokens) {
		                        	if (tempCounter == 0) { 
		                        		query += searchFormField.getName() + ":" + token;
		                        	} else {
		                        		query += " OR " + searchFormField.getName() + ":" + token;
		                        	}
		                        	tempCounter++;
		                        }
		                        query += ")";
	                			break;
	                	
	                		default:
		                        for (String token : tokens) {               
		                            query += " AND " + searchFormField.getName() + ":" + token;
		                        } 	                			
	                			break;
	                	}
	              
	                    // Single token
	                } else if (searchFormField.getType() != SearchFormField.Type.date ) {
	                    query += " AND " + searchFormField.getName() + ":" + tokens[0];
	              
	                    // Date range: special case with multiple token
	                } else if (searchFormField.getType() == SearchFormField.Type.date) {
	                    // Create range query for date fields
	                    SimpleDateFormat simpleFormat = new SimpleDateFormat("d.M.yyyy");
	                    Date low = null;
	                    Date high = null;
	                    Calendar calendar = Calendar.getInstance();
	                    try {
	                        low = simpleFormat.parse(searchFormField.getValue().get(0));
	                        calendar.setTime(low);
	                        low = calendar.getTime();
	                        high = simpleFormat.parse(searchFormField.getValue().get(1));
	                        calendar.setTime(high);
	                        calendar.add(Calendar.HOUR, +23); 
	                        calendar.add(Calendar.MINUTE, +59);
	                        calendar.add(Calendar.SECOND, +59);
	                        high = calendar.getTime();                      
	                    } catch (ParseException e) {
	                        logger.error("Date parse error", e);
	                    }
	                    simpleFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	                    
	                    query += " AND " + searchFormField.getName() + ":[" + simpleFormat.format(low) + " TO " + simpleFormat.format(high) + "]";
	                }
	            }
        	}
        }

        // TODO Query fields  
        Vector<String> queryFields = new Vector<String>();
        for(SolrField searchFormField : search.getFreetextSolrFields())
        {
            queryFields.add(searchFormField.getName());
        }
        
        if (false) {  // TODO partial matches
            queryFields.add("*_ngram");          
        }
        if (false) {  // TODO content search
            queryFields.add("content");
        }        
        
        Vector<String> facetFields = new Vector<String>();
        facetFields = search.getFacetFields();
        
        // Filters
        HashMap<String, String> filters = search.getFilters();
     
        // Facets
        for (String facetName : search.getFacetFilters().keySet()) {
            String values = "(";
            String prefix = "+";
            for (String facetValue : search.getFacetFilters().get(facetName)) {
                if ("empty".equals(facetValue)) {
                    values += "[\"\" TO *]";
                    filters.remove("+" + facetName);
                    prefix = "-";
                    break;
                } else {
                    values +=  " AND \"" + facetValue + "\"" ;                  
                }
              
            }
            values += ")";
            if (!values.equals("()")) {
                filters.put(prefix + facetName, values.replaceFirst(" AND ", ""));
            }
        } 

        // Fetch only objects with a type (to exclude content models).
        if (!filters.containsKey(SOLRFIELD_Type)) {
            filters.put(SOLRFIELD_Type, "*");        
        }
        
        // Add context
        filters.put(SOLRFIELD_Pid, organization.getName().toUpperCase()+"\\:*");
        
        // Access rights
        String accessRights = parseAccessRights(user);        
        
        // Query with Solr
        QueryResponse response = solr.query(query, queryFields, filters, accessRights, facetFields, search.getStart(), search.getRows(), search.getSorting());
        SolrDocumentList results = response.getResults();
        search.setQueryTime(response.getQTime());
        search.setTotalSize(results.getNumFound());
	    
        // Parse SolrDocumentList into an ArrayList of HashMaps.
        HashMap<String, String> ancestorPids = new HashMap<String, String>();
        for (SolrDocument doc : results) {
            HashMap<String, Object> row = new HashMap<String, Object>();
                
            // Add columns to row
            for (String key : doc.keySet()) {                               
                row.put(key, doc.get(key));
            }
            SearchResult result = new SearchResult(row);
            search.getResults().add(result);
            if (doc.get("m.isAncestor") != null ) { 
                ancestorPids.put((String) doc.get("m.isAncestor"), null); 
            }
        }

        // Add ancestor titles from pids        
        if (!ancestorPids.isEmpty()) {
            ancestorPids = getAncestorTitles(ancestorPids);
            for (SearchResult result : search.getResults()) {
                result.setAncestorTitle(ancestorPids.get(result.getMetadataFields().get("m.isAncestor")));
            }
        }             
        
		// Parse facets
		search.getFacets().clear();
		Iterator<FacetField> facetsIterator = response.getFacetFields().iterator();
		while (facetsIterator.hasNext()) {
			FacetField facetField = facetsIterator.next();
			if (facetField.getValues() == null) {
				throw new NullPointerException("FacetField values are null");
			}

			// Add facet content
			Iterator<Count> facetValueIterator = facetField.getValues().iterator();
			
			while (facetValueIterator.hasNext()) {
				Count count = facetValueIterator.next();
				if (count.getName() == null) {
					// cases where 'empty' is not allowed
					if ("archiveClass".equals(facetField.getName()) 
							|| "format".equals(facetField.getName())
							|| "type".equals(facetField.getName())) {
					// Add special facet values
					} else if (count.getCount() > 0) {							
						LinkedHashMap<String, Integer> facet = search.getFacets().get(facetField.getName());
						if (facet == null) {
							facet = new LinkedHashMap<String, Integer>();
						}
						facet.put("empty", new Integer((int) count.getCount()));
						search.getFacets().put(facetField.getName(), facet);
					}
				} else  {
					LinkedHashMap<String, Integer> facet = search.getFacets().get(facetField.getName());
					if (facet == null) {
						facet = new LinkedHashMap<String, Integer>();
					}
					facet.put(count.getName(), new Integer((int) count.getCount()));
					search.getFacets().put(facetField.getName(), facet);					
				}
			}        
		}
        return search;
	} 
    
    private HashMap<String, String> getAncestorTitles(HashMap<String, String> pids) {
        if (pids == null || pids.isEmpty()) {
            return null;
        }
        
        String pidList = "";
        for (String pid : pids.keySet()) {
            pidList += " PID:" + pid;
        }
        
        QueryResponse response = solr.query(pidList, new Vector<String>(), new HashMap<String, String>(), "", new Vector<String>(), -1, Integer.MAX_VALUE, "score desc");
        SolrDocumentList results = response.getResults();
        
        // Parse SolrDocumentList into an ArrayList of HashMaps.
        for (SolrDocument doc : results) {          
            pids.put((String)doc.get(SOLRFIELD_Pid),(String) doc.get(SOLRFIELD_Title));
        }

        return pids;
    }       
    
    // Combine filters with OR. Combine rules inside a filter with AND.
    public String parseAccessRights(User user) {
        String accessRights = "";
        Vector<HashMap<String, String>> acRightFilters = user.getAccessRightFilters();
        if (acRightFilters.size() == 0) { // This is an error case. Deny all access rights.
            accessRights = "-*:*";
        } 
        for (int i = 0; i < acRightFilters.size(); i++) {
            if (i == 0 && acRightFilters.size() > 1) {
                accessRights += "(";
            }
            
            HashMap<String, String> acFilter = acRightFilters.get(i);
            if (i >= 1) {
                accessRights += " OR (";
            }
    
            int index = 0;
            for (String key : acFilter.keySet()) {
                accessRights += key + ":" + acFilter.get(key).replace(":", "\\:");
                if (index < (acFilter.keySet().size()-1)) {
                    accessRights += " AND ";
                }
                index++;
            }
            
            if (acRightFilters.size() > 1) {
                accessRights += ")";
            }
        }
        
        return accessRights;
    }

    static public String convertToString(Object value, String solrFieldName) {
        String result = null;
        
        if (value == null) {
            result = null;
        } else if (value instanceof String) {
            result = (String)value;
        } else  if (value instanceof String[]) {
            String[] values = (String[])value;
            if (values.length == 0) {
                result = null;
            } else if (values.length == 1) {
                result = values[0];
            } else {
                logger.error("SolrManager:convertToString() "+solrFieldName+", "+values.length);
            }
        } else if (value instanceof Collection) {
            Collection c = (Collection)value;
            if (c.isEmpty()) {
                result = null;
            } else if (c.size() == 1) {
                Object o = c.iterator().next();
                result = convertToString(o, solrFieldName);
            } else {
                logger.error("SolrManager:convertToString() "+solrFieldName+", "+c.size());                
            }
        } else {
            logger.error("SolrManager:convertToString() "+solrFieldName+", "+value.getClass().getName());
        }
        return result;
    }
    
}
