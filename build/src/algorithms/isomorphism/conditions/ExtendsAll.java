package algorithms.isomorphism.conditions;

import algorithms.utils.Coverage;

public class ExtendsAll implements ConditionToExtend {

	@Override
	public boolean toContinue(Coverage cov) {
		return true;
	}

	@Override
	public void init() {}

}
