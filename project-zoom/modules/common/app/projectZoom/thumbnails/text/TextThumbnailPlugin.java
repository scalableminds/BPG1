package projectZoom.thumbnails.text;


import models.ResourceInfo;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.*;
import java.util.Map.Entry;
import java.util.List;

import org.apache.tika.Tika;

import projectZoom.thumbnails.Artifact;
import projectZoom.thumbnails.text.MyWordle.Word;
import projectZoom.thumbnails.text.PdfReader;
import projectZoom.thumbnails.text.OfficeReader;
import projectZoom.thumbnails.text.MyWordle;

import cue.lang.*;
import cue.lang.stop.StopWords;


public class TextThumbnailPlugin {
	
	private List<TextReader> readers;
	static int[] CLOUD_WIDTHS = {64, 128};
	static int[] THUMBNAIL_WIDTHS = {256, 512};
	static int[] GIF_WIDTHS = {32, 64, 128, 256, 512};
	static int GIF_PAGECOUNT = 3;
	static String TEMP_FOLDER = "/home/user/";

	
	public TextThumbnailPlugin() {
		
		readers = new ArrayList<TextReader>();
		readers.add(new PdfReader());
		readers.add(new OfficeReader());

	}
	
	public List<Artifact> onResourceFound(ResourceInfo ressourceInfo) {
		
		System.out.print("onResourceFound called ");

		List<Artifact> output = new ArrayList<Artifact>(); 
		
		if (!ressourceInfo.typ().equals("default"))
			return output;
		
		String filename = ressourceInfo.fileName();
		File file = new File(filename); 
		
		String mimetype = getMimeType(file);
		System.out.print(mimetype);
		
		Iterator<TextReader> iterator = readers.iterator();
		while (iterator.hasNext()) {
			TextReader reader = iterator.next();

			if (!reader.isSupported(mimetype))
				continue;

			output.addAll(reader.getTagClouds(file, CLOUD_WIDTHS));
			output.addAll(reader.getThumbnails(file, THUMBNAIL_WIDTHS));
			output.addAll(reader.getGifs(file, THUMBNAIL_WIDTHS, GIF_PAGECOUNT));
	
		}

		return output;
		
	}

	
	public void createTagCloud2(String text) {
		
		// count words
		final Counter<String> words = new Counter<String>();
		for (final String word : new WordIterator(text)) {
		    words.note(word);
		}
		System.out.println("Life: " + words.getCount("life"));
		List<Entry<String, Integer>> list = words.getAllByFrequency();
		//Entry<String, Integer>[] array = (Entry<String, Integer>[]) list.toArray();
		
		// set up wordly
		MyWordle app = new MyWordle();
		app.setAllowRotate(true);
		
		// fill wordly
		
		Random rand = new Random();
		
		Iterator<Entry<String, Integer>> iterator = list.iterator();
		int i = 0;
		while(i < 8) // for(int i = 0; i < j; ++i)
		{
			Entry<String, Integer> word = iterator.next();
			if (word.equals(null))
				break;
			
			if (StopWords.English.isStopWord(word.getKey())) 
				continue;
			
			Word w = new Word(word.getKey(), word.getValue());
			Color c = new Color(rand.nextInt(100),rand.nextInt(100),rand.nextInt(100));
			w.setFill(c);
			//c = new Color(rand.nextInt(100),rand.nextInt(100),rand.nextInt(100));
			w.setStroke(c);
			//w.setTitle(""+i);
			w.setLineHeight(0);
			//w.setFontFamily(rand.nextBoolean()?"Helvetica":"Courier");
			w.setFontFamily("Courier");
			app.add(w);	
			i++;
		}




		File fileOut=new File("/home/user/output.png");
		app.doLayout();
		try {
			app.saveAsPNG(fileOut);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

}

	public String getMimeType(File file) {
		/*
		String mimeType = "";
		FileInputStream is = null;
		
		try {
		      is = new FileInputStream(file);
	
		      ContentHandler contenthandler = new BodyContentHandler();
		      Metadata metadata = new Metadata();
		      metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());
		      Parser parser = new AutoDetectParser();
		      parser.parse(is, contenthandler, metadata, null);
		      mimeType = metadata.get(Metadata.CONTENT_TYPE);
	    }
	    catch (Exception e) {
	      e.printStackTrace();
	    }
	    finally {
	        if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	    }
		
		return mimeType;
		*/

        String mimeType = null;
 
        try {
 
            Tika tika = null;
 
            // Creating new Instance of org.apache.tika.Tika
            tika = new Tika();
 
            // Detecting MIME Type of the File 
            mimeType = tika.detect(file);
 
        } catch (FileNotFoundException e) {
        	e.printStackTrace();
        } catch (IOException e) {
        	e.printStackTrace();
        }
 
        // returning detected MIME Type
        return mimeType;

	}
}
