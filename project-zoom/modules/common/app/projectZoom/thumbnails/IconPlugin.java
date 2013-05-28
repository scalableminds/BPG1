package projectZoom.thumbnails;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import models.ResourceInfo;


public class IconPlugin {
	
	static String ICON_FOLDER = "public/icons/";
	static String defaultIcon = "unknown";

	
	public IconPlugin() {

	}
	
	public InputStream onResourceFound(File resource, ResourceInfo ressourceInfo) {
		
		System.out.print("onResourceFound called ");
		System.out.print(System.getProperty("user.dir"));
		String fn = ressourceInfo.name();
		String ext = fn.substring(fn.lastIndexOf(".") + 1);

		String url = ICON_FOLDER + ext + ".png";
		System.out.print(ext);
	
		if (url!=null) {
			try {
				return new FileInputStream(url);
			} catch (FileNotFoundException e) {
				//e.printStackTrace();
			}
		}
		
		String urlDefault = ICON_FOLDER + defaultIcon + ".png";

		try {
			return new FileInputStream(urlDefault.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return new ByteArrayInputStream(new byte[0]);
		}
	}
}
