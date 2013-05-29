
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import projectZoom.thumbnails.*;
import projectZoom.thumbnails.image.*;

import models.ResourceInfo;

public class ThumbnailsImageTests {

	String folder = "/home/user/testfiles/";
	
	@Before
	public void setUp() throws Exception {
	}
	
		
	@Test
	public void onRessourceFoundJpgTest() {
		String filename = folder + "test.jpg";
		File file = new File(filename);
		ImageThumbnailPlugin imageThumbnailPlugin = new ImageThumbnailPlugin();
		ResourceInfo res = new ResourceInfo(filename, "default"); 
		List<TempFile> arts = imageThumbnailPlugin.onResourceFound(file, res);
		
		assertTrue(arts.size() == 4);	
	}
	
	@Test
	public void onRessourceFoundBmpTest() {
		String filename = folder + "test.bmp";
		File file = new File(filename);
		ImageThumbnailPlugin imageThumbnailPlugin = new ImageThumbnailPlugin();
		ResourceInfo res = new ResourceInfo(filename, "default"); 
		List<TempFile> arts = imageThumbnailPlugin.onResourceFound(file, res);
		
		assertTrue(arts.size() == 4);
	}	

	@Test
	public void onRessourceFoundPngTest() {
		String filename = folder + "test.png";
		File file = new File(filename);
		ImageThumbnailPlugin imageThumbnailPlugin = new ImageThumbnailPlugin();
		ResourceInfo res = new ResourceInfo(filename, "default"); 
		List<TempFile> arts = imageThumbnailPlugin.onResourceFound(file, res);
		
		assertTrue(arts.size() == 4);
	}
	
	@Test
	public void onRessourceFoundTifTest() {
		String filename = folder + "test.tif";
		File file = new File(filename);
		ImageThumbnailPlugin imageThumbnailPlugin = new ImageThumbnailPlugin();
		ResourceInfo res = new ResourceInfo(filename, "default"); 
		List<TempFile> arts = imageThumbnailPlugin.onResourceFound(file, res);
		
		assertTrue(arts.size() == 4);
	}	
	
	@Test
	public void onRessourceFoundGifTest() {
		String filename = folder + "test.gif";
		File file = new File(filename);
		ImageThumbnailPlugin imageThumbnailPlugin = new ImageThumbnailPlugin();
		ResourceInfo res = new ResourceInfo(filename, "default"); 
		List<TempFile> arts = imageThumbnailPlugin.onResourceFound(file, res);
		
		assertTrue(arts.size() == 4);
	}		
}
