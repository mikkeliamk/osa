package fi.mamk.osa.tags;

import java.net.URLEncoder;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.log4j.Logger;

public class UrlEncode extends TagSupport {

	private static final long serialVersionUID	= 1L;
	private String value;
	
	
	public int doStartTag() throws JspException {
		try{
			JspWriter out = pageContext.getOut();			
			out.print(URLEncoder.encode(value, "UTF-8"));
				
			return SKIP_BODY;
			
		} catch(Exception e) {
			Logger.getLogger(this.getClass()).error("Error using tag: ",e);
			return SKIP_BODY;
		}
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
