package projectZoom.thumbnails.video;

import java.util.ArrayList;
import java.util.List;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.xuggle.mediatool.IMediaReader;
import com.xuggle.mediatool.MediaListenerAdapter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.mediatool.event.IVideoPictureEvent;
import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IContainer;


import projectZoom.thumbnails.TempFile;

public class XuggleReader extends VideoReader {
	
	String[] MIME_TYPES = {"video/x-ms-wmv"};

    public int count = 3;
    private static int PRETIME = 1;

    // The video stream index, used to ensure we display frames from one and
    // only one video stream from the media container.
    private static int mVideoStreamIndex = -1;
    
    // Time of last frame write
    private static long mLastPtsWrite = Global.NO_PTS;
    
    public static long MICRO_SECONDS_BETWEEN_FRAMES = 10000000;

	
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
	public List<BufferedImage> getFrames(String filename, int count)
	{
		this.count = count;
		long duration = (long) (getVideoDuration(filename) * 0.000001);
		//System.out.println(duration);
		XuggleReader.MICRO_SECONDS_BETWEEN_FRAMES = (long) (Global.DEFAULT_PTS_PER_SECOND * (duration - 2 * PRETIME)) / (count - 1);
		//System.out.println(XuggleReader.MICRO_SECONDS_BETWEEN_FRAMES);
		IMediaReader mediaReader = ToolFactory.makeReader(filename);
		
		// stipulate that we want BufferedImages created in BGR 24bit color space
		mediaReader.setBufferedImageTypeToGenerate(BufferedImage.TYPE_3BYTE_BGR);
		
		ImageSnapListener il = new ImageSnapListener();
		mediaReader.addListener(il);
		
		// read out the contents of the media file and
		// dispatch events to the attached listener
		while (mediaReader.readPacket() == null) ;
		return il.output;
	}
	
    public long getVideoDuration(String videoFile) {
        // Create a Xuggler container object
        IContainer container = IContainer.make();

        // Open up the container
        if (container.open(videoFile, IContainer.Type.READ, null) < 0) {
                throw new IllegalArgumentException("Could not open file: " + videoFile);
        }

        return container.getDuration();
    }
    
    private static class ImageSnapListener extends MediaListenerAdapter {

    	ArrayList<BufferedImage> output = new ArrayList<BufferedImage>();
    	
        public void onVideoPicture(IVideoPictureEvent event) {

            if (event.getStreamIndex() != mVideoStreamIndex) {
                // if the selected video stream id is not yet set, go ahead an
                // select this lucky video stream
                if (mVideoStreamIndex == -1)
                    mVideoStreamIndex = event.getStreamIndex();
                // no need to show frames from this video stream
                else
                    return;
            }

            // if uninitialized, back date mLastPtsWrite to get the very first frame
            if (mLastPtsWrite == Global.NO_PTS)
                mLastPtsWrite = event.getTimeStamp() - MICRO_SECONDS_BETWEEN_FRAMES + PRETIME;

            // if it's time to write the next frame
            if (event.getTimeStamp() - mLastPtsWrite >= 
                    MICRO_SECONDS_BETWEEN_FRAMES) {
                                
            	output.add(event.getImage());

                // indicate file written
                double seconds = ((double) event.getTimeStamp()) / 
                    Global.DEFAULT_PTS_PER_SECOND;

                // update last write time
                mLastPtsWrite += MICRO_SECONDS_BETWEEN_FRAMES;
            }

        }
    }
}
