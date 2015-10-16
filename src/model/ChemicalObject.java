package model;

import org.openscience.cdk.Molecule;

import model.graph.MonomerGraph;


public interface ChemicalObject {

	public String getSmiles ();
	
	public Molecule getMolecule();
	
	public int getSize();

	public String getName();
	
	public MonomerGraph getGraph();

	public String getId();
	
}
