package algorithms.utils.planarity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org._3pq.jgrapht.UndirectedGraph;
import org._3pq.jgrapht.edge.UndirectedEdge;
import org._3pq.jgrapht.graph.UndirectedSubgraph;
import org.openscience.cdk.Atom;
import org.openscience.cdk.graph.BiconnectivityInspector;
import org.openscience.cdk.ringsearch.cyclebasis.CycleBasis;
import org.openscience.cdk.ringsearch.cyclebasis.SimpleCycle;

@SuppressWarnings("serial")
public class Biconnected extends UndirectedSubgraph {
	
	private Collection<SimpleCycle> cycles;

	@SuppressWarnings("unchecked")
	public Biconnected(UndirectedGraph g, Set<Atom> vSubset, Set<UndirectedEdge> eSubset) {
		super(g, vSubset, eSubset);
		
		CycleBasis cb = new CycleBasis(this);
		this.cycles = cb.cycles();
	}
	
	/*
	 * Find segments in a biconnected graph
	 */
	@SuppressWarnings("unchecked")
	public List<Segment> getSegments(SimpleCycle mainCycle)
	{
		List<Segment> segments = new ArrayList<>();
		
		Set<UndirectedEdge> absents = new HashSet<>();
		// Add all graph vertices
		Iterator<UndirectedEdge> iv = this.edgeSet().iterator();
		while (iv.hasNext())
			absents.add(iv.next());
		// Remove main cycle vertices.
		Iterator<UndirectedEdge> ic = mainCycle.edgeSet().iterator();
		while (ic.hasNext())
			absents.remove(ic.next());
		
		while (absents.size() != 0)
		{
			// Depth first search for segments
			Set<Atom> segV = new HashSet<>();
			Set<UndirectedEdge> segE = new HashSet<>();
			Stack<UndirectedEdge> stack = new Stack<>();
			
			UndirectedEdge first = absents.iterator().next();
			stack.push(first);
			segE.add(first);
			
			while (!stack.isEmpty())
			{
				Set<UndirectedEdge> edges = new HashSet<>();
				
				UndirectedEdge e = stack.pop();
				
				// Add neighbors edges non already folloed
				edges.addAll(this.edgesOf(e.getSource()));
				edges.addAll(this.edgesOf(e.getTarget()));
				for (UndirectedEdge neighbor : edges)
					if (absents.contains(neighbor))
					{
						stack.push(neighbor);
						absents.remove(neighbor);
					}
				
				// Add verticies
				segV.add((Atom) e.getSource());
				segV.add((Atom) e.getTarget());
			}
			
			// Segment creation
			Segment seg = new Segment(segV, segE);
			segments.add(seg);
		}
		
		return segments;
	}
	
	public Set<SimpleCycle> getSeparatedCycles () {
		Set<SimpleCycle> sepCycles = new HashSet<>();
		
		Iterator<SimpleCycle> i = cycles.iterator();
		while (i.hasNext())
		{
			SimpleCycle c = i.next();
			if (this.getSegments(c).size() >= 2)
				sepCycles.add(c);
		}
		
		return sepCycles;
	}
	
	/*
	 * Function to create biconned components
	 */
	@SuppressWarnings("unchecked")
	public static List<Biconnected> createBiconnectedComponents(UndirectedGraph g)
	{
		BiconnectivityInspector bi = new BiconnectivityInspector(g);
		List<Set<UndirectedEdge>> biconnecteds = bi.biconnectedSets();
		
		List<Biconnected> bcs = new ArrayList<>();
		
		for (Set<UndirectedEdge> edges : biconnecteds)
		{
			HashSet<Atom> vertices = new HashSet<>();
			
			Iterator<UndirectedEdge> i = edges.iterator();
			while (i.hasNext())
			{
				UndirectedEdge e = i.next();
				vertices.add((Atom)e.getSource());
				vertices.add((Atom)e.getTarget());
			}
			
			Biconnected sg = new Biconnected(g, vertices, edges);
			
			bcs.add(sg);
		}
		
		return bcs;
	}

}
