package fi.mamk.osa.tags;

import java.io.IOException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

import fi.mamk.osa.auth.User;

public class ResourceBundleTag extends TagSupport {
	
	private static final long	serialVersionUID	= 1L;
	private String context;
	private String key;
	
	@Override
	public int doStartTag() {
		
		// search bundle context for specified key
		// if none is found return the key itself as is
		User user = (User) pageContext.getSession().getAttribute("user");
		Locale locale = pageContext.getRequest().getLocale();
		if(user != null && user.getLocale() != null) {
        	locale = user.getLocale();
        }
		ResourceBundle bundle = ResourceBundle.getBundle("fi.mamk.yksa.messages",locale);

		// If there is a context specified add separator
		// else it will search the root of the bundle.
		if (context.length() > 0) {
			context += ".";
		}
		
		String value = key;
		try {
			value = bundle.getString(context + key);
		} catch (NullPointerException e) {
			// Fine. Return the key. 
		} catch (MissingResourceException e) {
			// Fine. Return the key.
		} catch (Exception e) {
			Logger.getLogger(this.getClass()).error("Tag error", e);
		}
		
		
		JspWriter out = pageContext.getOut();
		try {
			out.print(value);
		} catch (IOException e) {
			Logger.getLogger(this.getClass()).error("Tag error", e);
		}
				
		return SKIP_BODY;
	}
	

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
