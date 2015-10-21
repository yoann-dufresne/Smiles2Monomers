package db;

import model.Monomer;


public class MonomersDB extends ChemicalObjectDB<Monomer> {
	
	public MonomersDB() {
		super();
	}

	@Override
	public DB<Monomer> createNew() {
		return new MonomersDB();
	}

}
