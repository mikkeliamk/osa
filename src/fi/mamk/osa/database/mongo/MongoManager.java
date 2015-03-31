package fi.mamk.osa.database.mongo;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.sourceforge.stripes.action.LocalizableMessage;

import org.apache.log4j.Logger;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.map.DeserializationConfig.Feature;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;

import fi.mamk.osa.auth.User;
import fi.mamk.osa.bean.MetaDataElement;
import fi.mamk.osa.core.DestructionPlan;
import fi.mamk.osa.core.Osa;
import fi.mamk.osa.database.DatabaseManager;
import fi.mamk.osa.fedora.RepositoryManager;
import fi.mamk.osa.ui.BasketItem;
import fi.mamk.osa.ui.DisposalListItem;
import flexjson.JSONDeserializer;

public class MongoManager extends DatabaseManager {
    
    private static final Logger logger = Logger.getLogger(MongoManager.class);
	public static String EVENT_REMOVE = "remove";
	public static String EVENT_RENAME = "rename";
	public static String EVENT_ADD 	  = "add";
	public static String EVENT_UPDATE = "update";
	
	// Collections names
	private static final String DB_BASKET	   = "basket";
	private static final String DB_DISPOSAL	   = "disposal";
	private static final String DB_LATESTEDITS = "latestEdits";
	private static final String DB_METADATA	   = "metadatafiles";
	
	// Names of arrays holding the documents in collections
	private static final String ARRAY_BASKET	  = "pids";
	private static final String ARRAY_DISPOSAL	  = "disposalItems";
	private static final String ARRAY_LATESTEDITS = "pids";
	private static final String ARRAY_METADATA	  = "metadatafiles";
	
	//Only for testing purposes, for final implementation these should be specified in conf.
	private String Host;
	private int port;
	private String defaultDB;
	private String defaultCollection;
	
	private MongoClient mongo;
	private DB db;
	private DBCollection table;
	
	public MongoManager(String host, int port, String dbname, String collection) {
		this.Host = host;
		this.port = port;
		this.defaultDB = dbname;
		this.defaultCollection = collection;
		try {
			this.mongo = new MongoClient(this.Host,this.port);
			this.db = mongo.getDB(this.defaultDB);
			this.table = db.getCollection(this.defaultCollection);
			
		} catch (UnknownHostException e) {
		    logger.error("MongoManager initializing error, "+e);
		}
		
	}
	
	public void close() {
	    this.mongo.close();
	}
	
	@Override
	public boolean testConnection() {
		// TODO Auto-generated method stub
		return super.testConnection();
	}
	
	public void selectDB(String db) {
		this.db = mongo.getDB(db);	
	}
	
	public void selectCollection(String collection) {
		this.table = db.getCollection(collection);
	}
	
	public void insertObject(String json) {
		this.selectCollection(defaultCollection);
		DBObject dbocject = (DBObject) JSON.parse(json);
		this.table.insert(dbocject);
	}
	
	public void removeObject(String objectid) {
		this.selectCollection(defaultCollection);
	    BasicDBObject query = new BasicDBObject();
	    query.put("_id", new ObjectId(objectid));
	    table.remove(query);
	}
	
	public String getFeedbackSize(){
		this.selectCollection(defaultCollection);	
		long feedbackSize = table.count();		
		return String.valueOf(feedbackSize+1);
	}
	
	public String listItems() {
		
		this.selectCollection(defaultCollection);
		DBCursor cursor = table.find();
		String jsonResult = "[";	
		while (cursor.hasNext()) {
			jsonResult += cursor.next().toString() +",";
		}

		cursor.close();		
		jsonResult += "]";			
		return jsonResult;
	}
	
