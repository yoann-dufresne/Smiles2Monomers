package model;

import io.parsers.SmilesConverter;
import model.graph.MonomerGraph;

import org.openscience.cdk.exception.InvalidSmilesException;



public class Polymer extends AbstractChemicalObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2520532051216140680L;
	private static boolean computeCoordinates;
	// Peptides informations
	private int id_peptide;
	private String name;
	private Monomer[] monomers;
	private MonomerGraph graph;

	/**
	 * 
	 * @param id_peptide NORINE id
	 * @param name Peptide name
	 * @param type 
	 * @param structure Linear, cyclic, double cyclic, ...
	 * @param graph Monomers graph
	 * @param cid 
	 * @param smiles Complete peptide in SMILES format
	 * @param monomers List of the monomers in the peptide
	 */
	public Polymer(int id_peptide, String name, String smiles, Monomer[] monomers) {
		this.id_peptide = id_peptide;
		this.name = name;
		this.monomers = monomers;
		try {
			this.smiles = SmilesConverter.conv.toCanonicalSmiles(smiles);
		} catch (InvalidSmilesException e) {
			e.printStackTrace();
		}
		
		if (Polymer.computeCoordinates)
			this.generateCoordinate = true;
	}
	
	@Override
	public int hashCode() {
		return this.id_peptide;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Polymer))
			return false;
		
		Polymer pep = (Polymer)obj;
		return this.id_peptide == pep.id_peptide;
	}
	
	public void setId_peptide(int id_peptide) {
		this.id_peptide = id_peptide;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public void setGraph(MonomerGraph graph) {
		this.graph = graph;
	}

	public Monomer[] getMonomeres() {
		return monomers;
	}
	public void setMonomeres(Monomer[] monomeres) {
		this.monomers = monomeres;
	}

	@Override
	public String toString() {
		return "Peptide ["
				+ "id_peptide=" + id_peptide
				+ ", name=" + name
				+ ", smiles=" + smiles
				+ ", monomeres=" + this.getMonomersList ()
		+ "]";
	}

	private String getMonomersList() {
		String list = "";
		
		for (int i=0 ; i<this.monomers.length ; i++)
			if (i == 0)
				list += this.monomers[i].getCode();
			else
				list += "," + this.monomers[i].getCode();
		
		return list;
	}

	@Override
	public MonomerGraph getGraph() {
		return this.graph;
	}

	@Override
	public String getId() {
		return "" + this.id_peptide;
	}
	
	public void setSmiles (String smiles) {
		this.smiles = smiles;
	}

	public static void setComputeCoordinates(boolean compute) {
		Polymer.computeCoordinates = compute;
	}

}
