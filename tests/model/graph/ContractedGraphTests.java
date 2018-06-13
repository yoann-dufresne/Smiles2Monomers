package model.graph;

import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PolymersJsonLoader;
import io.loaders.json.RulesJsonLoader;

import java.util.Set;

import model.Polymer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import algorithms.MonomericSpliting;
import algorithms.ResidueCreator;
import algorithms.isomorphism.chains.ChainLearning;
import algorithms.isomorphism.chains.ChainsDB;
import algorithms.utils.Coverage;
import db.FamilyDB;
import db.MonomersDB;
import db.PolymersDB;
import db.RulesDB;

public class ContractedGraphTests {

	private Coverage coverage;
	private ContractedGraph contractedGraph;

	@Before
	public void setUp() throws Exception {
		MonomersDB monos = new MonomersJsonLoader().loadFile("data_tests/monos.json");
		PolymersDB peps = new PolymersJsonLoader(monos).loadFile("data_tests/peps.json");
		RulesDB rules = RulesJsonLoader.loader.loadFile("data_tests/rules.json");
		
		ResidueCreator rc = new ResidueCreator(rules);
		FamilyDB families = rc.createResidues(monos);
		
		ChainLearning cl = new ChainLearning(peps);
		cl.setMarkovianSize(3);
		cl.learn(families);
		ChainsDB chains = cl.getDb();
		
		MonomericSpliting ms = new MonomericSpliting(families, chains, 2, 2, 3);
		Polymer pol = peps.getObject("633");
		ms.computeCoverage(pol);
		this.coverage = ms.getCoverage();
		this.coverage.calculateGreedyCoverage();
		
		this.contractedGraph = new ContractedGraph(coverage);
	}
	
	@Test
	public void contractionFromCoverageTest() {
		@SuppressWarnings("rawtypes")
		Set abg = this.contractedGraph.vertexSet();
		Assert.assertTrue(abg.size() == 7);
	}

}
