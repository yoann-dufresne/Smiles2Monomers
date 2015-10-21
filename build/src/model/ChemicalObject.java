package model;

import model.graph.MonomerGraph;

import org.openscience.cdk.interfaces.IMolecule;


public interface ChemicalObject {

	public String getSMILES ();
	
	public IMolecule getMolecule();
	
	public int getSize();

	public String getName();
	
	public MonomerGraph getGraph();

	public String getId();
	
}
