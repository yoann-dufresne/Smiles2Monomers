package algorithms.isomorphism.indepth;

import org._3pq.jgrapht.edge.UndirectedEdge;

import model.graph.ContractedGraph;
import model.graph.ContractedGraph.Vertex;
import algorithms.utils.Coverage;

public class RemoveMostNeighboorsMatchs implements RemoveStrategy {
	
	private Neighborhood neig;
	
	public RemoveMostNeighboorsMatchs() {
		this.neig = new Neighborhood();
	}

	@Override
	public void remove(Coverage cov, ContractedGraph cg) {
		this.neig.calculate(cg);
		
		while (this.neig.size() > 0) {
			Vertex v = this.neig.highest();
			
			cov.removeUsedMatch(v.res, v.vertices);
			this.adaptGraph (cg, v);
			this.neig.calculate(cg);
		}
		
		this.cleanGraph(cg);
	}

	private void adaptGraph(ContractedGraph cg, Vertex v) {
		for (Object o : cg.edgesOf(v)) {
			UndirectedEdge ue = (UndirectedEdge)o;
			Vertex source = (Vertex)ue.getSource();
			if (source.id.startsWith("?"))
				source.id = "";
			Vertex target = (Vertex)ue.getTarget();
			if (target.id.startsWith("?"))
				target.id = "";
		}
	}
	
	private void cleanGraph(ContractedGraph cg) {
		// Rename unknow nodes
		for (Object o : cg.vertexSet()) {
			Vertex v = (Vertex)o;
			if ("".equals(v.id))
				v.id = "?";
		}
	}

	@Override
	public void nextLevel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		
	}

}
