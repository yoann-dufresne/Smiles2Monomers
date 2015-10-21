package algorithms.isomorphism;

import static org.junit.Assert.fail;
import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PeptideJsonLoader;
import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import algorithms.isomorphism.blocs.Bloc;
import algorithms.isomorphism.blocs.SequenceLearning;
import algorithms.isomorphism.blocs.SequenceLearning.ChainsIndex;
import algorithms.isomorphism.blocs.SequenceLearning.FrequencesIndex;
import db.FamilyDB;
import db.MonomersDB;
import db.PeptidesDB;
import db.RulesDB;

public class SequenceLearningTests {
	
	private MonomersDB monos;
	private PeptidesDB learningBase;
	private FamilyDB families;
	
	private HashMap<String, Integer> size1Frequencies;
	private HashMap<String, Integer> size3Frequencies;
	private HashMap<String, String> size3Blocs;

	@Before
	public void setUp() throws Exception {
		// Loading test monomers
		MonomersDB premonos = MonomersJsonLoader.loader.loadFile("data_tests/monos.json");
		
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
		PeptideJsonLoader pjl = new PeptideJsonLoader(premonos);
		PeptidesDB base = pjl.loadFile("data_tests/peps.json");
		// Creation of learning base with only 1 polymer.
		this.learningBase = new PeptidesDB();
		this.learningBase.addObject("AM-toxin II", base.getObject("306"));
		
		// Construct reference
		this.constructReference();
	}
	
	public void constructReference () {
		// Size 1 frequencies
		this.size1Frequencies = new HashMap<String, Integer>();
		
		this.size1Frequencies.put("C=O", 4);
		this.size1Frequencies.put("[H]C([H])O", 0);
		this.size1Frequencies.put("[H]CC", 5);
		this.size1Frequencies.put("[H]CN[H]", 2);
		this.size1Frequencies.put("[H]NC([H])[H]", 0);
		this.size1Frequencies.put("[H]CC([H])[H]", 1);
		
		// Size 3 frequencies and blocs
		this.size3Blocs = new HashMap<>();
		this.size3Frequencies = new HashMap<>();
		
		this.size3Frequencies.put("[H]NC([H])C([H])([H])O", 0);
		this.size3Blocs.put("[H]NC([H])C([H])([H])O", "C,0,O,2,0,-1,-1;C,0,C,1,2,-1,0;C,0,N,1,1,2,-1");
		this.size3Frequencies.put("[H]C(C)C([H])([H])O", 0);
		this.size3Blocs.put("[H]C(C)C([H])([H])O", "C,0,O,2,0,-1,-1;C,0,C,1,2,-1,0;C,0,C,0,1,-1,2");
		this.size3Frequencies.put("[H]NC([H])C=O", 2);
		this.size3Blocs.put("[H]NC([H])C=O", "C,0,N,1,1,-1,-1;C,0,C,0,1,-1,0;C,1,O,0,0,2,-1");
		this.size3Frequencies.put("[H]C([H])C([H])C=O", 1);
		this.size3Blocs.put("[H]C([H])C([H])C=O", "C,0,C,1,2,-1,-1;C,0,C,0,1,-1,0;C,1,O,0,0,2,-1");
		this.size3Frequencies.put("[H]NC([H])C([H])([H])N[H]", 0);
		this.size3Blocs.put("[H]NC([H])C([H])([H])N[H]", "C,0,N,2,1,-1,-1;C,0,C,1,2,-1,0;C,0,N,1,1,2,-1");
		this.size3Frequencies.put("[H]NC([H])([H])C([H])C", 0);
		this.size3Blocs.put("[H]NC([H])([H])C([H])C", "C,0,N,2,1,-1,-1;C,0,C,1,2,-1,0;C,0,C,0,1,-1,2");
		this.size3Frequencies.put("[H]NC([H])(C)C([H])[H]", 1);
		this.size3Blocs.put("[H]NC([H])(C)C([H])[H]", "C,0,C,1,2,-1,-1;C,0,N,1,1,0,-1;C,0,C,0,1,-1,0");
	}
	
	

	@Test
	public void generateSize1Test() {
		// Computing
		SequenceLearning learning = new SequenceLearning(this.learningBase);
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
	public void generateSize2Test() {
		// Computing
		SequenceLearning learning = new SequenceLearning(this.learningBase);
		learning.setMarkovianSize(3);
		learning.learn(this.families);
		FrequencesIndex frequences = learning.getFrequence();
		ChainsIndex chains = learning.getSequences();
		
		// Verification
		for (String pattern : chains.keySet()) {
			Bloc chain = chains.get(pattern);
			if (chain.getSize() < 3)
				continue;
			
			if (!this.size3Frequencies.containsKey(pattern) ||
					this.size3Frequencies.get(pattern) != frequences.get(pattern) ||
					!this.size3Blocs.get(pattern).equals(chain.getSerial()))
				fail(pattern + " : " + frequences.get(pattern) + " -> " + this.size3Blocs.get(pattern));
		}
		
		Assert.assertTrue(frequences.size() == 19);
	}/**/

}
