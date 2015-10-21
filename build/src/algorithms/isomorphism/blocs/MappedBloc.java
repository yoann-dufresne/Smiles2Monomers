package algorithms.isomorphism.blocs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.ChemicalObject;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;

import algorithms.isomorphism.MatchingType;

public class MappedBloc implements Comparable<MappedBloc> {

	private ChemicalObject chem;
	private Bloc bloc;
	private List<Integer> atomsMapping;
	private List<Integer> bondsMapping;
	private Map<Integer, Integer> hydrogensMapping;
	private List<MatchingType> matchings;

	public MappedBloc(ChemicalObject chem, Bloc bloc, List<Integer> mappedAtoms, List<Integer> mappedBonds, List<MatchingType> matchings, Map<Integer, Integer> hydrogens) {
		this.chem = chem;
		this.bloc = bloc;
		this.atomsMapping = mappedAtoms;
		this.bondsMapping = mappedBonds;
		this.hydrogensMapping = hydrogens;
		this.matchings = matchings;
	}
	
	public MappedBloc (ChemicalObject chem) {
		this.chem = chem;
		this.atomsMapping = new ArrayList<>();
		this.bondsMapping = new ArrayList<>();
		this.hydrogensMapping = new HashMap<>();
		this.matchings = new ArrayList<>();
		this.bloc = null;
	}
	
	/**
	 * Get position of an atom idx in the mapping
	 * @param molIdx
	 * @return
	 */
	public int getMappingIdx (int molIdx) {
		if (!this.atomsMapping.contains(molIdx))
			return -1;
		
		return this.atomsMapping.indexOf(molIdx);
	}
	
	public Bloc getBloc() {
		return bloc;
	}
	
	@Override
	public String toString() {
		if (this.bloc == null)
			return "";
		
		String atoms = "" + atomsMapping.get(0);
		for (int i=1 ; i<atomsMapping.size() ; i++)
			atoms += "," + atomsMapping.get(i);

		String bonds = "" + bondsMapping.get(0);
		for (int i=1 ; i<bondsMapping.size() ; i++)
			bonds += "," + bondsMapping.get(i);
		
		String types = "" + this.matchings.get(0);
		for (int i=1 ; i<matchings.size() ; i++)
			types += "," + matchings.get(i);
		
		return bloc.getSerial() + ";" + chem.getId() + ";" + atoms + ";" + bonds + ";" + types;
	}
	
	public ChemicalObject getChemObject() {
		return chem;
	}
	
	public List<Integer> getAtomsMapping() {
		return atomsMapping;
	}
	
	public List<Integer> getBondsMapping() {
		return bondsMapping;
	}
	
	public Map<Integer, Integer> getHydrogensMapping() {
		return hydrogensMapping;
	}
	
	public List<MatchingType> getMatchings() {
		return matchings;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MappedBloc))
			return false;
		
		MappedBloc mb = (MappedBloc)obj;
		
		return this.chem.equals(mb.getChemObject())
				&& this.bloc.equals(mb.getBloc())
				&& this.atomsMapping.equals(mb.atomsMapping)
				&& this.bondsMapping.equals(mb.bondsMapping);
	}
	
	/**
	 * Get neighbors bonds idxs
	 * @param idxs Blocs bonds idxs
	 * @param res Search in res
	 * @return neighbors bonds
	 */
	public List<Integer> getNeighborsBonds(IMolecule mol) {
		List<Integer> idxs = null;
		
		if (this.bondsMapping.size() == 0) {
			idxs = new ArrayList<>();
			for (IBond b : mol.bonds())
				idxs.add(mol.getBondNumber(b));
			return idxs;
		}
		
		idxs = this.getBondsMapping();
		ArrayList<Integer> neighbors = new ArrayList<>();
		
		for (Integer idx : idxs) {
			IBond bond = mol.getBond(idx);
			
			List<IBond> nBonds = new ArrayList<>(); 
			nBonds.addAll(mol.getConnectedBondsList(bond.getAtom(0)));
			nBonds.addAll(mol.getConnectedBondsList(bond.getAtom(1)));
			
			for (IBond nb : nBonds) {
				int nIdx = mol.getBondNumber(nb);
				if (!idxs.contains(nIdx) && !neighbors.contains(nIdx))
					neighbors.add(nIdx);
			}
		}
		
		return neighbors;
	}

	@Override
	public int compareTo(MappedBloc mb) {
		return this.getBloc().getSize() - mb.getBloc().getSize();
	}
	
	public Transformation getTransformationTo (MappedBloc to) {
		return new Transformation(this, to);
	}
	
	
	
	public class Transformation {
		
		public int[] atoms;
		public int[] bonds;

		public Transformation(MappedBloc from, MappedBloc to) {
			this.atoms = new int[from.atomsMapping.size()];
			this.bonds = new int[from.bondsMapping.size()];
			
			for (int i=0 ; i<this.atoms.length ; i++) {
				int idx = to.atomsMapping.get(i);
				this.atoms[i] = from.atomsMapping.indexOf(idx);
			}
			
			for (int i=0 ; i<this.bonds.length ; i++) {
				int idx = to.bondsMapping.get(i);
				this.bonds[i] = from.bondsMapping.indexOf(idx);
			}
		}
		
	}



	/**
	 * To add real hydrogens from the chemical object to the bloc mapping
	 * @return new BlocMapping with hydrogens.
	 */
	public void addAllHydrogens() {
		// Create the list of extensions
		Bloc b = this.bloc;
		while (b != null) {
			b.setSerial(null);
			Extension ext = b.getExt().clone();
			b.setExtension (ext);
			IAtom a0 = ext.getBond().getAtom(0);
			IAtom a1 = ext.getBond().getAtom(1);
			int hs = 0;
			
			int idx0 = this.atomsMapping.get(b.getAbsolutePosition1());
			hs = this.getChemObject().getMolecule().getAtom(idx0).getImplicitHydrogenCount();
			a0.setImplicitHydrogenCount(hs);
			this.hydrogensMapping.put(idx0, hs);
			
			int idx1 = this.atomsMapping.get(b.getAbsolutePosition2());
			hs = this.getChemObject().getMolecule().getAtom(idx1).getImplicitHydrogenCount();
			a1.setImplicitHydrogenCount(hs);
			this.hydrogensMapping.put(idx1, hs);
			
			if (a0.getAtomicNumber() == a1.getAtomicNumber()
					&& a0.getFlag(CDKConstants.ISAROMATIC) == a1.getFlag(CDKConstants.ISAROMATIC)
					&& a0.getImplicitHydrogenCount() == a1.getImplicitHydrogenCount()
					&& b.getPosition1() > b.getPosition2()) {
				b.invertPositions();
			}
			
			b = b.getSubBlc();
		}
	}
	
}
