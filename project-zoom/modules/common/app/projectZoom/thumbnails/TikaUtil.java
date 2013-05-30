package projectZoom.thumbnails;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.tika.Tika;

public class TikaUtil {

	
	public static String getMimeType(File file) {

		String mimeType = null;
 
        try {
 
            Tika tika = null;
 
            // Creating new Instance of org.apache.tika.Tika
            tika = new Tika();
 
            // Detecting MIME Type of the File 
            mimeType = tika.detect(file);
 
        } catch (FileNotFoundException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }
 
        // returning detected MIME Type
        return mimeType;

	}
}
