package model;

import io.parsers.SmilesConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openscience.cdk.exception.InvalidSmilesException;

public class Family {
	private List<Monomer> monomers;
	private List<String> monoNames;
	private Map<Integer, Residue> residues;
	private Set<Link> dependances;
	
	private String monoSMILES;

	private String name;
	
	public Family() {
		this.monomers = new ArrayList<>();
		this.residues = new HashMap<>();
		this.dependances = new HashSet<>();
		this.monoSMILES = "";
		this.monoNames = new ArrayList<>();
	}
	
	public void addMonomer (Monomer mono) {
		if (this.monomers.contains(mono))
			return;
		
		this.monomers.add(mono);
		this.monoNames.add(mono.getName());
		this.name = null;
		
		if ("".equals(this.monoSMILES))
			try {
				this.monoSMILES = SmilesConverter.conv.toCanonicalSmiles(mono.getSMILES());
			} catch (InvalidSmilesException e) {
				System.err.println("Impossible to parse " + this.getName());
			}
	}
	
	public void addResidue (Residue res) {
		if (!this.monoNames.contains(res.getMonoName())) {
			this.monoNames.add(res.getMonoName());
			this.name = null;
		}
		
		this.residues.put(new Integer(res.getId()), res);
	}
	
	public void addDependance (Residue from, Residue to) {
		this.dependances.add(new Link(new Integer(from.getId()), new Integer(to.getId())));
	}

	public void addDependance(Link l) {
		this.dependances.add(new Link(l.from, l.to));
	}
	
	public void addDependance(int from, int to) {
		this.dependances.add(new Link(from, to));
	}
	
	public List<Residue> getRoots () {
		List<Residue> residues = new ArrayList<>(this.residues.values());
		for (Link dep : this.dependances) {
			Residue res = this.residues.get(dep.to);
			if (residues.contains(res))
				residues.remove(res);
		}
		
		return residues;
	}
	
	public Set<Residue> getChildrenOf (Residue res) {
		Set<Residue> children = new HashSet<>();
		
		for (Link dep : this.dependances)
			if (dep.from.equals(new Integer(res.getId())))
				children.add(this.residues.get(dep.to));
		
		return children;
	}
	
	public Set<Residue> getParentsOf (Residue res) {
		Set<Residue> parents = new HashSet<>();
		
		for (Link dep : this.dependances)
			if (dep.to.equals(new Integer(res.getId())))
				parents.add(this.residues.get(dep.from));
		
		return parents;
	}
	
	public List<Monomer> getMonomers() {
		return monomers;
	}
	
	public Set<Residue> getResidues() {
		return new HashSet<>(residues.values());
	}
	
	public Set<Link> getDepandances() {
		return dependances;
	}
	
	public boolean containsMonomer (String monoName) {
		for (Monomer m : this.monomers) {
			if (m.getCode().equals(monoName))
				return true;
		}
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Family))
			return false;
		Family family = (Family)obj;
		return this.monoSMILES.equals(family.monoSMILES);
	}
	
	@Override
	public int hashCode() {
		return this.monoSMILES.hashCode();
	}
	
	
	
	
	public class Link {
		private Integer from;
		private Integer to;
		
		public Link(Integer from, Integer to) {
			this.from = from;
			this.to = to;
		}
		
		public Integer getFrom() {
			return from;
		}
		
		public Integer getTo() {
			return to;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Link))
				return false;
			
			Link link = (Link)obj;
			return this.from == link.from && this.to == link.to;
		}
		
		@Override
		public int hashCode() {
			return (this.from + "-" + this.to).hashCode();
		}
		
		@Override
		public String toString() {
			return this.from + " -> " + this.to;
		}
	}




	public String getName() {
		if (this.name == null) {
			Collections.sort(this.monoNames);
			
			this.name = "";
			for (String name : this.monoNames)
				this.name += "," + name;
			this.name = this.name.substring(1);
		}
		
		return this.name;
	}
	
}
