package model;

import io.parsers.SmilesConverter;
import model.graph.MonomerGraph;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;

public abstract class AbstractChemicalObject implements ChemicalObject {
	
	protected String smiles;
	protected int size;
	protected IMolecule mol;
	
	protected Boolean generateH;
	protected Boolean generateCoordinate;
	protected Boolean explicitHydrogens;

	@Override
	public String getSMILES() {
		return this.smiles;
	}

	@Override
	public IMolecule getMolecule() {
		return this.getMol();
	}
	
	private IMolecule getMol() {
		if (this.mol == null) {
			try {
				boolean generateCoordinates = this.generateCoordinate == null ? false : this.generateCoordinate;
				boolean explicite = this.explicitHydrogens == null ? false : this.explicitHydrogens;
				if (this.generateH != null && this.generateH.equals(new Boolean(true)))
					this.mol = SmilesConverter.conv.transform(this.smiles, false, generateCoordinates, explicite);
				else
					this.mol = SmilesConverter.conv.transform(this.smiles, true, generateCoordinates, explicite);
			} catch (InvalidSmilesException e) {
				System.err.println("Impossible to parse \"" + this.smiles + "\"");
				Molecule mol = new Molecule();
				return mol;
			}
		}
		
		return this.mol;
	}
	
	
	public int getSize() {
		if (this.size == 0) {
			for (IAtom a : this.getMolecule().atoms())
				this.size += a.getImplicitHydrogenCount() + 1;
		}
		
		return this.size;
	}

	@Override
	public abstract String getName();

	@Override
	public abstract MonomerGraph getGraph();
	
	@Override
	public String toString() {
		return this.smiles;
	}
	
	public void setExplicitHydrogens(boolean explicitHydrogens) {
		this.explicitHydrogens = explicitHydrogens;
	}
	
	public void setGenerateCoordinate(Boolean generateCoordinate) {
		this.generateCoordinate = generateCoordinate;
	}

}
