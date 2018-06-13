package algorithms.isomorphism;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.ChemicalObject;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;

import algorithms.isomorphism.chains.Chain;
import algorithms.isomorphism.chains.Extension;
import algorithms.isomorphism.chains.Extension.BondMapping;
import algorithms.isomorphism.chains.MappedChain;

public class Isomorphism {
	
	public static boolean verbose = true;
	private static boolean storeMappings = false;
	private static Map<Chain, List<MappedChain>> mappings = new HashMap<>();
	private static MatchingType matchingType = null;
	private static ChemicalObject co;

	/**
	 * Search a list of mapped blocs from a smaller mapped bloc
	 * @param mb Initial mapped bloc
	 * @param ext Extension to the previous mapped bloc
	 * @return New mapped blocs.
	 */
	public static List<MappedChain> searchFromPreviousMapping (MappedChain mb, Extension ext, MatchingType type) {
		List<MappedChain> mbs = new ArrayList<>();
		
		ChemicalObject co = mb.getChemObject();
		IMolecule mol = co.getMolecule();
		
		List<Integer> neighbors = mb.getNeighborsBonds(mol);
		// Create a new bloc for each neighbor
		for (int idx : neighbors) {
			// Create bloc
			IBond nb = mol.getBond(idx);
			
			List<BondMapping> matchings = ext.match(nb, type);
			for (BondMapping bm : matchings) {
				int atomIdx0 = mol.getAtomNumber(bm.a0);
				int atomIdx1 = mol.getAtomNumber(bm.a1);
				int blocPosition0 = mb.getMappingIdx(atomIdx0);
				int blocPosition1 = mb.getMappingIdx(atomIdx1);
				int hydrogen0 = bm.h0;
				int hydrogen1 = bm.h1;


				Chain bloc = new Chain(mb.getChain(), ext, blocPosition0, blocPosition1);
				
				List<Integer> atoms = new ArrayList<>(mb.getAtomsMapping());
				if (blocPosition0 == -1)
					atoms.add(atomIdx0);
				if (blocPosition1 == -1)
					atoms.add(atomIdx1);
				
				List<Integer> bonds = new ArrayList<>(mb.getBondsMapping());
				bonds.add(mol.getBondNumber(nb));
				
				List<MatchingType> matchingTypes = new ArrayList<>(mb.getMatchings());
				matchingTypes.add(bm.matchingType);
				
				Map<Integer, Integer> hydrogens = new HashMap<>(mb.getHydrogensMapping());
				if (!hydrogens.containsKey(atomIdx0) || hydrogens.get(atomIdx0)<=hydrogen0)
					hydrogens.put(atomIdx0, hydrogen0);
				if (!hydrogens.containsKey(atomIdx1) || hydrogens.get(atomIdx1)<=hydrogen1)
					hydrogens.put(atomIdx1, hydrogen1);
				
				MappedChain newMb = new MappedChain(co, bloc, atoms, bonds, matchingTypes, hydrogens);
				
				if (!mbs.contains(newMb))
					mbs.add(newMb);
			}
		}
		
		return mbs;
	}/**/
	
	
	public static List<MappedChain> searchFromPreviousMapping (MappedChain mc, Extension ext, int idx1, int idx2, MatchingType type) {
		IMolecule mol = mc.getChemObject().getMolecule();
		int extIdx = idx1 == -1 ? idx2 : idx1;
		List<MappedChain> mappings = new ArrayList<>();
		
		// Connected bonds search
		List<IBond> connectedBonds = null;
		if (extIdx == -1) {
			connectedBonds = new ArrayList<>();
			for (IBond bond : mol.bonds())
				connectedBonds.add(bond);
		} else {
			IAtom extAtom = mol.getAtom(mc.getAtomsMapping().get(extIdx));
			connectedBonds = mol.getConnectedBondsList(extAtom);
		}
		
		for (IBond neighbor : connectedBonds) {
			// No need to search bonds already present in mapping.
			if (mc.getBondsMapping().contains(mol.getBondNumber(neighbor)))
				continue;
			
			List<BondMapping> bms = ext.match(neighbor, type);
			for (BondMapping bm : bms) {
				// Verification of the compatibility with previous matching
				if (idx1 != -1 &&
						mol.getAtomNumber(bm.a0) != mc.getAtomsMapping().get(idx1))
					continue;
				if (idx2 != -1 &&
						mol.getAtomNumber(bm.a1) != mc.getAtomsMapping().get(idx2))
					continue;
				
				// Creation of the new mapping
				Chain chain = new Chain(mc.getChain(), ext, idx1, idx2);
				List<Integer> atoms = new ArrayList<>(mc.getAtomsMapping());
				Map<Integer, Integer> hydrogens = new HashMap<>(mc.getHydrogensMapping());
				if (idx1 == -1) {
					int an = mol.getAtomNumber(bm.a0);
					// To avoid cycles where there is no cycle.
					if (mc.getAtomsMapping().contains(an))
						continue;
					atoms.add(an);
					hydrogens.put(an, bm.h0);
				}
				if (idx2 == -1) {
					int an = mol.getAtomNumber(bm.a1);
					// To avoid cycles where there is no cycle.
					if (mc.getAtomsMapping().contains(an))
						continue;
					atoms.add(an);
					hydrogens.put(an, bm.h1);
				}
				List<Integer> bonds = new ArrayList<>(mc.getBondsMapping());
				bonds.add(mol.getBondNumber(neighbor));
				List<MatchingType> matchings = new ArrayList<>(mc.getMatchings());
				matchings.add(bm.matchingType);
				MappedChain newMc = new MappedChain(mc.getChemObject(), chain, atoms, bonds, matchings, hydrogens);
				
				mappings.add(newMc);
			}
		}
		
		return mappings;
	}

