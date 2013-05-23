package projectZoom.thumbnails.text;

public class PdfReader extends TextReader {

	String[] MIME_TYPES = {"application/pdf"};
	
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
}
