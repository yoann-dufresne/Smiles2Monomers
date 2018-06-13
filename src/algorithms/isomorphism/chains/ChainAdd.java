package algorithms.isomorphism.chains;

import org.json.simple.JSONObject;

import algorithms.isomorphism.MatchingType;
import model.Residue;

public abstract class ChainAdd {
	
	private Residue from;
	
	public ChainAdd(Residue from) {
		this.from = from;
	}
	
	public Residue getFrom() {
		return from;
	}

	public abstract void apply(MappedChain mc, MatchingType type) throws AtomNotFound;
	
	public abstract MappedChain applyAndClone (MappedChain mc, MatchingType type) throws AtomNotFound;

	public abstract JSONObject toJSON();
	
}
