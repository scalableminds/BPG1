
import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import projectZoom.thumbnails.text.TextThumbnailPlugin;

import projectZoom.thumbnails.text.TextReader;
import projectZoom.thumbnails.text.Artifact;

import models.Resource;
import models.ResourceInfo;

public class pdfTextExtraction {

	@Before
	public void setUp() throws Exception {
		
	}
	/*
	@Test
	public void cloudifyTest() {
		String fileName = "/home/user/t2.pdf";
		File file = new File(fileName);
		TextThumbnailPlugin textThumbnailPlugin = new TextThumbnailPlugin();
		String output = textThumbnailPlugin.onResourceFound(file);
		System.out.print(output);
		assertTrue(true);
	}
	
	@Test
	public void imageFromPdf() {
		String fileNameIn = "/home/user/test.docx";
		String fileNameOut = "/home/user/test22.pdf";
		File fileIn = new File(fileNameIn);
		File fileOut = new File(fileNameOut);
		TextReader reader = new PdfReader();
		reader.docToPdf(fileIn, fileOut, 5);
		assertTrue(true);
	}	

	@Test
	public void pdfToImage() {
		String fileNameIn = "/home/user/t2.pdf";
		File fileIn = new File(fileNameIn);
		//File fileOut = new File(fileNameOut);
		TextReader reader = new PdfReader();
		List<BufferedImage> images;
		try {
			images = reader.pdfToImages2(fileNameIn);
			reader.imagesToGif(images);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue(true);
	}	
	
	@Test
	public void onRessourceFoundPdfTest() {
		String filename = "/home/user/test2.pdf";
		File file = new File(filename);
		TextThumbnailPlugin textThumbnailPlugin = new TextThumbnailPlugin();
		ResourceInfo res = new ResourceInfo("/home/user/t2.pdf", "default"); 
		List<Artifact> arts = textThumbnailPlugin.onResourceFound(res);
		assertTrue(true);	
	}
	*/
	@Test
	public void onRessourceFoundDocxTest() {
		String filename = "/home/user/test.docx";
		TextThumbnailPlugin textThumbnailPlugin = new TextThumbnailPlugin();
		ResourceInfo res = new ResourceInfo(filename, "default"); 
		List<Artifact> arts = textThumbnailPlugin.onResourceFound(res);
		
		assertTrue(true);	
	}	
}
