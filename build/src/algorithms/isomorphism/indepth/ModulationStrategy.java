package algorithms.isomorphism.indepth;

import model.graph.ContractedGraph;
import algorithms.utils.Coverage;

public interface ModulationStrategy {

	public void modulate (Coverage cov, ContractedGraph cg);
	
	public void nextLevel();

	public void init();
	
}
