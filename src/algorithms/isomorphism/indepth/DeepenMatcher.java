package algorithms.isomorphism.indepth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;

import algorithms.utils.Coverage;
import algorithms.utils.Match;

/**
 * Matcher to go deeper in a previous coverage. For that we use a strategy for previous matchings movings and a "light" matching.
 * @author dufresne
 *
 */
public class DeepenMatcher {

	private DeepenMatcher() {}

	/**
	 * Transformation of mappings from tmp molecule to entire molecule.
	 */
	public static Match transform(Match match, IMolecule sub, IMolecule entire) {
		Match transformed = new Match(match.getResidue());
		
		// Atoms & Hydrogens
		for (int idx : match.getAtoms()) {
			IAtom a = sub.getAtom(idx);
			int realIdx = entire.getAtomNumber(a);
			transformed.addAtom(realIdx);
			transformed.addHydrogens(realIdx, match.getHydrogensFrom(idx));
		}
		
		// Bonds
		for (int idx : match.getBonds()) {
			IBond b = sub.getBond(idx);
			transformed.addBond(entire.getBondNumber(b));
		}
		
		return transformed;
	}

	/**
	 * Create a new molecule without covered atoms
	 */
	public static Molecule mask(Coverage coverage) {
		Molecule covMol = coverage.getChemicalObject().getMolecule();
		Molecule mol = new Molecule();
		
		List<Integer> coveredIdxs = DeepenMatcher.calculateCoveredAtoms(coverage);
		for (IAtom atom : covMol.atoms())
			if (!coveredIdxs.contains(covMol.getAtomNumber(atom)))
				mol.addAtom(atom);

		HashSet<IBond> bonds = new HashSet<>();
		for (IAtom atom : mol.atoms()) {
			for (IBond bond : covMol.getConnectedBondsList(atom))
				if (mol.contains(bond.getAtom(0)) && mol.contains(bond.getAtom(1))) {
					if (!bonds.contains(bond)) {
						mol.addBond(bond);
						bonds.add(bond);
					}
				}
		}
		
		return mol;
	}

	/**
	 * Create an ordered list of covered atoms indexes
	 */
	public static List<Integer> calculateCoveredAtoms(Coverage coverage) {
		List<Integer> covered = new ArrayList<>();

		for (Match match : coverage.getUsedMatches())
			covered.addAll(match.getAtoms());
		Collections.sort(covered);
		
		return covered;
	}
	
}
