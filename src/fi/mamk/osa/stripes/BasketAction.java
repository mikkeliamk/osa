package fi.mamk.osa.stripes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import fi.mamk.osa.auth.User;
import fi.mamk.osa.core.Osa;
import fi.mamk.osa.ui.BasketItem;
import fi.mamk.osa.utils.BasketItemComparator;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.LocalizableMessage;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;

@UrlBinding("/Basket.action")
public class BasketAction extends OsaBaseActionBean {

    private String pid;
    private Vector<String> pidArray = new Vector<String>();

    @HandlesEvent("addToBasket")
    public Resolution addToBasket()	{
        String success = "success";
        for (int i=0; i<pidArray.size(); i++) {
            success = Osa.dbManager.get("mongo").insertToBasket(this.getContext().getUser(), pidArray.get(i));
        }
        return new StreamingResolution(MIME_TEXT, success);
    }
	
    @HandlesEvent("showBasket")
    public Resolution showBasket() {
        String json = Osa.dbManager.get("mongo").listBasketContent(this.getContext().getUser());
        return new StreamingResolution(MIME_JS, json);
    }
	
    @HandlesEvent("showBasketDatatables")
    public Resolution showBasketDatatables() {
        
        List<BasketItem> basketItems = new ArrayList<BasketItem>();
        basketItems = Osa.dbManager.get("mongo").getBasketItems(this.getContext().getUser());
        Collections.sort(basketItems, new BasketItemComparator(BasketItemComparator.NAME, false));
        
        StringBuilder json = new StringBuilder();
        json.append("{" +
                    "\"sEcho\": 1," +
                    "\"iTotalRecords\": "+basketItems.size()+"," +
                    "\"iTotalDisplayRecords\": " +basketItems.size()+",");
    	
        json.append("\"aaData\": [");
    	if (basketItems.size() > 0) {
	        for (BasketItem basketItem : basketItems) {
	    		json.append("[");
	    		// #1 col
	    		json.append("\"<input type='checkbox' name='formNames' value='"+basketItem.getType()+"' data-pid='"+basketItem.getPid()+"' data-name='"+basketItem.getName()+"'/>" +
						    "<input type='hidden' name='pidArray[]' value='"+basketItem.getPid()+"' disabled='disabled'/>\","); 
	    		// #3 col
	    		json.append("\"<div class='datatables-td-container'>"+
					      	"<a href='Ingest.action?pid="+basketItem.getPid()+"&view=' title='"+new LocalizableMessage("button.open").getMessage(getUserLocale())+"'><h3 class='datatables-header'>"+basketItem.getName()+"</h3></a>"+
					      	"<div>"+
						      	"<span>"+new LocalizableMessage("link.add."+basketItem.getType()).getMessage(getUserLocale())+"</span>"+
						      	"<img src='img/icons/silk/basket_delete.png' " +
						      		"data-pid='"+basketItem.getPid()+"' " +
				      				"class='pointerCursor basketaction deleteicon' " +
				      				"title='"+new LocalizableMessage("message.removefrombasket").getMessage(getUserLocale())+" "+
				      						  new LocalizableMessage("tooltip.basket.remove").getMessage(getUserLocale())+"' " +
		      						"alt='[x]' />"+
					      	"</div>"+
				      	  "</div>\",");
	    		
	    		json.deleteCharAt(json.length()-1);
	    		json.append("],");
	        }
	        json.deleteCharAt(json.length()-1);
    	}
    	
        json.append("]}");
        return new StreamingResolution(MIME_JS, json.toString());
    }
	
    @HandlesEvent("deleteFromBasket")
    public Resolution deleteFromBasket() {
        for (int i=0; i<pidArray.size(); i++) {
            Osa.dbManager.get("mongo").deleteFromBasket(this.getContext().getUser(), pidArray.get(i));
        }
        
        return new StreamingResolution(MIME_TEXT, "true");
    }
	
    public boolean checkIfExists(String pid) {
        boolean exists = Osa.dbManager.get("mongo").checkIfExists(this.getContext().getUser(), pid);
        return exists;
    }

	@HandlesEvent("getBasketSize")
	public Resolution getBasketSize() {
        User user = this.getContext().getUser();
        int size = 0;
        if (user != null) {
            size = Osa.dbManager.get("mongo").getBasketSize(user);
        }
        return new StreamingResolution(MIME_TEXT, String.valueOf(size));
	}
	
    public String getPid() {
        return pid;
    }
    
    public void setPid(String pid) {
        this.pid = pid;
    }
    
    public Vector<String> getPidArray() {
        return pidArray;
    }
    
    public void setPidArray(Vector<String> pidArray) {
        this.pidArray = pidArray;
    }
}
