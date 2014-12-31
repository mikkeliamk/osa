package fi.mamk.osa.bean;

import java.io.InputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Vector;
import org.apache.log4j.Logger;
import fi.mamk.osa.bean.MetaDataElement.MetaDataType;

/**
 * Datastream class
 */
public abstract class DataStream implements Serializable {
    
    private static final long serialVersionUID = 8605655186952919889L;
    private static final Logger logger = Logger.getLogger(DataStream.class);
    
    public static final String DS_LABEL         = "dsLabel";
    public static final String DS_CHECKSUM      = "dsChecksum";
    public static final String DS_CHECKSUMTYPE  = "dsChecksumType";
    public static final String DS_CONTROLGROUP  = "dsControlGroup";
    public static final String DS_CREATEDATE    = "dsCreateDate";
    public static final String DS_FORMATURI     = "dsFormatURI";
    public static final String DS_ID            = "sID";
    public static final String DS_INFOTYPE      = "dsInfoType";
    public static final String DS_LOCATION      = "dsLocation";
    public static final String DS_LOCTYPE       = "dsLocationType";
    public static final String DS_MIME          = "dsMIME";
    public static final String DS_PID           = "id";
    public static final String DS_SIZE          = "dsSize";
    public static final String DS_STATE         = "dsState";
    public static final String DS_VERSIONABLE   = "dsVersionable";
    public static final String DS_VERSIONID     = "dsVersionID";
    
    // The algorithm used to compute the checksum
    public static enum ChecksumType {
        DEFAULT("DEFAULT"),
        DISABLED("DISABLED"),
        MD5("MD5"),
        SHA1("SHA-1"),
        SHA256("SHA-256"),
        SHA385("SHA-385"),
        SHA512("SHA-512"),
        HAVAL("HAVAL"),
        TIGER("TIGER"),
        WHIRLPOOL("TIGER");
        
        private String name;       

        private ChecksumType(String s) {
            name = s;
        }
        
        public static ChecksumType getEnum(String s) {
            if (DEFAULT.name.equals(s))
                return DEFAULT;
            else if (DISABLED.name.equals(s))
                return DISABLED;
            else if (MD5.name.equals(s))
                return MD5;
            else if (SHA1.name.equals(s))
                return SHA1;
            else if (SHA256.name.equals(s))
                return SHA256;
            else if (SHA385.name.equals(s))
                return SHA385;
            else if (SHA512.name.equals(s))
                return SHA512;
            else if (HAVAL.name.equals(s))
                return HAVAL;
            else if (TIGER.name.equals(s))
                return TIGER;
            else if (WHIRLPOOL.name.equals(s))
                return WHIRLPOOL;
            throw new IllegalArgumentException("No Enum specified for this string");
        }
    }
    
    // indicates the kind of datastream
    public static enum ControlGroup {
        DEFAULT,
        E,          // ExternallyReferencedContent
        R,          // RedirectedContent
        M,          // ManagedContent
        X;          // InlineXML
        
        public static ControlGroup getEnum(String s) {
            if (E.name().equals(s))
                return E;
            else if (R.name().equals(s))
                return R;
            else if (M.name().equals(s))
                return M;
            else if (X.name().equals(s))
                return X;
            throw new IllegalArgumentException("No Enum specified for this string");
        }
    }
    
    // datastream state
    public static enum State {
        DEFAULT,
        A,          // Active
        I,          // Inactive
        D;          // Deleted
        
        public static State getEnum(String s) {
            if (A.name().equals(s))
                return A;
            else if (I.name().equals(s))
                return I;
            else if (D.name().equals(s))
                return D;
            throw new IllegalArgumentException("No Enum specified for this string");
        }
    }
    
    // Content location type
    public static enum LocationType {
        DEFAULT,
        URL,
        INTERNAL_ID;
        
        public static LocationType getEnum(String s) {
            if (URL.name().equals(s))
                return URL;
            else if (INTERNAL_ID.name().equals(s))
                return INTERNAL_ID;
            throw new IllegalArgumentException("No Enum specified for this string");
        }
    }
        
    // a true/false indication as to whether the datastream should be versioned by the Fedora repository service
    protected boolean bIsVersionable;
    // datastream size
    protected int size;
    // checksum type
    protected ChecksumType eChecksumType;
    // indicates the kind of datastream
    protected ControlGroup eControlGroup;
    // location type
    protected LocationType eLocationType;
    // MIME type
    protected String eMimeType;
    // datastream state
    protected State eState;
    // checksum
    protected String checksum;
    // creation date for the datastream version
    protected String createDate;
    // unique identifier for the datastream
    protected String dsID;
    // format uri
    protected String formatURI;
    // info type
    protected String infoType;
    // user-assigned descriptive label for the datastream
    protected String label;
    // latest modification date
    protected String lastModifiedDate;
    // points to content on a external web server or to an internal identifier instead of a URL
    protected String location; 
    // digital object identifier
    protected String PID;
    // version
    protected String versionID;
    // The owner(s) of the object
    protected Vector<String> vectOwnerId;
    
