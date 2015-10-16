package algorithms.isomorphism.conditions;

import algorithms.utils.Coverage;

public interface ConditionToExtend {

	public boolean toContinue (Coverage cov);

	public void init();
	
}
