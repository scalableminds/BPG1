package projectZoom.thumbnails.text;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class PdfReader implements TextReader {

	/**
	 * @param args
	 */
	public PdfReader() {
	
	}
	
	public String getContent() throws IOException, SAXException, TikaException {
		
		Parser parser = new AutoDetectParser();
		String fileName = "/home/user/test.docx";
		
		BodyContentHandler handler = new BodyContentHandler(10000000);
        Metadata metadata = new Metadata();
        InputStream is;
        
        String output = ""; 

        try {
            is = new FileInputStream(fileName);

            parser.parse(is, handler, metadata, new ParseContext());
            is.close();             
            output = handler.toString();            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return output;
	}

}
