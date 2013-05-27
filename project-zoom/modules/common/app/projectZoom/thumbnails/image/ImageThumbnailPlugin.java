package projectZoom.thumbnails.image;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;

import models.ResourceInfo;
import projectZoom.thumbnails.*;



public class ImageThumbnailPlugin {
	
	private List<ImageReader> readers;
	static int[] THUMBNAIL_WIDTHS = {64, 128, 256, 512};
	static String TEMP_FOLDER = "/home/user/";

	
	public ImageThumbnailPlugin() {
		
		readers = new ArrayList<ImageReader>();
		readers.add(new DefaultReader());

	}
	
	public List<Artifact> onResourceFound(ResourceInfo ressourceInfo) {
		
		System.out.print("onResourceFound called ");

		List<Artifact> output = new ArrayList<Artifact>(); 
		
		if (!ressourceInfo.typ().equals("default"))
			return output;
		
		String filename = ressourceInfo.fileName();
		File file = new File(filename); 
		
		String mimetype = TikaUtil.getMimeType(file);
		System.out.print(mimetype);
		
		Iterator<ImageReader> iterator = readers.iterator();
		while (iterator.hasNext()) {
			ImageReader reader = iterator.next();

			if (!reader.isSupported(mimetype))
				continue;

			output.addAll(reader.getImages(ressourceInfo.fileName(), THUMBNAIL_WIDTHS));
		}
		return output;
	}
}
