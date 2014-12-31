package fi.mamk.osa.database.sql;

import org.jdom.*;
import org.jdom.input.*;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.io.*;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;


public class DatabaseConfiguration {
    
    private String dbDriverName = null;
    private String dbUser = null;
    private String dbPassword = null;
    private String dbURI = null;
    
    private int dbPoolMinSize = 0;
    private int dbPoolMaxSize = 0;
    private int dbTimeout = 0;

    private static final Logger logger = Logger.getLogger( DatabaseConfiguration.class  );

    /**
     * Default constructor
     */
    public DatabaseConfiguration() {
        
    }
    
    /**
     * Constructor
     * @param xmlFile
     */
    public DatabaseConfiguration(String xmlFile) {
        SAXBuilder builder = new SAXBuilder();

        try {

            InputStream is = new FileInputStream( xmlFile );

            Document doc = builder.build ( is );
            Element root = doc.getRootElement();

            dbDriverName = root.getChild("dbDriverName").getTextTrim();
            dbUser = root.getChild("dbUser").getTextTrim();
            dbPassword = root.getChild("dbPassword").getTextTrim();
            dbURI = root.getChild("dbURI").getTextTrim();
            dbPoolMinSize = Integer.parseInt( root.getChild("dbPoolMinSize").getTextTrim() );
            dbPoolMaxSize = Integer.parseInt( root.getChild("dbPoolMaxSize").getTextTrim() );
            dbTimeout = Integer.parseInt( root.getChild("dbTimeout").getTextTrim() );
            logger.info("XXXXXXXXXXXXXX "+dbUser+"\n");

        }   catch ( Exception ex ) {
            logger.error( "Could not read configuration file: ", ex );
        }

    }

    public String getDbDriverName() {
        return dbDriverName;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getDbURI() {
        return dbURI;
    }

    public int getDbPoolMinSize() {
        return dbPoolMinSize;
    }

    public int getDbPoolMaxSize() {
        return dbPoolMaxSize;
    }

    public int getDbTimeout() {
        return dbTimeout;
    }

    /**
     * Function: setDbDriverName
     * @param dbDriverName the dbDriverName to set
     */
    public void setDbDriverName(String dbDriverName) {
        this.dbDriverName = dbDriverName;
    }

    /**
     * @param dbUser the dbUser to set
     */
    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    /**
     * @param dbPassword the dbPassword to set
     */
    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    /**
     * @param dbURI the dbURI to set
     */
    public void setDbURI(String dbURI) {
        this.dbURI = dbURI;
    }

    /**
     * @param dbPoolMinSize the dbPoolMinSize to set
     */
    public void setDbPoolMinSize(int dbPoolMinSize) {
        this.dbPoolMinSize = dbPoolMinSize;
    }

    /**
     * @param dbPoolMaxSize the dbPoolMaxSize to set
     */
    public void setDbPoolMaxSize(int dbPoolMaxSize) {
        this.dbPoolMaxSize = dbPoolMaxSize;
    }

    /**
     * @param dbTimeout the dbTimeout to set
     */
    public void setDbTimeout(int dbTimeout) {
        this.dbTimeout = dbTimeout;
    }

    @Override
    public String toString() {
        
        ReflectionToStringBuilder tsb = 
            new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return tsb.toString();
    }
}

