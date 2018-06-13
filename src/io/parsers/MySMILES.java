package io.parsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

/**
 * @author Yoann Dufresne
 * 
 * Temporary class before I find a way to create partial aromatic query
 *
 */
public class MySMILES {

	/**
	 * 
	 * @param molecule
	 * @return
	 */
	public String convert (IMolecule molecule, boolean hydrogens) {
		if (hydrogens) {
			try {
				molecule = molecule.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			AtomContainerManipulator.convertImplicitToExplicitHydrogens(molecule);
		}
		
		DFS dfs = new DFS(molecule);
		List<AtomNode> firsts = dfs.sort(new ArrayList<>(dfs.nodes.values()), null, dfs.mol).get(0);
		
		HashMap<AtomNode, DFS> clones = new HashMap<MySMILES.AtomNode, MySMILES.DFS>();
		for (AtomNode an : firsts) {
			clones.put(an, dfs.clone());
		}
		
		DFS best = null;
		for (AtomNode an : firsts) {
			DFS clone = clones.get(an);
			this.createDFS(clone, clone.nodes.get(an.atom), null);
			
			if (best == null)
				best = clone;
			else if (clone.toString().compareTo(best.toString()) < 0)
				best = clone;
		}
		
		return best.toString();
	}
	
	private void createDFS (DFS dfs, AtomNode current, AtomNode from) {
		current.state = State.CHECK;
		dfs.path.add(current);
		
		List<AtomNode> toExplore = new ArrayList<MySMILES.AtomNode>();
		
		// For all neighbors save enighbors to explore
		for (IAtom a : dfs.mol.getConnectedAtomsList(current.atom)) {
			AtomNode nei = dfs.nodes.get(a);
			if (from != null && nei.atom == from.atom)
				continue;
			
			switch (nei.state) {
			case FREE:
				toExplore.add(nei);
				nei.state = State.TOEXPLORE;
			case TOEXPLORE:
				// TODO better smiles with less () and cycles first
				break;
			default :
				if (nei.rings == null)
					nei.rings = new ArrayList<Integer>();
				nei.rings.add(dfs.ringNumber);
				
				if (current.rings == null)
					current.rings = new ArrayList<Integer>();
				current.rings.add(dfs.ringNumber++);
				break;
			}
		}
		
		if (toExplore.size() == 0)
			return;
		
		// Sort neighbors to explore.
		List<List<AtomNode>> sortedNeighbors = dfs.sort (toExplore, current, dfs.mol);
		
		for (List<AtomNode> similar : sortedNeighbors) {
			HashMap<List<AtomNode>, DFS> clones = new HashMap<>();
			// Clone if there are most than one possibility
			List<List<AtomNode>> alternatives = this.getAllAlternatives (similar);
			for (List<AtomNode> alt : alternatives)
				clones.put(alt, dfs.clone());
			
			// Explore all alternatives for equivalent nodes
			DFS bestClone = null;
			for (List<AtomNode> alternative : alternatives) {
				DFS clone = clones.get(alternative);
				
				for (AtomNode node : alternative) {
					node = clone.nodes.get(node.atom);
					// To avoid 2 usages of same node if there are rings
					if (node.state != State.TOEXPLORE) {
						clones.remove(node);
						continue;
					}
					
					clone.path.add(new TextNode("("));
					this.createDFS(clone, node, current);
					clone.path.add(new TextNode(")"));
				}
				
				// Select best clone
				if (bestClone == null)
					bestClone = clone;
				else if (bestClone.toString().compareTo(clone.toString()) > 0)
					bestClone = clone;
			}
			
			// Transformation of the current DFS with the best clone extensions.
			for (int idx=dfs.path.size() ; idx<bestClone.path.size() ; idx++) {
				Node node = bestClone.path.get(idx);
				if (node instanceof AtomNode) {
					AtomNode cloneNode = (AtomNode)node;
					IAtom atom = cloneNode.atom;
					
					AtomNode an = dfs.nodes.get(atom);
					an.state = cloneNode.state;
					an.rings = cloneNode.rings;
					an.sortLink = cloneNode.sortLink;
					
					dfs.path.add(an);
				} else {
					dfs.path.add(new TextNode(((TextNode)node).txt));
				}
			}
			dfs.ringNumber = bestClone.ringNumber;
		}
	}
	
	private List<List<AtomNode>> getAllAlternatives(List<AtomNode> similar) {
		List<List<AtomNode>> alternatives = new ArrayList<>();
		alternatives.add(new ArrayList<AtomNode>());
		
		for (int position=0 ; position<similar.size() ; position++) {
			List<List<AtomNode>> nextAlt = new ArrayList<>();
			
			for (List<AtomNode> alt : alternatives) {
				for (int idx=0 ; idx<similar.size() ; idx++) {
					if (!alt.contains(similar.get(idx))) {
						List<AtomNode> clone = new ArrayList<>(alt);
						clone.add(similar.get(idx));
						nextAlt.add(clone);
					}
				}
			}
			
			alternatives = nextAlt;
		}
		
		return alternatives;
	}

	/**
	 * Structure needed for a DFS.
	 */
	private class DFS {
		private IMolecule mol;
		private Map<IAtom, AtomNode> nodes;
		private List<Node> path;
		private int ringNumber;
		
		public DFS(IMolecule mol) {
			this.mol = mol;
			this.path = new ArrayList<Node>();
			this.ringNumber = 1;
			
			// Creation of the nodes
			this.nodes = new HashMap<IAtom, MySMILES.AtomNode>();
			for (IAtom a : mol.atoms())
				this.nodes.put(a, new AtomNode(a));
		}
		
		public List<List<AtomNode>> sort(List<AtomNode> toSort, AtomNode current, IMolecule mol) {
			List<List<AtomNode>> sortedNodes = new ArrayList<List<AtomNode>>();
			
			for (AtomNode node : toSort) {
				if (current == null)
					node.sortLink = Order.SINGLE;
				else {
					IBond bond = mol.getBond(node.atom, current.atom);
					node.sortLink = bond.getOrder();
				}
			}
			
			// Sort collection
			Collections.sort(toSort);
			
			//Split collection into similar groups.
			ArrayList<AtomNode> currentGroup = new ArrayList<>();
			sortedNodes.add(currentGroup);
			for (AtomNode node : toSort) {
				if (!currentGroup.isEmpty() && // Not empty and not similar node
						!node.toString().equals(currentGroup.get(0).toString())) {
					currentGroup = new ArrayList<MySMILES.AtomNode>();
					sortedNodes.add(currentGroup);
				}
				currentGroup.add(node);
			}
			
			return sortedNodes;
		}

		protected DFS clone() {
			DFS dfs = new DFS(this.mol);
			dfs.nodes = new HashMap<IAtom, MySMILES.AtomNode>();
			for (AtomNode node : this.nodes.values())
				dfs.nodes.put(node.atom, new AtomNode(node));
			dfs.path = new ArrayList<Node>();
			for (Node node : this.path)
				if (node instanceof TextNode)
					dfs.path.add(node);
				else {
					dfs.path.add(dfs.nodes.get(((AtomNode)node).atom));
				}
			
			return dfs;
		}
		
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			for (Node node : this.path)
				sb.append(node.toString());
			return sb.toString();
		}
	}
	
	
	private interface Node {}
	
