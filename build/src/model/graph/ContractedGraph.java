package model.graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import model.Family.Link;
import model.Residue;

import org._3pq.jgrapht.graph.SimpleGraph;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;

import algorithms.utils.Coverage;
import algorithms.utils.Match;

@SuppressWarnings("serial")
public class ContractedGraph extends SimpleGraph implements Cloneable {

	public Map<Integer, Vertex> verticiesOfatoms;
	private Coverage cov;
	
	public ContractedGraph(Coverage cov) {
		this.cov = cov;
		this.verticiesOfatoms = new HashMap<>();
		
		IMolecule mol = cov.getChemicalObject().getMolecule();
		
		// Create nodes with covered atoms contracted
		this.initCoveredVerticies(cov);
		// Create nodes not covered
		this.addUncoveredVerticies(mol);
		// Create links between contracted and unknown nodes
		this.createLinks(mol);
		// Contract unknown nodes
		this.contractUnknown();
	}

	// Create nodes with covered atoms contracted
	private void initCoveredVerticies (Coverage cov) {
		List<Match> matches = cov.getUsedMatches();
		for (Match match : matches) {
			Vertex v = new Vertex();
			v.id = match.getId();
			v.res = match.getResidue();
			v.vertices.addAll(match.getAtoms());
			for (int id : match.getAtoms()) {
				this.verticiesOfatoms.put(id, v);
			}
			
			this.addVertex(v);
		}
	}
	
	// Create nodes not covered
	private void addUncoveredVerticies (IMolecule mol) {
		for (IAtom atom : mol.atoms()) {
			int idx = mol.getAtomNumber(atom);
			
			if (!this.verticiesOfatoms.keySet().contains(idx)) {
				Vertex v = new Vertex();
				v.id = "?";
				v.vertices.add(idx);
				this.addVertex(v);
				this.verticiesOfatoms.put(idx, v);
			}
		}
	}
	
	// Create links between contracted and unknown nodes
	private void createLinks(IMolecule mol) {
		this.removeAllEdges(this.edgeSet());
		
		for (Object o : this.vertexSet()) {
			Vertex v = (Vertex)o;
			
			for (int idx : v.vertices) {
				IAtom a = mol.getAtom(idx);
				List<IAtom> neighbors = mol.getConnectedAtomsList(a);
				
				for (IAtom n : neighbors) {
					int aIdx = mol.getAtomNumber(n);
					
					if (!v.vertices.contains(aIdx)) {
						// Base edge
						Vertex vOa = this.verticiesOfatoms.get(aIdx);
						LabeledEdge edge = new LabeledEdge(v, vOa);
						
						// Labels
						// TODO : Add kind of bond label.
						
						this.addEdge(edge);
					}
				}
			}
		}
	}
	
	// Contract unknown nodes
	@SuppressWarnings("unchecked")
	private void contractUnknown() {
		Stack<Vertex> toCompute = new Stack<Vertex>();
		toCompute.addAll(this.vertexSet());
		
		while (!toCompute.isEmpty()) {
			Vertex current = toCompute.pop();
			if (!current.id.equals("?"))
				continue;
		
			// Search similar neighbors to agglomerate them.
			for (Vertex n : this.getNeighbors(current)) {
				if (!"?".equals(n.id))
					continue;
				
				// Create a new contract node
				Vertex old = current;
				current = new Vertex();
				current.id = "?";
				toCompute.push(current);
				
				// Add all the vertices of both nodes in the new one
				current.vertices.addAll(old.vertices);
				for (int id : old.vertices)
					this.verticiesOfatoms.put(id, current);
				current.vertices.addAll(n.vertices);
				for (int id : n.vertices)
					this.verticiesOfatoms.put(id, current);
				this.addVertex(current);
				
				// redirect all neighbors edges of both of vertices
				for (Vertex nn : this.getNeighbors(n)) {
					if (nn == old) {
						this.removeEdge(old, n);
						this.removeEdge(n, old);
					} else {
						this.removeEdge(nn, n);
						this.removeEdge(n, nn);
						/*System.out.println(current.id + "\n" + nn.id);
						System.out.println(current.vertices + "\n" + nn.vertices);*/
						this.addEdge(new LabeledEdge(current, nn));
					}
				}
				for (Vertex nn : this.getNeighbors(old)) {
					this.removeEdge(nn, old);
					this.removeEdge(old, nn);
					this.addEdge(new LabeledEdge(current, nn));
				}
				
				// Remove old vertices
				toCompute.remove(old);
				toCompute.remove(n);
				this.removeVertex(old);
				this.removeVertex(n);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	public Set<Vertex> getNeighbors (Vertex v) {
		Set<Vertex> neighbors = new HashSet<>();
		
		List<LabeledEdge> edges = this.edgesOf(v);
		for (LabeledEdge le : edges) {
			Vertex n = (Vertex) (le.getSource().equals(v) ? le.getTarget() : le.getSource());
			neighbors.add(n);
		}
		
		return neighbors;
	}


	/*public Vertex fusion(Vertex n, Vertex v) {
		// Create new vertex
		Vertex current = new Vertex();
		current.id = "?";
		
		// Add all the vertices of both nodes in the new one
		current.vertices.addAll(v.vertices);
		for (int id : v.vertices)
			this.verticiesOfatoms.put(id, current);
		current.vertices.addAll(n.vertices);
		for (int id : n.vertices)
			this.verticiesOfatoms.put(id, current);
		this.addVertex(current);
		
		// redirect all neighbors edges of both of vertices
		for (Vertex nn : this.getNeighbors(n)) {
			if (nn == v) {
				this.removeEdge(v, n);
				this.removeEdge(n, v);
			} else {
				this.removeEdge(nn, n);
				this.removeEdge(n, nn);
				this.addEdge(new UndirectedEdge(current, nn));
			}
		}
		for (Vertex nn : this.getNeighbors(v)) {
			this.removeEdge(nn, v);
			this.removeEdge(v, nn);
			this.addEdge(new UndirectedEdge(current, nn));
		}
		
		// Remove old vertices
		this.removeVertex(v);
		this.removeVertex(n);
		
		return current;
	}*/
	
	@Override
	public ContractedGraph clone() {
		return new ContractedGraph(this.cov);
	}
	
	
	public class Vertex {
		public String id;
		public Residue res;
		public Set<Integer> vertices;
		public final Map<Integer, Link> bonds;
		
		public Vertex() {
			this.vertices = new HashSet<>();
			this.bonds = new HashMap<>();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Vertex))
				return false;
			
			Vertex v = (Vertex)obj;
			return this.vertices.equals(v.vertices);
		}
		
		@Override
		public int hashCode() {
			return this.vertices.hashCode();
		}
		
		@Override
		public String toString() {
			return this.vertices.toString();
		}
	}
	
}
