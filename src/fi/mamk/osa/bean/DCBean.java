package fi.mamk.osa.bean;

import org.apache.log4j.Logger;

/**
 * Class for dublin core datastream
 * 
 * lhmElements          contains dublin core data
 */
public class DCBean extends DataStream {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(DCBean.class);
    
    public static final String DC_DSID  = "DC";
    public static final String DC_LABEL = "Dublin Core Record";

    // Resource types (The nature or genre of the resource)
    public static enum Type {
        Undefined,
        Collection,
        Dataset,
        Drawing,
        Email,
        Event,
        Folder,
        Image,
        InteractiveResource,
        Model,
        MovingImage,
        PhysicalObject,
        Service,
        Software,
        Sound,
        Text,
        other;
        
        public static Type getEnum(String s) {
            if (Collection.name().equals(s))
                return Collection;
            else if (Dataset.name().equals(s))
                return Dataset;
            else if (Drawing.name().equals(s))
                return Drawing;
            else if (Email.name().equals(s))
                return Email;
            else if (Event.name().equals(s))
                return Event;
            else if (Folder.name().equals(s))
                return Folder;
            else if (Image.name().equals(s))
                return Image;
            else if (InteractiveResource.name().equals(s))
                return InteractiveResource;
            else if (Model.name().equals(s))
                return Model;
            else if (MovingImage.name().equals(s))
                return MovingImage;
            else if (PhysicalObject.name().equals(s))
                return PhysicalObject;
            else if (Service.name().equals(s))
                return Service;
            else if (Software.name().equals(s))
                return Software;
            else if (Sound.name().equals(s))
                return Sound;
            else if (Text.name().equals(s))
                return Text;
            else if (other.name().equals(s))
                return other;
            throw new IllegalArgumentException("No Enum specified for this string");
        }
    }
    
    // Constructor
    public DCBean(String PID) 
    {
        super(PID);
        dsID = DCBean.DC_DSID;
    }
        
}
