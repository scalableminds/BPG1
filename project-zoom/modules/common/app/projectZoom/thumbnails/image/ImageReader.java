package projectZoom.thumbnails.image;

import projectZoom.thumbnails.*;

import java.util.List;
import java.util.ArrayList;

public class ImageReader {

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
	
	public List<TempFile> getImages(String filename, int[] widths)
	{
		return new ArrayList<TempFile>();
	}
	
}