	private class TextNode implements Node {
		private String txt;

		public TextNode(String txt) {
			this.txt = txt;
		}
		
		@Override
		public String toString() {
			return this.txt;
		}
	}
	
	/**
	 * Nodes including atoms, states and possible ring options.
	 */
	private class AtomNode implements Node, Comparable<AtomNode> {
		private IAtom atom;
		private State state;
		private List<Integer> rings;
		
		private Order sortLink;

		public AtomNode(IAtom a) {
			this.atom = a;
			this.state = State.FREE;
		}
		
		public AtomNode (AtomNode node) {
			this.atom = node.atom;
			this.sortLink = node.sortLink;
			this.state = node.state;
			if (node.rings != null)
				this.rings = new ArrayList<Integer>(node.rings);
		}
		
		@Override
		public int compareTo(AtomNode node) {
			return this.toString().compareTo(node.toString());
		}
		
		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			
			// Bond string
			if (this.sortLink != null)
				switch (this.sortLink) {
				case DOUBLE:
					sb.append("=");
					break;
				case TRIPLE:
					sb.append("#");
				default:
					break;
				}
			
			// Atom string
			if (this.atom.getFlag(CDKConstants.ISAROMATIC))
				sb.append(this.atom.getSymbol().toLowerCase());
			else
				sb.append(this.atom.getSymbol());
			
			// Rings
			if (this.rings != null)
				for (int num : this.rings) {
					sb.append('[');
					sb.append(num);
					sb.append(']');
				}
			
			// Concatenation
			return sb.toString();
		}
		
		@Override
		public int hashCode() {
			return this.atom.hashCode();
		}
	}
	
	/**
	 * States of a node for the DFS
	 */
	private enum State {
		FREE, TOEXPLORE, CHECK, RING;
	}
}
