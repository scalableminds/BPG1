package projectZoom.thumbnails;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import models.ResourceInfo;
import models.DefaultResourceTypes;


public class IconPlugin {
	
	static String ICON_FOLDER = "public/icons/";
	static String ALT_ICON_FOLDER = "modules/common/public/icons/";
	static String DEFAULT_ICON = "unknown";
	static int RESOLUTION = 32;
	static String SUFFIX = ".png";

	
	public IconPlugin() {

	}
	
	public List<TempFile> onResourceFound(File resource, ResourceInfo ressourceInfo) {
		
		System.out.println("Icon onResourceFound called ");
		String fn = ressourceInfo.name();
		String ext = fn.substring(fn.lastIndexOf(".") + 1);

		String url = ICON_FOLDER + ext + ".png";
		System.out.println(ext);
		
		List<TempFile> output = new ArrayList<TempFile>();
		
		File f = new File(url);
		if(f.exists()) { 
			TempFile tempFile = new TempFile(
					String.valueOf(RESOLUTION) + SUFFIX, 
					DefaultResourceTypes.PRIMARY_THUMBNAIL());
			tempFile.copyToTempByFileName(url);
			output.add(tempFile);
			return output;
		 }

		String altUrl = ICON_FOLDER + ext + ".png";
		f = new File(altUrl);
		if(f.exists()) {
			TempFile tempFile = new TempFile(
					String.valueOf(RESOLUTION) + SUFFIX, 
					DefaultResourceTypes.PRIMARY_THUMBNAIL());
			tempFile.copyToTempByFileName(altUrl);
			output.add(tempFile);
			return output;
		}

		String urlDefault = ICON_FOLDER + DEFAULT_ICON + ".png";
		f = new File(urlDefault);
		if(f.exists()) {
			TempFile tempFile = new TempFile(
					String.valueOf(RESOLUTION) + SUFFIX, 
					DefaultResourceTypes.PRIMARY_THUMBNAIL());
			tempFile.copyToTempByFileName(urlDefault);
			output.add(tempFile);
			return output;
		}
		
		String altUrlDefault = ALT_ICON_FOLDER + DEFAULT_ICON + ".png";
		f = new File(altUrlDefault);
		if(f.exists()) {
			TempFile tempFile = new TempFile(
					String.valueOf(RESOLUTION) + SUFFIX, 
					DefaultResourceTypes.PRIMARY_THUMBNAIL());
			tempFile.copyToTempByFileName(altUrlDefault);
			output.add(tempFile);
			return output;
		} 
		
		return output;
	}
	
}
