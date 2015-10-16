package algorithms.isomorphism.chains;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.ChemicalObject;
import model.Family;
import model.Polymer;
import model.Residue;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;

import algorithms.isomorphism.Isomorphism;
import algorithms.isomorphism.MatchingType;
import algorithms.isomorphism.chains.Extension.BondMapping;
import db.FamilyDB;
import db.PolymersDB;

public class ChainLearning {

	public static boolean verbose;
	
	private PolymersDB learningBase;

	private int markovianSize;
	private ChainsIndex chains;
	private FrequencesIndex frequence;
	private PolymerMappings polymers;

	private ChainsIndex finalChains;
	private ChainsDB db;
	
	
	public ChainLearning(PolymersDB learningBase) {
		this.learningBase = learningBase;
		this.markovianSize = 3;
		this.chains = new ChainsIndex();
		this.finalChains = new ChainsIndex();
		this.frequence = new FrequencesIndex();
		this.polymers = new PolymerMappings();
		this.db = new ChainsDB();
	}


	/**
	 * Set value or the size of the sequences that will be compute with a markovian model.
	 * The rest of the sequence will be compute with a greedy algorithm.
	 * @param size Markovian model usage limit.
	 */
	public void setMarkovianSize(int size) {
		this.markovianSize = size;
	}


	/**
	 * Learn Sequences from families using the learning base.
	 * @param families families to index.
	 */
	public void learn(FamilyDB families) {
		this.chains.clear();
		this.finalChains.clear();
		this.db = new ChainsDB();
		
		List<Residue> roots = new ArrayList<>();
		for (Family family : families.getObjects()) {
			for (Residue root : family.getRoots()) {
				roots.add(root);
			}
		}
		
		// --- Roots ---
		// Init for size 1
		ResidueMappings residueIndex_1 = new ResidueMappings();
		this.initLearn (roots, residueIndex_1);
		
		// Markovian recursive
		ResidueMappings previous = null;
		ResidueMappings current = residueIndex_1;
		for (int size=2 ; size <= this.markovianSize ; size++) {
			previous = current;
			current = this.learnMarkovianNext(previous);
		}
		
		// Greedy recursive
		for (Residue root : current.keySet()) {
			MappedChain bestMarkov = this.getBest(current.get(root));
			MappedChain greedyMB = this.learnGreedy(bestMarkov);
			this.chains.put(greedyMB.getChain().getSmiles(), greedyMB.getChain());
			this.finalChains.put(greedyMB.getChemObject().getId(), greedyMB.getChain());
		}
		
		// Create index structures all over families.
		for (Family fam : families.getObjects()) {
			FamilyChainsDB fc = new FamilyChainsDB(fam);
			this.db.addObject(fam.getName(), fc);
			
			this.addAddsToSons (fc, fam);
		}
	}
	
	// -------------------------------- Initialization ----------------------------------

	private void initLearn (List<Residue> roots, ResidueMappings mappings) {
		for (Residue res : roots) {
			List<MappedChain> resMappings = new ArrayList<>();
			
			for (IBond bond : res.getMolecule().bonds()) {
				Extension ext = new Extension(bond);
				Chain bloc = new Chain(ext);
				String smiles = bloc.getMySmiles();
				this.chains.put(smiles, bloc);
				this.frequence.put(smiles, 0);
				
				// Creation of residue matchings (sequences of size 1)
				for (BondMapping bm : ext.match(bond, MatchingType.EXACT)) {
					MappedChain resMap = this.createMappingFromMatch(res, bond, bloc, bm);
					resMappings.add(resMap);
				}
			}
			
			mappings.put(res, resMappings);
		}
		
		this.frequencesInit();
	}

