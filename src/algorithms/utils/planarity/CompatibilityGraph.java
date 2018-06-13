package algorithms.utils.planarity;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org._3pq.jgrapht.edge.UndirectedEdge;
import org._3pq.jgrapht.graph.SimpleGraph;
import org.openscience.cdk.Atom;
import org.openscience.cdk.ringsearch.cyclebasis.SimpleCycle;

@SuppressWarnings("serial")
public class CompatibilityGraph extends SimpleGraph {

	public CompatibilityGraph(SimpleCycle mainCycle, List<Segment> segments) {
		for (Segment s1 : segments)
			this.addVertex(s1);
		
		for (Segment s1 : segments)
			for (Segment s2 : segments) {
				if (s1.equals(s2))
					continue;
				
				if (!this.isCompatible(mainCycle, s1, s2)) {
					if (!(this.containsEdge(s1, s2) || this.containsEdge(s2, s1)))
						this.addEdge(s1, s2);
				}
			}
	}
	
	@SuppressWarnings("unchecked")
	public boolean isCompatible (SimpleCycle mainCycle, Segment s1, Segment s2) {
		Set<Atom> inCycle = new HashSet<>(mainCycle.vertexSet());
		Stack<Atom> stack = new Stack<>();
		
		// Init
		Iterator<Atom> i = inCycle.iterator();
		Atom init = null;
		do {
			init = i.next();
		} while (!s2.containsVertex(init));
		inCycle.remove(init);
		stack.push(init);
		
		
		// follow path
		while (!stack.isEmpty())
		{
			Atom a = stack.pop();
			
			if (s1.containsVertex(a))
				continue;
			
			List<UndirectedEdge> edges = mainCycle.edgesOf(a);
			for (UndirectedEdge e : edges)
			{
				if (inCycle.contains(e.getSource())) {
					inCycle.remove(e.getSource());
					stack.push((Atom) e.getSource());
				}
				
				if (inCycle.contains(e.getTarget())) {
					inCycle.remove(e.getTarget());
					stack.push((Atom) e.getTarget());
				}
			}
		}
		
		
		// Is compatible ?
		for (Atom a : inCycle)
			if (s2.containsVertex(a)) {
				return false;
			}
		return true;
	}

}