	public String insertToBasket(User user, String pid) {
		this.selectCollection(DB_BASKET);
		
		String success = "";
		String username = user.getOrganization().getName()+":"+user.getMail();
		String objName = Osa.searchManager.getItemByPid(pid, user, user.getLocale()).replace("[", "").replace("]", "");
		String objType = Osa.searchManager.getObjectType(pid, user);
		
		Vector<HashMap<String, String>> pidVector = new Vector<HashMap<String, String>>();
		HashMap<String, String> pidDatamap = new HashMap<String, String>();
		
		BasicDBObject query = new BasicDBObject();
		DBCursor cursor = table.find(new BasicDBObject("username", username), new BasicDBObject(ARRAY_BASKET,1).append("_id",false));
		// If basket already has items
		if (cursor.size() != 0) {
			success = this.updateBasket(user, pid, objName);
		} else {
			// If basket is empty, create new
			pidDatamap.put("pid", pid);
			pidDatamap.put("name", objName);
			pidDatamap.put("type", objType);
			pidVector.add(pidDatamap);
			
			query.put("username", username);
			query.put(ARRAY_BASKET, pidVector);
			
			this.table.insert(query);
			success = "success";
		}
		cursor.close();
		return success;
	}
	
	public String updateBasket(User user, String newpid, String objname) {
		this.selectCollection(DB_BASKET);
		String username = user.getOrganization().getName()+":"+user.getMail();
		String success = "";
		String objType = Osa.searchManager.getObjectType(newpid, user);
		
		if (!checkIfExists(user, newpid)) {
			BasicDBObject updateQuery = new BasicDBObject("username", username);
			BasicDBObject updateCommand = new BasicDBObject();
			
			updateCommand.put("$push", 
							new BasicDBObject(ARRAY_BASKET, new BasicDBObject("pid",newpid)
							.append("name", objname)
							.append("type", objType)));
			this.table.update(updateQuery, updateCommand);
			success = "success";
		} else {
			success = "alreadyexists";
		}
		return success;
	}
	
	/**
     * @param user      name of the user (cn from ldap)
     * @param pid       pid of the object to be deleted
     */
    public void deleteFromBasket(User user, String pid) {
        this.selectCollection(DB_BASKET);
        String username = user.getOrganization().getName()+":"+user.getMail();
        // Delete from this user
        BasicDBObject updateQuery = new BasicDBObject("username", username);
        // from pids array where pid = given pid
        BasicDBObject updateCommand = new BasicDBObject(ARRAY_BASKET, new BasicDBObject("pid", pid));
        this.table.update(updateQuery, new BasicDBObject("$pull", updateCommand));
    }
    
    public void syncBaskets(String pid, String event, String newName) {
        this.selectCollection(DB_BASKET);
        BasicDBObject updateQuery = new BasicDBObject();
        // Deletes all entries with given pid. The second (multi) true flag enables multi update
        if (event.equals(EVENT_REMOVE)) {
	        BasicDBObject updateCommand = new BasicDBObject(ARRAY_BASKET, new BasicDBObject("pid", pid));
	        this.table.update(updateQuery, new BasicDBObject("$pull", updateCommand), false, true);
        }
        else if (event.equals(EVENT_RENAME)) {
        	updateQuery = new BasicDBObject("pids.pid", pid);
        	BasicDBObject updateCommand = new BasicDBObject("pids.$.name", newName);
	        this.table.update(updateQuery, new BasicDBObject("$set", updateCommand), false, true);
        }
    }
    
	/**
	 * @param user		User object
	 * @param pid		pid of the object
	 * @return true or false
	 */
	public boolean checkIfExists(User user, String pid) {
		this.selectCollection(DB_BASKET);
		String username = user.getOrganization().getName()+":"+user.getMail();
		boolean exists = false;		
		DBCursor cursor = table.find(new BasicDBObject("username", username), new BasicDBObject(ARRAY_BASKET,1).append("_id",false));
		
		while (cursor.hasNext()) {
			if (cursor.next().get(ARRAY_BASKET).toString().contains(pid)) {
				exists = true;
			}
		}
		cursor.close();
		return exists;
	}
	
