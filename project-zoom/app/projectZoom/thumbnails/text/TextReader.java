package projectZoom.thumbnails.text;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;


import projectZoom.thumbnails.text.MyWordle.Word;
import cue.lang.Counter;
import cue.lang.WordIterator;
import cue.lang.stop.StopWords;


public class TextReader {
	
	private String getText(File file) throws IOException, SAXException, TikaException {
		
		Parser parser = new AutoDetectParser();
		
		BodyContentHandler handler = new BodyContentHandler(10000000);
        Metadata metadata = new Metadata();
        InputStream is;
        
        String output = ""; 
        is = new FileInputStream(file);
        
        try {
            parser.parse (is, handler, metadata, new ParseContext());           
            output = handler.toString();            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            is.close();  
        }
   
        return output;
	}
	
	private List<Entry<String, Integer>> getWordList(String text)
	{
		return this.getWordList(text, Integer.MAX_VALUE);
	}
	
	private List<Entry<String, Integer>> getWordList(String text, Integer maxCount)
	{
		// count words
		final Counter<String> words = new Counter<String>();
		for (final String word : new WordIterator(text)) {
		    words.note(word);
		}

		List<Entry<String, Integer>> list = words.getAllByFrequency();

		Iterator<Entry<String, Integer>> iterator = list.iterator();
		int i = 0;
		int m = Math.min(list.size(), maxCount);
		
		List<Entry<String, Integer>> outputList = new ArrayList<Entry<String, Integer>>();
		
		while(i < m) 
		{
			if (iterator.hasNext() == false)
				break;
			
			Entry<String, Integer> currentWord = iterator.next();
			
			if (StopWords.English.isStopWord(currentWord.getKey())) 
				continue;
			
			outputList.add(currentWord);
			
			i++;
		}
	
		return outputList;
	}
	
	
	public void getTagClouds(File file, int[] widths) throws IOException, SAXException, TikaException {
		
		String text = this.getText(file);
		List<Entry<String, Integer>> words = this.getWordList(text);
		Random rand = new Random();
		
		for (int i = 0; i < widths.length; i++)
		{
			int width = widths[i];
	
			// set up wordly
			MyWordle wordle = new MyWordle();
			wordle.setAllowRotate(true);
			wordle.setSortType(1);
			wordle.setUseArea(false);
			
			float maxWords = Math.min((width *  width) / 1024, words.size());			
			Entry<String, Integer> firstWord = words.get(0);
			Entry<String, Integer> lastWord = words.get((int)Math.floor(maxWords)-1);
			int lowestCount = lastWord.getValue();
			int highestCount = firstWord.getValue();
			
			//wordle.setSmallestSize(200);
			//wordle.setBiggestSize((int)((lowestCount / highestCount) * 100.0 + 100));
			
			Iterator<Entry<String, Integer>> iterator = words.iterator();
		
			// fill wordly
		
			int j = 0;

			
			while(j < maxWords) 
			{
				if (iterator.hasNext() == false)
					break;
				
				Entry<String, Integer> word = iterator.next();
				float delta = (float) (((float)word.getValue() / (float)highestCount) * 100.0);
				int size = (int)delta + 20 - lowestCount;
				Word w = new Word(word.getKey(), size);
				Color c = new Color(rand.nextInt(100),rand.nextInt(100),rand.nextInt(100));
				w.setFill(c);
				//c = new Color(rand.nextInt(100),rand.nextInt(100),rand.nextInt(100));
				w.setStroke(c);
				//w.setTitle(""+i);
				w.setLineHeight(0);
				//w.setFontFamily(rand.nextBoolean()?"Helvetica":"Courier");
				w.setFontFamily("Verdana");
				wordle.add(w);	
				j++;
			}
	

			File fileOut=new File("/home/user/output" + Integer.toString(width) + ".png");
			wordle.doLayout();
			try {
				wordle.getAsPNG(fileOut, width);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void convertToPdf(File fileIn, File fileOut, int count)
	{
		OfficeManager officeManager = new DefaultOfficeManagerConfiguration().buildOfficeManager();
	    officeManager.start();

	    OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
	    converter.convert(fileIn, fileOut);
	        
	    officeManager.stop();
		
	}

}
