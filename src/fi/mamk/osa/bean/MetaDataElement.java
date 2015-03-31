package fi.mamk.osa.bean;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import fi.mamk.osa.ui.FormElement;

/**
 * Class for meta data
 * All datastreams contains MetaDataElements in container
 */
public class MetaDataElement implements Serializable {

    private static final long serialVersionUID = -5496828257828486275L;
    private static final Logger logger = Logger.getLogger(MetaDataElement.class);
    
    // indicates the kind of metadata
    public static enum MetaDataType {
        string,     // String
        date,       // Date
        integer,    // Integer
        link,       //
        relation,   // Object relationships
        select;     // Enum value
        
        public static MetaDataType getEnum(String s) {
            if (string.name().equalsIgnoreCase(s))
                return string;
            else if (date.name().equalsIgnoreCase(s))
                return date;
            else if (integer.name().equalsIgnoreCase(s))
                return integer;
            else if (link.name().equalsIgnoreCase(s))
                return link;
            else if (relation.name().equalsIgnoreCase(s))
                return relation;
            else if (select.name().equalsIgnoreCase(s))
                return select;
            else if (s.startsWith("text") || s.startsWith("time"))
                return string;
            throw new IllegalArgumentException("No Enum specified for this string");
        }
    }
    
    private String name;
    private String visibleName;
    private String value;
    private Vector<String> values;
    private String visibleValue;
    private MetaDataType eMetaDataType;
    // Multivalue types: FormElement.MULTIVALUE_DELIMITED / FormElement.MULTIVALUE_MULTIFIELD / FormElement.MULTIVALUE_NONE
    private String multivalueType;
    // Name of the ontology, if used in this field
    private String source;
    // Is value automatically generated
    private boolean generated;
    // For original, attachment, preview content
    private InputStream inputStream;

    // list of datastreams, where metadata belongs to
    // used, when mapping metadata to datastreams determined in contentmodel
    private List<String> datastreams;
    String datastream;
    
    // Enumerated values for field
    private List<String> enumValues;

    private Vector<LinkedHashMap<String, MetaDataElement>> nestedElements;

    // Default constructor
    public MetaDataElement() {
        this.name = null;
        this.visibleName = null;
        this.value = null;
        this.values = new Vector<String>();
        this.generated = false;
        this.eMetaDataType = MetaDataType.string;
        this.multivalueType = FormElement.MULTIVALUE_NONE;
        this.source = null;
        this.inputStream = null;       
        this.nestedElements = new Vector<LinkedHashMap<String, MetaDataElement>>();
        this.datastream = null;
        this.datastreams = new ArrayList<String>();
    }
    
    // Constructor
    public MetaDataElement(String name, String visibleName, String value, MetaDataType eType) {
        this.name = name;
        this.visibleName = visibleName;
        this.value = value;
        this.values = new Vector<String>();
        this.values.add(value);
        
        if (isRelation(name)) {
            this.eMetaDataType = MetaDataType.relation;
        } else {
            this.eMetaDataType = eType;
        }
        
        this.generated = false;
        this.multivalueType = FormElement.MULTIVALUE_NONE;
        this.source = null;
        this.inputStream = null;
        this.nestedElements = new Vector<LinkedHashMap<String, MetaDataElement>>();
        this.datastream = null;
        this.datastreams = new ArrayList<String>();
    }
    
    // Constructor
    public MetaDataElement(MetaDataElement element) {
        this.name = element.getName();
        this.visibleName = element.getVisibleName();
        this.value = element.getValue();
        this.values = new Vector<String>();
        this.values = element.getValues();
        this.generated = element.getGenerated();
        this.eMetaDataType = element.getMetaDataType();
        this.multivalueType = element.getMultivalueType();
        this.source = element.getSource();
        this.inputStream = element.getInputStream();
        this.nestedElements = new Vector<LinkedHashMap<String, MetaDataElement>>();
        this.nestedElements = element.getNestedElements(); 
        this.datastream = element.getDatastream();
        this.datastreams = element.getDatastreams();
    }
    
    // Constructor
    public MetaDataElement(String name, 
                           String visibleName, 
                           String value, 
                           MetaDataType eType, 
                           String multivalue, 
                           InputStream is,
                           Vector<LinkedHashMap<String, MetaDataElement>> nestedElements,
                           String ds) {
        this.name = name;
        this.visibleName = visibleName;
        this.value = value;
        this.values = new Vector<String>();
        this.values.add(value);
        
        if (isRelation(name)) {
            this.eMetaDataType = MetaDataType.relation;
        } else {
            this.eMetaDataType = eType;
        }
        
        this.generated = false;
        this.multivalueType = multivalue;
        this.source = null;
        this.inputStream = is;
        this.nestedElements = new Vector<LinkedHashMap<String, MetaDataElement>>();
        if (nestedElements != null) {
            this.nestedElements = nestedElements;
        }
        this.datastream = ds;
        this.datastreams = new ArrayList<String>();
        this.setDatastream(ds);
    }
    
    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;       

        if (object != null && object instanceof MetaDataElement) {
            String objectName = ((MetaDataElement) object).getName();
            isEqual = (this.name.equalsIgnoreCase(objectName));
        }

