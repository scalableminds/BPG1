

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import projectZoom.thumbnails.TempFile;
import projectZoom.thumbnails.text.TextThumbnailPlugin;
import scala.Option;

import models.Resource;

public class ThumbnailsPdfTests {
	
	String folder = "/home/user/testfiles/";
	
	Option<String> empty = Option.apply(null);

	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void onRessourceFoundPdfTest() {
		
		String filename = folder + "test.pdf";
		File file = new File(filename);
		TextThumbnailPlugin textThumbnailPlugin = new TextThumbnailPlugin();
		Resource res = new Resource(filename, "default", empty); 
		List<TempFile> arts = textThumbnailPlugin.onResourceFound(file, res);
		
		assertTrue(arts.size() == 8);	
	}	
}
