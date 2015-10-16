package db;

import model.ChemicalObject;

public abstract class ChemicalObjectDB<T extends ChemicalObject> extends DB<T> {

	@Override
	public void addObject(String id, T o) {
		super.addObject(id, o);
		// To pre-load molecules
		o.getMolecule();
	}
	
}
