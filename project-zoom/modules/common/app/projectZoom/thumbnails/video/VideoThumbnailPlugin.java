package projectZoom.thumbnails.video;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;
import org.apache.tika.Tika;

import models.ResourceInfo;
import projectZoom.thumbnails.*;
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
	
	public List<Artifact> onResourceFound(File resource, ResourceInfo ressourceInfo) {
		
		System.out.print("onResourceFound called ");

		List<Artifact> output = new ArrayList<Artifact>(); 
		
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
