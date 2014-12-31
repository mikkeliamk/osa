package fi.mamk.osa.core;

import java.io.Serializable;

public class InheritedMetadataElement implements Serializable {
    
    private static final long serialVersionUID = -7438939665518952180L;
    
    public static final String INHERITANCE_BEHAVIOUR_incremental = "incremental";
    public static final String INHERITANCE_BEHAVIOUR_replaceExisting = "replaceExisting";
    public static final String INHERITANCE_BEHAVIOUR_replaceEmpty = "replaceEmpty";
    public static final String INHERITANCE_BEHAVIOUR_replaceAll = "replaceAll";
    
    private String name;
    private boolean defaultInheritance;
    private boolean manualInheritance;
    private String behaviour;
    private String contentModel;
    private String value;
    private String visibleValue;
    private String fieldType;
    
    // Constructor
    public InheritedMetadataElement() {
        this.name = "";
        this.behaviour = "";
        this.contentModel = "";
        this.value = "";
        this.visibleValue = "";
        this.defaultInheritance = false;
        this.manualInheritance = false;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isDefaultInheritance() {
        return defaultInheritance;
    }
    public void setDefaultInheritance(boolean defaultInheritance) {
        this.defaultInheritance = defaultInheritance;
    }
    public boolean isManualInheritance() {
        return manualInheritance;
    }
    public void setManualInheritance(boolean manualInheritance) {
        this.manualInheritance = manualInheritance;
    }
    public String getBehaviour() {
        return behaviour;
    }
    public void setBehaviour(String behaviour) {
        this.behaviour = behaviour;
    }
	public String getContentModel() {
		return contentModel;
	}
	public void setContentModel(String contentModel) {
		this.contentModel = contentModel;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getVisibleValue() {
        return visibleValue;
    }
    public void setVisibleValue(String value) {
        this.visibleValue = value;
    }
    public String getFieldType() { 
        return this.fieldType; 
    }
    public void setFieldType(String value) { 
        this.fieldType = value; 
    }
}