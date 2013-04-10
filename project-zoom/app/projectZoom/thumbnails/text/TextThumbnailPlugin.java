package projectZoom.thumbnails.text;

import java.io.IOException;
import java.util.*;
import org.eclipse.zest.cloudio.ICloudLabelProvider;
import org.eclipse.zest.cloudio.TagCloud;
import org.eclipse.zest.cloudio.TagCloudViewer;
import org.eclipse.zest.cloudio.Word;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import projectZoom.thumbnails.text.PdfReader;

public class TextThumbnailPlugin {
	
	private List<TextReader> readers;

	public TextThumbnailPlugin() {
		
		readers = new ArrayList<TextReader>();
		
		readers.add(new PdfReader());

	}
	
	public String onResourceFound() {
		System.out.print("onResourceFound called ");
		String output = ""; 
		Iterator<TextReader> iterator = readers.iterator();
		while (iterator.hasNext()) {
			TextReader reader = iterator.next();
			try {
				output = reader.getContent();
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
		//createTagCloud();
		return output;
		
	}

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


}
