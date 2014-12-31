package fi.mamk.osa.core;

import java.io.Serializable;

public class ContentmodelMetadataElement implements Serializable {
    private static final long serialVersionUID = 8146909219105931003L;
    private String name;
    private String contentModel;
    private boolean allowDocuments;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getContentModel() {
        return contentModel;
    }
    public void setContentModel(String contentModel) {
        this.contentModel = contentModel;
    }
    public Boolean getAllowDocuments() {
        return allowDocuments;
    }
    public void setAllowDocuments(Boolean allowDocuments) {
        this.allowDocuments = allowDocuments;
    }

}