	public static List<MappedChain> searchAChain (Chain bloc, ChemicalObject co, MatchingType type) {
		if (Isomorphism.storeMappings && (Isomorphism.matchingType != type || (Isomorphism.mappings.size() > 0 &&
				!Isomorphism.co.equals(co)))) {
			Isomorphism.mappings.clear();
			Isomorphism.co = co;
			Isomorphism.matchingType = type;
		}
		return searchAChainRecur(bloc, co, type);
	}/**/

	/**
	 * Search a bloc in a chemical object
	 * @param chain Searched bloc
	 * @param co Chemical object
	 * @return All mappings of bloc on co.
	 */
	public static List<MappedChain> searchAChainRecur (Chain chain, ChemicalObject co, MatchingType type) {
		if (Isomorphism.mappings.containsKey(chain))
			return Isomorphism.mappings.get(chain);
		
		List<MappedChain> mbs;
		List<MappedChain> newMbs = new ArrayList<>();
		
		if (chain.getSize() == 1) {
			mbs = new ArrayList<>();
			mbs.add(new MappedChain(co));
		} else {
			mbs = searchAChainRecur(chain.getSubBlc(), co, type);
		}
		
		for (MappedChain mb : mbs) {
			List<MappedChain> newMbsFromOne = searchFromPreviousMapping(mb, chain.getExt(), chain.getPosition1(), chain.getPosition2(), type);
			// Filter equals mappings
			for (MappedChain mc : newMbsFromOne)
				if (!newMbs.contains(mc))
					newMbs.add(mc);
		}
		
		if (Isomorphism.storeMappings)
			Isomorphism.mappings.put(chain, newMbs);
		return newMbs;
	}


	public static boolean existMapping(MappedChain mb, List<MappedChain> mbs) {
		for (MappedChain test : mbs) {
			if (mb.getChemObject().equals(test.getChemObject())
					&& test.getChain().getSmiles().equals(mb.getChain().getSmiles())
					&& test.getBondsMapping().containsAll(mb.getBondsMapping()))
				return true;
		}
		return false;
	}
	
	public static void setMappingStorage (boolean mappingStorage) {
		Isomorphism.storeMappings = mappingStorage;
		if (!mappingStorage)
			Isomorphism.mappings.clear();
	}
	
}
