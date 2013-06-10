package projectZoom.thumbnails.image;


import java.io.File;
import java.util.*;

import models.DefaultResourceTypes;
import models.ResourceLike;
import projectZoom.thumbnails.*;



public class ImageThumbnailPlugin extends ThumbnailPlugin {
	
	private List<ImageReader> readers;
	static int[] THUMBNAIL_WIDTHS = {64, 128, 256, 512};
	static String TEMP_FOLDER = "/home/user/";

	
	public ImageThumbnailPlugin() {
		
		readers = new ArrayList<ImageReader>();
		readers.add(new DefaultReader());
		//readers.add(new VisualAttentionReader());

	}
	
	public List<TempFile> onResourceFound(File file, ResourceLike resource) {
		
		System.out.println("Image onResourceFound called ");

		List<TempFile> output = new ArrayList<TempFile>(); 
		
		if (!resource.typ().equals("default"))
			return output;
		
		String mimetype = TikaUtil.getMimeType(file);
		System.out.println(mimetype);
		
		Iterator<ImageReader> iterator = readers.iterator();
		while (iterator.hasNext()) {
			ImageReader reader = iterator.next();

			if (!reader.isSupported(mimetype))
				continue;

			List<TempFile> tempFiles = reader.getImages(file, resource.name(), THUMBNAIL_WIDTHS);
			for (TempFile t: tempFiles)
				t.setType(DefaultResourceTypes.PRIMARY_THUMBNAIL());
			output.addAll(tempFiles);
		}
		return output;
	}
}
