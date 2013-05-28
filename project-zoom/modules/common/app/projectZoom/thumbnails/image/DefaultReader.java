package projectZoom.thumbnails.image;

import projectZoom.thumbnails.*;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class DefaultReader extends ImageReader{

	String[] MIME_TYPES = {
			"image/jpeg",
			"image/tiff",
			"image/gif",
			"image/png",
			"image/x-ms-bmp"
	};
	
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
	
	public List<Artifact> getImages(String filename, int[] widths)
	{
		List<Artifact> output = new ArrayList<Artifact>();
		File file = new File(filename);
		BufferedImage in;
		try {
			in = ImageIO.read(file);
			BufferedImage inputImage = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = inputImage.createGraphics();
			g.drawImage(in, 0, 0, null);
			g.dispose();	
			for (int width: widths)
			{
				Artifact a = new Artifact(width + ".png");
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
