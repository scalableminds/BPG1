package projectZoom.thumbnails.text;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import projectZoom.thumbnails.text.TextThumbnailPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;


public class pdfTextExtraction {

	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void test() {
		String fileName = "/home/user/test.docx";
		File file = new File(fileName);
		TextThumbnailPlugin textThumbnailPlugin = new TextThumbnailPlugin();
		String output = textThumbnailPlugin.onResourceFound(file);
		System.out.print(output);
		assertTrue(true);
	}

}
