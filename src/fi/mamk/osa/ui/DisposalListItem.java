package fi.mamk.osa.ui;

import java.io.Serializable;

public class DisposalListItem implements Serializable {

    private static final long serialVersionUID = -4165180772198290183L;
    private String pid;
	private String objectName;
	private String objectType; 
	private String deleter;
	private String disposalDateString;
	private long disposalDateTimestamp;
	
	public DisposalListItem(){
	    this.pid = "";
        this.objectName = "";
        this.objectType = "";
        this.deleter = "";
        this.disposalDateString = "";
        this.disposalDateTimestamp = 0;
	}
	
	public DisposalListItem(String pid, String objectName, String objectType, String deleter, String disposalDateString) {
		this.pid = pid;
		this.objectName = objectName;
		this.objectType = objectType;
		this.deleter = deleter;
		this.disposalDateString = disposalDateString;
	}

	public DisposalListItem(String pid, String objectName, String objectType, String deleter, String disposalDateString, long disposalDateTimestamp) {
		this.pid = pid;
		this.objectName = objectName;
		this.objectType = objectType;
		this.deleter = deleter;
		this.disposalDateString = disposalDateString;
		this.disposalDateTimestamp = disposalDateTimestamp;
	}

	public String getPid() {
		return pid;
	}
	public void setPid(String pid) {
		this.pid = pid;
	}
	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public String getDeleter() {
		return deleter;
	}
	public void setDeleter(String deleter) {
		this.deleter = deleter;
	}
	public String getDisposalDateString() {
		return disposalDateString;
	}
	public void setDisposalDateString(String disposalDateString) {
		this.disposalDateString = disposalDateString;
	}
	public long getDisposalDateTimestamp() {
		return disposalDateTimestamp;
	}
	public void setDisposalDateTimestamp(long disposalDateTimestamp) {
		this.disposalDateTimestamp = disposalDateTimestamp;
	}

}
