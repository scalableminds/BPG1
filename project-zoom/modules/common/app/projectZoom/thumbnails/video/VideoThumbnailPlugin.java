package projectZoom.thumbnails.video;


import java.io.File;
import java.util.*;
import java.util.List;

import models.ResourceInfo;
import projectZoom.thumbnails.*;
import projectZoom.thumbnails.video.VideoReader;


public class VideoThumbnailPlugin extends ThumbnailPlugin {
	
	private List<VideoReader> readers;
	static int[] THUMBNAIL_WIDTHS = {64, 128, 256, 512};
	static int THUMBNAIL_Count = 3;
	static String TEMP_FOLDER = "/home/user/";

	
	public VideoThumbnailPlugin() {
		
		readers = new ArrayList<VideoReader>();
		readers.add(new XuggleReader());

	}
	
	public List<TempFile> onResourceFound(File resource, ResourceInfo ressourceInfo) {
		
		System.out.print("onResourceFound called ");

		List<TempFile> output = new ArrayList<TempFile>(); 
		
		if (!ressourceInfo.typ().equals("default"))
			return output;
		
		String mimetype = TikaUtil.getMimeType(resource);
		System.out.print(mimetype);
		
		Iterator<VideoReader> iterator = readers.iterator();
		while (iterator.hasNext()) {
			VideoReader reader = iterator.next();

			if (!reader.isSupported(mimetype))
				continue;

			output.addAll(reader.getFrames(ressourceInfo.name(), 512, THUMBNAIL_Count));
		}
		return output;
	}
}
