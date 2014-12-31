package fi.mamk.osa.stripes;

import net.sourceforge.stripes.action.ActionBeanContext;
import fi.mamk.osa.auth.User;
import fi.mamk.osa.search.Search;
import fi.mamk.osa.ui.Gui;

public class OsaActionBeanContext extends ActionBeanContext {

	public User getUser() {
		return (User) getRequest().getSession().getAttribute("user");
	}
	
	public void setUser(User user) {
		getRequest().getSession().setAttribute("user", user);
	}
	
	public Gui getGui() {
		return (Gui) getRequest().getSession().getAttribute("gui");
	}
	
	public void setGui(Gui gui) {
		getRequest().getSession().setAttribute("gui", gui);
	}
	
	public void setSearch(Search search) {
		getRequest().getSession().setAttribute("search", search);
	}
	
	public Search getSearch() {
		return (Search) getRequest().getSession().getAttribute("search");
	}
		
}