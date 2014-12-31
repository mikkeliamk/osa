package fi.mamk.osa.database.sql.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

//import fi.mamk.osa.bean.OsaResourceBean;
import fi.mamk.osa.database.sql.DataModel;
import fi.mamk.osa.database.sql.SQLManager;

/**
 * A Class that binds xml Schema to a database 
 * @author Jussi Juvén
 */
public class MySQLDataModel extends DataModel {
    
    private static final Logger logger = Logger.getLogger(MySQLDataModel.class); 
        
    public HashMap<String,String> resourceColumns = new HashMap<String, String>();
    public HashMap<String,String> simpleTables = new HashMap<String, String>();
    public HashMap<String,HashMap<String,String>> complexTables = new HashMap<String,HashMap<String,String>>();       
    
    /**
     * Default Constructor for a Datamodel
     * 
     * @param xmlFile xml Schema file to parse
     * @throws java.lang.Exception
     */
    public MySQLDataModel() throws Exception {
//        if(!this.loadSchemaFromBean((null))) {
//            throw new Exception("Unable to load Schema from bean");
//        }
        logger.debug("Checking database...");
        if(!this.checkDatabase()) {
            throw new Exception("database error");
        }
        
        logger.debug("Database checked. - OK");
        
    }

    
    /**
     * Checks database against loaded Schema.
     * 
     * @return success
     * @throws java.lang.Exception
     */
    private boolean checkDatabase() {
        
        boolean retval = true;
        Connection con = null;
        try {       
            
            con = SQLManager.dataSource.getConnection();
            String catalog = con.getCatalog();    
                          
            // checking tables
            Vector<String> dbTables = new Vector<String>();            
            ResultSet rs = con.getMetaData().getTables(catalog, null, "%", null);
            while(rs.next()) {
                String tablename = rs.getString("TABLE_NAME");
                dbTables.add(tablename.toLowerCase());
            }
            logger.debug("Catalog: "+catalog+", "+dbTables.size()+" tables.");
                             
        }
        catch(Exception ex)  {
            logger.error("Error initializing database: ", ex);
            retval = false;
        }
        finally{
            try {               
                if(con!=null && !con.isClosed()) {
                    con.close();
                    logger.debug("Connection closed.");
                }
            }
            catch(Exception e) {
                logger.error("Error finalizing connection. ",e);
            }                    
           
        }
        return retval;
    }
        

    @Override
    public HashMap<String, String> getResourceColumns() {       
        return this.resourceColumns;
    }


    @Override
    public HashMap<String, String> getSimpleTables() {
        return this.simpleTables;
    }


    @Override
    public HashMap<String, HashMap<String, String>> getComplexTables() {        
        return this.complexTables;
    }
    
}
