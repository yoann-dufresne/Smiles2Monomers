package algorithms.utils.planarity;

import java.util.Iterator;
import java.util.Set;

import org._3pq.jgrapht.edge.UndirectedEdge;
import org._3pq.jgrapht.graph.AbstractGraph;
import org._3pq.jgrapht.graph.SimpleGraph;
import org.openscience.cdk.Atom;

/**
 * 
 * @author dufresne
 *
 * Represent a segment for a planarity test
 */
@SuppressWarnings("serial")
public class Segment extends SimpleGraph {
	

	public Segment (Set<Atom> vertices, Set<UndirectedEdge> edges) {
		super();
		this.addAllVertices(vertices);
		this.addAllEdges(edges);
	}
	
	@SuppressWarnings("unchecked")
	public boolean isStringIn (AbstractGraph sg) {
		Iterator<Atom> segIt = this.vertexSet().iterator();
		int cpt = 0;
		while (segIt.hasNext())
		{
			Atom a = segIt.next();
			if (sg.vertexSet().contains(a))
				cpt++;
		}
		
		if (cpt == 2)
			return true;
		return false;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		String s = "";
		boolean first = true;
		Iterator<Atom> i = this.vertexSet().iterator();
		while (i.hasNext()) {
			if (first)
				first = false;
			else
				s += "\n";
			s += i.next().toString();
		}
		return s;
	}
	
}
