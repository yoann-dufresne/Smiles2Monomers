package model;

import org.openscience.cdk.interfaces.IMolecule;

import model.graph.MonomerGraph;

public class OtherChemicalObject extends AbstractChemicalObject {

	public OtherChemicalObject(IMolecule mol) {
		this.mol = mol;
	}
	
	@Override
	public String getId() {
		return "other";
	}

	@Override
	public String getName() {
		return "other";
	}

	@Override
	public MonomerGraph getGraph() {
		return null;
	}

}
