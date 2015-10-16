package algorithms.isomorphism.conditions;

import algorithms.utils.Coverage;

public class ExtendsNTimes implements ConditionToExtend {
	
	private int maxVal;
	private int n;

	public ExtendsNTimes(int maxIterationNumber) {
		this.maxVal = maxIterationNumber;
		this.n = maxIterationNumber;
	}

	@Override
	public boolean toContinue(Coverage cov) {
		if (this.n == 0)
			return false;
		else
			this.n -= 1;
		return true;
	}

	@Override
	public void init() {
		this.n = this.maxVal;
	}

}
