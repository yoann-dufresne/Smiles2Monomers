package algorithms.isomorphism;

import model.ChemicalObject;
import model.Family;
import algorithms.utils.Coverage;

public interface FamilyMatcher {

	public void setChemicalObject (ChemicalObject co);
	
	public Coverage matchFamilly(Family family);

	void setAllowLightMatch(MatchingType type);
	
}
