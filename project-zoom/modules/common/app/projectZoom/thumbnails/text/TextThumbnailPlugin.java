package projectZoom.thumbnails.text;


import models.ResourceInfo;

import java.io.File;

import java.util.*;
import java.util.List;

import projectZoom.thumbnails.*;


public class TextThumbnailPlugin {
	
	private List<TextReader> readers;
	static int[] CLOUD_WIDTHS = {64, 128};
	static int[] THUMBNAIL_WIDTHS = {256, 512};
	static int[] GIF_WIDTHS = {32, 64, 128, 256, 512};
	static int GIF_PAGECOUNT = 3;
	static String TEMP_FOLDER = "/home/user/";

	
	public TextThumbnailPlugin() {
		
		readers = new ArrayList<TextReader>();
		readers.add(new PdfReader());
		readers.add(new OfficeReader());

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
		
		Iterator<TextReader> iterator = readers.iterator();
		while (iterator.hasNext()) {
			TextReader reader = iterator.next();

			if (!reader.isSupported(mimetype))
				continue;

			output.addAll(reader.getTagClouds(file, CLOUD_WIDTHS));
			output.addAll(reader.getThumbnails(file, THUMBNAIL_WIDTHS));
			output.addAll(reader.getGifs(file, THUMBNAIL_WIDTHS, GIF_PAGECOUNT));
	
		}

		return output;
		
	}
}
