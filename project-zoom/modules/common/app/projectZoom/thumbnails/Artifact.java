package projectZoom.thumbnails;

import java.io.File;
import java.util.UUID;

public class Artifact {
	
	static String TEMP_FOLDER = "/home/user/";
	
	private File file;
	private String name;
	
	public File getFile() {
		return this.file;
	}
	
	public String getName() {
		return this.name;
	}

	public Artifact(String name){
		this.name = name;
		String suffix = "foo";
		if (name.length() > 3)
			suffix = name.substring(name.length() - 3);
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		this.file = new File(TEMP_FOLDER + uuid + "." + suffix);
	}
}
