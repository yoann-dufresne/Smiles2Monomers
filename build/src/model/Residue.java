package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.graph.MonomerGraph;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;


public class Residue extends AbstractChemicalObject implements Comparable<Residue> {
	//private static boolean alphabetic = false;

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
	
	
	private Residue (String monoName, String smiles) {
		this.smiles = smiles;
		this.monoName = monoName;
		this.name = this.monoName;
		this.generateH = true;
		this.linkedAtoms = new HashMap<>();
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
			res.setIdx(Residue.currentIdx ++);
			
			residues.add(res);
			Residue.residueDirectory.put(smarts, residues);
		}
		
		return res;
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
		this.linkedAtoms.put(atom, type);
		
		if (this.getLinks().values().contains(type) && type.getWeights().length > 1)
			this.weight = this.weight + type.getWeights()[1];
		else
			this.weight = this.weight + type.getWeights()[0];
		
		this.changeName();
	}
	
	private void changeName() {
		this.name = this.monoName;
		List<Rule> links = new ArrayList<>(this.linkedAtoms.values());
		Collections.sort(links);
		for (Rule r : links)
			this.name += "_" + r.getName();
	}

	public Map<IAtom, Rule> getLinks() {
		return this.linkedAtoms;
	}

	public void setMol(IMolecule mol) {
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
		//if (Residue.alphabetic) {
			return this.name.compareTo(res.name);
		/*} else {
			if (this.getSize() != res.getSize())
				return res.getSize() - this.getSize();
			else if (this.getLinks().size() != res.getLinks().size())
				return this.links.size() - res.links.size();
			else
				return res.getWeight() - this.getWeight();
		}*/
	}
	
	public String getId() {
		return "" + idx;
	}
	
	public void setIdx(int idx) {
		this.idx = idx;
	}
	
	/*public static void setAlphabeticSort (boolean alphabetic) {
		Residue.alphabetic = alphabetic;
	}*/

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
