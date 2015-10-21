package db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class DB<T> {

	protected Map<String, T> database;
	protected int size;
	
	public DB() {
		this.database = new HashMap<>();
		this.size = 0;
	}
	
	public void addObject (String id, T o) {
		this.database.put(id, o);
		this.size = this.database.size();
	}
	
	public void addObjects (Map<String, T> map) {
		for (Entry<String, T> entry : map.entrySet())
			this.addObject(entry.getKey(), entry.getValue());
	}
	
	public void addDB (DB<T> db) {
		for (String s : db.getObjectNames())
		{
			T o = null;
			try {
				o = db.getObject(s);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			this.addObject(s, o);
		}
	}
	
	public boolean removeObject (String name) {
		if (this.contains(name)) {
			this.database.remove(name);
			this.size--;
			return true;
		}
		
		return false;
	}
	
	public boolean contains (String id) {
		return this.database.containsKey(id);
	}
	
	public T getObject (String id) throws NullPointerException {
		T obj = this.database.get(id);
		
		if (obj == null)
			throw new NullPointerException(id + " not seems to be loaded");
		return obj;
	}
	
	public Set<String> getObjectNames () {
		return this.database.keySet(); 
	}
	
	public List<T> getObjects () {
		List<T> objects = new ArrayList<>();
		for (T obj : this.database.values())
			objects.add(obj);
		
		return objects;
	}
	
	public int size () {
		return this.size;
	}

	public abstract DB<T> createNew();
	
}
