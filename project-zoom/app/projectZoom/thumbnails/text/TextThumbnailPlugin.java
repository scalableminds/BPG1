package projectZoom.thumbnails.text;

import java.io.IOException;
import java.util.*;

import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;

import projectZoom.thumbnails.text.PdfReader;

public class TextThumbnailPlugin {
	
	private List<TextReader> readers;

	public TextThumbnailPlugin() {
		
		readers = new ArrayList<TextReader>();
		
		readers.add(new PdfReader());

	}
	
	public void onResourceFound() {
		System.out.print("onResourceFound called ");
		
		Iterator<TextReader> iterator = readers.iterator();
		while (iterator.hasNext()) {
			TextReader reader = iterator.next();
			try {
				reader.getContent();
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
		
		
		
	}
	
	

}
