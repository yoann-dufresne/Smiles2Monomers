package db;

import model.Polymer;


public class PeptidesDB extends ChemicalObjectDB<Polymer> {
	
	public PeptidesDB() {
		super();
	}

	@Override
	public DB<Polymer> createNew() {
		return new PeptidesDB();
	}

}
