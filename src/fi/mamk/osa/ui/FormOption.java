package fi.mamk.osa.ui;

import java.io.Serializable;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class FormOption implements Serializable, Cloneable {

    private static final long serialVersionUID = -8884893842150813923L;
    private String messageKey;
	private String value;
	
	public FormOption() {
		
	}
	
	public FormOption(String messageKey, String value) {
		this.messageKey = messageKey;
		this.value = value;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return messageKey;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.messageKey = name;
	}
	
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	public String toString() {
        ReflectionToStringBuilder tsb = 
       	new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return tsb.toString();
	}
}
