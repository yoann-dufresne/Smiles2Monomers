package algorithms.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.ChemicalObject;
import model.Family;
import model.Monomer;
import model.Residue;
import model.graph.ContractedGraph;
import model.graph.MonomerGraph;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;

import db.FamilyDB;
import db.ResiduesDB;

public class Coverage implements Comparable<Coverage>, Cloneable {

	private ChemicalObject co;
	private List<Match> matches;
	private ResiduesDB residues;
	private IMolecule mol;
	
	private HashSet<Match> usedMatches;
	
	private Residue[] coverage;
	
	private Map<Residue, Integer> corrects;
	private Map<Residue, Integer> incorrects;
	private Map<String, Integer> notFound;
	private String id;
	private MonomerGraph monoGraph;
	private IMolecule currentMaskedMol;
	private boolean alreadyCalculate;
	private FamilyDB families;
	
	public Coverage(ChemicalObject co) {
		this.co = co;
		this.matches = new ArrayList<>();
		this.residues = new ResiduesDB ();
		this.mol = this.co.getMolecule();
		this.usedMatches = new HashSet<>();
		this.alreadyCalculate = false;
		
		this.coverage = new Residue[this.co.getSize()];
	}
	
	public void addMatch (Match match) {
		this.matches.add(match);
		Residue res = match.getResidue();
		this.residues.addObject(res.getSmiles(), res);
	}
	
	public void addMatches (List<Match> matches) {
		for (Match match : matches)
			this.addMatch(match);
	}

	public void addMatches(Coverage cov) {
		this.addMatches(cov.getMatches());
	}
	
	public void addListMatches (Residue res, List<Match> matches) {
		List<Match> matchesSet = new ArrayList<>();
		
		for (Match match : matches)
			matchesSet.add(new Match(match));
		
		this.addMatches(matchesSet);
	}
	
	public ChemicalObject getChemicalObject () {
		return this.co;
	}
	
	public void calculateGreedyCoverage () {
		this.alreadyCalculate = true;
		Collections.sort(this.matches);
		
		// Clear coverage
		this.coverage = new Residue[this.coverage.length];
		for (Match match : this.usedMatches) {
			for (int idx : match.getAtoms())
				if (this.coverage[idx] == null)
					this.coverage[idx] = match.getResidue();
				else {
					System.err.println("Impossible coverage");
				}
		}
		
		for (Match match : matches) {
			boolean possible = true;
			
			for (int i : match.getAtoms()) {
				if (this.coverage[i] != null) {
					possible = false;
					break;
				}
			}
			
			if (possible) {
				this.usedMatches.add(match);
				
				for (int i : match.getAtoms())
					this.coverage[i] = match.getResidue();
			}
		}
	}
	
	public double getCoverageRatio () {
		if (!this.alreadyCalculate)
			this.calculateGreedyCoverage();
		
		int coveredAtoms = 0;
		for (Match match : this.usedMatches)
			coveredAtoms += match.getAtoms().size();
		
		int pepAtoms = 0;
		for (@SuppressWarnings("unused") IAtom a : this.co.getMolecule().atoms())
			pepAtoms++;
		
		double ratio = new Double(coveredAtoms) / new Double(pepAtoms);
		return ratio;
	}

	public double getCorrectness(FamilyDB families) {
		if (!this.alreadyCalculate)
			this.calculateGreedyCoverage();
		
		if (this.corrects == null)
			this.calculateCorrectIncorrectNotFound(families);
		
		int pepAtoms = 0;
		for (@SuppressWarnings("unused") IAtom a : this.co.getMolecule().atoms())
			pepAtoms++;
		
		int corAtoms = 0;
		for (Residue res : this.corrects.keySet()) {
			int resAtoms = res.getMolecule().getAtomCount();
			corAtoms += resAtoms * this.corrects.get(res);
		}
		
		double ratio = new Double(corAtoms) / new Double(pepAtoms);
		return ratio;
	}
	
	public void removeUsedMatch (Residue res, Set<Integer> atoms) {
		Match toRemove = null;
		
		for (Match match : this.usedMatches)
			if (res.equals(match.getResidue()) && match.getAtoms().containsAll(atoms)) {
				toRemove = match;
				break;
			}
		
		if (toRemove != null) {
			this.usedMatches.remove(toRemove);
			for (int idx : atoms)
				this.coverage[idx] = null;
		}
	}
	
	public void removeUsedMatch (Match match) {
		if (this.usedMatches.contains(match)) {
			this.usedMatches.remove(match);
			for (int idx : match.getAtoms())
				this.coverage[idx] = null;
		}
	}
	
	public void addUsedMatch (Match match) {
		if (!this.usedMatches.contains(match)) {
			this.usedMatches.add(match);
			for (int idx : match.getAtoms())
				this.coverage[idx] = match.getResidue();
		}
	}
	
