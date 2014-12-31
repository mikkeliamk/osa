package fi.mamk.osa.database.sql;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

import fi.mamk.osa.database.DatabaseManager;

/**
 * An abstract class for managing databases
 *
 */
public abstract class SQLManager extends DatabaseManager {
    
    private static final Logger logger = Logger.getLogger(SQLManager.class);
    
    public static DataSource dataSource = null;
    public static MiniConnectionPoolManager poolManager = null;
  
    /**
     * Get db connection
     * @return
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        }
        if (poolManager != null) {
            return poolManager.getConnection();
        }
        return null;
    }
    
    /** 
     * Checks the database status.
     * 
     * @return true if database is working properly. 
     *         false if any of needed databases is offline
     */
    public abstract boolean testDatabase();    
}