	public List<BasketItem> getBasketItems(User user) {
		this.selectCollection(DB_BASKET);
		String username = user.getOrganization().getName()+":"+user.getMail();
		DBCursor cursor = table.find(new BasicDBObject("username", username), new BasicDBObject(ARRAY_BASKET,1).append("_id",false));
		List<BasketItem> basketItems = new ArrayList<BasketItem>();
		
		if (cursor.size() > 0) {
			while (cursor.hasNext()) {
				List<Map<String,String>> result = new JSONDeserializer<List<Map<String,String>>>().deserialize(cursor.next().get(ARRAY_BASKET).toString());
		        for (int i = 0; i < result.size(); i++) {
			        BasketItem basketItem = new BasketItem();
			        basketItem.setPid(result.get(i).get("pid"));
			        basketItem.setType(result.get(i).get("type"));
			        basketItem.setName(result.get(i).get("name"));
			        basketItems.add(basketItem);
		        }
			}
		}
		cursor.close();
		return basketItems;
	}
	
	/**
	 * @param user		name of the user (cn from ldap)
	 * 
	 * @return Content of the basket in json format
	 */
	public String listBasketContent(User user) {
		this.selectCollection(DB_BASKET);
		String username = user.getOrganization().getName()+":"+user.getMail();
		String jsonresult = "";
		// Exclude database row id from results with .append("_id",false)
		DBCursor cursor = table.find(new BasicDBObject("username", username), new BasicDBObject(ARRAY_BASKET,1).append("_id",false));
    	
		if (cursor.size() > 0) {
			while (cursor.hasNext()) {
				jsonresult = "["+cursor.next().toString();
			}
		}

		if (cursor.size() > 0) {
			jsonresult += "]";
		}
		cursor.close();
		return jsonresult;
	}
	
	/** Counts the amount of objects in given user's basket.
	 * 
	 * @param	user	Username, ldap cn
	 * @return	Integer number of items in basket
	 */
	public int getBasketSize(User user) {
	    this.selectCollection(DB_BASKET);
	    String username = user.getOrganization().getName()+":"+user.getMail();
	    DBCursor cursor = table.find(new BasicDBObject("username", username), new BasicDBObject(ARRAY_BASKET,1).append("_id",false));
	    int size = 0;
	    
	    while (cursor.hasNext()) {
	        Map<String,List<Map<String,String>>> result = new JSONDeserializer<Map<String,List<Map<String,String>>>>().deserialize(cursor.next().toString());
	        List<Map<String,String>> map = result.get(ARRAY_BASKET);
	        size += map.size();
	    }
	    cursor.close();
		return size;
	}
	
	public void clearTable(String tablename) {
		this.selectCollection(tablename);
		this.table.drop();
		logger.info("MongoManager: Table "+table.getName()+ " dropped.");
	}

