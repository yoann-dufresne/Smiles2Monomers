package db;

import algorithms.utils.Coverage;

public class CoveragesDB extends DB<Coverage> {

	public CoveragesDB() {
		super();
	}
	
	@Override
	public DB<Coverage> createNew() {
		return new CoveragesDB();
	}
	
	@Override
	public void addObject(String id, Coverage o) {
		if (!this.database.containsKey(id))	
			super.addObject(id, o);
		else {
			Coverage prev = this.database.get(id);
			prev.addMatches(o);
		}
	}

}
