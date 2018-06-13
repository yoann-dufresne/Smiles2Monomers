package algorithms.isomorphism.indepth;

import java.util.HashSet;
import java.util.Set;

import model.graph.ContractedGraph;
import model.graph.ContractedGraph.Vertex;
import algorithms.utils.Coverage;

public class RemoveByDistance implements RemoveStrategy {
	
	private int maxDistance;
	private int distance;

	public RemoveByDistance(int max) {
		this.maxDistance = max;
		this.distance = 1;
	}

	@Override
	public void remove(Coverage cov, ContractedGraph cg) {
		ContractedGraph clone = cg.clone();
		this.removeRecur(cov, clone, 0);
	}

	private void removeRecur(Coverage cov, ContractedGraph cg, int currentDist) {
		if (this.distance == currentDist)
			return;
		
		Set<Vertex> neighbors = new HashSet<>();
		for (Object o : cg.vertexSet()) {
			Vertex v = (Vertex)o;
			if (v.id.startsWith("?")) {
				neighbors.addAll(cg.getNeighbors(v));
			}
		}
		
		for (Vertex n : neighbors) {
			if (n.res == null)
				continue;
			cov.removeUsedMatch(n.res, n.vertices);
			n.res = null;
			n.id = "?";
		}
		
		this.removeRecur(cov, cg, currentDist+1);
	}

	@Override
	public void nextLevel() {
		if (this.distance < this.maxDistance)
			this.distance += 1;
	}

	@Override
	public void init() {
		this.distance = 1;
	}

}
