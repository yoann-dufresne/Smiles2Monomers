package io.loaders.json;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import db.DB;
import io.loaders.AbstractLoader;

public abstract class AbstractJsonLoader<B extends DB<T>, T> extends AbstractLoader<B, T> {
	
	public B loadFile (String filename) {
		File f = new File(filename);
		if (!f.exists()) {
			System.err.println("Impossible to load " + f.getPath());
			System.exit(1);
		}
		
		// BDD Creation
		B db = this.createDB();
		this.LoadFromString (db, filename);
		return db;
	}
	
	@Override
	protected void LoadFromString(B db, String filename) {
		JSONArray array = null;
		try {
			array = (JSONArray) JSONValue.parse(new FileReader(new File(filename)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (Object obj : array) {
			JSONObject jso = (JSONObject) obj;
			T tObj = this.objectFromFormat(jso);
			if (tObj != null)
				db.addObject(this.getObjectId(tObj), tObj);
		}
	}

	@Override
	public T objectFromFormat(Object obj) {
		return this.objectFromJson((JSONObject)obj);
	}

	protected abstract T objectFromJson(JSONObject obj);
	protected abstract String getObjectId(T tObj);
	
	@SuppressWarnings("unchecked")
	@Override
	public void saveFile (B db, String filename) {
		JSONArray array = new JSONArray();
		for (T obj : db.getObjects())
			array.addAll(this.getArrayOfElements(obj));
		
		File f = new File(filename);
		try {
			FileWriter fw = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(fw);
			
			String json = array.toJSONString();
			bw.write(json);
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void saveFile(T[] objs, String filename) {
		JSONArray array = new JSONArray();
		for (T obj : objs)
			array.addAll(this.getArrayOfElements(obj));
		
		File f = new File(filename);
		try {
			FileWriter fw = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(fw);
			
			String json = array.toJSONString();
			bw.write(json);
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected abstract JSONArray getArrayOfElements(T obj);

	protected String getHeader() {return "";}
	protected StringBuffer toFormat (T obj) {return new StringBuffer();}
	protected String getFooter() {return "";};

}
