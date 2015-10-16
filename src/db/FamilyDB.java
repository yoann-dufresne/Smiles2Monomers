package db;

import io.parsers.SmilesConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Family;
import model.Family.Link;
import model.Monomer;
import model.Residue;

import org.openscience.cdk.exception.InvalidSmilesException;

public class FamilyDB extends DB<Family> {

	private Set<Family> uniqFamilies;
	
	public FamilyDB() {
		super();
		this.uniqFamilies = new HashSet<>();
	}

	@Override
	public DB<Family> createNew() {
		return new FamilyDB();
	}
	
	public void init (MonomersDB monoDB) {
		Map<String, List<Monomer>> clusters = new HashMap<>();
		for (Monomer mono : monoDB.getObjects())
		{
			String smiles = null;
			try {
				smiles = SmilesConverter.conv.toCanonicalSmiles(mono.getSmiles());
			} catch (InvalidSmilesException e) {
				System.err.println("Impossible to parse " + mono.getName() + " id:" + mono.getId());
				System.err.println(mono.getSmiles());
				continue;
			}
			List<Monomer> cluster = clusters.containsKey(smiles) ? clusters.get(smiles) : new ArrayList<Monomer>();
			cluster.add(mono);
			clusters.put(smiles, cluster);
		}
		
		for (String smiles : clusters.keySet()) {
			Family family = new Family();
			
			for (Monomer mono : clusters.get(smiles))
			{
				family.addMonomer(mono);
				this.addObject(mono.getCode(), family);
			}
		}
	}
	
	public Set<Family> getFamilies () {
		return this.uniqFamilies;
	}

	public ResiduesDB getResidues() {
		ResiduesDB resDB = new ResiduesDB();
		
		for (String name : this.database.keySet()) {
			Family fam = this.database.get(name);
			
			for (Residue res : fam.getResidues())
				resDB.addObject(res.getId(), res);
		}
		
		return resDB;
	}
	
	public boolean areInSameFamily (String mono1, String mono2) {
		Family family1 = null;
		Family family2 = null;
		try {
			family1 = this.getObject(mono1);
			family2 = this.getObject(mono2);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		return family1.equals(family2);
	}
	
	@Override
	public void addObject(String id, Family f) {
		Family prev = null;
		
		for (Monomer m : f.getMonomers())
			if (this.database.containsKey(m.getId())) {
				prev = this.database.get(m.getId());
				break;
			}
		
		if (prev == null) {
			for (Monomer m : f.getMonomers()) {
				super.addObject(m.getId(), f);
				this.uniqFamilies.add(f);
			}
		} else {
			for (Monomer m : f.getMonomers()) {
				prev.addMonomer(m);
				this.database.put(m.getId(), prev);
			}
			for (Residue res : f.getResidues())
				prev.addResidue(res);
			for (Link l : f.getDepandances())
				prev.addDependance(l);
		}
	}
	
	@Override
	public List<Family> getObjects () {
		List<Family> objects = new ArrayList<>();
		objects.addAll(this.uniqFamilies);
		
		return objects;
	}
	
	@Override
	public int size() {
		return this.uniqFamilies.size();
	}
	
}