    // content data
    protected LinkedHashMap<String, MetaDataElement> lhmElements = new LinkedHashMap<String, MetaDataElement>();

    // Constructor
    public DataStream(String PID) {
        this.PID = PID;
        this.bIsVersionable = true;
        this.createDate = null;
        this.dsID = null;
        this.eChecksumType = ChecksumType.DEFAULT;
        this.eControlGroup = ControlGroup.DEFAULT;
        this.eLocationType = LocationType.DEFAULT;
        this.eMimeType = "";
        this.eState = State.DEFAULT;
        this.lastModifiedDate = null;
        this.versionID = null;
    }
    
    // Constructor
    public DataStream(String pid, ControlGroup cGroup, State state) {
        this.PID = pid;
        this.eControlGroup = cGroup;
        this.eState = state;
        this.bIsVersionable = true;
    }
    
    // Constructor
    public DataStream(LinkedHashMap<String, MetaDataElement> elements) {
        this.lhmElements = elements;
    }
    
    public ChecksumType getChecksumType() { return this.eChecksumType; }
    public void setChecksumType(ChecksumType value) { this.eChecksumType = value; }
    
    public ControlGroup getControlGroup() { return this.eControlGroup; }
    public void setControlGroup(ControlGroup value) { this.eControlGroup = value; }
    
    public String getCreateDate() { return this.createDate; }
    public void setCreateDate(String date) { this.createDate = date; }
    
    public String getDataStreamID() { return this.dsID; }
    public void setDataStreamID(String id) { this.dsID = id; }
    
    public LinkedHashMap<String, MetaDataElement> getMetaDataElements() { return this.lhmElements; }
    public void setMetaDataElements(LinkedHashMap<String, MetaDataElement> elements) { this.lhmElements = elements; }
    
    public void setMetaDataElement(MetaDataElement newElement) {
        if (lhmElements.containsKey(newElement.getName())) {
            MetaDataElement element = lhmElements.get(newElement.getName());
            element.getValues().add(newElement.getValue());
            lhmElements.put(newElement.getName(), element);
        } else {
            lhmElements.put(newElement.getName(), newElement);
        }
    }
    
    public void setMetaDataElement(String name, String visibleName, String value, MetaDataType eType) {
        if (lhmElements.containsKey(name)) {
        	MetaDataElement element = lhmElements.get(name);
        	element.getValues().add(value);
        	lhmElements.put(name, element);
        } else {
        	MetaDataElement mdElement = new MetaDataElement(name, visibleName, value, eType);
        	lhmElements.put(name, mdElement);
        }
    }
    
    public void setMetaDataElement(String name, 
                                   String visibleName, 
                                   String value, 
                                   MetaDataType eType, 
                                   String multivalueType, 
                                   Vector<LinkedHashMap<String, MetaDataElement>> nestedElements, 
                                   InputStream is) {
        
        if (lhmElements.containsKey(name)) {
            MetaDataElement element = lhmElements.get(name);
            element.getValues().add(value);
            lhmElements.put(name, element);
        } else {
            MetaDataElement mdElement = new MetaDataElement(name, visibleName, value, eType, multivalueType, is, nestedElements, null);
            lhmElements.put(name, mdElement);
        }
    }

    public void setNestedMetaDataElement(String parent, String name, String visibleName, String value, MetaDataType eType) {
        MetaDataElement parentElement = null;
        if (lhmElements.containsKey(parent)) {
            parentElement = lhmElements.get(parent);
            
        } else {
            parentElement = new MetaDataElement(parent, parent, null, eType);
            lhmElements.put(parent, parentElement);
        }
        
        MetaDataElement nestedElement = new MetaDataElement(name, visibleName, value, eType);
        lhmElements.get(parent).setNestedElement(name, nestedElement);
    }
    
