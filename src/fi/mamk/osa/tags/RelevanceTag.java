package fi.mamk.osa.tags;

import java.text.DecimalFormat;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.log4j.Logger;

import fi.mamk.osa.stripes.SearchAction;

public class RelevanceTag extends TagSupport{

    private static final Logger logger = Logger.getLogger(SearchAction.class);

	private static final long serialVersionUID = 1L;
	private String score;
	
	/** Creates an HTML span tag containing images created according to given Solr score
	 * 
	 * @param score		Solr score as a floating point number
	 * @return			String of HTML
	 */
	public static String getRelevanceIcon(float score) {
		String html = "";
		int stars = 0;
		
		if (score > 0.2) 	{stars++;}
		if (score > 0.5) 	{stars++;}
		if (score > 0.99) 	{stars++;}
		if (score > 2) 		{stars++;}
		if (score > 5) 		{stars++;}
		
		if (score > 10) {
			// Great search!
			html = "<span class='relevancy-indicator' title='"+new DecimalFormat("#0.000").format(score)+"'>" +
						"<img src='img/icons/silk/award_star_gold_3.png' alt='&#9733;'>" +
				   "</span>";
		} else {
			html = "<span class='relevancy-indicator' title='"+new DecimalFormat("#0.000").format(score)+"'>";
			for (int i = 0; i < 5; i++) {
				if (stars > i) {
					html += "<img src='img/icons/silk/bullet_star.png' alt='&#9733;'>";
				} else {
					html += "<img src='img/icons/silk/bullet_star_gray.png' alt='&#9734;'>";
				}
			}
			html += "</span>";
		}
		return html;
	}
	
	@Override
	public int doStartTag() throws JspException {
		JspWriter out = pageContext.getOut();
		try {
			float sc = Float.parseFloat(score);
			out.print(getRelevanceIcon(sc));
		} catch (Exception e) {
			logger.info("Tag error: "+e);
		}
		return SKIP_BODY;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}
}
