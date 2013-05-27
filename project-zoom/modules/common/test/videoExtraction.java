
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import projectZoom.thumbnails.Artifact;
import projectZoom.thumbnails.video.VideoThumbnailPlugin;

import projectZoom.thumbnails.video.VideoReader;

import models.Resource;
import models.ResourceInfo;

public class videoExtraction {

	@Before
	public void setUp() throws Exception {
	}
	
		
	@Test
	public void onRessourceFoundWMVTest() {
		
		String filename = "/home/user/test.wmv";
		VideoThumbnailPlugin textThumbnailPlugin = new VideoThumbnailPlugin();
		ResourceInfo res = new ResourceInfo(filename, "default"); 
		List<Artifact> arts = textThumbnailPlugin.onResourceFound(res);
		
		assertTrue(true);	
	}
}
