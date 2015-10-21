package io.loaders.csv;

import io.loaders.AbstractLoader;
import io.parsers.CSVParser;

import java.util.List;
import java.util.Map;

import db.DB;

public abstract class AbstractCsvLoader<B extends DB<T>, T> extends AbstractLoader<B, T> {

	@Override
	protected void LoadFromString(B db, String inner) {
		List<Map<String, String>> objs = CSVParser.parse(inner);
		for (Map<String, String> obj : objs) {
			T tObj = this.objectFromFormat(obj);
			db.addObject(this.getObjectId(tObj), tObj);
		}
	}

	@Override
	public T objectFromFormat(Object obj) {
		@SuppressWarnings("unchecked")
		Map<String, String> tObj = (Map<String, String>) obj;
		return this.objectFromCSV(tObj);
	}
	
	protected StringBuffer toFormat (T obj) {
		return this.toCsv(obj);
	}


	protected String getFooter() {return "";}

	protected abstract T objectFromCSV(Map<String, String> obj);
	protected abstract String getObjectId(T tObj);
	protected abstract StringBuffer toCsv(T obj);
	
}
