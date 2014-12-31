package fi.mamk.osa.stripes;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Set;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;
import fi.mamk.osa.core.Osa;

public class OsaApplicationListener implements ServletContextListener {
    
    private static final Logger logger = Logger.getLogger(OsaApplicationListener.class);
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        
        if (Osa.taskScheduler != null) {
            // stop scheduler
            Osa.taskScheduler.stop();
        }
        
        if (Osa.dbManager.containsKey("mongo")) {
            // close mongo
            Osa.dbManager.get("mongo").close();
        }
        
        if (Osa.dbManager.containsKey("sql")) {
            // close mariadb
            Osa.dbManager.get("sql").close();
        }

        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                
            } catch (SQLException e) {
                logger.error("Error deregistering driver "+ driver+", "+e);
            }
        }
        
        // Handle remaining threads
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);
        for (Thread t:threadArray) {
            if (t.getName().contains("Abandoned connection cleanup thread")) {
                synchronized(t) {
                    t.interrupt();
                }
            }
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent context) {
    }

}
