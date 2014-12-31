package fi.mamk.osa.ui;

import java.io.Serializable;
import java.util.List;
import java.util.Vector;

public class FormElement implements Serializable {

    private static final long serialVersionUID = -3756519340228830695L;

    // Field types
    public static enum FieldType 
    {
        undefined,
        checkbox,
        date,
        hidden,
        link,
        linked,
        nested,
        radio,
        relation,
        select,
        textarea,
        textfield,
        timerange,
        units;
        
        public static FieldType getEnum(String s)
        {
            if (checkbox.name().equalsIgnoreCase(s))
                return checkbox;
            else if (date.name().equalsIgnoreCase(s))
                return date;
            else if (link.name().equalsIgnoreCase(s))
                return link;
            else if (linked.name().equalsIgnoreCase(s))
                return linked;
            else if (radio.name().equalsIgnoreCase(s))
                return radio;
            else if (relation.name().equalsIgnoreCase(s))
                return relation;
            else if (select.name().equalsIgnoreCase(s))
                return select;
            else if (textarea.name().equalsIgnoreCase(s))
                return textarea;
            else if (textfield.name().equalsIgnoreCase(s))
                return textfield;
            else if (timerange.name().equalsIgnoreCase(s))
                return timerange;
            else if (nested.name().equalsIgnoreCase(s))
                return nested;
            else if (units.name().equalsIgnoreCase(s))
                return units;
            else if (hidden.name().equalsIgnoreCase(s))
                return hidden;
            throw new IllegalArgumentException("No Enum specified for this string");
        }
    }
    
    public static final String MULTIVALUE_DELIMITED  = "delimited";
    public static final String MULTIVALUE_MULTIFIELD = "multifield";
    public static final String MULTIVALUE_NONE 		 = "false";
    
    // Element name
    public String name;
    // Form where element belongs to
    private String form;
    // Tab of the form
    private String tab;
    
    // Value for element
    private String value;
    
    // Field type
    private FieldType fieldType;
    // Enumerated values for field
    List<String> enumValues;
    // Is value automatically generated
    public boolean generated;
    // Is element readonly
    public boolean readonly;
    // Is element value mandatory
    private boolean required;
    // Multivalue types: FormElement.MULTIVALUE_DELIMITED / FormElement.MULTIVALUE_MULTIFIELD / FormElement.MULTIVALUE_NONE
    private String multivalue;
    // Name of the ontology, if used in this field
    private String source;
    private String nestingStyle;
    
    private Vector<FormElement> nestedFormElements = new Vector<FormElement>();
    
    // Default constructor
    public FormElement()
    {
        this.name = "";
        this.form = "";
        this.tab = "";
        this.fieldType = FieldType.undefined;
        this.enumValues = null;
        this.generated = false;
        this.readonly = true;
        this.required = false;
        this.multivalue = FormElement.MULTIVALUE_NONE;
        this.source = null;
        this.nestingStyle = null;
    }
    
    // Constructor
    public FormElement(String name, 
            String form, 
            String tab, 
            FieldType fieldType, 
            List<String> enumValues, 
            boolean readonly, 
            boolean generated, 
            boolean required,
            String multivalue,
            String source,
            String nestingStyle,
            Vector<FormElement> nestedElements)
    {
        this.name = name;
        this.form = form;
        this.tab = tab;
        this.fieldType = fieldType;
        this.enumValues = enumValues;
        this.readonly = readonly;
        this.generated = generated;
        this.required = required;
        this.multivalue = multivalue;
        this.source = source;
        this.nestingStyle = nestingStyle;
        
        if (required == true) {
        	this.required = true;
        } else {
        	this.required = false;
        }
        
        this.nestedFormElements = nestedElements;
    }
    
    public FieldType getFieldType() {
        return this.fieldType;
    }
    
    public void setFieldType(FieldType value) {
        this.fieldType = value;
    }
    
    public List<String> getEnumValues() {
        return this.enumValues;
    }
    
    public void setEnumValues(List<String> value) {
        this.enumValues = value;
    }
    
    public String getForm() {
        return this.form;
    }
    
    public void setForm(String value) {
        this.form = value;
    }
    
    public boolean getGenerated() {
        return this.generated;
    }
    
    public void setGenerated(boolean value) {
        this.generated = value;
    } 

    public String getName() {
        return this.name;
    }
    
    public void setName(String value) {
        this.name = value;
    }
    
    public String getTab() {
        return this.tab;
    }
    
    public void setTab(String value) {
        this.tab = value;
    }
    
    public boolean getReadonly() {
        return this.readonly;
    }
    
    public void setReadonly(boolean value) {
        this.readonly = value;
    } 
    
    public String getValue() {
        return this.value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

    public String getMultivalue() {
        return multivalue;
    }

    public void setMultivalue(String multivalue) {
        this.multivalue = multivalue;
    }

    public Vector<FormElement> getNestedFormElements() {
        return nestedFormElements;
    }

    public void setNestedFormElements(Vector<FormElement> nestedFormElements) {
        this.nestedFormElements = nestedFormElements;
    }
    
    public String getSource() {
        return this.source;
    }
    
    public void setSource(String value) {
        this.source = value;
    }

	public String getNestingStyle() {
		return nestingStyle;
	}

	public void setNestingStyle(String nestingStyle) {
		this.nestingStyle = nestingStyle;
	}
}