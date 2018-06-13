package io.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipManager {
	
	private static final int BUFFER = 2048;
	private byte data[] = new byte[BUFFER];
	
	private File zip;
	private BufferedOutputStream buff;
	private ZipOutputStream out;

	public ZipManager(String path) {
		this.zip = new File(path);
		this.zip.delete();
		try {
			this.zip.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		FileOutputStream dest = null;
		try {
			dest= new FileOutputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		this.buff = new BufferedOutputStream(dest);
		this.out = new ZipOutputStream(buff);
	}
	
	public void addFile (String path, String name) {
		//System.out.println(name);
		
		// Open the file
		File file = new File(path);
		FileInputStream fi = null;
		try {
			fi = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		BufferedInputStream buffi = new BufferedInputStream(fi, BUFFER);
		
		// Create zip entry
		ZipEntry entry= new ZipEntry(name);
		try {
			this.out.putNextEntry(entry);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Write the entry
		int count;
	    try {
			while((count = buffi.read(data, 0, BUFFER)) != -1) {
			    out.write(data, 0, count);
			}
			this.out.closeEntry();
			buffi.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void addDirectory (String path, String prefixName) {
		File f= new File(path);
		this.addDirectory(path, f.getName(), prefixName);
	}
	
	public void writeZip () {
		try {
			this.out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addDirectory(String path, String newName, String prefixName) {
		File directory = new File(path);
		if (!directory.isDirectory()) return;
		
		for (File file : directory.listFiles()) {
			String prefix = prefixName + "/" + newName;
			//System.out.println(prefix);
			
			if (file.isDirectory())
				this.addDirectory(file.getPath(), prefix);
			else
				this.addFile(file.getPath(), prefix + "/" + file.getName());
		}
	}
	
}
