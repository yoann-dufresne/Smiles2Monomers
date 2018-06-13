package algorithms.isomorphism.indepth;

import java.util.HashMap;

import org._3pq.jgrapht.edge.UndirectedEdge;

import model.graph.ContractedGraph;
import model.graph.ContractedGraph.Vertex;

/**
 * This class is a count of missing neighbors for each residue used in a coverage. A miss is a neighbor area without any residue matched.
 * @author dufresne
 *
 */
@SuppressWarnings("serial")
public class Neighborhood extends HashMap<Vertex, Integer> {
	
	public void calculate (ContractedGraph cg) {
		this.clear();
		
		for (Object o : cg.vertexSet()) {
			Vertex v = (Vertex)o;
			
			if (v.id.startsWith("?"))
				continue;
			
			int nb = 0;
			for (Object o2 : cg.edgesOf(v)) {
				UndirectedEdge e = (UndirectedEdge)o2;
				Vertex source = (Vertex)e.getSource();
				Vertex target = (Vertex)e.getTarget();
				if (source.id.startsWith("?") || target.id.startsWith("?"))
					nb++;
			}
			
			if (nb > 0)
				this.put(v, nb);
		}
	}

	public Vertex highest() {
		Vertex v = null;
		int score = 0;
		
		for (Vertex candidate : this.keySet())
			if (this.get(candidate) > score) {
				v = candidate;
				score = this.get(candidate);
			}
		
		return v;
	}
	
}
