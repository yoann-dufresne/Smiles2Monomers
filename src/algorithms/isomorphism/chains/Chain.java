package algorithms.isomorphism.chains;

import io.parsers.MySMILES;
import io.parsers.SmilesConverter;

import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.Bond;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.tools.SaturationChecker;


public class Chain implements Comparable<Chain> {
	
	private Chain subBlc;
	private Extension ext;
	private int position1;
	private int position2;
	private int absolutePosition1;
	private int absolutePosition2;
	
	private int blocSize;
	private String serial;
	private List<IAtom> atoms;
	
	private IMolecule mol;
	private String smiles;
	private String mysmiles;
	
	private static MySMILES smilesGenerator = new MySMILES();
	private static SaturationChecker sc = new SaturationChecker();
	
	/**
	 * Construct a bloc with a sub-bloc, an extension and positions of atoms extension in the sub-bloc.
	 * If an atom of the extension is not present in the sub-bloc, position need to be -1
	 * @param subBloc Bloc of this bloc size - 1 or null for bloc of size 1.
	 * @param ext Extension of the sub-bloc to create this bloc.
	 * @param position1 Position for the first Atom of extension in the sub-bloc
	 * @param position2 Position for the second Atom of extension in the sub-bloc
	 */
	public Chain(Chain subBloc, Extension ext, int position1, int position2) {
		this.subBlc = subBloc;
		this.ext = ext;
		
		int maxSubIdx = -1;
		if (this.subBlc != null)
			maxSubIdx = this.subBlc.getMaximalAbsolutePosition();
		
		IAtom a0 = ext.getBond().getAtom(0);
		IAtom a1 = ext.getBond().getAtom(1);
		if (a0.getAtomicNumber() == a1.getAtomicNumber()
				&& a0.getFlag(CDKConstants.ISAROMATIC) == a1.getFlag(CDKConstants.ISAROMATIC)
				&& a0.getImplicitHydrogenCount() == a1.getImplicitHydrogenCount()) {
			if (position1 > position2) {
				int tmp = position1;
				position1 = position2;
				position2 = tmp;
			}
			
			this.position1 = position1;
			if (position1 == -1)
				this.absolutePosition1 = maxSubIdx + 1;
			else
				this.absolutePosition1 = position1;

			this.position2 = position2;
			if (position2 == -1) {
				maxSubIdx = Math.max(maxSubIdx, this.absolutePosition1);
				this.absolutePosition2 = maxSubIdx + 1;
			} else
				this.absolutePosition2 = position2;
		} else {
			this.position1 = position1;
			if (position1 == -1)
				this.absolutePosition1 = maxSubIdx + 1;
			else
				this.absolutePosition1 = position1;

			this.position2 = position2;
			if (position2 == -1) {
				maxSubIdx = Math.max(maxSubIdx, this.absolutePosition1);
				this.absolutePosition2 = maxSubIdx + 1;
			} else
				this.absolutePosition2 = position2;
		}
		
		this.calculateSerial();
		
		this.blocSize = 0;
		this.blocSize = this.getSize();
	}/**/
	
	public Chain (Extension ext) {
		this.subBlc = null;
		this.ext = ext;
		this.position1 = -1;
		this.absolutePosition1 = 0;
		this.position2 = -1;
		this.absolutePosition2 = 1;
		
		this.calculateSerial();
		
		this.blocSize = 0;
		this.blocSize = this.getSize();
	}
	
	public Chain (String serial) {
		String extS = serial;
		int maxSubIdx = -1;
		if (serial.contains(";")) {
			int n = extS.lastIndexOf(";");
			this.subBlc = new Chain(extS.substring(0, n));
			extS = extS.substring(n+1);
			maxSubIdx = Math.max(this.subBlc.absolutePosition1, this.subBlc.absolutePosition2);
		}
		
		this.ext = new Extension(extS);
		String[] split = extS.split(",");
		this.position1 = new Integer(split[split.length-2]);
		this.absolutePosition1 = this.position1 == -1 ? maxSubIdx++ + 1 : this.position1;
		this.position2 = new Integer(split[split.length-1]);
		this.absolutePosition2 = this.position2 == -1 ? maxSubIdx + 1 : this.position2;
	}

	public int getSize() {
		if (this.blocSize != 0)
			return this.blocSize;
		
		if (this.subBlc == null)
			this.blocSize = 1;
		else
			this.blocSize = this.subBlc.getSize() + 1;
		
		return this.blocSize;
	}
	
	/**
	 * Give the atom position in the chain atom list. -1 if a is not present.
	 * @param a Atom to search
	 * @return The position of a
	 */
	public int getAtomPosition (IAtom a) {
		if (!this.atoms.contains(a))
			return -1;
		
		return (this.atoms.indexOf(a));
	}
	
	private void calculateSerial () {
		if (this.subBlc != null)
			this.serial = this.subBlc.getSerial() + ";";
		else
			this.serial = "";
		
		this.serial += this.ext.getSerial() + "," + this.position1 + "," + this.position2;
	}
	
	public String getSerial() {
		if (this.serial == null)
			this.calculateSerial();
		return serial;
	}
	
	public void setSerial(String serial) {
		this.serial = serial;
	}
	
