package algorithms.isomorphism;

import static org.junit.Assert.fail;
import io.loaders.json.FamilyChainIO;
import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PolymersJsonLoader;
import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import model.Residue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import algorithms.isomorphism.chains.Chain;
import algorithms.isomorphism.chains.ChainLearning;
import algorithms.isomorphism.chains.ChainLearning.ChainsIndex;
import algorithms.isomorphism.chains.ChainLearning.FrequencesIndex;
import algorithms.isomorphism.chains.ChainsDB;
import algorithms.isomorphism.chains.FamilyChainsDB;
import db.FamilyDB;
import db.MonomersDB;
import db.PolymersDB;
import db.RulesDB;

public class ChainLearningTests {
	
	private MonomersDB monos;
	private PolymersDB learningBase;
	private FamilyDB families;
	
	private HashMap<String, Integer> size1Frequencies;
	private HashMap<String, Integer> size3Frequencies;
	private HashMap<String, String> size3Blocs;
	private HashMap<String, String> greedyPatterns;
	private HashMap<Object, Object> markovPatterns;

	@Before
	public void setUp() throws Exception {
		// Loading test monomers
		MonomersDB premonos = new MonomersJsonLoader().loadFile("data_tests/monos.json");
		
		// Creation of a subset of monos
		this.monos = new MonomersDB();
		this.monos.addObject("Dpr", premonos.getObject("Dpr"));
		this.monos.addObject("D-Ser", premonos.getObject("D-Ser"));
		
		// Loading test rules
		RulesDB rules = RulesJsonLoader.loader.loadFile("data_tests/rules.json");
		
		// Loading test residues
		ResidueJsonLoader rjl = new ResidueJsonLoader(rules, this.monos);
		this.families = rjl.loadFile("data_tests/res.json");
		
		// Loading test polymers
		PolymersJsonLoader pjl = new PolymersJsonLoader(premonos);
		PolymersDB base = pjl.loadFile("data_tests/peps.json");
		// Creation of learning base with only 1 polymer.
		this.learningBase = new PolymersDB();
		this.learningBase.addObject("AM-toxin II", base.getObject("306"));
		
		// Construct reference
		this.constructReference();
	}
	
	public void constructReference () {
		// Size 1 frequencies
		this.size1Frequencies = new HashMap<String, Integer>();
		
		this.size1Frequencies.put("C(=O)", 4);
		this.size1Frequencies.put("C(H)(H)(O)", 0);
		this.size1Frequencies.put("C(C(H))", 3); // aromatics ok 5 if not
		this.size1Frequencies.put("C(H)(N(H))", 2);
		this.size1Frequencies.put("C(H)(H)(N(H))", 0);
		this.size1Frequencies.put("C(C(H)(H))(H)", 1);
		
		// Size 3 frequencies and blocs
		this.size3Blocs = new HashMap<>();
		this.size3Frequencies = new HashMap<>();
		
		this.size3Frequencies.put("C(C(H)(H)(O))(H)(N(H))", 0);
		this.size3Blocs.put("C(C(H)(H)(O))(H)(N(H))", "C,0,O,2,0,-1,-1;C,0,C,1,2,-1,0;C,0,N,1,1,2,-1");
		this.size3Frequencies.put("C(C(C(H)(H)(O))(H))", 0);
		this.size3Blocs.put("C(C(C(H)(H)(O))(H))", "C,0,O,2,0,-1,-1;C,0,C,1,2,-1,0;C,0,C,0,1,-1,2");
		this.size3Frequencies.put("C(=O)(C(H)(N(H)))", 2);
		this.size3Blocs.put("C(=O)(C(H)(N(H)))", "C,0,N,1,1,-1,-1;C,0,C,0,1,-1,0;C,1,O,0,0,2,-1");
		this.size3Frequencies.put("C(=O)(C(C(H)(H))(H))", 1);
		this.size3Blocs.put("C(=O)(C(C(H)(H))(H))", "C,0,C,1,2,-1,-1;C,0,C,0,1,-1,0;C,1,O,0,0,2,-1");
		this.size3Frequencies.put("C(C(H)(H)(N(H)))(H)(N(H))", 0);
		this.size3Blocs.put("C(C(H)(H)(N(H)))(H)(N(H))", "C,0,N,2,1,-1,-1;C,0,C,1,2,-1,0;C,0,N,1,1,2,-1");
		
		this.size3Frequencies.put("C(C(C(H)(H)(N(H)))(H))", 0);
		this.size3Blocs.put("C(C(C(H)(H)(N(H)))(H))", "C,0,N,2,1,-1,-1;C,0,C,1,2,-1,0;C,0,C,0,1,-1,2");

		this.size3Frequencies.put("C(C(C(H)(H))(H)(N(H)))", 1);
		this.size3Blocs.put("C(C(C(H)(H))(H)(N(H)))", "C,0,C,1,2,-1,-1;C,0,N,1,1,0,-1;C,0,C,0,1,-1,0");		
		//                   C(C(C(H)(H))(H)(N(H)))    C,0,C,1,2,-1,-1;C,0,C,0,1,-1,0;C,0,N,1,1,0,-1
		
		
		this.greedyPatterns = new HashMap<>();
		this.greedyPatterns.put("87", "C,0,O,2,0,-1,-1;C,0,C,1,2,-1,0;C,0,C,0,1,-1,2;C,0,N,1,1,2,-1;C,1,O,0,0,3,-1");
		this.greedyPatterns.put("86", "C,0,C,1,2,-1,-1;C,0,N,1,1,0,-1;C,0,C,0,1,-1,0;C,1,O,0,0,3,-1");
		this.greedyPatterns.put("95", "C,0,N,2,1,-1,-1;C,0,C,1,2,-1,0;C,0,N,1,1,2,-1;C,0,C,0,1,-1,2;C,1,O,0,0,4,-1");
		
		this.markovPatterns = new HashMap<>();
		this.markovPatterns.put("87", "C,0,O,2,0,-1,-1;C,0,C,1,2,-1,0;C,0,N,1,1,2,-1;C,0,C,0,1,-1,2;C,1,O,0,0,4,-1");
		this.markovPatterns.put("86", "C,0,C,1,2,-1,-1;C,0,N,1,1,0,-1;C,0,C,0,1,-1,0;C,1,O,0,0,3,-1");
		this.markovPatterns.put("95", "C,0,N,2,1,-1,-1;C,0,C,1,2,-1,0;C,0,C,0,1,-1,2;C,0,N,1,1,2,-1;C,1,O,0,0,3,-1");
	}
	
	

