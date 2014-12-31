package fi.mamk.osa.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import fi.mamk.osa.auth.AuthenticationManager;
import fi.mamk.osa.auth.LdapManager;
import fi.mamk.osa.database.DatabaseManager;
import fi.mamk.osa.database.mongo.MongoManager;
import fi.mamk.osa.database.sql.DatabaseConfiguration;
import fi.mamk.osa.database.sql.mysql.ConnectionManager;
import fi.mamk.osa.database.sql.mysql.MySQLManager;
import fi.mamk.osa.fedora.Fedora3Manager;
import fi.mamk.osa.fedora.RepositoryManager;
import fi.mamk.osa.scheduler.TaskScheduler;
import fi.mamk.osa.search.SearchManager;
import fi.mamk.osa.solr.SolrManager;
import fi.mamk.osa.workflow.WorkflowManager;

/**
 * osa servlet class
 */
public class Osa extends GenericServlet
{
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(Osa.class);

    public static AuthenticationManager authManager;

    public static HashMap<String, DatabaseManager> dbManager = new HashMap<String, DatabaseManager>();
    public static SearchManager searchManager;
    public static RepositoryManager fedoraManager;
    public static WorkflowManager workflowManager;
    public static TaskScheduler taskScheduler;
    
    public static String persistence;
    public static String systemDatabase;
    public static String documentDatabase;
    public static String authentication;
    public static String search;
    public static String logging;
    public static String defaultConfiguration;
    public static String defaultRoot;
    public static String configFolder;
    public static String onkiAccessKey;
    
    public static String tempDirectory;
    public static String dataDirectory;
    public static String logDirectory;
    public static String importDirectory; 
    public static String ingestDirectory;
    public static String failedDirectory;
    public static String uploadDirectory;
    
    // TODO: move to database manager class
    public static DatabaseConfiguration dbConfiguration;
    
    // TODO: move to solr manager class
    public static String solrServerUrl;
	
