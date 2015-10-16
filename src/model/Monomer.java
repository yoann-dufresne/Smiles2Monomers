package model;

import model.graph.MonomerGraph;


public class Monomer extends AbstractChemicalObject implements Comparable<Monomer> {
	
	private static final long serialVersionUID = -4470440995114512902L;
	private static boolean computeCoordinates;
	// Monomer informations from NORINE
	private String code;
	private String name_aa;
	
	/**
	 * Monomer contructor with NORINE informations.
	 * @param code Monomer mnemonic
	 * @param name_aa Complete name
	 * @param smiles SMILES representation
	 */
	public Monomer(String code, String name_aa, String smiles) {
		this.code = code;
		this.name_aa = name_aa;
		this.smiles = smiles;
		
		if (Monomer.computeCoordinates)
			this.generateCoordinate = true;
	}
	
	/**
	 * To clone a monomer instance
	 * @param monomer Original monomer
	 */
	public Monomer(Monomer monomer) {
		this.code = monomer.code;
		this.name_aa = monomer.name_aa;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Monomer) {
			Monomer objM = (Monomer)obj;
			return objM.getCode().equals(this.code);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return this.code.hashCode();
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}

	public String getName_aa() {
		return name_aa;
	}
	public void setName_aa(String name_aa) {
		this.name_aa = name_aa;
	}

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}
	
	@Override
	public String toString() {
		return "Monomer [code=" + code + ", name_aa=" + name_aa + ", smiles=" + smiles + "]";
	}

	@Override
	public int compareTo(Monomer m) {
		return this.code.compareTo(m.code);
	}

	@Override
	public String getName() {
		return this.code;
	}

	@Override
	public MonomerGraph getGraph() {
		Monomer[] nodes = {this}; 
		MonomerGraph g = new MonomerGraph(nodes);
		return g;
	}

	@Override
	public String getId() {
		return this.code;
	}
	
	public static void setComputeCoordinates(boolean computeCoordinates) {
		Monomer.computeCoordinates = computeCoordinates;
	}
}
