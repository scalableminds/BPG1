
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import projectZoom.thumbnails.*;
import projectZoom.thumbnails.image.*;

import models.ResourceInfo;

public class thumbnailsImageTests {

	String folder = "/home/user/testfiles/";
	
	@Before
	public void setUp() throws Exception {
	}
	
		
	@Test
	public void onRessourceFoundJpgTest() {
		String filename = folder + "test.jpg";
		ImageThumbnailPlugin imageThumbnailPlugin = new ImageThumbnailPlugin();
		ResourceInfo res = new ResourceInfo(filename, "default"); 
		List<Artifact> arts = imageThumbnailPlugin.onResourceFound(res);
		
		assertTrue(true);	
	}
	
	@Test
	public void onRessourceFoundBmpTest() {
		String filename = folder + "test.bmp";
		ImageThumbnailPlugin imageThumbnailPlugin = new ImageThumbnailPlugin();
		ResourceInfo res = new ResourceInfo(filename, "default"); 
		List<Artifact> arts = imageThumbnailPlugin.onResourceFound(res);
		
		assertTrue(true);	
	}	

	@Test
	public void onRessourceFoundPngTest() {
		String filename = folder + "test.png";
		ImageThumbnailPlugin imageThumbnailPlugin = new ImageThumbnailPlugin();
		ResourceInfo res = new ResourceInfo(filename, "default"); 
		List<Artifact> arts = imageThumbnailPlugin.onResourceFound(res);
		
		assertTrue(true);	
	}
	
	@Test
	public void onRessourceFoundTifTest() {
		String filename = folder + "test.tif";
		ImageThumbnailPlugin imageThumbnailPlugin = new ImageThumbnailPlugin();
		ResourceInfo res = new ResourceInfo(filename, "default"); 
		List<Artifact> arts = imageThumbnailPlugin.onResourceFound(res);
		
		assertTrue(true);	
	}	
	
	@Test
	public void onRessourceFoundGifTest() {
		String filename = folder + "test.gif";
		ImageThumbnailPlugin imageThumbnailPlugin = new ImageThumbnailPlugin();
		ResourceInfo res = new ResourceInfo(filename, "default"); 
		List<Artifact> arts = imageThumbnailPlugin.onResourceFound(res);
		
		assertTrue(true);	
	}		
}
