package algorithms.isomorphism.indepth;

import model.graph.ContractedGraph;
import algorithms.utils.Coverage;

public interface RemoveStrategy {

	public void remove (Coverage cov, ContractedGraph cg);
	
	public void nextLevel();

	public void init();
	
}
