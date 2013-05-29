package projectZoom.thumbnails;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import models.DefaultResourceTypes;

public class TempFile {
		
	private File file;
	private String name;
	private String type;
	
	public File getFile() {
		return this.file;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public TempFile(String name) {
		new TempFile(name, DefaultResourceTypes.DEFAULT_TYP());
	}
	
	public TempFile(String name, String type) {
		this.name = name;
		this.type = type;
		String suffix = "foo";
		String prefix = "tmp";
		
		if (name.length() > 3) {
			suffix = name.substring(name.lastIndexOf(".") + 1);
			prefix = name.substring(0, name.lastIndexOf(".") + 1);
		}
			
		this.file = null;
		try {
			this.file = File.createTempFile(prefix, suffix);
			this.file.deleteOnExit();
			System.out.println("Temp file : " + this.file.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("couldn't generate artifact-file in temp folder");
			e.printStackTrace();
		}
	}
	
	public InputStream getStream() {
		FileInputStream is = null;
		try {
			is = new FileInputStream(this.file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return is;
	}
	
	public void copyToTempByFileName(String fileName) {
		InputStream inputStream = null;
		OutputStream outputStream = null;
	 
		try {
			// read this file into InputStream
			inputStream = new FileInputStream(fileName);
	 
			// write the inputStream to a FileOutputStream
			outputStream = new FileOutputStream(this.file);
	 
			int read = 0;
			byte[] bytes = new byte[1024];
	 
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
	 
			System.out.println("Done!");
	 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (outputStream != null) {
				try {
					// outputStream.flush();
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	 
			}
		}
	}
}
