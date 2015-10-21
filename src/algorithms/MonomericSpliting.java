package algorithms;

import org.openscience.cdk.Molecule;

import algorithms.isomorphism.ChainsFamilyMatching;
import algorithms.isomorphism.FamilyMatcher;
import algorithms.isomorphism.Isomorphism;
import algorithms.isomorphism.MatchingType;
import algorithms.isomorphism.chains.ChainsDB;
import algorithms.isomorphism.conditions.ConditionToExtend;
import algorithms.isomorphism.conditions.ExtendsNTimes;
import algorithms.isomorphism.indepth.DeepenMatcher;
import algorithms.isomorphism.indepth.Modulation;
import algorithms.isomorphism.indepth.RemoveByDistance;
import algorithms.isomorphism.indepth.RemoveStrategy;
import algorithms.utils.Coverage;
import algorithms.utils.Match;
import db.FamilyDB;
import db.PolymersDB;
import model.Family;
import model.OtherChemicalObject;
import model.Polymer;
import model.graph.ContractedGraph;

public class MonomericSpliting {

	public static boolean verbose = false;
	
	private Coverage coverage;
	private FamilyDB families;
	
	private boolean allowLightMatchs;
	
	private FamilyMatcher matcher;
	private ConditionToExtend condition;
	private RemoveStrategy remover;
	private Modulation modulation;

	private int retry;
	
	public MonomericSpliting(FamilyDB families, ChainsDB chains, int removeDistance, int retryCount, int modulationDepth) {
		this.families = families;
		this.matcher = new ChainsFamilyMatching(chains);
		this.remover = new RemoveByDistance(removeDistance);
		this.modulation = new Modulation(modulationDepth);
		this.retry = retryCount;
	}
	
	public Coverage[] computeCoverages (PolymersDB polDB) {
		Coverage[] covs = new Coverage[polDB.size()];
		
		int idx=0;
		for (Polymer pol : polDB.getObjects()) {
			this.computeCoverage(pol);
			covs[idx] = this.getCoverage();
			idx += 1;
		}
		
		return covs;
	}

	/**
	 * Calculate an object Coverage with all matches from families. 
	 * @param pep Peptide to match
	 */
	public void computeCoverage(Polymer pep) {
		this.coverage = new Coverage(pep);
		this.matcher.setChemicalObject(pep);
		Isomorphism.setMappingStorage(true);
		
		// Step 1 : Strict Matching
		if (verbose) {
			System.out.println("+Strict matching");
			System.out.println("++Search residues");
		}
		this.matchAllFamilies(MatchingType.STRONG);
		double ratio = this.coverage.getCoverageRatio();
		if (ratio < 1.0) {
			if (verbose)
				System.out.println("++Modulation");
			this.coverage = this.modulation.modulate(this.coverage);
		}
		Coverage save = this.coverage.clone();
		
		// conditions to go to light matching
		if (!this.allowLightMatchs || ratio == 1.0) {
			Isomorphism.setMappingStorage(false);
			return;
		}
			
		
		// Step 2 : Light matching
		// Initialization
		// TODO : Why if/else ?
		if (this.condition == null)
			this.condition = new ExtendsNTimes(this.retry);
		else
			this.condition.init();
		
		this.remover.init();
		// Successive matchings
		int depth = 0;
		while (this.coverage.getCoverageRatio() < 1.0 &&
				this.condition.toContinue(this.coverage)) {
			depth++;
			// Contract the atomic graph to monomeric graph
			ContractedGraph contracted = new ContractedGraph(this.coverage);
			// Remove monomers from the current solution to try with other matching strategy
			this.remover.remove(this.coverage, contracted);
			
			// Create a masked molecule to only search on free polymer areas
			Molecule mol = DeepenMatcher.mask (this.coverage);
			this.coverage.setCurrentMaskedMol(mol);
			OtherChemicalObject tmp = new OtherChemicalObject(mol);
			this.matcher.setChemicalObject(tmp);
			
			if (verbose) {
				System.out.println("+Light matching, depth " + depth);
				System.out.println("++Search residues");
			}
			// Compute for all families with ligth matching
			this.matchAllFamilies(MatchingType.LIGHT);
			
			// Re-compute coverage
			this.coverage.calculateGreedyCoverage();
			if (this.coverage.getCoverageRatio() < 1.0) {
				if (verbose)
					System.out.println("++Modulation");
				this.coverage = this.modulation.modulate(this.coverage);
			}
			
			if (this.coverage.getCoverageRatio() > save.getCoverageRatio())
				save = this.coverage.clone();
			
			this.remover.nextLevel();
		}
		Isomorphism.setMappingStorage(false);
		
		if (save.getCoverageRatio() > this.coverage.getCoverageRatio())
			this.coverage = save;
	}
	
	/**
	 * Function to match independently all families onto the polymer.
	 * Can easily be parallelized
	 * @param matchType Strict/Light
	 */
	private void matchAllFamilies (MatchingType matchType) {
		/*int nbFamilies = this.families.getFamilies().size();
		int i=1;
		String polName = this.coverage.getChemicalObject().getName();/**/
		
		for (Family family : this.families.getFamilies()) {
			/*/ Display
			if (verbose) {
				System.out.println("In " + polName);
				System.out.println("  Family " + i++ + "/" + nbFamilies);
			}/**/
			
			// Time initialization
			//long time = System.currentTimeMillis();
			
			// Matching
			this.matcher.setAllowLightMatch(matchType);
			Coverage cov = this.matcher.matchFamilly(family);
			
			if (matchType.equals(MatchingType.LIGHT)) {
				for (Match match : cov.getMatches()) {
					Match transformed = DeepenMatcher.transform(match, this.coverage.getCurrentMaskedMol(),
							this.coverage.getChemicalObject().getMolecule());
					
					this.coverage.addMatch(transformed);
				}
			} else
				this.coverage.addMatches(cov);
			
			/*if (verbose)
				System.out.println("  Search " + matchType.getClass().getCanonicalName() +
						" for family " + family.getName() + " in " + (System.currentTimeMillis()-time) + "\n");/**/
		}
	}
	

	public Coverage getCoverage() {
		return this.coverage;
	}
	
	public FamilyDB getFamilies() {
		return families;
	}
	
	public void setAllowLightMatchs(boolean allowLightMatchs) {
		this.allowLightMatchs = allowLightMatchs;
	}
	
	public void setConditionToExtend (ConditionToExtend condition) {
		this.condition = condition;
	}

	public static void setVerbose(boolean verbose) {
		MonomericSpliting.verbose = verbose;
	}
	
}
