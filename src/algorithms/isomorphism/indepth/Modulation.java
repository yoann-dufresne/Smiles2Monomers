package algorithms.isomorphism.indepth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;

import algorithms.isomorphism.MatchingType;
import algorithms.utils.Coverage;
import algorithms.utils.Match;

public class Modulation {

	private int maxDepth;
	
	private Coverage best;

	private Map<Integer, Set<Match>> index;
	private Map<Integer, Set<Match>> usedIndex;

	private long startTime;
	public static long maxTime = 5;

	public Modulation(int maxDepth) {
		this.maxDepth = maxDepth;
	}
	
	public Coverage modulate (Coverage cov) {
		this.best = cov.clone();
		this.indexCoverage(cov);
		
		int penality = 0;
		do {
			this.startTime = System.currentTimeMillis();
			this.recursiveModulation (cov, new HashSet<Match>(), new HashSet<Integer>(), this.maxDepth-penality);
			penality++;
		} while ((System.currentTimeMillis() - this.startTime) / 1000
				> maxTime);
		
		return this.best;
	}

	private boolean recursiveModulation(Coverage cov, HashSet<Match> banned, HashSet<Integer> unmovable, int depth) {
		if (depth == 0 || (System.currentTimeMillis() - this.startTime) / 1000 > maxTime)
			return false;
		
		List<Match> matchsOnUncoveredAtoms = this.getMatchOnUncoveredAtoms(cov, banned, unmovable);
		int idx=0;
		for (Match match : matchsOnUncoveredAtoms) {
			if (idx++>=50)
				break;
			Set<Match> removed = null;
			try {
				removed = this.removeNReplaceMatches (match, cov, banned, unmovable);
			} catch (ImpossibleToReplace e) {
				continue;
			}
			
			if (cov.getCoverageRatio() == 1.0) {
				this.best = cov;
				return true;
			}
			
			if (this.recursiveModulation(cov, banned, unmovable, depth-1))
				return true;
			else {
				if (best.getCoverageRatio() < cov.getCoverageRatio())
					this.best = cov.clone();
				
				for (int atomIdx : match.getAtoms())
					unmovable.remove(atomIdx);
				banned.removeAll(removed);
				this.rollBack (cov, match, removed);
			}
		}
		
		return false;
	}

	/**
	 * Read a coverage and extract for each position all the possible matchings
	 * @param cov
	 * @return
	 */
	private void indexCoverage(Coverage cov) {
		this.index = new HashMap<>();
		this.usedIndex = new HashMap<>();
		
		IMolecule mol = cov.getChemicalObject().getMolecule();
		for (IAtom a : mol.atoms()) {
			this.index.put(mol.getAtomNumber(a), new HashSet<Match>());
			this.usedIndex.put(mol.getAtomNumber(a), new HashSet<Match>());
		}
		
		// Global index
		for (Match match : cov.getMatches())
			for (int i : match.getAtoms())
				this.index.get(i).add(match);
		
		// Used index
		for (Match match : cov.getUsedMatches())
			for (int i : match.getAtoms())
				this.usedIndex.get(i).add(match);
	}

	/**
	 * Get ordered matches from uncovered parts.
	 * The order depends on number on covering atoms and size of the matches
	 * @param cov
	 * @param contract
	 * @param banned 
	 * @return
	 */
	private List<Match> getMatchOnUncoveredAtoms(Coverage cov, Set<Match> banned, Set<Integer> unmovable) {
		List<Match> matches = new ArrayList<>();
		Set<Integer> uncovered = new HashSet<>();
		
		for (int atomIdx : this.usedIndex.keySet()) {
			if (this.usedIndex.get(atomIdx).size() == 0)
				uncovered.add(atomIdx);
		}
		
		Map<Match, Integer> uncoveredByMatch = new HashMap<>();
		for (int idx : uncovered) {
			for (Match match : this.index.get(idx)) {
				if (banned.contains(match) || !Collections.disjoint(match.getAtoms(), unmovable))
					continue;
				int val = uncoveredByMatch.containsKey(match) ? uncoveredByMatch.get(match) : 0;
				uncoveredByMatch.put(match, val+1);
				if (val == 0)
					matches.add(match);
			}
		}
		Collections.sort(matches, new ModulationComparator(uncoveredByMatch));
		
		return matches;
	}

	/**
	 * Remove matching incompatible with match and place it
	 * @param match
	 * @param cov
	 * @param banned 
	 * @param unmovable 
	 * @return
	 */
	private Set<Match> removeNReplaceMatches(Match match, Coverage cov, HashSet<Match> banned, HashSet<Integer> unmovable) throws ImpossibleToReplace {
		HashSet<Match> removed = new HashSet<>();
		//banned.add(match);
		unmovable.addAll(match.getAtoms());
		
		for (int idx : match.getAtoms()) {
			for (Match candidate : this.usedIndex.get(idx))
				if (!banned.contains(candidate)) {
					removed.add(candidate);
					banned.add(candidate);
				}
		}
		
		for (Match toRemove : removed) {
			cov.removeUsedMatch(toRemove);
			for (int idx : toRemove.getAtoms())
				this.usedIndex.get(idx).remove(toRemove);
		}
		
		cov.addUsedMatch(match);
		for (int idx : match.getAtoms())
			this.usedIndex.get(idx).add(match);
		
		return removed;
	}
	
	private void rollBack(Coverage cov, Match match, Set<Match> removed) {
		cov.removeUsedMatch(match);
		for (int idx : match.getAtoms())
			this.usedIndex.get(idx).remove(match);
		
		for (Match rem : removed) {
			cov.addUsedMatch(rem);
			for (int idx : rem.getAtoms())
				this.usedIndex.get(idx).add(rem);
		}
	}
	
	private class ModulationComparator implements Comparator<Match> {
		
		private Map<Match, Integer> uncoveredSize;

		private ModulationComparator(Map<Match, Integer> uncoveredSize) {
			this.uncoveredSize = uncoveredSize;
		}

		@Override
		public int compare(Match m1, Match m2) {
			int uncov = this.uncoveredSize.get(m2) - this.uncoveredSize.get(m1);
			if (uncov != 0)
				return uncov;
			
			int sizes = m2.size() - m1.size();
			if (sizes != 0)
				return sizes;
			
			int links = m2.getExtLinks().size() - m1.getExtLinks().size();
			if (links != 0)
				return links;
			
			int val1 = 0, val2 = 0;
			for (MatchingType mt : m1.getQualities().values())
				switch (mt) {
				case LIGHT:
					val1 += 1;
					break;
				case STRONG:
					val1 += 2;
					break;
				case EXACT:
					val1 += 3;
					break;
				default:
					break;
				}
			for (MatchingType mt : m2.getQualities().values())
				switch (mt) {
				case LIGHT:
					val2 += 1;
					break;
				case STRONG:
					val2 += 2;
					break;
				case EXACT:
					val2 += 3;
					break;
				default:
					break;
				}
			int qualities = val2 - val1;
			if (qualities != 0)
				return qualities;
			
			return 0;
		}
		
	}
	
}
