package db;

import static org.junit.Assert.assertEquals;
import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PolymersJsonLoader;
import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;
import io.parsers.SmilesConverter;
import model.Monomer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IMolecule;

public class LoadersTests {

	private MonomersDB jsonMonos;
	private RulesDB jsonRules;
	private FamilyDB jsonFamilies;

	@Before
	public void setUp() throws Exception {
		this.jsonMonos = new MonomersJsonLoader().loadFile("data_tests/monos.json");
		this.jsonRules = RulesJsonLoader.loader.loadFile("data_tests/rules.json");
		
		ResidueJsonLoader rjl = new ResidueJsonLoader(jsonRules, this.jsonMonos);
		this.jsonFamilies = rjl.loadFile("data_tests/res.json");
	}

	@After
	public void tearDown() throws Exception {
	}

	// ----------- Monomers -----------
	@Test
	public void completeMonomersLoaderJson() {
		assertEquals(this.jsonMonos.size(), 10);
	}
	
	
	// ----------- Peptides -----------
	@Test
	public void completePeptidesLoader() {
		PolymersJsonLoader loader = new PolymersJsonLoader(new MonomersDB());
		PolymersDB db = loader.loadFile("data_tests/peps.json");
		
		assertEquals(4, db.size());
	}
	
	@Test
	public void addaTest() {
		String smiles = "CC(C=C(C)C=CC(C(C)C(=O)O)N)C(CC1=CC=CC=C1)OC";
		try {
			smiles = SmilesConverter.conv.toCanonicalSmiles(smiles);
		} catch (InvalidSmilesException e) {
			System.err.println("Impossible to parse " + smiles);
		}
		Monomer adda = new Monomer("Adda", "Adda", smiles);
		IMolecule mol = adda.getMolecule();
		
		Assert.assertTrue("O=C(O)C(C)C(N)C=CC(=CC(C)C(OC)Cc1ccccc1)C".equals(SmilesConverter.conv.mol2Smiles(mol)));
	}
	
	// ----------- Rules -----------
	@Test
	public void rulesLoadersComparison () {
		Assert.assertTrue(this.jsonRules.size() == 7);
	}
	
	// ----------- Rules -----------
	@Test
	public void ResiduesLoadersComparison () {
		Assert.assertTrue(this.jsonFamilies.size() == 10);
	}

}
