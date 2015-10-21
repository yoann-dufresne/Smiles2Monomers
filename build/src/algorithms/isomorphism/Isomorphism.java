package algorithms.isomorphism;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.ChemicalObject;

import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;

import algorithms.isomorphism.blocs.Bloc;
import algorithms.isomorphism.blocs.Extension;
import algorithms.isomorphism.blocs.MappedBloc;
import algorithms.isomorphism.blocs.Extension.BondMapping;

public class Isomorphism {
	
	public static boolean verbose = true;

	/**
	 * Search a list of mapped blocs from a smaller mapped bloc
	 * @param mb Initial mapped bloc
	 * @param ext Extension to the previous mapped bloc
	 * @return New mapped blocs.
	 */
	public static List<MappedBloc> searchFromPreviousMapping (MappedBloc mb, Extension ext, MatchingType type) {
		List<MappedBloc> mbs = new ArrayList<>();
		
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


				Bloc bloc = new Bloc(mb.getBloc(), ext, blocPosition0, blocPosition1);
				
				//System.out.println("  " + mb);
				//System.out.println("  " + ext);
				//System.out.println("  " + bloc);
				
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
				
				MappedBloc newMb = new MappedBloc(co, bloc, atoms, bonds, matchingTypes, hydrogens);
				
				if (!mbs.contains(newMb))
					mbs.add(newMb);
			}
		}
		
		return mbs;
	}/**/

	public static List<MappedBloc> searchABloc (Bloc bloc, ChemicalObject co, MatchingType type) {
		return filter (searchABlocRecur(bloc, co, type));
	}/**/
	
	/**
	 * Filter a list of mapping saving only one mapping over same bonds and atoms
	 * @param mappings
	 * @return
	 */
	public static List<MappedBloc> filter(List<MappedBloc> mappings) {
		List<MappedBloc> filtered = new ArrayList<>();
		
		for (MappedBloc mb : mappings)
			if (!existMapping(mb, filtered))
				filtered.add(mb);
		
		return filtered;
	}

	/**
	 * Search a bloc in a chemical object
	 * @param bloc Searched bloc
	 * @param co Chemical object
	 * @return All mappings of bloc on co.
	 */
	public static List<MappedBloc> searchABlocRecur (Bloc bloc, ChemicalObject co, MatchingType type) {
		List<MappedBloc> mbs;
		List<MappedBloc> newMbs = new ArrayList<>();
		
		if (bloc.getSize() == 1) {
			mbs = new ArrayList<>();
			mbs.add(new MappedBloc(co));
		} else {
			mbs = searchABlocRecur(bloc.getSubBlc(), co, type);
			//System.out.println("go");
		}
		
		
		for (MappedBloc mb : mbs) {
			List<MappedBloc> newMbsFromOne = searchFromPreviousMapping(mb, bloc.getExt(), type);
			//System.out.println(bloc);
			for (MappedBloc newMb : newMbsFromOne) {
				//System.out.println(newMb);
				if (newMb.getBloc().getSerial().equals(bloc.getSerial()))
					newMbs.add(newMb);
			}
			//System.out.println();
		}
		
		/*if (verbose) {
			System.out.println("recherche : " + bloc.getSerial());
			if (newMbs.size() == 0)
				System.out.println("Nothing");
			for (MappedBloc mb : newMbs)
				System.out.println(mb);
			System.out.println();
		}*/
		
		return newMbs;
	}


	public static boolean existMapping(MappedBloc mb, List<MappedBloc> mbs) {
		for (MappedBloc test : mbs) {
			if (mb.getChemObject().equals(test.getChemObject())
					&& test.getBloc().getSmiles().equals(mb.getBloc().getSmiles())
					&& test.getBondsMapping().containsAll(mb.getBondsMapping()))
				return true;
		}
		return false;
	}
	
}
