

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import projectZoom.thumbnails.TempFile;
import projectZoom.thumbnails.text.TextThumbnailPlugin;

import models.ResourceInfo;

public class ThumbnailsPdfTests {
	
	String folder = "/home/user/testfiles/";

	@Before
	public void setUp() throws Exception {
		
	}
	
	@Test
	public void onRessourceFoundPdfTest() {
		
		String filename = folder + "test.pdf";
		File file = new File(filename);
		TextThumbnailPlugin textThumbnailPlugin = new TextThumbnailPlugin();
		ResourceInfo res = new ResourceInfo(filename, "default"); 
		List<TempFile> arts = textThumbnailPlugin.onResourceFound(file, res);
		
		assertTrue(arts.size() == 8);	
	}	
}
