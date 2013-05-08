package projectZoom.thumbnails.text;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
//import org.eclipse.zest.cloudio.ICloudLabelProvider;
//import org.eclipse.zest.cloudio.TagCloud;
//import org.eclipse.zest.cloudio.Word;

import java.util.Map.Entry;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import projectZoom.thumbnails.text.MyWordle.Word;
import projectZoom.thumbnails.text.PdfReader;
import projectZoom.thumbnails.text.MyWordle;

import cue.lang.*;
import cue.lang.stop.StopWords;


public class TextThumbnailPlugin {
	
	private List<TextReader> readers;
	int[] widths = {64, 128};

	public TextThumbnailPlugin() {
		
		readers = new ArrayList<TextReader>();
		
		readers.add(new PdfReader());

	}
	
	public String onResourceFound(File file) {
		System.out.print("onResourceFound called ");
		String output = ""; 
		Iterator<TextReader> iterator = readers.iterator();
		while (iterator.hasNext()) {
			TextReader reader = iterator.next();
			try {
				reader.getTagClouds(file, this.widths);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TikaException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		}
		//createTagCloud2(output);
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
	/*
	public void createTagCloud() {
		final Display display = new Display();
		final Shell shell = new Shell(display);
		TagCloud cloud = new TagCloud(shell, SWT.NONE);
		// Generate some dummy words - color, weight and fontdata must
		// always be defined.
		List<Word> words = new ArrayList<Word>();
		Word w = new Word("Hello");
		w.setColor(display.getSystemColor(SWT.COLOR_DARK_CYAN));
		w.weight = 1;
		w.setFontData(cloud.getFont().getFontData().clone());
		words.add(w);
		w = new Word("Cloudio");
		w.setColor(display.getSystemColor(SWT.COLOR_DARK_GREEN));
		w.setFontData(cloud.getFont().getFontData().clone());
		w.weight = 0.5;
		w.angle = -45;
		words.add(w);
		shell.setBounds(50,50, 300, 300);
		cloud.setBounds(0,0, shell.getBounds().width, shell.getBounds().height);
		   // Assign the list of words to the cloud:
		   //cloud.setWords(words, null);
		shell.open();
		while (!shell.isDisposed()) {
		    if (!display.readAndDispatch()) display.sleep();
		}
		display.dispose();		
	}
	*/


}