	/**
	 * construct recursively a molecule corresponding to the bloc.
	 * Attention, this function is not very efficient because of multiple molecules creation.
	 * @return Bloc molecule. Can return null if there are problems of compatibility between sub-bloc and extension positions.
	 */
	public IMolecule getMolecule () {
		if (this.mol != null)
			return this.mol;
		
		IMolecule mol = null;
		this.atoms = new ArrayList<>();
		
		// Sub-bloc molecule
		if (this.getSize() > 1) {
			IMolecule subMol = this.subBlc.getMolecule();
			
			mol = new Molecule();
			for (IAtom a : subMol.atoms())
				mol.addAtom(a);
			for (IBond b : subMol.bonds())
				mol.addBond(b);
			
			for (IAtom a : this.subBlc.atoms) {
				IAtom cloneA = mol.getAtom(subMol.getAtomNumber(a));
				this.atoms.add(cloneA);
			}
		} else {
			mol = new Molecule();
		}
		
		// Extension of mol
		IAtom a1 = this.ext.getBond().getAtom(0);
		if (this.position1 == -1) {
			try {
				a1 = (IAtom) a1.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			mol.addAtom(a1);
			this.atoms.add(a1);
			// Hydrogens
			int hydrogens = this.ext.getBond().getAtom(0).getImplicitHydrogenCount();
			a1.setImplicitHydrogenCount(hydrogens);
			// Aromatic
			if (this.ext.getBond().getAtom(0).getFlag(CDKConstants.ISAROMATIC))
				a1.setFlag(CDKConstants.ISAROMATIC, true);
			else
				a1.setFlag(CDKConstants.ISAROMATIC, false);
		} else {
			if (a1.getAtomicNumber() == this.atoms.get(this.position1).getAtomicNumber())
				a1 = this.atoms.get(this.position1);
			else return null;
		}
		
		IAtom a2 = this.ext.getBond().getAtom(1);
		if (this.position2 == -1) {
			try {
				a2 = (IAtom) a2.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			mol.addAtom(a2);
			this.atoms.add(a2);
			// Hydrogen
			int hydrogens = this.ext.getBond().getAtom(1).getImplicitHydrogenCount();
			a2.setImplicitHydrogenCount(hydrogens);
			// Aromatic
			if (this.ext.getBond().getAtom(1).getFlag(CDKConstants.ISAROMATIC))
				a2.setFlag(CDKConstants.ISAROMATIC, true);
			else
				a2.setFlag(CDKConstants.ISAROMATIC, false);
		} else
			if (a2.getAtomicNumber() == this.atoms.get(this.position2).getAtomicNumber())
				a2 = this.atoms.get(this.position2);
			else return null;
		
		mol.addBond(new Bond(a1, a2, this.ext.getBond().getOrder()));
		
		this.mol = mol;
		
		return mol;
	}
	
	public String getSmiles() {
		if (this.smiles == null)
			this.smiles = SmilesConverter.conv.mol2Smiles(this.getMolecule(), true);
			
		return this.smiles;
	}
	
	public String getMySmiles() {
		if (this.mysmiles == null)
			this.mysmiles = Chain.smilesGenerator.convert(this.getMolecule(), true);
			
		return this.mysmiles;
	}
	
	public Chain getSubBlc() {
		return subBlc;
	}
	
	public Extension getExt() {
		return ext;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Chain))
			return false;
		
		Chain b = (Chain)obj;
		return b.getSerial().equals(this.getSerial());
	}
	
	@Override
	public int hashCode() {
		return this.getSerial().hashCode();
	}

	@Override
	public int compareTo(Chain b) {
		return this.getSize() - b.getSize();
	}
	
	public int getPosition1() {
		return position1;
	}
	
	public int getAbsolutePosition1() {
		return absolutePosition1;
	}
	
	public int getPosition2() {
		return position2;
	}
	
	public int getAbsolutePosition2() {
		return absolutePosition2;
	}
	
	private int getMaximalAbsolutePosition() {
		if (this.subBlc == null)
			return Math.max(this.absolutePosition1, this.absolutePosition2);
		
		int max = this.subBlc.getMaximalAbsolutePosition();
		max = Math.max(max, this.absolutePosition1);
		max = Math.max(max, this.absolutePosition2);
		return max;
	}
	
	@Override
	public String toString() {
		return this.getSerial();
	}

	public void setExtension(Extension ext) {
		this.ext = ext;
	}
	
	public void invertPositions() {
		int tmp = this.position1;
		this.position1 = this.position2;
		this.position2 = tmp;
		
		tmp = this.absolutePosition1;
		this.absolutePosition1 = this.absolutePosition2;
		this.absolutePosition2 = tmp;
	}

	public int getPrevArity() {
		IAtom a = null;
		if (this.atoms == null)
			this.getMolecule();
		
		if (this.position1 == -1 && this.position2 == -1)
			return 1;
		else if (this.position1 != -1) {
			a = this.atoms.get(this.position1);
		} else {
			a = this.atoms.get(this.position2);
		}
		
		IMolecule mol = this.getMolecule();
		int valency = 1;
		try {
			valency = sc.calculateNumberOfImplicitHydrogens(a);
		} catch (CDKException e) {
			e.printStackTrace();
		}
		int hydrogens = a.getImplicitHydrogenCount();
		int connected = mol.getConnectedAtomsCount(a);
		int arity = valency - connected - hydrogens + 1;
		return arity < 1 ? 1 : arity;
	}
	
}
