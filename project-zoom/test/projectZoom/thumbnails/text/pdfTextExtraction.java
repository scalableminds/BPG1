package projectZoom.thumbnails.text;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.junit.Before;
import org.junit.Test;
import projectZoom.thumbnails.text.TextThumbnailPlugin;

import projectZoom.thumbnails.text.TextReader;


public class pdfTextExtraction {

	@Before
	public void setUp() throws Exception {
		
	}
/*
	@Test
	public void cloudifyTest() {
		String fileName = "/home/user/test.docx";
		File file = new File(fileName);
		TextThumbnailPlugin textThumbnailPlugin = new TextThumbnailPlugin();
		String output = textThumbnailPlugin.onResourceFound(file);
		System.out.print(output);
		assertTrue(true);
	}
*/
	@Test
	public void imageFromPdf() {
		String fileNameIn = "/home/user/test.docx";
		String fileNameOut = "/home/user/test22.pdf";
		File fileIn = new File(fileNameIn);
		File fileOut = new File(fileNameOut);
		TextReader reader = new PdfReader();
		reader.convertToPdf(fileIn, fileOut, 5);
		assertTrue(true);
	}	

}
