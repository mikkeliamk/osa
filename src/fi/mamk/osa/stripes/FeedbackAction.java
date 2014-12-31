package fi.mamk.osa.stripes;

import fi.mamk.osa.core.Osa;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;

@UrlBinding("/Feedback.action")
public class FeedbackAction extends OsaBaseActionBean {

    private String jsonData = "dagds";
    
    @HandlesEvent("sentFeedback")
    public Resolution receiveFeedback(){
        StringBuilder data = new StringBuilder();
        data.append(jsonData);
        data.insert(data.indexOf("\"image\""),"\"identifier\" : \" "+Osa.dbManager.get("mongo").getFeedbackSize() +" \",");
        Osa.dbManager.get("mongo").insertObject(data.toString());
        return new StreamingResolution(MIME_TEXT, "Feedback received.");
    }

    @HandlesEvent("listFeedback")
    public Resolution listFeedback(){
        jsonData = Osa.dbManager.get("mongo").listItems();			
        return new ForwardResolution("feedback.jsp");
    }
	 
    @HandlesEvent("removeFeedback")
    public Resolution removeFeedback(){
        Osa.dbManager.get("mongo").removeObject(jsonData);
        return new StreamingResolution(MIME_TEXT, "Feedback removed.");
    }

    public String getJsonData() {
    	return jsonData;
    }
    
    public void setJsonData(String jsonData) {
    	this.jsonData = jsonData;
    }
	
}
