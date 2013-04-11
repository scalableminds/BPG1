package projectZoom.thumbnails.text;

import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.xml.sax.SAXException;


public interface TextReader {
	
	public String getContent() throws IOException, SAXException, TikaException;

}
