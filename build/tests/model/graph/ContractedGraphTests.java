package model.graph;

import static org.junit.Assert.assertTrue;
import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PeptideJsonLoader;
import io.loaders.json.RulesJsonLoader;
import model.Polymer;

import org.junit.Before;
import org.junit.Test;

import algorithms.MonomericSpliting;
import algorithms.ResidueCreator;
import algorithms.isomorphism.blocs.BlocsDB;
import algorithms.isomorphism.blocs.BlocsLearning;
import algorithms.utils.Coverage;
import db.FamilyDB;
import db.MonomersDB;
import db.PeptidesDB;
import db.RulesDB;

public class ContractedGraphTests {

	private Coverage coverage;
	private ContractedGraph contractedGraph;

	@Before
	public void setUp() throws Exception {
		MonomersDB monos = MonomersJsonLoader.loader.loadFile("data_tests/monos.json");
		PeptidesDB peps = new PeptideJsonLoader(monos).loadFile("data_tests/peps.json");
		RulesDB rules = RulesJsonLoader.loader.loadFile("data_tests/rules.json");
		
		ResidueCreator rc = new ResidueCreator(rules);
		FamilyDB families = rc.createResidues(monos);
		
		BlocsLearning bl = new BlocsLearning(families, peps);
		bl.searchForSize(4);
		BlocsDB blocs = bl.getBlocs();
		
		MonomericSpliting ms = new MonomericSpliting(families, blocs);
		Polymer pep = peps.getObject("" + 633);
		ms.calculateCoverage(pep);
		this.coverage = ms.getCoverage();
		this.coverage.calculateGreedyCoverage();
		
		this.contractedGraph = new ContractedGraph(coverage);
	}
	
	@Test
	public void contractionFromCoverageTest() {
		assertTrue(this.contractedGraph.vertexSet().size() == 7);
	}

}
