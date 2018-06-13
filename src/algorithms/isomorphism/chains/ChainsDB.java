package algorithms.isomorphism.chains;

import db.DB;

public class ChainsDB extends DB<FamilyChainsDB> {
	
	public ChainsDB() {
		super();
	}

	@Override
	public DB<FamilyChainsDB> createNew() {
		return new ChainsDB();
	}

}
