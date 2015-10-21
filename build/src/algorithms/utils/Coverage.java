package algorithms.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.ChemicalObject;
import model.Family;
import model.Monomer;
import model.Residue;
import model.graph.MonomerGraph;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;

import db.FamilyDB;
import db.ResiduesDB;

public class Coverage implements Comparable<Coverage> {

	private ChemicalObject co;
	private List<Match> matches;
	private ResiduesDB residues;
	private IMolecule mol;
	
	private List<Match> usedMatches;
	
	private Residue[] coverage;
	private int nbMatchesForCoverage;
	
	private Map<String, Integer> corrects;
	private Map<String, Integer> incorrects;
	private Map<String, Integer> notFound;
	private String id;
	private MonomerGraph monoGraph;
	private IMolecule currentMaskedMol;
	
	public Coverage(ChemicalObject co) {
		this.co = co;
		this.matches = new ArrayList<>();
		this.residues = new ResiduesDB ();
		this.mol = this.co.getMolecule();
		this.usedMatches = new ArrayList<>();
		
		this.nbMatchesForCoverage = 0;
		this.coverage = new Residue[this.co.getSize()];
		for (int i=0 ; i<this.co.getSize() ; i++)
			this.coverage[i] = null;
	}
	
	public void addMatch (Match match) {
		this.matches.add(match);
		Residue res = match.getResidue();
		this.residues.addObject(res.getSMILES(), res);
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
		Collections.sort(this.matches);
		
		for (Match match : matches) {
			boolean possible = true;
			
			System.out.println(match.getResidue().getName() + " :");
			System.out.println("Size : " + match.size());
			System.out.println("Links : " + match.getResidue().getLinks().size());
			System.out.println("Score : " + match.getResidue().getWeight());
			System.out.println("atoms : " + match.getAtoms());
			System.out.println("hydrogens : " + match.getHydrogens());
			System.out.println();
			
			for (int i : match.getAtoms()) {
				if (this.coverage[i] != null) {
					possible = false;
					break;
				}
			}
			
			if (possible) {
				this.nbMatchesForCoverage++;
				this.usedMatches.add(match);
				
				for (int i : match.getAtoms())
					this.coverage[i] = match.getResidue();
			}
		}
		
		//System.out.println("-- End coverage --\n");
	}
	
	public double getCoverageRatio () {
		if (this.usedMatches.size() == 0)
			this.calculateGreedyCoverage();
		
		int coveredAtoms = 0;
		for (Match match : this.usedMatches)
			coveredAtoms += match.getAtoms().size();
		
		int pepAtoms = 0;
		for (@SuppressWarnings("unused") IAtom a : this.co.getMolecule().atoms())
			pepAtoms++;
		
		return new Double(coveredAtoms) / new Double(pepAtoms);
	}
	
	public void removeUsedMatch (Residue res, Set<Integer> atoms) {
		for (int i=0 ; i<this.usedMatches.size() ; i++) {
			Match match = this.usedMatches.get(i);
			
			if (res.equals(match.getResidue()) && match.getAtoms().containsAll(atoms)) {
				this.usedMatches.remove(i);
				this.nbMatchesForCoverage -= 1;
				for (int idx : atoms)
					this.coverage[idx] = null;
				break;
			}
		}
	}
	
	public MonomerGraph getMonomericCoverage () {
		if (this.monoGraph != null)
			return this.monoGraph;
		
		if (this.coverage == null)
			this.calculateGreedyCoverage();
		
		this.monoGraph = new MonomerGraph(this);
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
		return nbMatchesForCoverage;
	}
	
	public void setNbMatchesForCoverage (int nb) {
		this.nbMatchesForCoverage = nb;
	}
	
	public List<Match> getUsedMatches() {
		return usedMatches;
	}

	public Map<String, Integer> getCorrectMonomers(FamilyDB families) {
		this.calculateCorrectIncorrectNotFound(families);
		return this.corrects;
	}
	
	public Map<String, Integer> getIncorrectMonomers(FamilyDB families) {
		this.calculateCorrectIncorrectNotFound(families);
		return this.incorrects;
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
				int nb = this.corrects.containsKey(r.getMonoName()) ? this.corrects.get(r.getMonoName()) : 0;
				this.corrects.put(r.getMonoName(), nb+1);
			} else {
				int nb = this.incorrects.containsKey(r.getMonoName()) ? this.incorrects.get(r.getMonoName()) : 0;
				this.incorrects.put(r.getMonoName(), nb+1);
			}
		}
		
		for (Monomer m : realMonos) {
			int nb = this.notFound.containsKey(m.getName()) ? this.notFound.get(m.getName()) : 0;
			this.notFound.put(m.getName(), nb+1);
		}
	}

	@Override
	public int compareTo(Coverage c) {
		return new Double (this.getCoverageRatio() * 1000).intValue() - new Double(c.getCoverageRatio() * 1000).intValue();
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

	public void setId(String string) {
		this.id = string;
	}

	public void setCurrentMaskedMol(IMolecule mol) {
		this.currentMaskedMol = mol;
	}
	
	public IMolecule getCurrentMaskedMol() {
		return currentMaskedMol;
	}
	
}
