package fi.mamk.osa.bean;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.imgscalr.Scalr;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.TIFFDecodeParam;

import fi.mamk.osa.stripes.OsaActionBeanContext;

/**
 * Class for several thumb datastreams
 * 
 * lhmElements          contains datastream url and content (key=datastream name)
 * lhmElementProperties contains datastream properties (key=datastream name)
 */
public class ThumbBean extends DataStream {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(ThumbBean.class);
    
    public static final String THUMB_DSID       = "thumb";
    public static final String THUMB_EXTENSION  = "png";
    
    // container for thumb-objects property data
    LinkedHashMap<String, DataStream> lhmElementProperties = new LinkedHashMap<String, DataStream>();
    
    // Constructor
    public ThumbBean(String PID) {
        super(PID);
        dsID = ThumbBean.THUMB_DSID;
    }
    
    public LinkedHashMap<String, DataStream> getElementProperties() { return this.lhmElementProperties; }
    public void setElementProperties(LinkedHashMap<String, DataStream> elements) { this.lhmElementProperties = elements; }
    
    public void setElementProperty(String dataStream, String propertyName, String propertyValue) {
        if (this.lhmElementProperties.containsKey(dataStream)) {
            this.lhmElementProperties.get(dataStream).setProperty(propertyName, propertyValue);
        } else {
            DataStream ds = new ThumbBean(dataStream);
            ds.setProperty(propertyName, propertyValue);
            this.lhmElementProperties.put(dataStream, ds);
        }
    }
    
    /**
     * createPreview for documents
     * @param filePath    absolute path 
     * @param pid         A persistent, unique identifier for the object
     * @param format      file format
     */
    public static File createThumbFile(String filePath, String pid, String format) {
        String fileExtension                = filePath.substring(filePath.lastIndexOf('.'), filePath.length());
        File thumbFile                      = null;
        BufferedImage bufferedImage         = null;
        BufferedImage thumb                 = null;
        // use the same path, but change the extension to png
        String thumbnailPathAndName         = FilenameUtils.removeExtension(filePath)+"."+THUMB_EXTENSION;
        FileOutputStream fileOutputStream   = null;
           
        if (fileExtension.equalsIgnoreCase(".pdf")) {
            try {
                // open the PDF file
                PDDocument document = PDDocument.load(filePath);
                                                
                PDPage firstPage = (PDPage) document.getDocumentCatalog().getAllPages().get(0);
                // get page 1 as an image
                bufferedImage = firstPage.convertToImage();
                thumb = Scalr.resize(bufferedImage, 250);
                
                //File outputfile = new File(thumbnailPathAndName);
                fileOutputStream = new FileOutputStream(thumbnailPathAndName);
                ImageIO.write(thumb, THUMB_EXTENSION, fileOutputStream);
                
                // close the pdf file
                document.close();
                
            } catch (IOException e) {
                logger.error("Error creating thumbnail for pdf file: "+e.getMessage());
            }
            
        } else if (format.equalsIgnoreCase("image")) {
            
            try {
                // Decide if or not try to make an animated gif thumb for animated gif files
                // At the moment it does not and current solution (Scalr) does not support making animated gifs
                
                if (fileExtension.equalsIgnoreCase(".tif")) {
                    SeekableStream s = new FileSeekableStream(filePath);
                    TIFFDecodeParam param = null;
                    ImageDecoder dec = ImageCodec.createImageDecoder("tiff", s, param);
                    RenderedImage op = dec.decodeAsRenderedImage(0);
                    // RenderedImage to BufferedImage
                    ColorModel cm = op.getColorModel();
                    int width = op.getWidth();
                    int height = op.getHeight();
                    WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
                    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
                    Hashtable properties = new Hashtable();
                    String[] keys = op.getPropertyNames();
                    if (keys!=null) {
                        for (int i = 0; i < keys.length; i++) {
                            properties.put(keys[i], op.getProperty(keys[i]));
                        }
                    }
                    bufferedImage = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
                    op.copyData(raster);
                    
                    thumb = Scalr.resize(bufferedImage, 200); // Scale the original image around the width and height of 150 px, maintaining original aspect ratio
                    fileOutputStream = new FileOutputStream(thumbnailPathAndName);
                    ImageIO.write(thumb, THUMB_EXTENSION, fileOutputStream);
                    
                } else {
                    bufferedImage = ImageIO.read(new File(filePath));
                    thumb = Scalr.resize(bufferedImage, 200); // Scale the original image around the width and height of 150 px, maintaining original aspect ratio
                    fileOutputStream = new FileOutputStream(thumbnailPathAndName);
                    ImageIO.write(thumb, THUMB_EXTENSION, fileOutputStream);
                    
                }
                
            } catch (IOException e) {
                logger.error("Error creating thumbnail for image file: "+e.getMessage());
            } 

        }
        
        thumbFile = new File(thumbnailPathAndName);
        
        if (thumbFile.canRead()) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                logger.error("Error creating thumbnail file: "+e.getMessage());
            }
        }
                
        return thumbFile;
    }
    
    public static void createThumbFileAfterUpload(String mimeType, String filename, String dir, OsaActionBeanContext context) {
        
        // Images and pdf
        if (mimeType != null && 
                mimeType.contains("image") 
                || FilenameUtils.getExtension(filename).equalsIgnoreCase("pdf")
                || FilenameUtils.getExtension(filename).equalsIgnoreCase("jpf")
                || FilenameUtils.getExtension(filename).equalsIgnoreCase("jp2")
                ) {
            String copyFilename = "";
            try {
                // Make a copy of uploaded file which the thumbnail image will be generated from
                copyFilename = THUMB_DSID+"_"+filename;
                FileUtils.copyFile(new File(dir + filename), new File(dir + copyFilename));

            } catch (IOException e) {
                e.printStackTrace();
            }

            File thumbFile = createThumbFile(dir + copyFilename, null, "image");
            // If the copied file's name does not equal the generated thumbnail's name, delete the copy.
            // This is to delete the extra image copy if the copy does not have same extension as the thumbnail (not .png).
            if (!thumbFile.getName().equals(copyFilename)) {
                new File(dir + THUMB_DSID+"_"+filename).delete();
            }
        // Rest of the formats. Uses generic "no preview" image for thumbnail 
        } else {
            String copyFilename = filename;
            String copyFileExt  = FilenameUtils.getExtension(filename);
            try {
                if (copyFileExt != null && !copyFileExt.isEmpty()) {
                    copyFilename = copyFilename.replace(copyFileExt, THUMB_EXTENSION);
                } else {
                    copyFilename = copyFilename+"."+THUMB_EXTENSION;
                }
                FileUtils.copyFile(new File(context.getServletContext().getRealPath("/img/nopreview.png")), 
                                   new File(dir + THUMB_DSID+"_"+copyFilename));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
