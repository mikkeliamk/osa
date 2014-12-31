package fi.mamk.osa.database.sql.mysql;

import fi.mamk.osa.database.sql.SQLManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

public class MySQLManager extends SQLManager {

    private static final Logger logger = Logger.getLogger(MySQLManager.class);
    
    public MySQLManager() {
        super();
        testDatabase();
    }
    
    @Override
    public boolean testDatabase() {
        
        try {
            // testing osa-db, clears now currentusers-table
            Connection conn = SQLManager.getConnection();
            String sql = "TRUNCATE TABLE osa.currentusers";
            Statement st = conn.createStatement();
            st.executeUpdate(sql);
            conn.close();
            
        } catch (Exception e) {
            logger.error("Database check failed."+e);
            return false;
        }
        return true;     
    }
    
    public long getCtxTime(String organization){
    	
    	long contextTime = 0;
    	
        try {     

            Connection conn = SQLManager.getConnection();
            String sql = "SELECT * FROM contexttime WHERE organization='"+organization+"'";
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(sql);                   

            while (rs.next()){
            	contextTime = rs.getLong("contexttime");
            }
            conn.close();
            
        } catch (Exception e) {
            logger.error("Database check failed."+e);
        }
        
        return contextTime;
    	
    }
    
    public void setCtxTime(String organization){

	    long ctxtime = System.currentTimeMillis();
	    	
	    try {
	    		
	        Connection conn = SQLManager.getConnection();
	        String sql = "insert into contexttime (organization, contexttime) values('"+organization+"', "+ctxtime+") on duplicate key update contexttime=values(contexttime)";
	        Statement st = conn.createStatement();
	        st.executeUpdate(sql);
	        conn.close();
	        
	    } catch (Exception e) {
	        logger.error("Database check failed. "+e);
	    }

    }
    
    /**
     * Add user into logged in users -list in database
     */
    public boolean setCurrentuser(String user, String sessionid) {
        
        boolean status = true;
        
        try {
            Connection conn = SQLManager.getConnection();
            String sql = "INSERT INTO currentusers (user, sessionid) VALUES ('"+user+"', '"+sessionid+"')";
            Statement st = conn.createStatement();
            st.executeUpdate(sql);
            conn.close();
            
        } catch (Exception e) {
            status = false;
            logger.error("Insert into database table currentusers failed. "+e);
        }
        
        return status;
    }
    
    /**
     * Remove user from logged in users -list in database
     */
    public String removeCurrentuser(String user) {
        
        String sessionid = "";
        
        try {
            Connection conn = SQLManager.getConnection();
            String sql = "SELECT sessionid FROM currentusers WHERE user='"+user+"';";
            Statement st = conn.createStatement();
            ResultSet result = st.executeQuery(sql);
            while (result.next()) {
                sessionid = result.getString("sessionid");
            }
            
            sql = "DELETE FROM currentusers WHERE user='"+user+"';";
            st = conn.createStatement();
            st.executeUpdate(sql);                   
            conn.close();
            
        } catch (Exception e) {
            logger.error("Database check failed."+e);
        }
        
        return sessionid;
    }
    
    /**
     * Function for generating next id below parent pid
     */
    public String getNextId(String parentPid) {
        long id = 0;
        
        try {
            Connection conn = SQLManager.getConnection();
            String sql = "SELECT highestID FROM idGen WHERE pid='"+parentPid+"'";
            Statement st = conn.createStatement();
            ResultSet result = st.executeQuery(sql);

            while (result.next()) {
                id = result.getLong("highestID");
            }
            conn.close();
            
            id = id+1;
            
            conn = SQLManager.getConnection();
            sql = "UPDATE idGen SET highestID="+id+" WHERE pid='"+parentPid+"'";
            st = conn.createStatement();
            st.executeUpdate(sql);                   
            conn.close();
            
        } catch (Exception e) {
            logger.error("Id generation failed."+e);
        }
        return Long.toString(id);
    }
    
    /**
     * Initialise collection pid
     * @param parentId      pid of parent
     * @param id            initial id
     */
    public void initIdGeneration(String parentPid, int id) {
        try {
            Connection conn = SQLManager.getConnection();
            String sql = "INSERT INTO idGen VALUES('"+parentPid+"', "+Integer.toString(id)+") ON DUPLICATE KEY UPDATE pid=pid";
            Statement st = conn.createStatement();
            st.executeUpdate(sql);
            conn.close();
            
        } catch (Exception e) {
            logger.error("Id initialisation failed."+e);
        } 
    }
        
}
