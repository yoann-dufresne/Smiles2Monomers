package io.loaders;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import db.DB;

public abstract class AbstractLoader<B extends DB<T>, T> implements Loader<B, T> {
	
	
	public B loadFile (String filename) {
		// BDD Creation
		B db = this.createDB();
		
		StringBuffer txt = new StringBuffer();
		File f = new File(filename);
		try {
			FileReader fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				txt.append(line);
				txt.append('\n');
			}
			fr.close();
		} catch (IOException e) {
			System.err.println("Impossible to load " + f.getPath());
			System.exit(1);
		}
		
		this.LoadFromString (db, txt.toString());
		
		return db;
	}
	
	protected abstract B createDB();
	protected abstract void LoadFromString (B db, String inner);
	public abstract T objectFromFormat (Object obj);
	
	public void saveFile (B db, String filename) {
		File f = new File(filename);
		try {
			FileWriter fw = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(this.getHeader() + "\n");
			for (T obj : db.getObjects())
				bw.write(this.toFormat(obj) + "\n");
			bw.write(this.getFooter() + "\n");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	protected abstract StringBuffer toFormat (T obj);
	protected abstract String getHeader();
	protected abstract String getFooter();

}
