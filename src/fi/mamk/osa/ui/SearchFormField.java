package fi.mamk.osa.ui;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import fi.mamk.osa.solr.SolrField;

/**A Class to wrap a field in search form
 * 
 * @author JJuven
 *
 */
public class SearchFormField implements Serializable {

    private static final long serialVersionUID = 4939420544493749919L;

    public static enum Type {textarea,text,select,date,radio,checkbox,hidden,custom_yearslider};
	
	private String name;
	private Type type;
	private String messageKey;
	
	private boolean editable               = true;
	private boolean required               = false;
	private boolean generated              = false;
	private boolean visible                = true;
	private boolean autocomplete 		   = false;
	private Vector<FormOption> options	   = null;
	private LinkedHashMap<String, SearchFormField> subFields      = new LinkedHashMap<String, SearchFormField>();
	private Vector<SolrField> solrFields   = new Vector<SolrField>();
	private Vector<String> value 		   = null;
	private String defaultValue            = null;

	
	
	public SearchFormField() {
		
	}
	
	public SearchFormField(String name, Type type, String messageKey) {
		this.name = name;
		this.type = type;
		this.messageKey = messageKey;
		
	}
	
	public String toString() {
        ReflectionToStringBuilder tsb = 
       	new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
       return tsb.toString();
	}
	
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * @return the editable
	 */
	public boolean isEditable() {
		return editable;
	}

	/**
	 * @param editable the editable to set
	 */
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	/**
	 * @return the required
	 */
	public boolean isRequired() {
		return required;
	}

	/**
	 * @param required the required to set
	 */
	public void setRequired(boolean required) {
		this.required = required;
	}

	/**
	 * @return the generated
	 */
	public boolean isGenerated() {
		return generated;
	}

	/**
	 * @param generated the generated to set
	 */
	public void setGenerated(boolean generated) {
		this.generated = generated;
	}

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * @return the autocomplete
	 */
	public boolean isAutocomplete() {
		return autocomplete;
	}

	/**
	 * @param autocomplete the autocomplete to set
	 */
	public void setAutocomplete(boolean autocomplete) {
		this.autocomplete = autocomplete;
	}

	/**
	 * @return the options
	 */
	public Vector<FormOption> getOptions() {
		return options;
	}

	/**
	 * @param options the options to set
	 */
	public void setOptions(Vector<FormOption> options) {
		this.options = options;
	}

	/**
	 * @return the solrFields
	 */
	public Vector<SolrField> getSolrFields() {
		return solrFields;
	}

	/**
	 * @param solrFields the solrFields to set
	 */
	public void setSolrFields(Vector<SolrField> solrFields) {
		this.solrFields = solrFields;
	}

	/**
	 * @return the value
	 */
	public Vector<String> getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(Vector<String> value) {
		this.value = value;
	}
	
	public void setValue(String value) {
		this.value.add(value);
	}

	/**
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the messageKey
	 */
	public String getMessageKey() {
		return messageKey;
	}

	/**
	 * @param messageKey the messageKey to set
	 */
	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}	
	
	public LinkedHashMap<String, SearchFormField> getSubFields() {
		return subFields;
	}

	public void setSubFields(LinkedHashMap<String, SearchFormField> subFields) {
		this.subFields = subFields;
	}
	
	public Collection<SearchFormField> getSubFieldValues()
	{
		return this.subFields.values();
	}
}
