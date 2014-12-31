package fi.mamk.osa.workflow;

import com.belvain.soswe.microservices.soswe_api.SosweClient;
import com.belvain.soswe.microservices.soswe_api.TypeNotSupportedException;
import com.sun.jersey.api.client.ClientHandlerException;

import fi.mamk.osa.bean.MetaDataElement;
import flexjson.JSONDeserializer;

import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

public class WorkflowManager {
    
    SosweClient sosweClient = null;
    
    private static final Logger logger = Logger.getLogger(WorkflowManager.class);
    
    public static final String WORKFLOW_PREINGEST = "Pre-ingest";
    public static final String WORKFLOW_BATCHINGEST = "Batch-ingest";
    public static final String WORKFLOW_ZIP = "Zip";
    
    public static final String OPT_FILENAME  = "filename";
    public static final String OPT_USERNAME  = "username";
    public static final String OPT_USERMAIL  = "usermail";
    public static final String OPT_ORG       = "organization";
    public static final String OPT_IMPORTDIR = "importdirectory";
    public static final String OPT_UPLOADDIR = "uploaddirectory";
    public static final String OPT_INGESTDIR = "ingestdirectory";
    public static final String OPT_FAILEDDIR = "faileddirectory";
    
    public WorkflowManager(String host, String port) throws UnknownHostException {
        int iPort = Integer.parseInt(port);
        sosweClient = new SosweClient(host, iPort);            
    }
    
    public boolean startWorkflow(String workflowname, Map<String, Object> opts) throws ClientHandlerException {
        boolean retVal = true;
        String answer = sosweClient.startWorkflow(workflowname, opts);      
        return retVal;
    }
    
    public String getStatus(String workflow, String fileName) {
        String statusMap = sosweClient.getStatus();
        String status = "";
        HashMap<String, String> values = new JSONDeserializer<HashMap<String, String>>().deserialize(statusMap);
        status = values.get(fileName);

        return status;
    }

    public String getWorkflows() {
        String workflows = "";
        try {
            workflows = sosweClient.getWorkflows("application/json");

        } catch (TypeNotSupportedException e) {
            logger.error("WorkflowManager:getWorkflows error: "+e);
        }
        return workflows;
    }
    
    public static String mapToString(Map<String, MetaDataElement> map) {
        StringBuilder stringBuilder = new StringBuilder();
         
        for (String key : map.keySet()) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("&");
            }
            String value = map.get(key).toString();
            try {
                stringBuilder.append((key != null ? URLEncoder.encode(key, "UTF-8") : ""));
                stringBuilder.append("=");
                stringBuilder.append(value != null ? URLEncoder.encode(value, "UTF-8") : "");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("This method requires UTF-8 encoding support", e);
            }
        }
        return stringBuilder.toString();
    }
    
}