	@Test
	public void generateSize1Test() {
		// Computing
		ChainLearning learning = new ChainLearning(this.learningBase);
		learning.setMarkovianSize(1);
		learning.learn(this.families);
		FrequencesIndex frequences = learning.getFrequence();
		
		
		// Verification
		for (String pattern : frequences.keySet()) {
			if (!this.size1Frequencies.containsKey(pattern) ||
					this.size1Frequencies.get(pattern) != frequences.get(pattern))
				fail(pattern + " : " + frequences.get(pattern));
		}
		
		Assert.assertTrue(frequences.size() == 6);
	}
	
	@Test
	public void generateSize3Test() {
		// Computing
		ChainLearning learning = new ChainLearning(this.learningBase);
		learning.setMarkovianSize(3);
		learning.learn(this.families);
		FrequencesIndex frequences = learning.getFrequence();
		ChainsIndex chains = learning.getAllChains();
		
		// Verification
		for (String pattern : chains.keySet()) {
			Chain chain = chains.get(pattern);
			if (chain.getSize() != 3)
				continue;
			
			if (!this.size3Frequencies.containsKey(pattern) ||
					this.size3Frequencies.get(pattern) != frequences.get(pattern) ||
					!this.size3Blocs.get(pattern).equals(chain.getSerial())) {
				System.out.println(pattern);
				System.out.println(chain.getSerial());
				fail(pattern + " : " + frequences.get(pattern) + " -> " + this.size3Blocs.get(pattern));
			}
		}
		
		Assert.assertTrue(frequences.size() == 19);
	}/**/
	
	@Test
	public void generateGreedyTest() {
		// Computing
		ChainLearning learning = new ChainLearning(this.learningBase);
		learning.setMarkovianSize(3);
		learning.learn(this.families);
		ChainsIndex chains = learning.getFinalChains();
		
		// Verification
		for (String idx : chains.keySet()) {
			Chain chain = chains.get(idx);
			
			if (!this.greedyPatterns.containsKey(idx) ||
					!this.greedyPatterns.get(idx).equals(chain.getSerial()))
				fail(idx + " not found");/**/	
		}
		
		Assert.assertTrue(chains.size() == 3);
	}/**/
	
	@Test
	public void generateMarkov6Test() {
		// Computing
		ChainLearning learning = new ChainLearning(this.learningBase);
		learning.setMarkovianSize(6);
		learning.learn(this.families);
		ChainsIndex chains = learning.getFinalChains();
		
		// Verification
		for (String pattern : chains.keySet()) {
			Chain chain = chains.get(pattern);
			
			if (!this.markovPatterns.containsKey(pattern) ||
					!this.markovPatterns.get(pattern).equals(chain.getSerial()))
				fail(pattern + " not found");/**/	
		}
		
		Assert.assertTrue(chains.size() == 3);
	}/**/
	
