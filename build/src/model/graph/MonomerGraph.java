package model.graph;

import java.util.ArrayList;
import java.util.List;

import model.Monomer;
import algorithms.utils.Coverage;

public class MonomerGraph {

	public Monomer[] nodes;
	public List<MonomerLinks> links; 
	
	public MonomerGraph(Monomer[] nodes) {
		this.nodes = nodes;
		this.links = new ArrayList<>();
	}
	
	public MonomerGraph (Coverage cov) {
		
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
