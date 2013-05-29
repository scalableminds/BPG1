package projectZoom.thumbnails.video;


import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

import javax.imageio.ImageIO;

import models.DefaultResourceTypes;
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
		
		System.out.println("Video onResourceFound called ");

		List<TempFile> output = new ArrayList<TempFile>(); 
		
		if (!ressourceInfo.typ().equals("default"))
			return output;
		
		String mimetype = TikaUtil.getMimeType(resource);
		System.out.println(mimetype);
		
		Iterator<VideoReader> iterator = readers.iterator();
		while (iterator.hasNext()) {
			VideoReader reader = iterator.next();

			if (!reader.isSupported(mimetype))
				continue;
			
			List<BufferedImage> frames = reader.getFrames(ressourceInfo.name(), THUMBNAIL_Count);
			
			try {
				for (int width: THUMBNAIL_WIDTHS)
				{
					List<BufferedImage> resizedImages = new ArrayList<BufferedImage>();
					
					for (BufferedImage b: frames)
						resizedImages.add(ImageUtil.resizeBufferedImage(b, width));
					
					// default image
					BufferedImage firstImage = resizedImages.get(0);
					TempFile firstTempFile = new TempFile(width + ".png", DefaultResourceTypes.PRIMARY_THUMBNAIL());
					ImageIO.write(firstImage, "png", firstTempFile.getFile());
					output.add(firstTempFile);
	
					// sec 
					TempFile gif = ImageUtil.imagesToGif(resizedImages, width);
					if (gif != null) {
						gif.setType(DefaultResourceTypes.SECONDARY_THUMBNAIL());
						output.add(gif);
					}
					
				}			
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return output;
	}
}