	@Test
	public void saveTest() {
		String line = null;
		String expected = "[{\"roots\":{\"86\":\"C,0,C,1,2,-1,-1;C,0,N,1,1,0,-1;C,0,C,0,1,-1,0;C,1,O,0,0,3,-1\",\"87\":\"C,0,O,2,0,-1,-1;C,0,C,1,2,-1,0;C,0,C,0,1,-1,2;C,0,N,1,1,2,-1;C,1,O,0,0,3,-1\"},\"family\":\"D-Ser\",\"extensions\":{\"79\":[{\"idx2\":-1,\"idx1\":3,\"from\":\"85\",\"type\":\"extension\",\"ext\":\"C,0,O,0,1\"}],\"78\":[{\"idx2\":-1,\"idx1\":3,\"from\":\"84\",\"type\":\"extension\",\"ext\":\"C,0,O,0,1\"}],\"77\":[{\"num\":1,\"idx\":1,\"from\":\"84\",\"type\":\"hydrogen\"}],\"82\":[{\"idx2\":-1,\"idx1\":3,\"from\":\"87\",\"type\":\"extension\",\"ext\":\"C,0,O,0,1\"}],\"83\":[{\"idx2\":-1,\"idx1\":3,\"from\":\"86\",\"type\":\"extension\",\"ext\":\"C,0,O,0,1\"}],\"80\":[{\"idx2\":-1,\"idx1\":3,\"from\":\"81\",\"type\":\"extension\",\"ext\":\"C,0,O,0,1\"}],\"81\":[{\"num\":1,\"idx\":1,\"from\":\"87\",\"type\":\"hydrogen\"}],\"86\":[],\"87\":[],\"84\":[{\"num\":1,\"idx\":4,\"from\":\"87\",\"type\":\"hydrogen\"}],\"85\":[{\"num\":1,\"idx\":2,\"from\":\"86\",\"type\":\"hydrogen\"}]}},{\"roots\":{\"95\":\"C,0,N,2,1,-1,-1;C,0,C,1,2,-1,0;C,0,N,1,1,2,-1;C,0,C,0,1,-1,2;C,1,O,0,0,4,-1\"},\"family\":\"Dpr\",\"extensions\":{\"95\":[],\"94\":[{\"idx2\":-1,\"idx1\":4,\"from\":\"95\",\"type\":\"extension\",\"ext\":\"C,0,O,0,1\"}],\"93\":[{\"num\":1,\"idx\":3,\"from\":\"95\",\"type\":\"hydrogen\"}],\"92\":[{\"num\":1,\"idx\":1,\"from\":\"95\",\"type\":\"hydrogen\"}],\"91\":[{\"idx2\":-1,\"idx1\":4,\"from\":\"93\",\"type\":\"extension\",\"ext\":\"C,0,O,0,1\"}],\"90\":[{\"idx2\":-1,\"idx1\":4,\"from\":\"92\",\"type\":\"extension\",\"ext\":\"C,0,O,0,1\"}],\"89\":[{\"num\":1,\"idx\":1,\"from\":\"93\",\"type\":\"hydrogen\"}]}}]";
		
		// Computing
		ChainLearning learning = new ChainLearning(this.learningBase);
		learning.setMarkovianSize(3);
		learning.learn(this.families);
		
		ChainsDB db = learning.getDb();
		FamilyChainIO io = new FamilyChainIO(this.families);
		
		io.saveFile(db, "data_tests/chains.json");
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("data_tests/chains.json")));
			line = br.readLine();
			br.close();
		} catch (IOException e) {
			fail(e.getMessage());
		}
		
		Assert.assertTrue(line.equals(expected));
	}/**/
	
	@Test
	public void loadTest() {
		// Computing
		ChainLearning learning = new ChainLearning(this.learningBase);
		learning.setMarkovianSize(3);
		learning.learn(this.families);
		
		ChainsDB db = learning.getDb();
		FamilyChainIO io = new FamilyChainIO(this.families);
		
		io.saveFile(db, "data_tests/chains.json");
		ChainsDB loaded = io.loadFile("data_tests/chains.json");
		
		if (loaded.getObjects().size() != db.getObjects().size())
			fail("Not the same number of objects");
		
		FamilyChainsDB fc = db.getObjects().get(0);
		FamilyChainsDB fcLoaded = loaded.getObjects().get(0);
		
		if (fc.getRootChains().size() != fcLoaded.getRootChains().size())
			fail ("Root chain number different");
		
		for (Residue res : fc.getFamily().getResidues()) {
			if (fc.getAdds(res).size() != fcLoaded.getAdds(res).size())
				fail("Adds of " + res.getName() + "are not correctly loaded");
		}
		
		Assert.assertTrue(true);
	}/**/

}
