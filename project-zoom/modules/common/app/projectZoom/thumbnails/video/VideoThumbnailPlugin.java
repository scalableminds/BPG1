package projectZoom.thumbnails.video;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.List;
import org.apache.tika.Tika;

import models.ResourceInfo;
import projectZoom.thumbnails.Artifact;
import projectZoom.thumbnails.video.VideoReader;


public class VideoThumbnailPlugin {
	
	private List<VideoReader> readers;
	static int[] THUMBNAIL_WIDTHS = {64, 128, 256, 512};
	static int THUMBNAIL_Count = 3;
	static String TEMP_FOLDER = "/home/user/";

	
	public VideoThumbnailPlugin() {
		
		readers = new ArrayList<VideoReader>();
		readers.add(new XuggleReader());

	}
	
	public List<Artifact> onResourceFound(ResourceInfo ressourceInfo) {
		
		System.out.print("onResourceFound called ");

		List<Artifact> output = new ArrayList<Artifact>(); 
		
		if (!ressourceInfo.typ().equals("default"))
			return output;
		
		String filename = ressourceInfo.fileName();
		File file = new File(filename); 
		
		String mimetype = getMimeType(file);
		System.out.print(mimetype);
		
		Iterator<VideoReader> iterator = readers.iterator();
		while (iterator.hasNext()) {
			VideoReader reader = iterator.next();

			if (!reader.isSupported(mimetype))
				continue;

			output.addAll(reader.getFrames(ressourceInfo.fileName(), 512, THUMBNAIL_Count));
	
		}

		return output;
		
	}

	
	public String getMimeType(File file) {

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
