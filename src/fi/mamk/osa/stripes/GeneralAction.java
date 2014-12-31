package fi.mamk.osa.stripes;

import java.util.MissingResourceException;

import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.LocalizableMessage;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;

@UrlBinding("/General.action")
public class GeneralAction extends OsaBaseActionBean{
	private String msgBundleCode;

	@HandlesEvent("getLocalizedMessage")
	public Resolution getLocalizedMessage() {
		String msg = "";
		if (this.getContext().getUser() != null) {
			try {
				msg = new LocalizableMessage(msgBundleCode).getMessage(this.getContext().getUser().getLocale());
			} catch(MissingResourceException e) {
				return new StreamingResolution(MIME_TEXT, "???"+msgBundleCode+"???");
			}
		}
		return new StreamingResolution(MIME_TEXT, msg);
	}
	
	public String getMsgBundleCode() {
		return msgBundleCode;
	}

	public void setMsgBundleCode(String msgBundleCode) {
		this.msgBundleCode = msgBundleCode;
	}
}
