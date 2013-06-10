
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import projectZoom.thumbnails.*;
import projectZoom.thumbnails.image.ImageThumbnailPlugin;
import scala.Option;

import models.Resource;

public class ThumbnailsImageTests {

	String folder = "/home/user/testfiles/";
	String tempFolder = "/home/user/";
	
	Option<String> empty = Option.apply(null);
	
	@Before
	public void setUp() throws Exception {
	}
	
		
	@Test
	public void onRessourceFoundDocxTest() {
		String filename = folder + "test2.jpg";
		ImageThumbnailPlugin imagePlugin = new ImageThumbnailPlugin();
		Resource res = new Resource(filename, "default", empty); 
		File file = new File(filename);
		List<TempFile> tempFiles = imagePlugin.onResourceFound(file, res);
		assertTrue(tempFiles.size() == 1);	
	}

}
