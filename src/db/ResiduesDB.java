package db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import model.Residue;

public class ResiduesDB extends ChemicalObjectDB<Residue> {
	private Map<String, Set<Residue>> databaseByMonomer;
	private Map<Integer, Residue> databaseByIdx;
	
	public ResiduesDB() {
		super();
		this.databaseByMonomer = new HashMap<>();
		this.databaseByIdx = new HashMap<>();
	}
	
	public void addObject (String id, Residue r) {
		super.addObject(id, r);
		
		@SuppressWarnings("unchecked")
		Set<Residue> residues = (Set<Residue>) (this.databaseByMonomer.containsKey(r.getMonoName()) ?
				this.databaseByMonomer.get(r.getMonoName()) : new HashSet<>());
		residues.add(r);
		this.databaseByMonomer.put(r.getMonoName(), residues);
		if (new Integer(r.getId()) != 0)
			this.databaseByIdx.put(new Integer(r.getId()), r);
	}
	
	public void addObjects (Map<String, Residue> map) {
		for (Entry<String, Residue> r : map.entrySet())
			this.addObject(r.getKey(), r.getValue());
	}
	
	public Set<Residue> getResiduesOf (String monoName) {
		return this.databaseByMonomer.get(monoName);
	}
	
	public List<Residue> getOrderedResidues () {
		List<Residue> residues = new ArrayList<>(this.database.values());
		Collections.sort(residues);
		return residues;
	}
	
	public List<Residue> getAlphabeticResidues () {
		List<Residue> residues = new ArrayList<>(this.database.values());
		Collections.sort(residues);
		
		this.databaseByIdx.clear();
		for (Residue res : residues) {
			//res.setIdx(residues.indexOf(res)+1);
			this.databaseByIdx.put(residues.indexOf(res)+1, res);
		}
		
		return residues;
	}
	
	public int getIdx (Residue residue) {
		return new Integer((this.database.get(residue.getSmiles()).getId()));
	}
	
	public Residue getResidueAt (int idx) {
		return this.databaseByIdx.get(idx);
	}

	@Override
	public DB<Residue> createNew() {
		return new ResiduesDB();
	}
}
