package libsTests;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.smiles.SmilesParser;

public class CDKMoleculesTests {

	private String smiles;
	private IMolecule imol;
	private File testFile;

	@Before
	public void setUp() throws Exception {
		this.testFile = new File("data_tests/pictureTest.png");
		if (this.testFile.exists())
			this.testFile.delete();
		
		this.smiles = "c1ccccc1";
		
		SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		this.imol = null;
		try {
			this.imol = sp.parseSmiles(this.smiles);
		} catch (InvalidSmilesException e) {
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void smilesLoadingTest() {
		SmilesGenerator sg = new SmilesGenerator();
		sg.setUseAromaticityFlag(true);
		
		assertEquals(this.smiles,sg.createSMILES(this.imol));
	}

}
