package projectZoom.thumbnails.text;


import models.ResourceLike;
import models.DefaultResourceTypes;

import java.io.File;

import java.util.*;
import java.util.List;

import projectZoom.thumbnails.*;


public class TextThumbnailPlugin extends ThumbnailPlugin {
	
	private List<TextReader> readers;
	static int[] CLOUD_WIDTHS = {64, 128};
	static int[] THUMBNAIL_WIDTHS = {256, 512};
	static int[] GIF_WIDTHS = {64, 128, 256, 512};
	static int GIF_PAGECOUNT = 3;
	static String TEMP_FOLDER = "/home/user/";

	
	public TextThumbnailPlugin() {
		
		readers = new ArrayList<TextReader>();
		readers.add(new PdfReader());
		readers.add(new OfficeReader());

	}
	
	public List<TempFile> onResourceFound(File file, ResourceLike resource) {
		
		System.out.println("Text onResourceFound called ");

		List<TempFile> output = new ArrayList<TempFile>(); 
		
		if (!resource.typ().equals("default"))
			return output;
		
		String mimetype = TikaUtil.getMimeType(file);
		System.out.println(mimetype);
		
		Iterator<TextReader> iterator = readers.iterator();
		while (iterator.hasNext()) {
			TextReader reader = iterator.next();

			if (!reader.isSupported(mimetype))
				continue;

			List<TempFile> clouds = reader.getTagClouds(file, CLOUD_WIDTHS);
			for (TempFile a: clouds)
				a.setType(DefaultResourceTypes.PRIMARY_THUMBNAIL());
			output.addAll(clouds);

			List<TempFile> thumbnails = reader.getThumbnails(file, THUMBNAIL_WIDTHS);
			for (TempFile a: thumbnails)
				a.setType(DefaultResourceTypes.PRIMARY_THUMBNAIL());
			output.addAll(thumbnails);
			
			List<TempFile> gifs = reader.getGifs(
					file, 
					GIF_WIDTHS, 
					GIF_PAGECOUNT);
			for (TempFile a: gifs)
				a.setType(DefaultResourceTypes.SECONDARY_THUMBNAIL());
			output.addAll(gifs);
		}

		return output;
	}
}
