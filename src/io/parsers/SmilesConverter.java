package io.parsers;

import java.util.List;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import zz.cdk.modif.SmilesGenerator;

public class SmilesConverter {
	
	private SmilesParser sp;
	private SmilesGenerator sg;
	//private HanserRingFinder hrf;
	
	public static final SmilesConverter conv = new SmilesConverter();
	
	private SmilesConverter() {
		IChemObjectBuilder basic = DefaultChemObjectBuilder.getInstance();
		this.sp = new SmilesParser(basic);
		this.sp.setPreservingAromaticity(true);
		this.sg = new SmilesGenerator();
		this.sg.setUseAromaticityFlag(true);
		//this.hrf = new HanserRingFinder();
	}

	public IMolecule transform (String smiles) throws InvalidSmilesException {
		return this.transform(smiles, true, false, false);
	}
	
	public Molecule transform (String smiles, boolean addHydrogens, boolean calculateCoordinate, boolean expliciteHydrogens) throws InvalidSmilesException {
		Molecule imol = null;
		try {
			imol = new Molecule(sp.parseSmiles(smiles));
			
			// Add hydrogens
			if (addHydrogens)
			{
				CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(imol.getBuilder());
				try {
					adder.addImplicitHydrogens(imol);
					
					if (expliciteHydrogens)
						AtomContainerManipulator.convertImplicitToExplicitHydrogens(imol);
				} catch (CDKException e) {
					e.printStackTrace();
				}
			} else {
				for (IAtom a : imol.atoms())
					a.setImplicitHydrogenCount(0);

				if(!expliciteHydrogens)
					imol = new Molecule(AtomContainerManipulator.removeHydrogens(imol));
			}
			
			if (calculateCoordinate) {
				StructureDiagramGenerator sdg = new StructureDiagramGenerator(imol);
				try {
					sdg.generateCoordinates();
				} catch (CDKException e) {
					System.err.println(smiles);
					e.printStackTrace();
				}
				imol = new Molecule(sdg.getMolecule());
			}
		} catch (InvalidSmilesException e) {
			throw e;
		}
		return imol;
	}
	
	public String mol2Smiles (IMolecule mol, boolean hydrogens) {
		if (hydrogens) {
			try {
				mol = mol.clone();
				AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);
			} catch (CloneNotSupportedException e1) {
				e1.printStackTrace();
			}
		}
		return this.mol2Smiles(mol);
	}
	
	public String mol2Smiles (IMolecule mol) {
		return this.sg.createSMILES(mol);
	}

	public String toCanonicalSmiles(String smiles) throws InvalidSmilesException {
		IMolecule mol = this.transform(smiles, false, false, false);
		return this.mol2Smiles(mol);
	}

	public List<IAtom> getOrder() {
		return this.sg.order;
	}

}