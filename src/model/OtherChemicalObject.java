package model;

import org.openscience.cdk.Molecule;

import model.graph.MonomerGraph;

public class OtherChemicalObject extends AbstractChemicalObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7211025386320703248L;

	public OtherChemicalObject(Molecule mol) {
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
