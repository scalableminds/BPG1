package projectZoom.thumbnails.text;

import java.io.File;
import java.util.List;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import projectZoom.thumbnails.TempFile;

public class OfficeReader extends TextReader {
	String[] MIME_TYPES = {
			"application/msword",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
			"application/vnd.ms-excel",
			"application/vnd.oasis.opendocument.text",
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
			"application/vnd.oasis.opendocument.spreadsheet",
			"application/vnd.ms-powerpoint",
			"application/vnd.oasis.opendocument.presentation",
			"application/vnd.openxmlformats-officedocument.presentationml.presentation",
			"application/rtf",
			"text/plain",
			"text/css",
			"text/html",
			"application/javascript"
	};
	
	@Override
	public Boolean isSupported(String mimetype)
	{
		for (int i = 0; i < this.MIME_TYPES.length; i++)
		{
			String supportedMimeType = this.MIME_TYPES[i];
			if (mimetype.equals(supportedMimeType))
				return true;
		}
		return false;
	}	
	
	@Override
	public List<TempFile> getThumbnails(File file, int[] widths) {
		TempFile art = new TempFile("temp.pdf");
		this.docToPdf(file, art.getFile());
		List<TempFile> output = super.getThumbnails(art.getFile(), widths);
		return output;
	}
	
	@Override
	public List<TempFile> getGifs(File file, int[] widths, int pagecount) {
		TempFile art = new TempFile("temp.pdf");
		this.docToPdf(file, art.getFile());
		List<TempFile> output = super.getGifs(art.getFile(), widths, pagecount);
		return output;
	}	

	private void docToPdf(File fileIn, File fileOut)
	{
		OfficeManager officeManager = new DefaultOfficeManagerConfiguration().buildOfficeManager();
	    officeManager.start();

	    OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
	    
	    try {
	        converter.convert(fileIn, fileOut);
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    officeManager.stop();
	}
}