	/*
	 * Creation of an index of size 1
	 */
	private void frequencesInit() {
		for (Polymer pol : this.learningBase.getObjects())
			for (IBond bond : pol.getMolecule().bonds()) {
				// Create a bloc of size 1 for each root bond.
				Extension ext = new Extension(bond);
				Chain bloc = new Chain(ext);
				String smiles = bloc.getMySmiles();
				
				if (this.chains.containsKey(smiles)) {
					this.frequence.put(smiles, this.frequence.get(smiles)+1);
					
					// Create bond mappings
					Chain b = this.chains.get(smiles);
					//Extension.setAromacityTest(false);
					List<BondMapping> mappings = b.getExt().match(bond, MatchingType.EXACT);
					//Extension.setAromacityTest(true);
					
					// Save mappings on polymer
					List<MappedChain> mbs = this.polymers.containsKey(b) ? this.polymers.get(b) : new ArrayList<MappedChain>();
					for (BondMapping bm : mappings) {
						MappedChain polMap = this.createMappingFromMatch(pol, bond, b, bm);
						mbs.add(polMap);
					}
					this.polymers.put(bloc, mbs);
				}
			}
	}
	
	
	
	// --------------------------------- Markovian ------------------------------------
	
	private ResidueMappings learnMarkovianNext (ResidueMappings prevIndex) {
		ResidueMappings currentMappings = new ResidueMappings();
		
		// Create all extended blocs
		HashSet<Chain> blocs = new HashSet<>();
		for (Residue root : prevIndex.keySet()) {
			List<MappedChain> prevResIndex = prevIndex.get(root);
			List<MappedChain> resIndex = this.createBlocsFromPrevious(prevResIndex);
			if (resIndex.size() > 0)
				currentMappings.put(root, resIndex);
			else {
				currentMappings.put(root, prevResIndex);
				continue;
			}
			
			for (MappedChain mb : resIndex)
				blocs.add(mb.getChain());
		}
		
		// Filtering blocs
		for (Chain b : blocs) {
			String smiles = b.getMySmiles();
			
			if (!this.chains.containsKey(smiles)) {
				this.chains.put(smiles, b);
			}else {
				Chain rival = this.chains.get(smiles);
				double rivalScore = this.computeScore (rival);
				double score = this.computeScore (b);
				
				if (score < rivalScore) {
					this.chains.put(smiles, b);
					
					for (List<MappedChain> mbs : currentMappings.values()) {
						int size = mbs.size();
						for (int idx=size-1 ; idx>=0 ; idx--)
							if (mbs.get(idx).getChain().equals(rival))
								mbs.remove(idx);
					}
				} else {
					for (List<MappedChain> mbs : currentMappings.values()) {
						int size = mbs.size();
						for (int idx=size-1 ; idx>=0 ; idx--)
							if (mbs.get(idx).getChain().equals(b))
								mbs.remove(idx);
					}
				}
			}
			
			// Polymer matching and frequency computation for each new chain
			this.calculateFrequency (b);
		}
		
		return currentMappings;
	}
	
	
	// --------------------------------- Greedy ------------------------------------
	
	public MappedChain learnGreedy (MappedChain mb) {
		IMolecule mol = mb.getChemObject().getMolecule();
		
		List<Integer> neighbors = mb.getNeighborsBonds(mol);
		if (neighbors.size() == 0)
			return mb;
		
		Extension ext = this.getBestGreedyExt (mol, neighbors);
		MappedChain greedyMB = Isomorphism.searchFromPreviousMapping(mb, ext, MatchingType.EXACT).get(0);
		
		return this.learnGreedy(greedyMB);
	}


	private Extension getBestGreedyExt(IMolecule mol, List<Integer> neighbors) {
		Extension best = null;
		int bestScore = 1000000000;
		
		for (int bondId : neighbors) {
			Extension ext = new Extension(mol.getBond(bondId));
			Chain b = new Chain(ext);
			int score = this.frequence.get(b.getMySmiles());
			
			if (score <= bestScore) {
				best = ext;
				bestScore = score;
			}
		}
		
		return best;
	}


	// -------------------------------------- Children extensions -------------------------------------
	
