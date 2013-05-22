package projectZoom.thumbnails.text;

import java.io.File;
import java.util.List;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

public class OfficeReader extends TextReader {
	String[] MIME_TYPES = {
			"application/msword",
			"application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
	
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
	public List<Artifact> getThumbnails(File file, int[] widths) {
		Artifact art = new Artifact("temp.pdf");
		this.docToPdf(file, art.getFile());
		List<Artifact> output = super.getThumbnails(art.getFile(), widths);
		return output;
	}
	
	@Override
	public List<Artifact> getGifs(File file, int[] widths, int pagecount) {
		Artifact art = new Artifact("temp.pdf");
		this.docToPdf(file, art.getFile());
		List<Artifact> output = super.getGifs(art.getFile(), widths, pagecount);
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
