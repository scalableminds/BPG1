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
	static int[] GIF_WIDTHS = {64, 128, 256, 512};
	static int GIF_PAGECOUNT = 3;
	static String TEMP_FOLDER = "/home/user/";

	
	public TextThumbnailPlugin() {
		
		readers = new ArrayList<TextReader>();
		readers.add(new PdfReader());
		readers.add(new OfficeReader());

	}
	
	public List<Artifact> onResourceFound(File resource, ResourceInfo ressourceInfo) {
		
		System.out.print("onResourceFound called ");

		List<Artifact> output = new ArrayList<Artifact>(); 
		
		if (!ressourceInfo.typ().equals("default"))
			return output;
		
		String mimetype = TikaUtil.getMimeType(resource);
		System.out.print(mimetype);
		
		Iterator<TextReader> iterator = readers.iterator();
		while (iterator.hasNext()) {
			TextReader reader = iterator.next();

			if (!reader.isSupported(mimetype))
				continue;

			output.addAll(reader.getTagClouds(resource, CLOUD_WIDTHS));
			output.addAll(reader.getThumbnails(resource, THUMBNAIL_WIDTHS));
			output.addAll(reader.getGifs(resource, THUMBNAIL_WIDTHS, GIF_PAGECOUNT));
	
		}

		return output;
		
	}
}
