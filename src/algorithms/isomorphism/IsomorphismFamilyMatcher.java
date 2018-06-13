package algorithms.isomorphism;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import model.ChemicalObject;
import model.Family;
import model.Family.Link;
import model.Residue;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import algorithms.utils.Coverage;
import algorithms.utils.Match;

public class IsomorphismFamilyMatcher implements FamilyMatcher {

	private boolean verbose;
	private SMARTSQueryTool sqt;
	private ChemicalObject co;
	
	public IsomorphismFamilyMatcher () {
		try {
			this.sqt = new SMARTSQueryTool("CC");
		} catch (CDKException e) {
			e.printStackTrace();
		}
		
		this.verbose = true;
	}

	@Override
	public Coverage matchFamilly(Family family) {
		//Initialization
		Coverage cov = new Coverage(co);
		IMolecule mol = co.getMolecule();
		Set<Residue> markSet = new HashSet<>();
		Stack<Residue> searchSet = new Stack<>();
		searchSet.addAll(family.getRoots());
		
		//For all the nodes in the search group.
		while (!searchSet.isEmpty()) {
			Residue currentRes = searchSet.pop();
			
			// Search the current residue in mol.
			try {
				this.sqt.setSmarts(currentRes.getSmiles());
			} catch (CDKException e) {
				e.printStackTrace();
			}
			
			boolean isMatching = false;
			try {
				long time = System.currentTimeMillis();
				isMatching = this.sqt.matches(mol);
				if (verbose)
					System.out.println("    Search for " + currentRes.getName() + " in " + (System.currentTimeMillis()-time));
			} catch (CDKException e) {
				e.printStackTrace();
			}
			// If there is at least one occurrence.
			if (isMatching) {
				// Add matches to the coverage
				List<Match> matches = new ArrayList<>();
				for (List<Integer> lMatch : sqt.getMatchingAtoms()) {
					Match match = new Match(currentRes);
					for (int i : lMatch)
						match.addAtom(i);
					matches.add(match);
				}
				
				cov.addListMatches(currentRes, matches); // Change to compare.
				
				// Mark current residue to possibly add children.
				markSet.add(currentRes);
				
				// Add children with all parents marked
				Set<Residue> children = family.getChildrenOf(currentRes);
				for (Residue child : children) {
					boolean canBeAdded = true;
					for (Link dependance : family.getDepandances())
						if (dependance.getTo().equals(child))
							if (!markSet.contains(dependance.getFrom())) {
								canBeAdded = false;
								break;
						}
						
					if (canBeAdded)
						searchSet.add(child);
				}
			}
		}
		
		return cov;
	}

	@Override
	public void setChemicalObject(ChemicalObject co) {
		this.co = co;
	}

	@Override
	public void setAllowLightMatch(MatchingType type) {}

}