	private void addAddsToSons(FamilyChainsDB fc, Family fam) {
		for (Residue root : fam.getRoots()) {
			fc.addRootChain(root, this.finalChains.get(root.getId()));
			Set<Residue> children = fam.getChildrenOf(root);
			
			this.recurChildrenAdds (fc, root, children);
		}
	}
	
	
	private void recurChildrenAdds(FamilyChainsDB fc, Residue from, Set<Residue> children) {
		for (Residue child : children) {
			List<ChainAdd> adds = fc.getAdds(child);
			
			// If extensions already exist, jump to the next son.
			if (adds.size() > 0)
				continue;
			
			List<MappedChain> mcs = this.calculateMapping (fc, child, from, new MappedChain(child));
			//Collections.sort(mcs, new MappedChainComparator());
			MappedChain mc = mcs.get(0);
			
			// Hydrogen adds
			for (int atomIdx : mc.getAtomsMapping()) {
				IAtom a = mc.getChemObject().getMolecule().getAtom(atomIdx);
				int chainIdx = mc.getAtomsMapping().indexOf(atomIdx);
				
				Map<Integer, Integer> mapping = mc.getHydrogensMapping();
				int mappingHydrogens = mapping.containsKey(atomIdx) ? mapping.get(atomIdx) : 0;
				int molHydrogens = a.getImplicitHydrogenCount();
				
				if (mappingHydrogens < molHydrogens)
					adds.add(new HydrogenAdd(from, chainIdx, molHydrogens-mappingHydrogens));
			}
			
			// Extensions
			IMolecule resMol = mc.getChemObject().getMolecule();
			for (IBond bond : resMol.bonds()) {
				int bondIdx = resMol.getBondNumber(bond);
				
				if (mc.getBondsMapping().contains(bondIdx))
					continue;
				
				Extension ext = new Extension(bond);
				List<MappedChain> tmpMappings = Isomorphism.searchFromPreviousMapping(mc, ext, MatchingType.EXACT);
				
				for (MappedChain tmp : tmpMappings)
					if (tmp.getBondsMapping().get(tmp.getBondsMapping().size()-1) == bondIdx) {
						Chain chain = tmp.getChain();
						BondAdd ba = new BondAdd(from, ext, chain.getPosition1(), chain.getPosition2());
						adds.add(ba);
					}
			}/**/
			
			this.recurChildrenAdds(fc, child, fc.getFamily().getChildrenOf(child));
		}
	}

	private List<MappedChain> calculateMapping(FamilyChainsDB fc, Residue res, Residue from, MappedChain mc) {
		if (this.finalChains.containsKey(res.getId())) {
			Chain chain = this.finalChains.get(res.getId());
			List<MappedChain> mappings = Isomorphism.searchAChain(chain, mc.getChemObject(), MatchingType.STRONG);
			return mappings;
		} else {
			Residue ancestor = null;
			if (!this.finalChains.containsKey(from.getId())) {
				List<ChainAdd> adds = fc.getAdds(from);
				ancestor = adds.get(0).getFrom();
			}
			List<MappedChain> newMcs = this.calculateMapping(fc, from, ancestor, mc);
			
			List<MappedChain> toRemove = new ArrayList<>();
			for (MappedChain newMc : newMcs)
				for (ChainAdd add : fc.getAdds(res)) {
					try {
						add.apply(newMc, MatchingType.STRONG);
					} catch (Exception e) {
						toRemove.add(newMc);
						break;
					}
				}
			for (MappedChain mcRem : toRemove)
				newMcs.remove(mcRem);
			
			return newMcs;
		}
	}


	// -------------------------------------- Utils -------------------------------------
	/*
	 * Create the mapping from extension match
	 */
	private MappedChain createMappingFromMatch(ChemicalObject co, IBond bond, Chain bloc, BondMapping bm) {
		List<Integer> atoms = new ArrayList<>();
		atoms.add(co.getMolecule().getAtomNumber(bm.a0));
		atoms.add(co.getMolecule().getAtomNumber(bm.a1));
		
		List<Integer> bonds = new ArrayList<>();
		bonds.add(co.getMolecule().getBondNumber(bond));
		
		List<MatchingType> types = new ArrayList<>();
		types.add(MatchingType.EXACT);
		
		Map<Integer, Integer> hydrogens = new HashMap<>();
		hydrogens.put(co.getMolecule().getAtomNumber(bm.a0), bm.h0);
		hydrogens.put(co.getMolecule().getAtomNumber(bm.a1), bm.h1);
		
		return new MappedChain(co, bloc, atoms, bonds, types, hydrogens);
	}
	
