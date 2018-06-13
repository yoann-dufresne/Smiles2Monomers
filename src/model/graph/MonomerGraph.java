package model.graph;

import java.util.ArrayList;
import java.util.List;

import model.Monomer;
import model.Residue;

public class MonomerGraph {

	public Monomer[] nodes;
	public List<MonomerLinks> links;
	public Residue[] residues; 
	
	public MonomerGraph(Monomer[] nodes, Residue[] residues) {
		this.nodes = nodes;
		this.residues = residues;
		this.links = new ArrayList<>();
	}
	
	public MonomerGraph(Monomer[] nodes) {
		this.nodes = nodes;
		this.residues = null;
		this.links = new ArrayList<>();
	}
	
	public void createLink (int mono1, int mono2) {
		this.links.add(new MonomerLinks(mono1, mono2));
	}
	
	public class MonomerLinks {
		public int mono1;
		public int mono2;
		
		public MonomerLinks(int mono1, int mono2) {
			this.mono1 = mono1;
			this.mono2 = mono2;
		}
	}
	
}