    public String getProperty(String name) {
        String retValue = "";
        
        if (DataStream.DS_LABEL.equals(name)) {
            retValue = this.getLabel();
        } else if (DataStream.DS_VERSIONID.equals(name)) {
            retValue = this.getVersionID();
        } else if (DataStream.DS_CREATEDATE.equals(name)) {
            retValue = this.getCreateDate();
        } else if (DataStream.DS_STATE.equals(name)) {
            retValue = this.getState().toString();
        } else if (DataStream.DS_FORMATURI.equals(name)) {
            retValue = this.getFormatURI();
        } else if (DataStream.DS_CONTROLGROUP.equals(name)) {
            retValue = this.getControlGroup().toString();
        } else if (DataStream.DS_INFOTYPE.equals(name)) {
            retValue = this.getInfoType();
        } else if (DataStream.DS_CHECKSUM.equals(name)) {
            retValue = this.getChecksum();
        } else if (DataStream.DS_CHECKSUMTYPE.equals(name)) {
            retValue = this.getChecksumType().toString();
        } else if (DataStream.DS_LOCATION.equals(name)) {
            retValue = this.getLocation();
        } else if (DataStream.DS_LOCTYPE.equals(name)) {
            retValue = this.getLocationType().toString();
        } else if (DataStream.DS_VERSIONABLE.equals(name)) {
            retValue = String.valueOf(this.getIsVersionable());
        } else if (DataStream.DS_SIZE.equals(name)) {
            retValue = Integer.toString(this.getDataStreamSize());
        } else if (DataStream.DS_MIME.equals(name)) {
            retValue = this.getMimeFormat();
        } else {
            logger.info("missing "+name);
        }
        
        return retValue;
    }
    
    public void setProperty(String name, String value) {
        if (DataStream.DS_LABEL.equals(name)) {
            this.setLabel(value);
        } else if (DataStream.DS_VERSIONID.equals(name)) {
            this.setVersionID(value);
        } else if (DataStream.DS_CREATEDATE.equals(name)) {
            this.setCreateDate(value);
        } else if (DataStream.DS_STATE.equals(name)) {
            this.setState(DataStream.State.getEnum(value.toString()));
        } else if (DataStream.DS_FORMATURI.equals(name)) {
            this.setFormatURI(value);
        } else if (DataStream.DS_CONTROLGROUP.equals(name)) {
            this.setControlGroup(DataStream.ControlGroup.getEnum(value.toString()));
        } else if (DataStream.DS_INFOTYPE.equals(name)) {
            this.setInfoType(value);
        } else if (DataStream.DS_CHECKSUM.equals(name)) {
            this.setChecksum(value);
        } else if (DataStream.DS_CHECKSUMTYPE.equals(name)) {
            this.setChecksumType(DataStream.ChecksumType.getEnum(value.toString()));
        } else if (DataStream.DS_LOCATION.equals(name)) {
            this.setLocation(value);
        } else if (DataStream.DS_LOCTYPE.equals(name)) {
            this.setLocationType(DataStream.LocationType.getEnum(value.toString()));
        } else if (DataStream.DS_VERSIONABLE.equals(name)) {
            this.setIsVersionable(Boolean.parseBoolean(value.toString()));
        } else if (DataStream.DS_SIZE.equals(name)) {
            this.setDataStreamSize(Integer.parseInt(value.toString()));
        } else if (DataStream.DS_MIME.equals(name)) {
            this.setMimeFormat(value);
        } else {
            logger.info("missing "+name);
        }

    }
    
    public String getChecksum() { return this.checksum; }
    public void setChecksum(String value) { this.checksum = value; }
    
    public int getDataStreamSize() { return this.size; }
    public void setDataStreamSize(int value) { this.size = value; }
    
    public String getFormatURI() { return this.formatURI; }
    public void setFormatURI(String value) { this.formatURI = value; }
    
    public String getInfoType() { return this.infoType; }
    public void setInfoType(String value) { this.infoType = value; }
    
    public boolean getIsVersionable() { return this.bIsVersionable; }
    public void setIsVersionable(boolean bValue) { this.bIsVersionable = bValue; }
    
    public String getLabel() { return this.label; }
    public void setLabel(String value) { this.label = value; }
    
    public String getLastModDate() { return this.lastModifiedDate; }
    public void setLastModDate(String date) { this.lastModifiedDate = date; }
    
    public String getLocation() { return this.location; }
    public void setLocation(String value) { this.location = value; }
    
    public LocationType getLocationType() { return this.eLocationType; }
    public void setLocationType(LocationType value) { this.eLocationType = value; }
    
    public String getMimeFormat() { return this.eMimeType; }
    public void setMimeFormat(String value) { this.eMimeType = value; }
    
    public Vector<String> getOwnerID() { return this.vectOwnerId; }
    public void setOwnerID(Vector<String> owners) { this.vectOwnerId = owners; }
    
    public String getPID() { return this.PID; }
    public void setPID(String id) { this.PID = id; }
    
    public State getState() { return this.eState; }
    public void setState(State state) { this.eState = state; }
    
    public String getVersionID() { return this.versionID; }
    public void setVersionID(String value) { this.versionID = value; }
    
    public void removeMetaDataElement(String name) {
        this.getMetaDataElements().remove(name);
    }
    
}
