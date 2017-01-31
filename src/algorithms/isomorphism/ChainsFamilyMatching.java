package algorithms.isomorphism;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.openscience.cdk.interfaces.IAtom;

import model.ChemicalObject;
import model.Family;
import model.Residue;
import model.Rule;
import algorithms.isomorphism.chains.Chain;
import algorithms.isomorphism.chains.ChainAdd;
import algorithms.isomorphism.chains.ChainsDB;
import algorithms.isomorphism.chains.FamilyChainsDB;
import algorithms.isomorphism.chains.MappedChain;
import algorithms.utils.Coverage;
import algorithms.utils.Match;

public class ChainsFamilyMatching implements FamilyMatcher {
	
	private ChemicalObject co;
	private Coverage coverage;
	
	public Map<Residue, MappedChain> residuesOrdered;
	private MatchingType matchingType;
	private ChainsDB chains;
	
	private Queue<Residue> toMatch;
	private Map<Residue, List<MappedChain>> mappings;
	

	public ChainsFamilyMatching(ChainsDB chains) {
		this.chains = chains;
		this.toMatch = new LinkedList<>();
		this.mappings = new HashMap<>();
	}
	
	@Override
	public Coverage matchFamilly(Family family) {
		if (this.co == null)
			return null;
		this.coverage = new Coverage(co);
		FamilyChainsDB fc = this.chains.getObject(family.getJsonName());
		
		this.toMatch.addAll(family.getRoots());
		while (!toMatch.isEmpty()) {
			Residue res = toMatch.remove();
			List<MappedChain> mcs = null;
			List<ChainAdd> adds = fc.getAdds(res);
			
			// If the residue is a root
			if (adds.size() == 0) {
				Chain rootChain = fc.getRootChains().get(res);
				mcs = Isomorphism.searchAChain(rootChain, this.co, this.matchingType);
			}
			// From previous mapping
			else {
				mcs = new ArrayList<>();
				Residue from = adds.get(0).getFrom();
				for (MappedChain mc : mappings.get(from)) {
					try {
						MappedChain clone = mc;
						for (ChainAdd add : adds)
							clone = add.applyAndClone(mc, this.matchingType);
						mcs.add(clone);
					} catch (Exception e) {}
				}
			}
			
			// Save results and recursive add
			if (mcs.size() > 0) {
				this.mappings.put(res, mcs);
				this.addToCoverage (family, mcs, res);
				
				for (Residue child : fc.getFamily().getChildrenOf(res)) {
					if (fc.getAdds(child).get(0).getFrom().equals(res)) {
						this.toMatch.add(child);
					}
				}
			}
		}

		this.mappings.clear();
		return this.coverage;
	}

	private void addToCoverage(Family family, List<MappedChain> mcs, Residue res) {
		for (MappedChain mc : mcs)
			this.addToCoverage(family, mc, res);
	}

	private void addToCoverage(Family family, MappedChain mc, Residue res) {
		Match match = new Match(res);
		
		match.addAtoms(mc.getAtomsMapping());
		match.addBonds(mc.getBondsMapping());
		match.addHydrogens(mc.getHydrogensMapping());
		for (int idx=0 ; idx<mc.getMatchings().size() ; idx++) {
			match.addQuality(mc.getBondsMapping().get(idx), mc.getMatchings().get(idx));
		}
		
		// Compute external bonds by aligning chain on residue
		// 1- Retrieve the chain on root residue
		int current = 200;
		List<MappedChain> onResidue = null;
		Residue rootMol = null;
		for (Residue root : family.getRoots()) {
			List<MappedChain> mcs = Isomorphism.searchAChain(mc.getChain(), root, MatchingType.EXACT);
			if (mcs.size() > 0 && current > mcs.size()) {
				current = mcs.size();
				onResidue = mcs;
				rootMol = root;
			}
		}
		// 2- Look for atoms of interest
		MappedChain resMapping = onResidue.get(0);
		for (IAtom ia : rootMol.getAtomicLinks().keySet()) {
			Rule r = rootMol.getAtomicLinks().get(ia);
			
			int resMolIdx = rootMol.getMolecule().getAtomNumber(ia);
			int chainIdx = resMapping.getAtomsMapping().indexOf(resMolIdx);
			
			match.addExtLink(mc.getAtomsMapping().get(chainIdx), r);
		}
		
		// Add the match
		if (!this.coverage.getMatches().contains(match))
			this.coverage.addMatch(match);
	}

	@Override
	public void setChemicalObject(ChemicalObject co) {
		this.co = co;
	}

	@Override
	public void setAllowLightMatch(MatchingType type) {
		this.matchingType = type;
	}
	
	

}
