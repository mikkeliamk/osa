package fi.mamk.osa.tags;

import java.util.Vector;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.log4j.Logger;

import fi.mamk.osa.auth.Role;
import fi.mamk.osa.auth.User;
import fi.mamk.osa.bean.CaptureBean;
import fi.mamk.osa.bean.ManagementBean;
import fi.mamk.osa.bean.FedoraBean;
import fi.mamk.osa.core.Osa;

public class ShowObject extends TagSupport {
	
    private static final Logger logger = Logger.getLogger(ShowObject.class);
	private static final long serialVersionUID = 1L;
	
	private String objArClass;
	private String requiredLevel;
	private String publicity;
	private String objectId;

	public int doStartTag() throws JspException {
		try {
			HttpSession session = pageContext.getSession();
			if (session == null) {
				return SKIP_BODY;
			}
			
			User user = (User)session.getAttribute("user");
			Vector<Role> roles = user.getFedoraRoles();
			
			int requiredLvl = Integer.parseInt(requiredLevel);
			FedoraBean fObject = Osa.fedoraManager.findObject(objectId);
			
			String path = ((ManagementBean)fObject.getDataStream(FedoraBean.DATASTREAM_MANAGEMENT)).getPath();
            String publicityLevel = ((CaptureBean)fObject.getDataStream(FedoraBean.DATASTREAM_CAPTURE)).getPublicityLevel();
            
            if (Role.getAccessRightForObject(roles, path, publicityLevel) >= requiredLvl) {
            	return EVAL_BODY_INCLUDE;
            }
            
            return SKIP_BODY;
		} catch (Exception e) {
			logger.error("Tag error: "+e);
			return SKIP_BODY;
		}
	}
	
	public int doEndTag() throws JspException {
		return EVAL_PAGE;
	}
	
	public String getObjArClass() {
		return objArClass;
	}
	public void setObjArClass(String objArClass) {
		this.objArClass = objArClass;
	}
	public String getRequiredLevel() {
		return requiredLevel;
	}
	public void setRequiredLevel(String requiredLevel) {
		this.requiredLevel = requiredLevel;
	}
	public String getPublicity() {
		return publicity;
	}
	public void setPublicity(String publicity) {
		this.publicity = publicity;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}
	
}
