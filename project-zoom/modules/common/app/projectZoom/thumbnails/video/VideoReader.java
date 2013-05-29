package projectZoom.thumbnails.video;

import projectZoom.thumbnails.*;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

public class VideoReader {

	String[] MIME_TYPES = {};
	
	public Boolean isSupported(String mimetype)
	{
		for (int i = 0; i < this.MIME_TYPES.length; i++)
		{
			String supportedMimeType = this.MIME_TYPES[i];
			if (mimetype.equals(supportedMimeType))
				return true;
		}
		return false;
	}	
	
	public List<BufferedImage> getFrames(String filename, int count)
	{
		return new ArrayList<BufferedImage>();
	}
	
}
