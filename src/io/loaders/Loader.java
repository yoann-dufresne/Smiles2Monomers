package io.loaders;

import db.DB;


public interface Loader<B extends DB<T>, T> {

	public B loadFile (String filename);
	public void saveFile (B db, String filename);
	
}
