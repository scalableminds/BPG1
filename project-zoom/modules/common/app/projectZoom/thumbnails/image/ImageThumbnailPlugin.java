package projectZoom.thumbnails.image;


import java.io.File;
import java.util.*;

import models.DefaultResourceTypes;
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

			List<TempFile> tempFiles = reader.getImages(resource, ressourceInfo.name(), THUMBNAIL_WIDTHS);
			for (TempFile t: tempFiles)
				t.setType(DefaultResourceTypes.PRIMARY_THUMBNAIL());
			output.addAll(tempFiles);
		}
		return output;
	}
}
