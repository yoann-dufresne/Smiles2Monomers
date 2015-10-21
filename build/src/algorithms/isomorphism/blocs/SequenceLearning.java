package algorithms.isomorphism.blocs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import model.ChemicalObject;
import model.Family;
import model.Polymer;
import model.Residue;

import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;

import algorithms.isomorphism.Isomorphism;
import algorithms.isomorphism.MatchingType;
import algorithms.isomorphism.blocs.Extension.BondMapping;
import db.FamilyDB;
import db.PeptidesDB;

public class SequenceLearning {

	public static boolean verbose;
	
	private PeptidesDB learningBase;

	private int markovianSize;
	private ChainsIndex chains;
	private FrequencesIndex frequence;
	private PolymerMappings polymers;
	
	
	public SequenceLearning(PeptidesDB learningBase) {
		this.learningBase = learningBase;
		this.markovianSize = 3;
		this.chains = new ChainsIndex();
		this.frequence = new FrequencesIndex();
		this.polymers = new PolymerMappings();
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
			MappedBloc bestMarkov = this.getBest(current.get(root));
			MappedBloc bestGreedy = this.learnGreedy(bestMarkov);
			System.out.println(bestGreedy.getBloc().getSmiles());
			System.out.println(bestGreedy.getChemObject().getSMILES());
			System.out.println();
		}
		