	//************************************************************
	// METHODS FOR DISPOSAL LIST
	//************************************************************
	public boolean insertToDisposalList(User user, String pid, String date, String title) {
        this.selectCollection(DB_DISPOSAL);
        
        boolean success = false;
        long timestamp = 0;
        String username = "";
        String objName = "";
        String objType = "";
        String organization = "";

        if (user != null) {
            objName = Osa.searchManager.getItemByPid(pid, user, user.getLocale()).replace("[", "").replace("]", "");
            objType = Osa.searchManager.getObjectType(pid, user);
            username = user.getCn();
            organization = user.getOrganization().getName();
            
        } else {
            // when scheduled task executed, user not defined
            objName = title;
            // get type from pid
            String pidType = pid.split(":")[1].split("-")[0];
            if (RepositoryManager.DATATYPES_MAP.containsKey(pidType)) {
                objType = RepositoryManager.DATATYPES_MAP.get(pidType);
            }
            // automatic
            username = DestructionPlan.DESTRUCTIONREASON_Scheduled;
            // get organization from pid
            organization = pid.split(":")[0].toLowerCase();
        }
        
        Vector<HashMap<String, String>> pidVector = new Vector<HashMap<String, String>>();
		HashMap<String, String> pidDatamap = new HashMap<String, String>();
        
        BasicDBObject query = new BasicDBObject();
        DBCursor cursor = table.find(new BasicDBObject("organization", organization), new BasicDBObject(ARRAY_DISPOSAL,1).append("_id",false));
        
        if (cursor.size() != 0) {
            success = this.updateDisposalList(pid, objName, date, user);
            
        } else {
        	try {
    			timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date).getTime();
    		} catch (ParseException e1) {
    			e1.printStackTrace();
    		}
        	pidDatamap.put("pid", pid);
			pidDatamap.put("objname", objName);
			pidDatamap.put("objtype", objType);
			pidDatamap.put("username", username);
			pidDatamap.put("date", date);
			pidDatamap.put("timestamp", String.valueOf(timestamp));
			pidVector.add(pidDatamap);
			
			query.put("organization", organization);
			query.put(ARRAY_DISPOSAL, pidVector);
			
            this.table.insert(query);
            success = true;
        }
        cursor.close();
        return success;
    }
	
	public boolean updateDisposalList(String pid, String objectName, String disposalDate, User user) {
		this.selectCollection(DB_DISPOSAL);
		
		boolean success = false;
		boolean exists 	= false;
		long timestamp 	= 0;
		String organization = "";
		String username = "";
		String objType = "";
		
		if (user != null) {
		    organization = user.getOrganization().getName();
		    username = user.getCn();
		    objType = Osa.searchManager.getObjectType(pid, user);
		} else {
            // get organization from pid
            organization = pid.split(":")[0].toLowerCase();
            //
            username = DestructionPlan.DESTRUCTIONREASON_Scheduled;
            // get type from pid
            String pidType = pid.split(":")[1].split("-")[0];
            if (RepositoryManager.DATATYPES_MAP.containsKey(pidType)) {
                objType = RepositoryManager.DATATYPES_MAP.get(pidType);
            }
		}
				
		DBCursor existsCursor = table.find(new BasicDBObject("organization", organization), new BasicDBObject(ARRAY_DISPOSAL,1).append("_id",false));
		
		while (existsCursor.hasNext()) {
			if (existsCursor.next().get(ARRAY_DISPOSAL).toString().contains(pid)){
				exists = true;
			}
		}
		existsCursor.close();
		
		if (!exists) {
			try {
				timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(disposalDate).getTime();
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			
			BasicDBObject updateQuery = new BasicDBObject("organization", organization);
			BasicDBObject updateCommand = new BasicDBObject();

			updateCommand.put("$push", 
							new BasicDBObject(ARRAY_DISPOSAL, new BasicDBObject("pid",pid)
							.append("objname", objectName)
							.append("objtype", objType)
							.append("username", username)
							.append("date", disposalDate)
							.append("timestamp", String.valueOf(timestamp))));
			
			this.table.update(updateQuery, updateCommand);
			success = true;
		}
		
		return success;
	}
	
    /**
     * Remove object from disposal list
     * @param pid       	pid of the object to be deleted
     * @param organization  Name of the user organization
     */
    public void deleteFromDisposalList(String pid, String organization) {
        this.selectCollection(DB_DISPOSAL);
        BasicDBObject updateQuery = new BasicDBObject("organization", organization);
        BasicDBObject updateCommand = new BasicDBObject(ARRAY_DISPOSAL, new BasicDBObject("pid", pid));
        this.table.update(updateQuery, new BasicDBObject("$pull", updateCommand));
    }
        
    public List<DisposalListItem> getDisposalListItems(String organization){
    	this.selectCollection(DB_DISPOSAL);
    	List<DisposalListItem> disposalListItems = new ArrayList<DisposalListItem>();
        DBCursor cursor = table.find(new BasicDBObject("organization", organization), new BasicDBObject("_id", 0));
        
        if (cursor.size() > 0) {
            while (cursor.hasNext()) {
            	List<Map<String,String>> results = new JSONDeserializer<List<Map<String,String>>>().deserialize(cursor.next().get(ARRAY_DISPOSAL).toString());
	            for (int i = 0; i < results.size(); i++) {
	            	Map<String, String> item = results.get(i);
	            	DisposalListItem disposalItem = new DisposalListItem();
	            	
	            	disposalItem.setPid(item.get("pid"));
	            	disposalItem.setObjectName(item.get("objname"));
	            	disposalItem.setObjectType(item.get("objtype"));
	            	disposalItem.setDeleter(item.get("username"));
	            	disposalItem.setDisposalDateString(item.get("date"));
	            	disposalItem.setDisposalDateTimestamp(Long.parseLong(item.get("timestamp")));
	            	
					disposalListItems.add(disposalItem);
	            }
            }
        }
        cursor.close();
        return disposalListItems;
    }
    
    public int getDisposalListSize(String organization) {
    	this.selectCollection(DB_DISPOSAL);
    	//TODO Update mongo jar to 2.2 and use aggregation framework
    	DBCursor cursor = table.find(new BasicDBObject("organization", organization), new BasicDBObject(ARRAY_DISPOSAL,1).append("_id",false));
	    int size = 0;
	    
	    while (cursor.hasNext()) {
	        Map<String,List<Map<String,String>>> result = new JSONDeserializer<Map<String,List<Map<String,String>>>>().deserialize(cursor.next().toString());
	        List<Map<String,String>> map = result.get(ARRAY_DISPOSAL);
	        size += map.size();
	    }
	    cursor.close();
    	return size;
    }
    
	//************************************************************
	// METHODS FOR LATEST EDITED OBJECTS
	//************************************************************
    
    public void addToLatestEdited(User user, String pid, String objectName, String objectId, String eventType) {
    	this.selectCollection(DB_LATESTEDITS);
    	String username = user.getOrganization().getName()+":"+user.getMail();
    	String objName = objectName;
    	
    	Vector<HashMap<String, String>> pidVector = new Vector<HashMap<String, String>>();
		HashMap<String, String> pidDatamap = new HashMap<String, String>();
		
		BasicDBObject query = new BasicDBObject();
		DBCursor cursor = table.find(new BasicDBObject("username", username), new BasicDBObject(ARRAY_LATESTEDITS,1).append("_id",false)).sort(new BasicDBObject("editDate",1));
		if (cursor.size() != 0) {
			BasicDBObject updateQuery = new BasicDBObject("username", username);
			BasicDBObject updateCommand = new BasicDBObject();
			while (cursor.hasNext()) {
				// If updated object is not already on the list, update list
				if (!cursor.next().get(ARRAY_LATESTEDITS).toString().contains(pid)) {
							
					updateCommand.put("$push", new BasicDBObject(ARRAY_LATESTEDITS, 
							new BasicDBObject("pid",pid)
								.append("name", objName)
								.append("id", objectId)
								.append("event", eventType)
								.append("eventLocal", new LocalizableMessage("general."+eventType).getMessage(user.getLocale()))
								.append("editDate", String.valueOf(System.currentTimeMillis()))));
					// If the list has 12 items, remove last one (oldest)
					if (getLatestEditedCount(user) == 12) {
						BasicDBObject removeLastCommand = new BasicDBObject();
						removeLastCommand.put("$pop", new BasicDBObject(ARRAY_LATESTEDITS, -1));
						this.table.update(updateQuery, removeLastCommand);
					}
			
					this.table.update(updateQuery, updateCommand);
				} else {
					//TODO Refactor and use $set to update...
					updateCommand.put("$pull", new BasicDBObject(ARRAY_LATESTEDITS, new BasicDBObject("pid",pid)));
					this.table.update(updateQuery, updateCommand);
					
					updateCommand = new BasicDBObject();
					updateCommand.put("$push", new BasicDBObject(ARRAY_LATESTEDITS, 
							new BasicDBObject("pid",pid)
								.append("name", objName)
								.append("id", objectId)
								.append("event", eventType)
								.append("eventLocal", new LocalizableMessage("general."+eventType).getMessage(user.getLocale()))
								.append("editDate", String.valueOf(System.currentTimeMillis()))
								));
					this.table.update(updateQuery, updateCommand);
				}
			}
		// If list is empty, create new vector of objects
		} else {
			pidDatamap.put("pid", pid);
			pidDatamap.put("name", objName);
			pidDatamap.put("id", objectId);
			pidDatamap.put("event", eventType);
			pidDatamap.put("eventLocal", new LocalizableMessage("general."+eventType).getMessage(user.getLocale()));
			pidDatamap.put("editDate", String.valueOf(System.currentTimeMillis()));
			pidVector.add(pidDatamap);
			
			query.put("username", username);
			query.put(ARRAY_LATESTEDITS, pidVector);
			
			this.table.insert(query);
		}
		
		cursor.close();
    }
    
    /** Gets latest objects user has edited
     * @param	user	Username
     * @return	Object info in JSON
     */
    public String getLatestEdited(User user) {
    	String jsonresult 	= "";
    	String username 	= user.getOrganization().getName()+":"+user.getMail();
		this.selectCollection(DB_LATESTEDITS);
		// Exclude database row id from results with .append("_id",false)
		DBCursor cursor = table.find(new BasicDBObject("username", username), new BasicDBObject(ARRAY_LATESTEDITS,1).append("_id",false)).sort(new BasicDBObject("editDate", 1));
		
		if (cursor.size() > 0) {
			while (cursor.hasNext()) {
				jsonresult = "["+cursor.next().toString();
			}
		}

		if (cursor.size() > 0) {
			jsonresult += "]";
		}
		
		cursor.close();
		return jsonresult;
    }
    
    public int getLatestEditedCount(User user) {
	    this.selectCollection(DB_LATESTEDITS);
	    String username = user.getOrganization().getName()+":"+user.getMail();
	    DBCursor cursor = table.find(new BasicDBObject("username", username), new BasicDBObject(ARRAY_LATESTEDITS,1).append("_id",false));
	    int size = 0;
	    
	    while (cursor.hasNext()) {
	        Map<String,List<Map<String,String>>> result = new JSONDeserializer<Map<String,List<Map<String,String>>>>().deserialize(cursor.next().toString());
	        List<Map<String,String>> map = result.get(ARRAY_LATESTEDITS);
	        size += map.size();
	    }
	    
	    cursor.close();
		return size;
	}
    
    //************************************************************
    // METHODS FOR METADATA FILES
    //************************************************************
    
    public void addMetadataFile(User user, String filename, LinkedHashMap<String, MetaDataElement> map, boolean massEdit) {
        String username = user.getOrganization().getName()+":"+user.getMail();
    	this.selectCollection(DB_METADATA);
    	
    	BasicDBObject updateQuery = new BasicDBObject("username", username);
    	BasicDBObject updateCommand = new BasicDBObject();
    	BasicDBObject query = new BasicDBObject();
    	
    	DBCursor cursor = table.find(updateQuery, new BasicDBObject(ARRAY_METADATA,1).append("_id",false));
    
    	Vector<Object> metadatafiles = new Vector<Object>();
    	
    	MetaDataElement mdElem = new MetaDataElement();
    	mdElem.setName("filename");
    	mdElem.setValue(filename);
    	map.put("filename", mdElem);
    
    	Object serializedMap = null;
    
    	// User already has at least one file added to metadata list
    	if (cursor.size() != 0) {
    	    // Given file is not yet on the list, update db with the file
    	    if (!cursor.next().get(ARRAY_METADATA).toString().contains(filename)) {
                serializedMap = JSON.parse(new flexjson.JSONSerializer().deepSerialize(map));
                updateCommand.put("$push", new BasicDBObject(ARRAY_METADATA, serializedMap));
                this.table.update(updateQuery, updateCommand);
    	    } 
    	    // Given file is already on the list, update it's metadatas
    	    else {
    	        
                LinkedHashMap<String, MetaDataElement> oldMap = getMetadataFile(user, filename);
                // file has already data in mongo
                if (oldMap.size() > 0 && massEdit) {
                    // keep not modified metadata in database
                    for (Entry<String, MetaDataElement> entry : oldMap.entrySet()) {
                        if (!map.containsKey(entry.getKey())) {
                            map.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
    			
                serializedMap = JSON.parse(new flexjson.JSONSerializer().deepSerialize(map));
                updateCommand = new BasicDBObject();
                // Appends filename to filter so query is "get from this user where filename is this"
                updateQuery.append(ARRAY_METADATA+".filename.value", filename);
                updateCommand.put("$set", new BasicDBObject(ARRAY_METADATA+".$", serializedMap));
                  
                this.table.update(updateQuery, updateCommand, true, false);
            }
    	} 
    	// User does not have any files added to metadata list
    	else {
            serializedMap = JSON.parse(new flexjson.JSONSerializer().deepSerialize(map));
            metadatafiles.add(serializedMap);
            query.put("username", username);
            query.put(ARRAY_METADATA, metadatafiles);
            this.table.insert(query);
    	}
    	
    	cursor.close();
    	
    }
    
    public LinkedHashMap<String, MetaDataElement> getMetadataFile(User user, String filename) {
        String username = user.getOrganization().getName()+":"+user.getMail();
    	this.selectCollection(DB_METADATA);
    	DBCursor cursor = table.find(new BasicDBObject("username", username), 
    									new BasicDBObject(ARRAY_METADATA, 
											new BasicDBObject("$elemMatch", 
												new BasicDBObject("filename.value", filename))));
    	
    	List<LinkedHashMap<String, MetaDataElement>> resultSet = new ArrayList<LinkedHashMap<String, MetaDataElement>>();
    	
    	ObjectMapper objMapper = new ObjectMapper();
    	// Prevents mapper from throwing exception on fields that are not present in given TypeReference
    	// In this case returned JSON has field "class" that is not found in MetaDataElement class
    	// Note: This is for jackson version 1.x.x. For 2.x use DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
    	objMapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    	
    	while (cursor.hasNext()) {
    		// Deserializes returned JSON to given data structure
    		resultSet = objMapper.convertValue(cursor.next().get(ARRAY_METADATA), new TypeReference<List<LinkedHashMap<String,MetaDataElement>>>() { });
    	}
    	if (resultSet == null || resultSet.isEmpty()) {
    		return new LinkedHashMap<String, MetaDataElement>();
    	}
    	// Remove filename entry. Can be left in if needed.
    	resultSet.get(0).remove("filename");
    	cursor.close();
    	return resultSet.get(0);
    	
    }
    
    public void removeMetadataFile(User user, String filename) {
        String username = user.getOrganization().getName()+":"+user.getMail();
        this.selectCollection(DB_METADATA);
        
        // remove file data
        BasicDBObject updateObject = new BasicDBObject().append("$pull", new BasicDBObject(ARRAY_METADATA,new BasicDBObject("filename.value",filename)));
        BasicDBObject updateQuery = new BasicDBObject("username", username);
        table.update(updateQuery, updateObject);
    }
    
    public boolean checkIfHasMetadata(User user, String filename) {
        String username = user.getOrganization().getName()+":"+user.getMail();
    	this.selectCollection(DB_METADATA);
    	boolean hasMetadata = false;
    	
    	DBCursor cursor = table.find(new BasicDBObject("username", username), new BasicDBObject(ARRAY_METADATA,1).append("_id",false));
    	
    	while (cursor.hasNext()) {
    		if (cursor.next().get(ARRAY_METADATA).toString().contains(filename)) {
    		    hasMetadata = true;
    		}
    	}
    	cursor.close();
    	return hasMetadata;
    }    
}
