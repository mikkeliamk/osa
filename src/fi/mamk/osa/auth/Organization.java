package fi.mamk.osa.auth;

import java.io.Serializable;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/** 
 * A Class to wrap information of an organization.
 * 
 * @author Jussi Juvén
 */
public class Organization implements Serializable {

    private static final long serialVersionUID = -7938898193034374845L;
    
	/** Distinguished name of the organization */
	private String dn = null;
	/** Name of the organization */
	private String name = null;
    /** Description of the organization */
	private String description = null;
    /** Unique Identifier of the organization */
	private String identifier = null;
    /** URI-identifier of the organization, typically an URL */
	private String uri = null;
	/** Relative configuration file path */
	private String configurationFile = null;
	/** Absolute path of the TSM options file */ 
	private String tsmOptFile = null;
	
	
    /**
     * Default constructor.
     * Creates a null-valued organization object.
     */
    public Organization() {
    }
    /** 
     * Creates organization object with given values.
     * 
     * @param dn Distinguished name of the organization
     * @param name Name of the organization
     * @param description Description of the organization
     */
    public Organization(String dn, String name, String description) {
        this.name = name;
        this.description = description;
        this.dn = dn;
    }
    /** 
     * Creates organization object with given values.
     * 
     * @param dn Distinguished name of the organization
     * @param name Name of the organization
     * @param description Description of the organization
     * @param id Identifier of the organization
     */
    public Organization(String dn, String name, String description, String id) {
        this.name = name;
        this.description = description;
        this.dn = dn;
        this.identifier = id;
    }
    
    
    public boolean equals(Organization anotherOrganization) {
    	return (this.dn.equals(anotherOrganization.getDn()) && this.identifier.equals(anotherOrganization.getIdentifier()));
    }
    
    public String toString() {
        
        ReflectionToStringBuilder tsb = 
        	new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return tsb.toString();
    }
	/**
	 * @return the dn
	 */
	public String getDn() {
		return dn;
	}
	/**
	 * @param dn the dn to set
	 */
	public void setDn(String dn) {
		this.dn = dn;
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the identifier
	 */
	public String getIdentifier() {
		return identifier;
	}
	/**
	 * @param identifier the identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	/**
	 * @return the uri
	 */
	public String getUri() {
		return uri;
	}
	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	public void setConfigurationFile(String configurationFile) {
		this.configurationFile = configurationFile;
	}
	public String getConfigurationFile() {
		return configurationFile;
	}
	public void setTsmOptFile(String tsmOptFile) {
		this.tsmOptFile = tsmOptFile;
	}
	public String getTsmOptFile() {
		return tsmOptFile;
	}
    
}
