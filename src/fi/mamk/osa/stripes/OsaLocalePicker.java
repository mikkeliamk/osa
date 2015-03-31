package fi.mamk.osa.stripes;

import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import net.sourceforge.stripes.localization.DefaultLocalePicker;
import org.apache.log4j.Logger;


/**
 * LocalePicker class determines what Locale a particular request will use.
 * 
 */
public class OsaLocalePicker extends DefaultLocalePicker {
    
    private static final Logger logger = Logger.getLogger(OsaLocalePicker.class);
    
    /**
     * Default constructor.
     */
    public OsaLocalePicker() {
    }
 
    @Override
    public Locale pickLocale(HttpServletRequest request) {
        Locale locale = null;
        locale = (Locale) request.getSession().getAttribute("userLocale");

        if (locale!=null) {
            return locale;
        } else {
            return super.pickLocale(request);
        }
    }
    
}
