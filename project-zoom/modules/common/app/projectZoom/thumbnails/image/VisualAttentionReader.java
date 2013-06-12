package projectZoom.thumbnails.image;

import projectZoom.thumbnails.*;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class VisualAttentionReader extends ImageReader{

	StentifordModel st;
	String[] MIME_TYPES = {
			"image/jpeg",
			"image/tiff",
			"image/gif",
			"image/png",
			"image/x-ms-bmp"
	};
	
	public VisualAttentionReader() {
		
		this.st = new StentifordModel();

	}
	
	
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
	
	public List<TempFile> getImages(File file, String filename, int[] widths)
	{
		List<TempFile> output = new ArrayList<TempFile>();
		BufferedImage in;
		try {
			in = ImageIO.read(file);
			BufferedImage inputImage = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = inputImage.createGraphics();
			st.extract(inputImage);
			g.drawImage(in, 0, 0, null);
			g.dispose();	
			for (int width: widths)
			{
				TempFile a = new TempFile(width + ".png");
				BufferedImage outputImage = ImageUtil.resizeBufferedImage(inputImage, width);
				ImageIO.write(outputImage, "png", a.getFile());
				output.add(a);
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}
	

		return output;
	}
	
}
