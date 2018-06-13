package algorithms.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import algorithms.isomorphism.MatchingType;
import model.Residue;
import model.Rule;

public class Match implements Comparable<Match> {
	
	private String id;
	private Residue res;
	private Set<Integer> atoms;
	private Map<Integer, Integer> hydrogens;
	private Set<Integer> bonds;
	private Map<Integer, Rule> extLinks;
	private Map<Integer, MatchingType> qualities;
	
	public Match(Residue res) {
		this.res = res;
		this.atoms = new HashSet<>();
		this.hydrogens = new HashMap<>();
		this.bonds = new HashSet<>();
		this.extLinks = new HashMap<>();
		this.qualities = new HashMap<>();
	}
	
	public Match (Match match) {
		this.atoms = new HashSet<>(match.atoms);
		this.hydrogens = new HashMap<>(match.hydrogens);
		this.bonds = new HashSet<>(match.bonds);
		this.extLinks = new HashMap<>(match.extLinks);
	}
	
	/**
	 * This ids are not unique !
	 * @return Id based on atoms numbers
	 */
	public String getId() {
		if (this.id == null) {
			this.id = "";
			ArrayList<Integer> ids = new ArrayList<>(this.atoms);
			Collections.sort(ids);
			for (int i : ids)
				this.id += "-" + i;
			this.id = this.id.substring(1);
		}
		
		return this.id;
	}
	
	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Match))
			return false;
		
		Match match = (Match)o;
		
		if (!this.res.equals(match.res))
			return false;
		
		for (int idx : this.hydrogens.keySet())
			if (match.hydrogens.containsKey(idx)) {
				if (this.hydrogens.get(idx).intValue() != match.hydrogens.get(idx).intValue())
					return false;
			} else
				return false;
		
		return this.atoms.size() == match.atoms.size() && this.atoms.containsAll(match.atoms);
	}
	
	// Adders
	
	public void addAtom (int atom) {
		this.atoms.add(atom);
		this.id = null;
	}
	
	public void addAtoms (Collection<Integer> atoms) {
		this.atoms.addAll(atoms);
		this.id = null;
	}
	
	public void addHydrogens(int atom, int hydrogens) {
		this.hydrogens.put(atom, hydrogens);
	}
	
	public void addHydrogens(Map<Integer, Integer> hydrogens) {
		this.hydrogens.putAll(hydrogens);
	}
	
	public void addBond (int bond) {
		this.bonds.add(bond);
	}
	
	public void addBonds (Collection<Integer> bonds) {
		this.bonds.addAll(bonds);
	}
	
	public void addExtLink (int atom, Rule link) {
		this.extLinks.put(atom, link);
	}
	
	// Getters
	
	public Residue getResidue() {
		return res;
	}
	
	public Set<Integer> getAtoms() {
		return atoms;
	}
	
	public Map<Integer, Integer> getHydrogens() {
		return hydrogens;
	}
	
	public int getHydrogensFrom(int idx) {
		return this.hydrogens.get(idx);
	}
	
	public Set<Integer> getBonds() {
		return bonds;
	}
	
	public Map<Integer, Rule> getExtLinks() {
		return extLinks;
	}
	
	@Override
	public String toString() {
		return this.atoms.toString();
	}
	
	public int size () {
		int size = this.atoms.size();
		for (int val : this.hydrogens.values())
			size += val;
		
		return size;
	}

	@Override
	public int compareTo(Match match) {
		int size = this.size();
		int otherSize = match.size();
		
		if (size != otherSize)
			return otherSize - size;
		
		if (this.res.getIdxLinks().size() != match.res.getIdxLinks().size())
			return this.res.getIdxLinks().size() - match.res.getIdxLinks().size();
		
		return match.res.getWeight() - this.res.getWeight();
	}

	public void addQuality(int bondIdx, MatchingType matchingType) {
		this.qualities.put(bondIdx, matchingType);
	}
	
	public Map<Integer, MatchingType> getQualities () {
		return this.qualities;
	}
	
}