        return isEqual;
    }

    /**
     * Check if metadata element has value
     * @return
     */
    public boolean isEmpty() {
        boolean retValue = true;
        if (this.getValue() != null) {
            retValue = false;
        } else if (this.getValues().size() > 0 && this.getValues().get(0) != null) {
            retValue = false;
        } else if (this.getNestedElements().size() > 0) {
            Vector<LinkedHashMap<String, MetaDataElement>> nestedElements = this.getNestedElements();
            for (LinkedHashMap<String, MetaDataElement> lhm : nestedElements) {
                if (lhm != null) {
                    Iterator<Entry<String, MetaDataElement>> iterLhm = lhm.entrySet().iterator();
                    while (iterLhm.hasNext()) {
                        Map.Entry<String, MetaDataElement> entryLhm = (Map.Entry<String, MetaDataElement>) iterLhm.next();
                        MetaDataElement nestedElement = entryLhm.getValue();
                        if (nestedElement.getValue() != null) {
                            retValue = false;
                        } else if (nestedElement.getValues().size() > 0 && nestedElement.getValues().get(0) != null) {
                            retValue = false;
                        }
                        break;
                    }
                }
            }
        }
        return retValue;
    }
    
    @Override
    public String toString() {
        return "metadataelement [name="+name+", value="+value+", values="+values.toString()+", type="+eMetaDataType.toString()+", nestedElements="+nestedElements+"]";
    }
    
    public List<String> getEnumValues() { return this.enumValues; }
    
    public void setEnumValues(List<String> value) {
        if (value != null) {
            this.enumValues = value;
        }
    }
    
    public MetaDataType getMetaDataType() { return this.eMetaDataType; }
    
    public void setMetaDataType(MetaDataType value) { this.eMetaDataType = value; }
    
    public String getName() { return this.name; }
    
    public void setName(String value) {
        this.name = value;
        if (isRelation(this.name)) {
            this.eMetaDataType = MetaDataType.relation;
        }
    }
    
    public String getVisibleName() { return this.visibleName; }
    
    public void setVisibleName(String value) {
        this.visibleName = value;
        if (isRelation(this.visibleName)) {
            this.eMetaDataType = MetaDataType.relation;
        }
    }
    
    public List<String> getDatastreams() { return this.datastreams; }
    
    public String getDatastream() { return this.datastream; }
    
    public void setDatastream(String value) {
        this.datastream = value;
        if (value != null || value != "") {
            this.datastreams.add(value);
        }
    }
    
    public String getValue() {
        return this.value;
    }
    
    public void removeValue() { 
        this.value = "";
        this.values.removeAllElements();
    }
    
    public void setValue(String value) { 
        this.value = value;
        if (!this.values.contains(value)) {
            this.values.add(value);
        }
    }
    
    public Vector<String> getValues() { 
        return this.values;
    }
    
    public void setValues(Vector<String> values) { 
        this.values = values;
    }
    
    public String getVisibleValue() {
        return visibleValue;
    }

    public void setVisibleValue(String visibleValue) {
        this.visibleValue = visibleValue;
    }
    
    public String getMultivalueType() {
        return multivalueType;
    }

    public void setMultivalueType(String value) {
        this.multivalueType = value;
    }

    public String getSource() {
        return this.source;
    }
    
    public void setSource(String value) {
        this.source = value;
    }
    
    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream value) {
        this.inputStream = value;
    }
    
    public boolean getGenerated() {
        return this.generated;
    }
    
    public void setGenerated(boolean value) {
        this.generated = value;
    }
    
    public boolean isRelation(String name) {
        boolean retValue = false;
        
        if (this.name.equalsIgnoreCase(CaptureBean.RELATION_ConformsTo)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_HasFormat)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_HasPart)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_HasRedaction)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_HasVersion)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_IsChildOf)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_IsFormatOf)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_IsMemberOf)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_IsParentOf)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_IsPartOf)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_IsRedactionOf)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_IsReferencedBy)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_IsReplacedBy)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_IsRequiredBy)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_IsVersionOf)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_References)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_Replaces)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_Requires)
            || this.name.equalsIgnoreCase(CaptureBean.RELATION_Source)
            )
        {
            retValue = true;
        }
        
        return retValue;
    }

	public Vector<LinkedHashMap<String, MetaDataElement>> getNestedElements() {
		return nestedElements;
	}

	public void setNestedElements(Vector<LinkedHashMap<String, MetaDataElement>> nestedElements) {
		this.nestedElements = nestedElements;
	}
	
	public void setNestedElement(String name, MetaDataElement nestedElement) {

	    boolean addNewHashMap = false;
        if (nestedElements.isEmpty()) {
            LinkedHashMap<String, MetaDataElement> lhm = new LinkedHashMap<String, MetaDataElement>();
            lhm.put(name, nestedElement);
            this.nestedElements.add(lhm);
            
        } else {
            
            for (int i=0; i<this.nestedElements.size(); i++) {
                LinkedHashMap<String, MetaDataElement> currentLhm = this.nestedElements.get(i);
                if (currentLhm.containsKey(name)) {
                    addNewHashMap = true;
                } else {
                    this.nestedElements.get(i).put(name, nestedElement);
                    addNewHashMap = false;
                }
            }
            
            if (addNewHashMap) {
                LinkedHashMap<String, MetaDataElement> lhm = new LinkedHashMap<String, MetaDataElement>();
                lhm.put(name, nestedElement);
                this.nestedElements.add(lhm);
                addNewHashMap = false;
            }
        }
    }

}
