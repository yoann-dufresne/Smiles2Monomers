package algorithms;

import org.openscience.cdk.interfaces.IMolecule;

import model.Family;
import model.OtherChemicalObject;
import model.Polymer;
import model.graph.ContractedGraph;
import algorithms.isomorphism.BlocsFamilyMatching;
import algorithms.isomorphism.FamilyMatcher;
import algorithms.isomorphism.MatchingType;
import algorithms.isomorphism.blocs.BlocsDB;
import algorithms.isomorphism.conditions.ConditionToExtend;
import algorithms.isomorphism.conditions.ExtendsNTimes;
import algorithms.isomorphism.indepth.DeepenMatcher;
import algorithms.isomorphism.indepth.ModulateByDistance;
import algorithms.isomorphism.indepth.ModulationStrategy;
import algorithms.utils.Coverage;
import algorithms.utils.Match;
import db.FamilyDB;

public class MonomericSpliting {

	public static boolean verbose = false;
	
	private Coverage coverage;
	private FamilyDB families;
	
	private boolean allowLightMatchs;
	
	private FamilyMatcher matcher;
	private ConditionToExtend condition;
	private ModulationStrategy modulation;

	public MonomericSpliting(FamilyDB families, BlocsDB blocs) {
		this.families = families;
		this.matcher = new BlocsFamilyMatching(blocs);
		this.modulation = new ModulateByDistance(3);
	}

	/**
	 * Calculate an object Coverage with all matches from families. 
	 * @param pep Peptide to match
	 */
	public void calculateCoverage(Polymer pep) {
		this.coverage = new Coverage(pep);
		this.matcher.setChemicalObject(pep);
		
		// Step 1 : Strict Matching
		this.MatchAllFamilies(MatchingType.STRONG);
		double ratio = this.coverage.getCoverageRatio();
		
		
		// conditions to go to light matching
		if (!this.allowLightMatchs || ratio == 1.0)
			return;
			
		
		// Step 2 : Light matching
		// Initialization
		// TODO : Why if/else ?
		if (this.condition == null)
			this.condition = new ExtendsNTimes(3);
		else
			this.condition.init();
		
		// Successive matchings
		while (this.coverage.getCoverageRatio() < 1 &&
				this.condition.toContinue(this.coverage)) {
			// Contract the atomic graph to monomeric graph
			ContractedGraph contracted = new ContractedGraph(this.coverage);
			// Remove monomers from the current solution to try with other matching strategy
			this.modulation.modulate(this.coverage, contracted);
			
			// Create a masked molecule to only search on free polymer areas
			IMolecule mol = DeepenMatcher.mask (this.coverage);
			this.coverage.setCurrentMaskedMol(mol);
			OtherChemicalObject tmp = new OtherChemicalObject(mol);
			this.matcher.setChemicalObject(tmp);
			
			// Compute for all families with ligth matching
			this.MatchAllFamilies(MatchingType.LIGHT);
			
			// Re-compute coverage
			this.coverage.calculateGreedyCoverage();
			this.modulation.nextLevel();
		}
	}
	
	/**
	 * Function to match independently all families onto the polymer.
	 * Can easily be parallelized
	 * @param matchType Strict/Light
	 */
	private void MatchAllFamilies (MatchingType matchType) {
		int nbFamilies = this.families.getFamilies().size();
		int i=1;
		String polName = this.coverage.getChemicalObject().getName(); 
		
		for (Family family : this.families.getFamilies()) {
			// Display
			if (verbose) {
				System.out.println("In " + polName);
				System.out.println("  Family " + i++ + "/" + nbFamilies);
			}
			
			// Time initialization
			long time = System.currentTimeMillis();
			
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
			
			if (verbose)
				System.out.println("  Search " + matchType.getClass().getCanonicalName() +
						" for family " + family.getName() + " in " + (System.currentTimeMillis()-time) + "\n");
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