		// TODO : --- Sons ---
	}
	
	// -------------------------------- Initialization ----------------------------------
	
	private void initLearn (List<Residue> roots, ResidueMappings mappings) {
		for (Residue res : roots) {
			List<MappedBloc> resMappings = new ArrayList<>();
			
			for (IBond bond : res.getMolecule().bonds()) {
				Extension ext = new Extension(bond);
				Bloc bloc = new Bloc(ext);
				String smiles = bloc.getSmiles();
				this.chains.put(smiles, bloc);
				this.frequence.put(smiles, 0);
				
				// Creation of residue matchings (sequences of size 1)
				for (BondMapping bm : ext.match(bond, MatchingType.EXACT)) {
					MappedBloc resMap = this.createMappingFromMatch(res, bond, bloc, bm);
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
				Bloc bloc = new Bloc(ext);
				String smiles = bloc.getSmiles();
				
				if (this.chains.containsKey(smiles)) {
					this.frequence.put(smiles, this.frequence.get(smiles)+1);
					
					// Create bond mappings
					Bloc b = this.chains.get(smiles);
					//Extension.setAromacityTest(false);
					List<BondMapping> mappings = b.getExt().match(bond, MatchingType.EXACT);
					//Extension.setAromacityTest(true);
					
					// Save mappings on polymer
					List<MappedBloc> mbs = this.polymers.containsKey(b) ? this.polymers.get(b) : new ArrayList<MappedBloc>();
					for (BondMapping bm : mappings) {
						MappedBloc polMap = this.createMappingFromMatch(pol, bond, b, bm);
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
		HashSet<Bloc> blocs = new HashSet<>();
		for (Residue root : prevIndex.keySet()) {
			List<MappedBloc> prevResIndex = prevIndex.get(root);
			List<MappedBloc> resIndex = this.createBlocsFromPrevious(prevResIndex);
			currentMappings.put(root, resIndex);
			
			for (MappedBloc mb : resIndex)
				blocs.add(mb.getBloc());
		}
		
		// Filtering blocs
		for (Bloc b : blocs) {
			String smiles = b.getSmiles();
			
			if (!this.chains.containsKey(smiles)) {
				this.chains.put(smiles, b);
			}else {
				Bloc rival = this.chains.get(smiles);
				double rivalScore = this.computeScore (rival);
				double score = this.computeScore (b);
				
				if (score < rivalScore) {
					this.chains.put(smiles, b);
					
					for (List<MappedBloc> mbs : currentMappings.values()) {
						int size = mbs.size();
						for (int idx=size-1 ; idx>=0 ; idx--)
							if (mbs.get(idx).getBloc().equals(rival))
								mbs.remove(idx);
					}
				} else {
					for (List<MappedBloc> mbs : currentMappings.values()) {
						int size = mbs.size();
						for (int idx=size-1 ; idx>=0 ; idx--)
							if (mbs.get(idx).getBloc().equals(b))
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
	
	public MappedBloc learnGreedy (MappedBloc mb) {
		IMolecule mol = mb.getChemObject().getMolecule();
		
		List<Integer> neighbors = mb.getNeighborsBonds(mol);
		if (neighbors.size() == 0)
			return mb;
		
		Extension ext = this.getBestGreedyExt (mol, neighbors);
		return Isomorphism.searchFromPreviousMapping(mb, ext, MatchingType.EXACT).get(0);
	}


	private Extension getBestGreedyExt(IMolecule mol, List<Integer> neighbors) {
		Extension best = null;
		int bestScore = 1000000000;
		
		for (int bondId : neighbors) {
			Extension ext = new Extension(mol.getBond(bondId));
			Bloc b = new Bloc(ext);
			int score = this.frequence.get(b.getSmiles());
			
			if (score <= bestScore) {
				best = ext;
				bestScore = score;
			}
		}
		
		return best;
	}


	// -------------------------------------- Utils -------------------------------------
	/*
	 * Create the mapping from extension match
	 */
	private MappedBloc createMappingFromMatch(ChemicalObject co, IBond bond, Bloc bloc, BondMapping bm) {
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
		
		return new MappedBloc(co, bloc, atoms, bonds, types, hydrogens);
	}
	
	private List<MappedBloc> createBlocsFromPrevious (List<MappedBloc> prevMbs) {
		List<MappedBloc> nextMbs = new ArrayList<>();
		
		for (MappedBloc mb : prevMbs) {
			
			ChemicalObject co = mb.getChemObject();
			IMolecule mol = co.getMolecule();
			
			List<Integer> neighbors = mb.getNeighborsBonds(mol);
			// Create a new bloc for each neighbor
			for (int idx : neighbors) {
				// Create bloc
				IBond nb = mol.getBond(idx);
				Extension ext = new Extension(nb);
				
				//Extension.setAromacityTest(false);
				List<MappedBloc> newMbs = Isomorphism.searchFromPreviousMapping (mb, ext, MatchingType.EXACT);
				//Extension.setAromacityTest(true);
				
				nextMbs.addAll(newMbs);
			}

		}

		return nextMbs;
	}
	
	
	private MappedBloc getBest (List<MappedBloc> mbs) {
		MappedBloc best = null;
		int bestScore = 1000000000;
		
		for (MappedBloc mb : mbs) {
			int score = this.computeScore(mb.getBloc());
			if (score <= bestScore) {
				best = mb;
				bestScore = score;
			}
		}
		
		return best;
	}
	
	/**
	 * Compute an estimation of search time for the given chain
	 * @param bloc Chain to evaluate.
	 * @return Evaluation of time.
	 */
	public int computeScore(Bloc bloc) {
		if (bloc.getSize() == 1)
			return 1;
		else {
			return this.computeScore(bloc.getSubBlc()) +
					this.frequence.get(bloc.getSubBlc().getSmiles()) * bloc.getPrevArity();
		}
	}
	
	/**
	 * 
	 * @param b
	 */
	private void calculateFrequency(Bloc b) {
		// Impossible to found a bloc with no presence of its subBloc in the learning base.
		if (this.frequence.get(b.getSubBlc().getSmiles()) == 0) {
			this.frequence.put(b.getSmiles(), 0);
			return;
		}
		
		// Extension of sub-mappings and calculation of frequencies
		List<MappedBloc> subMappings = this.polymers.get(b.getSubBlc());
		List<MappedBloc> mappings = new ArrayList<>();
		int nbMatchs = 0;
		for (MappedBloc mb : subMappings) {
			List<MappedBloc> newMbs = Isomorphism.searchFromPreviousMapping(mb, b.getExt(), MatchingType.EXACT);
			mappings.addAll(newMbs);
			nbMatchs += newMbs.size();
		}
		this.frequence.put(b.getSmiles(), nbMatchs);
		this.polymers.put(b, mappings);
	}


	// -------------------------------------- Misc ------------------------------------
	public ChainsIndex getSequences() {
		return chains;
	}
	
	public FrequencesIndex getFrequence() {
		return frequence;
	}


	// Need to stock by residue the mapped blocs
	@SuppressWarnings("serial")
	public class ResidueMappings extends HashMap<Residue, List<MappedBloc>> {};
	// Need to stock for each blocs all the learning polymer mappings (for future extensions and count)
	@SuppressWarnings("serial")
	public class PolymerMappings extends HashMap<Bloc, List<MappedBloc>> {};
	// Best chains for each smiles
	@SuppressWarnings("serial")
	public class ChainsIndex extends HashMap<String, Bloc> {};
	// Pattern (smiles) frequency in learning database.
	@SuppressWarnings("serial")
	public class FrequencesIndex extends HashMap<String, Integer> {};
}
