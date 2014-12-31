package fi.mamk.osa.core;

import java.util.ArrayList;

public class ArchiveLevel {

    private int order;
    private int maxOrder;
    
    private ArrayList<ContentmodelMetadataElement> contentmodels = new ArrayList<ContentmodelMetadataElement>();
    private ArrayList<InheritedMetadataElement> inheritance = new ArrayList<InheritedMetadataElement>();
    
    /**
     * Constructor
     */
    public ArchiveLevel() {
        order = -1;
        maxOrder = -1;
    }
    
    public int getOrder() {
        return order;
    }
    
    public void setOrder(int order) {
        this.order = order;
    }
    
    public int getMaxOrder() {
        return maxOrder;
    }
    
    public void setMaxOrder(int value) {
        this.maxOrder = value;
    }
    
    public boolean getAllowDocumentsForType(String contentModel) {
        boolean bValue = false;
        contentModel = contentModel.replace(" ", "");
        
        for (ContentmodelMetadataElement cElement : this.contentmodels) {
            if (cElement.getContentModel().endsWith(contentModel)) {
                bValue = cElement.getAllowDocuments();
                break;
            }
        }
        return bValue;
    }
    
    public boolean getAllowCollectionsForType(String contentModel) {
        boolean bValue = false;
        contentModel = contentModel.replace(" ", "");
        
        for (ContentmodelMetadataElement cElement : this.contentmodels) {
            if (cElement.getContentModel().endsWith(contentModel)) {
                bValue = true;
                break;
            }
        }
        return bValue;
    }

    public ArrayList<ContentmodelMetadataElement> getContentmodels() {
        return contentmodels;
    }
    
    public void setContentmodels(ArrayList<ContentmodelMetadataElement> contentmodels) {
        this.contentmodels = contentmodels;
    }
    
    public ArrayList<InheritedMetadataElement> getInheritance() {
        return inheritance;
    }
    
    public void setInheritance(ArrayList<InheritedMetadataElement> inheritance) {
        this.inheritance = inheritance;
    }
}