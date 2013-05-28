
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import projectZoom.thumbnails.*;

import models.ResourceInfo;

public class IconTests {

	String folder = "/home/user/testfiles/";
	String tempFolder = "/home/user/";
	
	@Before
	public void setUp() throws Exception {
	}
	
		
	@Test
	public void onRessourceFoundDocxTest() {
		String filename = folder + "test.docx";
		IconPlugin iconPlugin = new IconPlugin();
		ResourceInfo res = new ResourceInfo(filename, "default"); 
		InputStream is = iconPlugin.onResourceFound(res);
		try {	
			FileOutputStream outputStream = new FileOutputStream(new File(tempFolder + "32.png"));
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = is.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(is!=null);	
	}

	@Test
	public void onRessourceFoundUndefinedTest() {
		String filename = folder + "test.undefined";
		IconPlugin iconPlugin = new IconPlugin();
		ResourceInfo res = new ResourceInfo(filename, "default"); 
		InputStream is = iconPlugin.onResourceFound(res);
		try {	
			FileOutputStream outputStream = new FileOutputStream(new File(tempFolder + "32.png"));
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = is.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue(is!=null);	
	}
}
