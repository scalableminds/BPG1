
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import projectZoom.thumbnails.TempFile;
import projectZoom.thumbnails.video.VideoThumbnailPlugin;
import scala.Option;

import models.Resource;

public class ThumbnailsVideoTests {
	
	String folder = "/home/user/testfiles/";

	Option<String> empty = Option.apply(null);
	
	@Before
	public void setUp() throws Exception {
	}
	
		
	@Test
	public void onRessourceFoundWMVTest() {
		
		String filename = folder + "test.wmv";
		File file = new File(filename);
		VideoThumbnailPlugin textThumbnailPlugin = new VideoThumbnailPlugin();
		Resource res = new Resource("test.png", "default", empty); 
		List<TempFile> arts = textThumbnailPlugin.onResourceFound(file, res);
		
		assertTrue(arts.size() == 8);	
	}
}
