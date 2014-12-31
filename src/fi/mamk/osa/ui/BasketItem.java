package fi.mamk.osa.ui;

import java.io.Serializable;

public class BasketItem implements Serializable {

    private static final long serialVersionUID = -1241797282524576050L;
    private String pid;
    private String name;
    private String type;
	
    public BasketItem() {
    	
    }
	
	public BasketItem(String pid, String name, String type) {
		this.pid = pid;
		this.name = name;
		this.type = type;
	}

	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
}
