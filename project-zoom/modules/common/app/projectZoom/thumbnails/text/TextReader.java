package projectZoom.thumbnails.text;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFImageWriter;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;


import projectZoom.thumbnails.TempFile;
import projectZoom.thumbnails.GifFrame;
import projectZoom.thumbnails.ImageUtil;
import projectZoom.thumbnails.text.MyWordle.Word;
import cue.lang.Counter;
import cue.lang.WordIterator;
import cue.lang.stop.StopWords;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;
import javax.swing.*;



public class TextReader {
	
	String[] MIME_TYPES = {};
	
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
	
	
	public List<TempFile> getThumbnails(File file, int[] widths) {
		List<TempFile> output = new ArrayList<TempFile>();
		try {
			List<BufferedImage> images = this.pdfToImages2(file, 1);
			for (int width: widths) {				
				TempFile art = new TempFile(width + ".png");
				BufferedImage image = ImageUtil.resizeBufferedImage(images.get(0), width);
				if (image != null)
				{
					ImageIO.write(image, "png", art.getFile());
					output.add(art);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
	}
	
	
	public List<TempFile> getGifs(File file, int[] widths, int pagecount) {
		List<TempFile> output = new ArrayList<TempFile>();
		try {
			List<BufferedImage> images = this.pdfToImages2(file, pagecount);
			for(int width: widths) {
				List<BufferedImage> resizedImages = new ArrayList<BufferedImage>();
				for (BufferedImage b: images)
					resizedImages.add(ImageUtil.resizeBufferedImage(b, width));
				TempFile art = ImageUtil.imagesToGif(resizedImages, width);
				if (art != null)
					output.add(art);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;		
	}
		
	
	private String getText(File file) {
		
		Parser parser = new AutoDetectParser();
		
		BodyContentHandler handler = new BodyContentHandler(10000000);
        Metadata metadata = new Metadata();
        InputStream is = null;
        
        String output = ""; 
        
        
        try {
        	is = new FileInputStream(file);
            parser.parse (is, handler, metadata, new ParseContext());           
            output = handler.toString();            
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
		} catch (TikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace(); 
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
        } finally {
	        if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
        }
   
        return output;
	}
	
	private List<Entry<String, Integer>> getWordList(String text)
	{
		return this.getWordList(text, Integer.MAX_VALUE);
	}
	
	private List<Entry<String, Integer>> getWordList(String text, Integer maxCount)
	{
		// count words
		final Counter<String> words = new Counter<String>();
		for (final String word : new WordIterator(text)) {
		    words.note(word);
		}

		List<Entry<String, Integer>> list = words.getAllByFrequency();

		Iterator<Entry<String, Integer>> iterator = list.iterator();
		int i = 0;
		int m = Math.min(list.size(), maxCount);
		
		List<Entry<String, Integer>> outputList = new ArrayList<Entry<String, Integer>>();
		
		while(i < m) 
		{
			if (iterator.hasNext() == false)
				break;
			
			Entry<String, Integer> currentWord = iterator.next();
			
			if (StopWords.English.isStopWord(currentWord.getKey())) 
				continue;
			
			outputList.add(currentWord);
			
			i++;
		}
	
		return outputList;
	}
	
	
	public List<TempFile> getTagClouds(File file, int[] widths) {
		
		List<TempFile> output = new ArrayList<TempFile>();

		String text = "";
		try {
			text = this.getText(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<Entry<String, Integer>> words = this.getWordList(text);
		Random rand = new Random();
		
		for (int i = 0; i < widths.length; i++)
		{
			int width = widths[i];
	
			// set up wordly
			MyWordle wordle = new MyWordle();
			wordle.setAllowRotate(true);
			wordle.setSortType(1);
			wordle.setUseArea(false);
			
			float maxWords = Math.min((width *  width) / 1024, words.size());			
			Entry<String, Integer> firstWord = words.get(0);
			Entry<String, Integer> lastWord = words.get((int)Math.floor(maxWords)-1);
			int lowestCount = lastWord.getValue();
			int highestCount = firstWord.getValue();
			
			//wordle.setSmallestSize(200);
			//wordle.setBiggestSize((int)((lowestCount / highestCount) * 100.0 + 100));
			
			Iterator<Entry<String, Integer>> iterator = words.iterator();
		
			// fill wordly
		
			int j = 0;

			
			while(j < maxWords) 
			{
				if (iterator.hasNext() == false)
					break;
				
				Entry<String, Integer> word = iterator.next();
				float delta = (float) (((float)word.getValue() / (float)highestCount) * 100.0);
				int size = (int)delta + 20 - lowestCount;
				Word w = new Word(word.getKey(), size);
				Color c = new Color(rand.nextInt(100),rand.nextInt(100),rand.nextInt(100));
				w.setFill(c);
				//c = new Color(rand.nextInt(100),rand.nextInt(100),rand.nextInt(100));
				w.setStroke(c);
				//w.setTitle(""+i);
				w.setLineHeight(0);
				//w.setFontFamily(rand.nextBoolean()?"Helvetica":"Courier");
				w.setFontFamily("Verdana");
				wordle.add(w);	
				j++;
			}
	
			
			TempFile art = new TempFile(width + ".png");

			wordle.doLayout();
			try {
				wordle.getAsPNG(art.getFile(), width);
				output.add(art);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return output;
	}
	
	
	public List<BufferedImage> pdfToImages(String url) throws IOException
	{
		PDDocument document;

		document = PDDocument.load(url);

		int imageType = BufferedImage.TYPE_INT_RGB;
		String pwd = "";
		String imageFormat = "png";
		String prefix = "/home/user/test22";
		int pageCount = 3;
		int resolution = 160;
		PDFImageWriter imageWriter = new PDFImageWriter();
		boolean success = imageWriter.writeImage(document, imageFormat, pwd,
                1, pageCount, prefix, imageType, resolution);
		
		List<BufferedImage> images = new ArrayList<BufferedImage>();
		for (int i = 1; i <= pageCount; i++)
		{
			File img = new File(prefix + i + ".png");
			BufferedImage in = ImageIO.read(img);
			BufferedImage newImage = new BufferedImage(in.getWidth(), in.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = newImage.createGraphics();
			g.drawImage(in, 0, 0, null);
			g.dispose();			
			images.add(in);
		}
		
		return images;
		
	}
	
	public List<BufferedImage> pdfToImages2(File file, int pagecount) throws IOException
	{
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        FileChannel channel = raf.getChannel();
        ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        PDFFile pdffile = new PDFFile(buf);

		List<BufferedImage> images = new ArrayList<BufferedImage>();
		int maxpages = Math.min(pagecount, pdffile.getNumPages()); 
		for (int i = 1; i <= maxpages; i++)
		{
	        // draw the first page to an image
	        PDFPage page = pdffile.getPage(i);
	        // get the width and height for the doc at the default zoom
	        Rectangle rect = new Rectangle(0, 0, (int) page.getBBox().getWidth(), (int) page.getBBox().getHeight());
	        // generate the image
	        Image img = page.getImage(rect.width, rect.height, // width & height
	                rect, // clip rect
	                null, // null for the ImageObserver
	                true, // fill background with white
	                true // block until drawing is done
	                );
	        // save it as a file
	        BufferedImage bImg = toBufferedImage(img);
	        images.add(bImg);
		}
		
		return images;
		
	}	
	
	
	   // This method returns a buffered image with the contents of an image
    public static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        // This code ensures that all the pixels in the image are loaded
        image = new ImageIcon(image).getImage();
        // Determine if the image has transparent pixels; for this method's
        // implementation, see e661 Determining If an Image Has Transparent
        // Pixels
        boolean hasAlpha = hasAlpha(image);
        // Create a buffered image with a format that's compatible with the
        // screen
        BufferedImage bimage = null;
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha) {
                transparency = Transparency.BITMASK;
            }
            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
        } catch (HeadlessException e) {
            // The system does not have a screen
        }
        if (bimage == null) {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha) {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
        // Copy image to buffered image
        Graphics g = bimage.createGraphics();
        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bimage;
    }	

    public static boolean hasAlpha(Image image) {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage) {
            BufferedImage bimage = (BufferedImage)image;
            return bimage.getColorModel().hasAlpha();
        }
    
        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
         PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
        }
    
        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return cm.hasAlpha();
    }
	
}
