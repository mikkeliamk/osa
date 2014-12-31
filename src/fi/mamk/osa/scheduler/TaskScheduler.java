package fi.mamk.osa.scheduler;

import org.apache.log4j.Logger;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

public class TaskScheduler {
    
    private String jobname;
    private String triggername;
    private String logDirectory;
    private String configPath;
    private static final Logger logger = Logger.getLogger(TaskScheduler.class);
    private Scheduler scheduler;
    
    public TaskScheduler(String jobname, String triggername, String logDirectory, String configPath) {
        this.jobname = jobname;
        this.triggername = triggername;
        this.logDirectory = logDirectory;
        this.configPath = configPath;
        this.scheduler = null;
    }
    
    public void start() throws SchedulerException {
        
        JobDetail job = JobBuilder.newJob(ScheduleExecutor.class)
                  .withIdentity(jobname, "group1")
                  .requestRecovery()
                  .usingJobData("configPath", configPath)
                  .usingJobData("logDirectory", logDirectory)
                  .build();
        
        // schedule
        int timeInterval = 1;
        SimpleScheduleBuilder simpleScheduleBuilder = null;
        simpleScheduleBuilder = SimpleScheduleBuilder.simpleSchedule();
        simpleScheduleBuilder.withIntervalInHours(timeInterval * 24).repeatForever();
        
        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity(triggername, "group1")
                .withSchedule(simpleScheduleBuilder)
                .startAt(DateBuilder.dateOf(23, 55, 0))
                .forJob(job)
                .build();
        
        scheduler = new StdSchedulerFactory(configPath+"config/quartz.properties").getScheduler();
        scheduler.start();
        scheduler.scheduleJob(job, trigger);
        
    }
    
    public String getConfigPath() {
        return this.configPath;
    }
    
    public String getLogDirectory() {
        return this.logDirectory;
    }
    
    /**
     * Stop scheduler
     */
    public void stop() {
        try {
            
            scheduler.unscheduleJob(TriggerKey.triggerKey(triggername, "group1"));                       
            scheduler.pauseAll();
            scheduler.shutdown(true);
            
            int ct = 0;

            // waiting 30 seconds for the scheduler to shutdown
            while(ct < 30) {
                ct++;
                Thread.sleep(1000);
                if (scheduler.isShutdown()) {
                    break;
                }
            }
            
        } catch (SchedulerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
