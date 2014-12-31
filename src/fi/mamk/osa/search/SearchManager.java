package fi.mamk.osa.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import fi.mamk.osa.auth.Organization;
import fi.mamk.osa.auth.User;
import fi.mamk.osa.search.Search;
import fi.mamk.osa.ui.SearchConfiguration;
import fi.mamk.osa.ui.TreeNode;

public abstract class SearchManager {
	
	public static enum ProviderType  {solr};
	
	private ProviderType provider;
	private SearchConfiguration configuration;
	
    
	public abstract Search search(Search search, User user, Organization organization) throws Exception;
	public abstract boolean testSearch();

	public ProviderType getProvider() {
		return provider;
	}
	public String getItemByPid(String pid, User user, Locale locale){
		String title = "";
		return title;
	}
	public List<TreeNode> getChildren(String identifier, String pidType, User user, HashMap<String, String> filters, String accessRights){
		List<TreeNode> nodes = new ArrayList<TreeNode>();
		return nodes;
	}
    public HashMap<String, String> getDisposableItems(String dateTime) {
        HashMap<String, String> items = new HashMap<String, String>();
        return items;
    }
	public void setProvider(ProviderType provider) {
		this.provider = provider;
	}

	public SearchConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(SearchConfiguration configuration) {
		this.configuration = configuration;
	}
	public String getObjectPath(String identifier, User user, boolean viewOnly) {
		return null;
	}
	public String getObjectType(String identifier, User user) {
	    return null;
	}
	public String getObjectValue(String identifier, String mdName, User user) {
        return null;
    }
	public String parseAccessRights(User user) {
	    return null;
	}
}