	public MonomerGraph getMonomericGraph (FamilyDB families) {
		if (this.monoGraph != null)
			return this.monoGraph;
		
		if (this.coverage == null)
			this.calculateGreedyCoverage();

		this.monoGraph = new ContractedGraph(this).toMonomerGraph(families);
		return this.monoGraph;
	}
	
	public IMolecule getMolecule(boolean explicitHydrogens) {
		if (explicitHydrogens)
			return mol;
		else
			return this.co.getMolecule();
	}
	
	public List<Match> getMatches() {
		return matches;
	}
	
	public int nbMatchesForCoverage() {
		return this.usedMatches.size();
	}
	
	public HashSet<Match> getUsedMatches() {
		return usedMatches;
	}

	public Map<String, Integer> getCorrectMonomers(FamilyDB families) {
		this.calculateCorrectIncorrectNotFound(families);
		
		Map<String, Integer> corrects = new HashMap<>();
		for (Residue res : this.corrects.keySet()) {
			Family fam = families.getObject(res.getMonoName());
			String name = fam.getShortName();
			int val = corrects.containsKey(name) ? corrects.get(name) : 0;
			corrects.put(name, val+this.corrects.get(res));
		}
		
		return corrects;
	}
	
	public Map<String, Integer> getIncorrectMonomers(FamilyDB families) {
		this.calculateCorrectIncorrectNotFound(families);
		
		Map<String, Integer> incorrects = new HashMap<>();
		for (Residue res : this.incorrects.keySet()) {
			Family fam = families.getObject(res.getMonoName());
			String name = fam.getShortName();
			int val = incorrects.containsKey(name) ? incorrects.get(name) : 0;
			incorrects.put(name, val+this.incorrects.get(res));
		}
		
		return incorrects;
	}

	public Map<String, Integer> getNotFoundMonomers(FamilyDB families) {
		this.calculateCorrectIncorrectNotFound(families);
		return this.notFound;
	}
	
	public void calculateCorrectIncorrectNotFound (FamilyDB families) {
		this.corrects = new HashMap<>();
		this.incorrects = new HashMap<>();
		this.notFound = new HashMap<>();
		
		List<Monomer> realMonos = new ArrayList<>();
		for (Monomer m : this.co.getGraph().nodes)
			realMonos.add(m);
		
		for (Match match : this.usedMatches) {
			Residue r = match.getResidue();
			
			// For each is correct ?
			boolean correct = false;
			Family fam = null;
			try {
				fam = families.getObject(r.getMonoName());
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			
			for (Monomer m : fam.getMonomers()) {
				if (realMonos.contains(m)) {
					correct = true;
					realMonos.remove(m);
					break;
				}
			}
			
			
			if (correct) {
				int nb = this.corrects.containsKey(r) ? this.corrects.get(r) : 0;
				this.corrects.put(r, nb+1);
			} else {
				int nb = this.incorrects.containsKey(r) ? this.incorrects.get(r) : 0;
				this.incorrects.put(r, nb+1);
			}
		}
		
		for (Monomer m : realMonos) {
			String name = null;
			try {
				Family fam = families.getObject(m.getName());
				name = fam.getShortName();
			} catch (NullPointerException e) {
				name = m.getName();
			}
			int nb = this.notFound.containsKey(name) ? this.notFound.get(name) : 0;
			this.notFound.put(name, nb+1);
		}
	}

	@Override
	public int compareTo(Coverage c) {
		int val = new Double (this.getCoverageRatio() * 1000).intValue() - new Double(c.getCoverageRatio() * 1000).intValue();
		
		if (val == 0 && this.families != null)
			val = new Double (this.getCorrectness(this.families) * 1000).intValue() - new Double(c.getCorrectness(this.families) * 1000).intValue();
		
		return val;
	}

	public String getId() {
		if (this.id != null)
			return this.id;
		
		this.id = this.co.getId();
		
		List<Integer> ids = new ArrayList<>(this.usedMatches.size());
		for (Match match : this.usedMatches)
			ids.add(new Integer(match.getResidue().getId()));
		Collections.sort(ids);
		
		for (int i : ids)
			this.id += "-" + i;
			
		return this.id;
	}
	
	public Coverage clone() {
		Coverage cov = new Coverage(co);
		cov.alreadyCalculate = this.alreadyCalculate;
		cov.matches.addAll(this.matches);
		cov.usedMatches.addAll(this.usedMatches);
		
		cov.monoGraph = this.monoGraph;
		
		cov.residues = new ResiduesDB();
		cov.residues.addDB(this.residues);
		
		return cov;
	}

	public void setId(String string) {
		this.id = string;
	}

	public void setCurrentMaskedMol(IMolecule mol) {
		this.currentMaskedMol = mol;
	}
	
	public IMolecule getCurrentMaskedMol() {
		return currentMaskedMol;
	}
	
	public void setFamilies(FamilyDB families) {
		this.families = families;
	}
	
}
