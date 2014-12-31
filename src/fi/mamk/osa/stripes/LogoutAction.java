package fi.mamk.osa.stripes;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationErrors;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.log4j.Logger;

import fi.mamk.osa.auth.User;
import fi.mamk.osa.core.Osa;

@UrlBinding("/Logout.action")
public class LogoutAction extends OsaBaseActionBean {
    
    private static final Logger logger = Logger.getLogger(LogoutAction.class);
    
    @ValidationMethod(on="logout")
    public void handleLoginErrors(ValidationErrors errors) {

    }
 
    @HandlesEvent("logout")
    public Resolution logout() {
        
        HttpSession session = this.getContext().getRequest().getSession();
        HttpServletResponse response = this.getContext().getResponse();
        
        if (session != null) {
            User user = (User) session.getAttribute("user");

            if (user != null) {
                Osa.dbManager.get("sql").removeCurrentuser(user.getDn());
            }
            
            session.removeAttribute("user");
            session.invalidate();
        }
        
        response.setDateHeader("Expires",-1);
        response.setHeader("Cache-Control","no-store");
        response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
        response.setHeader("Pragma","no-cache"); //HTTP 1.0
        response.setContentType("text/html");
        
        return new StreamingResolution(MIME_TEXT, INDEX.replaceAll("/", ""));
    }
    
}