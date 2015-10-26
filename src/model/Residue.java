package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.graph.MonomerGraph;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;


public class Residue extends AbstractChemicalObject implements Comparable<Residue> {
	//private static boolean alphabetic = false;

	/**
	 * 
	 */
	private static final long serialVersionUID = -5961256677683240098L;
	private static int currentIdx = 0;
	private static final Map<String, List<Residue>> residueDirectory = new HashMap<>();
	public static void resetResidues () {
		currentIdx = 0;
		residueDirectory.clear();
	}
	
	private int idx;
	private String monoName;
	private String name;
	private int weight;
	
	private Map<IAtom, Rule> linkedAtoms;
	private Map<Integer, Rule> idxLinkedAtoms;
	
	
	private Residue (String monoName, String smiles) {
		this.smiles = smiles;
		this.monoName = monoName;
		this.name = this.monoName;
		this.generateH = true;
		this.setIdx(Residue.currentIdx ++);
	}
	
	public Residue (String monoName, String smiles, boolean createLinksMap) {
		this(monoName, smiles);
		this.idxLinkedAtoms = new HashMap<>();
	}
	
	public static boolean existingResidue (String smarts, String monoName) {
		if (Residue.residueDirectory.containsKey(smarts))
			for (Residue r : Residue.residueDirectory.get(smarts))
				if (r.getMonoName().equals(monoName))
					return true;
		return false;
	}
	
	public static Residue constructResidue (String monoName, String smarts) {
		Residue res = null;
		List<Residue> residues = null;
		if (Residue.residueDirectory.containsKey(smarts)) {
			residues = Residue.residueDirectory.get(smarts);
			for (Residue r : residues)
				if (r.getMonoName().equals(monoName))
					res = r;
		} else {
			residues = new ArrayList<>();
		}
		
		if (res == null) {
			res = new Residue(monoName, smarts);
			
			residues.add(res);
			Residue.residueDirectory.put(smarts, residues);
		}
		
		return res;
	}
	
	public void explicitToImplicitHydrogens () {
		IMolecule mol = this.getMolecule();
		for (IAtom a : mol.atoms())
			a.setImplicitHydrogenCount(0);
		
		List<IAtom> toRemove = new ArrayList<>();
		for (IAtom a : mol.atoms())
			if (a.getAtomTypeName().equals("H")) {
				IAtom connected = mol.getConnectedAtomsList(a).get(0);
				connected.setImplicitHydrogenCount(connected.getImplicitHydrogenCount()+1);
				toRemove.add(a);
			}
		
		for (IAtom a : toRemove) {
			mol.removeBond(mol.getConnectedBondsList(a).get(0));
			mol.removeAtom(a);
		}
	}

	public String getName() {
		return this.name;
	}

	public String getMonoName() {
		return monoName;
	}
	
	public int getWeight() {
		return weight;
	}
	
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
	public void addLink (IAtom atom, Rule type) {
		if (this.linkedAtoms == null)
			this.linkedAtoms = new HashMap<>();
		//this.idxLinkedAtoms = null;
		
		this.linkedAtoms.put(atom, type);
		
		if (this.getAtomicLinks().values().contains(type) && type.getWeights().length > 1)
			this.weight = this.weight + type.getWeights()[1];
		else
			this.weight = this.weight + type.getWeights()[0];
		
		this.changeName();
	}
	
	public void addIdxLink (int idx, Rule type) {
		if (this.idxLinkedAtoms == null)
			this.idxLinkedAtoms = new HashMap<>();
		//this.linkedAtoms = null;
		
		this.idxLinkedAtoms.put(idx, type);
		
		if (this.getIdxLinks().values().contains(type) && type.getWeights().length > 1)
			this.weight = this.weight + type.getWeights()[1];
		else
			this.weight = this.weight + type.getWeights()[0];
		
		this.changeName();
	}
	
	private void changeName() {
		this.name = this.monoName;
		List<Rule> links = new ArrayList<>(this.getIdxLinks().values());
		Collections.sort(links);
		for (Rule r : links)
			this.name += "_" + r.getName();
	}

	public Map<IAtom, Rule> getAtomicLinks() {
		if (this.linkedAtoms == null) {
			this.linkedAtoms = new HashMap<>();
			
			if (this.idxLinkedAtoms != null)
				for (Integer idx : this.idxLinkedAtoms.keySet()) {
					IAtom a = this.getMolecule().getAtom(idx);
					this.linkedAtoms.put(a, this.idxLinkedAtoms.get(idx));
				}
		}
		
		return this.linkedAtoms;
	}
	
	public Map<Integer, Rule> getIdxLinks() {
		if (this.idxLinkedAtoms == null) {
			this.idxLinkedAtoms = new HashMap<>();
			for (IAtom a : this.linkedAtoms.keySet()) {
				int idx = this.getMolecule().getAtomNumber(a);
				this.idxLinkedAtoms.put(idx, this.linkedAtoms.get(a));
			}
		}
		
		return this.idxLinkedAtoms;
	}

	public void setMol(Molecule mol) {
		this.mol = mol;
		this.size = 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Residue))
			return false;
		Residue r = (Residue)obj;
		
		return this.smiles.equals(r.smiles) && this.monoName.equals(r.monoName);
	}
	
	@Override
	public int hashCode() {
		return this.idx;
	}

	@Override
	public int compareTo(Residue res) {
		return this.name.compareTo(res.name);
	}
	
	public String getId() {
		return "" + idx;
	}
	
	public void setIdx(int idx) {
		this.idx = idx;
	}

	@Override
	public MonomerGraph getGraph() {
		Monomer[] nodes = {};
		return new MonomerGraph(nodes);
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
}
