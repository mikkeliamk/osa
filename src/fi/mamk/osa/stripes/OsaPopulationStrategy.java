package fi.mamk.osa.stripes;

import net.sourceforge.stripes.exception.StripesJspException;
import net.sourceforge.stripes.tag.DefaultPopulationStrategy;
import net.sourceforge.stripes.tag.InputTagSupport;

/**
 * Class OsaPopulationStrategy
 *
 */
public class OsaPopulationStrategy extends DefaultPopulationStrategy {
    /** Strategy to look at the page first, then the pageActionBean, then the request.*/
    public Object getValue(InputTagSupport tag) throws StripesJspException {
        Object value =  getValueFromTag(tag); 
        if (value==null) value = getValueFromActionBean(tag); 
        if (value==null) value = getValuesFromRequest(tag);
    
        return value;
    }

}
