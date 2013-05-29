package projectZoom.thumbnails.image;


import java.io.File;
import java.util.*;
import java.util.List;

import models.ResourceInfo;
import projectZoom.thumbnails.*;



public class ImageThumbnailPlugin extends ThumbnailPlugin {
	
	private List<ImageReader> readers;
	static int[] THUMBNAIL_WIDTHS = {64, 128, 256, 512};
	static String TEMP_FOLDER = "/home/user/";

	
	public ImageThumbnailPlugin() {
		
		readers = new ArrayList<ImageReader>();
		readers.add(new DefaultReader());

	}
	
	public List<TempFile> onResourceFound(File resource, ResourceInfo ressourceInfo) {
		
		System.out.println("Image onResourceFound called ");

		List<TempFile> output = new ArrayList<TempFile>(); 
		
		if (!ressourceInfo.typ().equals("default"))
			return output;
		
		String mimetype = TikaUtil.getMimeType(resource);
		System.out.println(mimetype);
		
		Iterator<ImageReader> iterator = readers.iterator();
		while (iterator.hasNext()) {
			ImageReader reader = iterator.next();

			if (!reader.isSupported(mimetype))
				continue;

			output.addAll(reader.getImages(ressourceInfo.name(), THUMBNAIL_WIDTHS));
		}
		return output;
	}
}