	private List<MappedChain> createBlocsFromPrevious (List<MappedChain> prevMbs) {
		List<MappedChain> nextMbs = new ArrayList<>();
		
		for (MappedChain mb : prevMbs) {
			
			ChemicalObject co = mb.getChemObject();
			IMolecule mol = co.getMolecule();
			
			List<Integer> neighbors = mb.getNeighborsBonds(mol);
			// Create a new bloc for each neighbor
			for (int idx : neighbors) {
				// Create bloc
				IBond nb = mol.getBond(idx);
				Extension ext = new Extension(nb);
				
				//Extension.setAromacityTest(false);
				List<MappedChain> newMbs = Isomorphism.searchFromPreviousMapping (mb, ext, MatchingType.EXACT);
				//Extension.setAromacityTest(true);
				
				nextMbs.addAll(newMbs);
			}

		}

		return nextMbs;
	}
	
	
	private MappedChain getBest (List<MappedChain> mbs) {
		MappedChain best = null;
		int bestScore = 1000000000;
		
		for (MappedChain mb : mbs) {
			int score = this.computeScore(mb.getChain());
			if (score <= bestScore) {
				best = mb;
				bestScore = score;
			}
		}
		
		return best;
	}
	
	/**
	 * Compute an estimation of search time for the given chain
	 * @param chain Chain to evaluate.
	 * @return Evaluation of time.
	 */
	public int computeScore(Chain chain) {
		if (chain.getSize() == 1)
			return 1;
		else {
			return this.computeScore(chain.getSubBlc()) +
					this.frequence.get(chain.getSubBlc().getMySmiles()) * chain.getPrevArity();
		}
	}
	
	/**
	 * 
	 * @param b
	 */
	private void calculateFrequency(Chain b) {
		// Impossible to found a bloc with no presence of its subBloc in the learning base.
		if (this.frequence.get(b.getSubBlc().getMySmiles()) == 0) {
			this.frequence.put(b.getMySmiles(), 0);
			return;
		}
		
		// Extension of sub-mappings and calculation of frequencies
		List<MappedChain> subMappings = this.polymers.get(b.getSubBlc());
		List<MappedChain> mappings = new ArrayList<>();
		int nbMatchs = 0;
		for (MappedChain mb : subMappings) {
			List<MappedChain> newMbs = Isomorphism.searchFromPreviousMapping(mb, b.getExt(), b.getPosition1(), b.getPosition2(), MatchingType.EXACT);
			mappings.addAll(newMbs);
			nbMatchs += newMbs.size();
		}
		this.frequence.put(b.getMySmiles(), nbMatchs);
		this.polymers.put(b, mappings);
	}


	// -------------------------------------- Misc ------------------------------------
	public ChainsIndex getAllChains() {
		return chains;
	}
	
	public ChainsIndex getFinalChains() {
		return this.finalChains;
	}
	
	public FrequencesIndex getFrequence() {
		return frequence;
	}
	
	public ChainsDB getDb() {
		return db;
	}


	// Need to stock by residue the mapped blocs
	@SuppressWarnings("serial")
	public class ResidueMappings extends HashMap<Residue, List<MappedChain>> {};
	// Need to stock for each blocs all the learning polymer mappings (for future extensions and count)
	@SuppressWarnings("serial")
	public class PolymerMappings extends HashMap<Chain, List<MappedChain>> {};
	// Best chains for each root residue
	@SuppressWarnings("serial")
	public class ChainsIndex extends HashMap<String, Chain> {};
	// Pattern (smiles) frequency in learning database.
	@SuppressWarnings("serial")
	public class FrequencesIndex extends HashMap<String, Integer> {};
	
	/*private class MappedChainComparator implements Comparator<MappedChain> {

		@Override
		public int compare(MappedChain mc1, MappedChain mc2) {
			int size1 = mc1.getAtomsMapping().size();
			for (int val : mc1.getHydrogensMapping().values())
				size1 += val;
			
			int size2 = mc2.getAtomsMapping().size();
			for (int val : mc2.getHydrogensMapping().values())
				size2 += val;
			
			return size2 - size1;
		}
		
	}*/
}
