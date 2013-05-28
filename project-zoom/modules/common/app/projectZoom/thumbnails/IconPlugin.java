package projectZoom.thumbnails;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import models.ResourceInfo;


public class IconPlugin {
	
	static String ICON_FOLDER = "public/icons/";
	static String ALT_ICON_FOLDER = "modules/common/public/icons/";
	static String DEFAULT_ICON = "unknown";

	
	public IconPlugin() {

	}
	
	public InputStream onResourceFound(File resource, ResourceInfo ressourceInfo) {
		
		System.out.println("Icon onResourceFound called ");
		String fn = ressourceInfo.name();
		String ext = fn.substring(fn.lastIndexOf(".") + 1);

		String url = ICON_FOLDER + ext + ".png";
		System.out.println(ext);
	
		try {
			return new FileInputStream(url);
		} catch (FileNotFoundException e) {}

		String altUrl = ICON_FOLDER + ext + ".png";
		try {
			return new FileInputStream(altUrl);
		} catch (FileNotFoundException e) {}

		String urlDefault = ICON_FOLDER + DEFAULT_ICON + ".png";
		try {
			return new FileInputStream(urlDefault);
		} catch (FileNotFoundException e) {}
		
		String altUrlDefault = ALT_ICON_FOLDER + DEFAULT_ICON + ".png";
		try {
			return new FileInputStream(altUrlDefault);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return new ByteArrayInputStream(new byte[0]);
		}
	}
}
