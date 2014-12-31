package fi.mamk.osa.scheduler;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import fi.mamk.osa.bean.DataStream.State;
import fi.mamk.osa.core.DestructionPlan;
import fi.mamk.osa.core.Osa;
import fi.mamk.osa.stripes.DisposalAction;

public class ScheduleExecutor implements Job { 

    private static final Logger logger = Logger.getLogger(ScheduleExecutor.class);
    // container for scheduled option per organization
    private HashMap<String, Boolean> scheduledOptionMap;
    
	@Override
	public void execute(JobExecutionContext jec) throws JobExecutionException {
	    
	    logger.info("Starting scheduled task...");
	    scheduledOptionMap = new HashMap<String, Boolean>();
	    int counter = 0;
	    
	    String configPath = jec.getJobDetail().getJobDataMap().getString("configPath");
	    String rootLogDirectory = jec.getJobDetail().getJobDataMap().getString("logDirectory");
	    String logContent = "";
	    String logDirectory = "";
	    List<String> organizations = new ArrayList<String>();
	        
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        // get current day
        String dateTime = dateFormat.format(date).split(" ")[0];
        String disposalTime = dateFormat.format(date);
        
        // Get files to be be destructed (destructionDate)
        HashMap<String, String> items = Osa.searchManager.getDisposableItems(dateTime);
        Iterator<Entry<String, String>> iter = items.entrySet().iterator();
        
        logger.info("Scheduled task: "+items.size()+" items found according to disposalTime ("+dateTime+")");
        
        while (iter.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
            String pid = entry.getKey();
            String title = entry.getValue();
            String organization = pid.split(":")[0].toLowerCase();

            // check scheduled-option in organization configuration file
            if (getScheludedOption(organization, configPath)) {
                if (!organizations.contains(organization)) {
                    organizations.add(organization);
                }
                
                boolean returnValue = Osa.dbManager.get("mongo").insertToDisposalList(null, pid, disposalTime, title);
                if (returnValue) {
                    Osa.fedoraManager.setObjectState(pid, State.I);
                    counter++;
                }
            }
        }
        
        // reset iterator
        iter = items.entrySet().iterator();
        
        // write log files to .../'organizationName'/
        for (String organization : organizations) {
            logDirectory = rootLogDirectory +"/"+ organization +"/";
            
            while (iter.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
                String pid = entry.getKey();
                String title = entry.getValue();
                String currentOrganization = pid.split(":")[0].toLowerCase();
                
                if (currentOrganization.equals(organization)) {
                    logContent += "[" + disposalTime + "] Automatic setting object \""
                            + title
                            + "\" [" + pid + "] inactive\n";
                }
            }
            
            DisposalAction.logDisposal(DestructionPlan.DESTRUCTIONTYPE_Dispose, logContent, logDirectory);
        }
        
        logger.info("Scheduled task: "+Integer.toString(counter)+" items of "+items.size()+" moved to disposal list.");
	}
	
	private boolean getScheludedOption(String organization, String configPath) {
	    
	    boolean scheduled = false;
	    
	    if (scheduledOptionMap.containsKey(organization)) {
	        return scheduledOptionMap.get(organization);
	    }
	    
        File configuration = null;
        DestructionPlan destructionPlan = null;
        configuration = new File(configPath+"config/orgs/"+organization+".xml");
        if (!configuration.canRead()) {
            // use default configuration if organization specific file not found
            configuration = new File(configPath +"config/orgs/default.xml");
        }
        
        if (configuration.canRead()) {
            destructionPlan = new DestructionPlan(configuration);
            scheduled = destructionPlan.isScheluded();
            scheduledOptionMap.put(organization, scheduled);
        }
        
	    return scheduled;
	}

}
