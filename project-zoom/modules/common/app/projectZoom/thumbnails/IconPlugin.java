package projectZoom.thumbnails;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import models.ResourceInfo;


public class IconPlugin {
	
	static String ICON_FOLDER = "./public/icons/";
	static String defaultIcon = "unknown";

	
	public IconPlugin() {

	}
	
	public InputStream onResourceFound(ResourceInfo ressourceInfo) {
		
		System.out.print("onResourceFound called ");
		System.out.print(System.getProperty("user.dir"));
		String fn = ressourceInfo.fileName();
		String ext = fn.substring(fn.lastIndexOf(".") + 1);

		String url = ICON_FOLDER + ext + ".png";
		System.out.print(ext);
	
		if (url!=null) {
			try {
				InputStream is = new FileInputStream(url);
				return is;
			} catch (FileNotFoundException e) {
				//e.printStackTrace();
			}
		}
		
		String urlDefault = ICON_FOLDER + defaultIcon + ".png";
		InputStream is = null;
		try {
			is = new FileInputStream(urlDefault.toString());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return is;
	}
}
