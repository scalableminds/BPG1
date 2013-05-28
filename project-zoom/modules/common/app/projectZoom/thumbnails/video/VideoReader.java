package projectZoom.thumbnails.video;

import java.io.File;
import projectZoom.thumbnails.*;
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
	
	public List<Artifact> getFrames(String filename, int width, int count)
	{
		return new ArrayList<Artifact>();
	}
	
}
