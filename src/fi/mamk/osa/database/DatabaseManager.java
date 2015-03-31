package fi.mamk.osa.database;

import java.util.LinkedHashMap;
import java.util.List;
import fi.mamk.osa.auth.User;
import fi.mamk.osa.bean.MetaDataElement;
import fi.mamk.osa.ui.BasketItem;
import fi.mamk.osa.ui.DisposalListItem;

public abstract class DatabaseManager {
	
	private String host;
	private int port;
	private String username;
	private String password;
		
	public boolean testConnection(){
		return true;
	}
	
	public void close(){
    }
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String listItems() {
		return "";
		// TODO Auto-generated method stub
	}

	public void insertObject(String jsonData) {
		// TODO Auto-generated method stub
	}

	public void removeObject(String jsonData) {

	}

	public long getCtxTime(String organization) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setCtxTime(String organization) {
		// TODO Auto-generated method stub
	}
	
	public String getNextId(String parentPid) {
	    return null;
	}
	
    public void initIdGeneration(String parentPid, int id) {
        
    }

	/*
	 * BASKET METHODS
	 */
	public String insertToBasket(User user, String pid) {
		return "";
	}
	
	public String updateBasket(User user, String pid, String objName) {
		return "";
	}
	
	public List<BasketItem> getBasketItems(User user){
		return null;
	}
	
	public String listBasketContent(User user) {
		return "";
	}
	
	public void deleteFromBasket(User user, String pid) {
		
	}
	
	/** Syncs users' baskets if object is removed from fedora or renamed
	 * 
	 * @param pid		Pid of an object that has been deleted/disposed/renamed
	 * @param event		Name of database modification event <b>[delete|rename]</b> 
	 * @param newName	Objects new name, leave null if deleting.
	 */
	public void syncBaskets(String pid, String event, String newName) {
		
	}
	
	public boolean checkIfExists(User user, String pid) {
		return true;
	}
	
	public int getBasketSize(User user) {
		return 0;
	}
	
	public void clearTable(String tablename) {
		
	}
	
	/*
	 * DISPOSAL LIST METHODS
	 */
	/** Inserts item to disposal list
	 * 
	 * @param user	User object of user that has requested the disposal of the item
	 * @param pid	Pid of item
	 * @param date	Disposal date as a string, format yyyy-MM-dd HH:mm:ss	
	 * @return		Boolean success
	 */
	public boolean insertToDisposalList(User user, String pid, String date, String title) {
	    return true;
	}
	public void deleteFromDisposalList(String pid, String organization) {
        
    }
	public String listDisposalListContent() {
        return "";
    }
	public List<DisposalListItem> getDisposalListItems(String organization) {
		return null;
	}
	public int getDisposalListSize(String organization){
		return 0;
	}
	
	public boolean setCurrentuser(String user, String sessionid) {
	    return true;
	}
	
	public String removeCurrentuser(String user) {
        return "";
    }
	
	/*
	 * LATEST EDITED LIST METHODS
	 */
	public void addToLatestEdited(User user, String pid, String objectName, String objectId, String eventType) {
		
	}
	public String getLatestEdited(User user) {
		return "";
	}
	public int getLatestEditedCount(User user){
		return 0;
	}
	
	/*
	 * METADATA FILE METHODS
	 */
	/**	Stores metadata to database for given file
	 * 
	 * @param username	Username
	 * @param filename	Filename
	 * @param map		Metadata fields & values from form
	 */
	public void addMetadataFile(User user, String filename, LinkedHashMap<String, MetaDataElement> map, boolean massEdit){
		
	}
	
	/** Gets metadata from database for given file
	 * 
	 * @param username		Username (cn)
	 * @param filename		File name
	 * @return				LinkedHashMap containing the metadata fields and their values
	 */
	public LinkedHashMap<String, MetaDataElement> getMetadataFile(User user, String filename) {
    	return null;
    }
	
	public boolean checkIfHasMetadata(User user, String filename) {
		return false;
	}
	
	public void removeMetadataFile(User user, String filename) {
	    
	}

	public String getFeedbackSize() {
		return "";
	}

}
