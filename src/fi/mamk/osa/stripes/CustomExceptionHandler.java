package fi.mamk.osa.stripes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.exception.DefaultExceptionHandler;
import net.sourceforge.stripes.exception.SourcePageNotFoundException;
import net.sourceforge.stripes.exception.StripesServletException;

public class CustomExceptionHandler extends DefaultExceptionHandler {
    private static final Logger logger = Logger.getLogger(CustomExceptionHandler.class);
	
	public Resolution handleSourcePageNotFoundException(SourcePageNotFoundException sourceException, HttpServletRequest request, HttpServletResponse response) {
		logger.info("handleSourcePageNotFoundException invoked " + sourceException.getMessage());
		return new ForwardResolution("/error.jsp").addParameter("exception", sourceException.getMessage());
	}
	
	public Resolution handleStripesServletException(StripesServletException servletException, HttpServletRequest request, HttpServletResponse response) {
		logger.info("handleStripesServletException invoked " + servletException.getMessage());
		return new ForwardResolution("/error.jsp").addParameter("exception", servletException.getMessage());
	}
	
	public Resolution handleGeneric(Exception exception, HttpServletRequest request, HttpServletResponse response) {
		logger.info("handleGeneric invoked " + exception.getMessage());
		return new ForwardResolution("/error.jsp").addParameter("exception", exception.getMessage());
	}

}