    /** 
     * 	Initializes Osa instance
     */
    public void init() throws ServletException {
        
        Properties properties = new Properties();
        try {
            
            properties.load(new FileInputStream(getServletContext().getRealPath("/") + this.getInitParameter("config")));
            
            // Directories
            // TODO check if directories exist and are accessible
            tempDirectory = properties.getProperty("osa.directory.temp");
            dataDirectory = properties.getProperty("osa.directory.data");
            logDirectory = properties.getProperty("osa.directory.log");
            importDirectory = properties.getProperty("osa.directory.import"); 
            ingestDirectory = properties.getProperty("osa.directory.ingest");
            failedDirectory = properties.getProperty("osa.directory.failed");
            uploadDirectory = properties.getProperty("osa.directory.upload");            
                        
            // Persistence
            persistence = properties.getProperty("osa.persistence");
            
            switch (persistence) {
        	case RepositoryManager.FEDORA3:
        	    String host = properties.getProperty("osa.fedora.server");
        	    String port = properties.getProperty("osa.fedora.port");
        	    String username = properties.getProperty("osa.fedora.username");
        	    String password = properties.getProperty("osa.fedora.password");
        	    String schemaPath =  getServletContext().getRealPath("/") + "WEB-INF/";
        	    
        		Osa.fedoraManager = new Fedora3Manager(host, port, username, password, schemaPath);
        		
        		// TODO: Check Fedora server status
        		break;
        		
        	case RepositoryManager.FEDORA4:
        	    // TODO: version 4 support
        	    break;
        
        	default:
        		throw new Exception("OSA instance requires a persistence module.");
            }                
            
            // Database
            systemDatabase = properties.getProperty("osa.database.system");
            
            // TODO: Add support for multiple coexisting databases.  
            switch (systemDatabase) {
                case "mysql":
                case "mariadb":
	                Osa.dbConfiguration = new DatabaseConfiguration();			
	                Osa.dbConfiguration.setDbURI(properties.getProperty("osa.mysql.connectionString"));
	                Osa.dbConfiguration.setDbDriverName(properties.getProperty("osa.mysql.driver"));
	                Osa.dbConfiguration.setDbUser(properties.getProperty("osa.mysql.user"));
	                Osa.dbConfiguration.setDbPassword(properties.getProperty("osa.mysql.password"));
	                Osa.dbConfiguration.setDbPoolMinSize(Integer.parseInt(properties.getProperty("osa.mysql.poolMinSize".trim())));
	                Osa.dbConfiguration.setDbPoolMaxSize(Integer.parseInt(properties.getProperty("osa.mysql.poolMaxSize").trim()));
	                Osa.dbConfiguration.setDbTimeout( Integer.parseInt(properties.getProperty("osa.mysql.timeout").trim()) );
	                
	                new ConnectionManager(dbConfiguration);
	                Osa.dbManager.put("sql", new MySQLManager());
	                //logger.info("Database configuration loaded.");	                
					break;
	
				default:
					throw new Exception("OSA instance requires a system database module.");
			}
            
            documentDatabase = properties.getProperty("osa.database.document");
            
            switch (documentDatabase) {
				case "mongodb":
					String mongohost = properties.getProperty("osa.mongo.host");
					int mongoport = Integer.parseInt(properties.getProperty("osa.mongo.port"));
					String mongoDefaultdb = properties.getProperty("osa.mongo.defaultdb");
					String mongoDefautCollection = properties.getProperty("osa.mongo.defaultcollection");
					
	                Osa.dbManager.put("mongo", new MongoManager(mongohost, mongoport, mongoDefaultdb, mongoDefautCollection));
	                //logger.info("Mongo configuration loaded.");	                
					break;
	
				default:
					throw new Exception("OSA instance requires a document database module.");
			}
            
            
            // Authentication
            authentication = properties.getProperty("osa.authentication");
            switch (authentication) {
            	case "ldap":
                    Osa.authManager = new LdapManager(
                            properties.getProperty("osa.ldap.server"),
                            properties.getProperty("osa.ldap.admindn"),
                            properties.getProperty("osa.ldap.managepw"),
                            properties.getProperty("osa.ldap.basedn"),
                            properties.getProperty("osa.ldap.instanceadmin")
                            );
                    
                    // TODO: Check LDAP server status
            		break;
            
            	default:
            		throw new Exception("OSA instance requires an authentication module.");
            }
          
            
            // Search
            search = properties.getProperty("osa.search");
            switch (search) {
				case "solr":
					
					// TODO Refactor inside the manager implementation
	            	Osa.solrServerUrl = properties.getProperty("osa.solr.server");

	            	Osa.searchManager = new SolrManager();
	            	
					// TODO test sorl server status
					break;

				default:
					throw new Exception("OSA instance requires a search module.");
			}

            
            // Logger
            logging = properties.getProperty("osa.logger");
            switch (logging) {
            	case "log4j":
                    String configurationFile = this.getInitParameter("logger");
                    File file = new File(getServletContext().getRealPath("/") + configurationFile);
                    if(!file.canRead()) 
                    {
                        throw new Exception("Couldn't read logger configuration file at " + file.getAbsolutePath());
                    }
                    
                    PropertyConfigurator.configure(file.getAbsolutePath()); 
            		break;
            
            	default:
            		throw new Exception("OSA instance requires a logger module.");
            }            
            
            
            // Workflows
            String engineHost = properties.getProperty("osa.workflowengine.host");
            String enginePort = properties.getProperty("osa.workflowengine.port");
            try {
                Osa.workflowManager = new WorkflowManager(engineHost, enginePort);
            } catch (Exception e) {
                logger.info("WorkflowManager not available: "+e);
            }
           
            // Load default conf.
            Osa.defaultConfiguration = properties.getProperty("osa.default.configuration");
            Osa.configFolder = getServletContext().getRealPath("/") + "WEB-INF/config/";
            Osa.defaultRoot = properties.getProperty("osa.default.root");
            
            
            // ONKI access key according to external IP address
            final String prefix = "osa.onki.key.";
            String externalIP = getIP();
            char[] chArr = prefix.toCharArray();  
            chArr[chArr.length -1]++;  
            String endPrefix = new String(chArr);
            Set<String> allNames = properties.stringPropertyNames();  
            SortedSet<String> names = new TreeSet<String>(allNames).subSet(prefix, endPrefix);
            Iterator<String> it = names.iterator();  
            while (it.hasNext()) {
                String key = it.next();
                if (key.endsWith(externalIP)) {
                    Osa.onkiAccessKey = properties.getProperty(key);
                    break;
                }
            }
            
            if (Osa.onkiAccessKey == null) {
                // Default value
                Osa.onkiAccessKey = properties.getProperty("osa.onki.key.default");
            }
            
            // Scheduled tasks
            if (properties.getProperty("osa.scheduledtasks").equals("1")) {
                String configPath =  getServletContext().getRealPath("/") + "WEB-INF/";
                taskScheduler = new TaskScheduler("AutomaticDestruction", "triggername", logDirectory, configPath);
                taskScheduler.start();
            }
            
            logger.info("OSA version [" + getVersionString() + "] + started. See log for more information.");

        } catch (Exception e) {
            logger.error("Error initializing OSA instance. " + e);
            System.exit(0);
        }
    }
    
    public void service(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
    	throw new UnsupportedOperationException("Not supported.");	
    }
    
    public static String getVersionString() {
    	return "release 1.0 - december 2014";
    }
    
    /**
     * Get external IP address
     */
    public String getIP() throws IOException {
        String ip = "";
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
        ip = in.readLine();
        return ip;
    }

}
