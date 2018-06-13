package db;

import model.Polymer;


public class PolymersDB extends ChemicalObjectDB<Polymer> {
	
	public PolymersDB() {
		super();
	}

	@Override
	public DB<Polymer> createNew() {
		return new PolymersDB();
	}

}
