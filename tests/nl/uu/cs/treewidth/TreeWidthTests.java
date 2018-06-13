package nl.uu.cs.treewidth;

import static org.junit.Assert.assertEquals;
import io.loaders.json.PolymersJsonLoader;
import io.parsers.SmilesConverter;
import model.Polymer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.exception.InvalidSmilesException;

import db.MonomersDB;
import db.PolymersDB;

public class TreeWidthTests {

	private PolymersDB pepDB;

	@Before
	public void setUp() throws Exception {
		String pepDBname = "data_tests/peps.json";
		
		// Loading databases
		MonomersDB monoDB = new MonomersDB();
		PolymersJsonLoader pcl = new PolymersJsonLoader(monoDB);
		this.pepDB = pcl.loadFile(pepDBname);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		MolTreeWidth mtw = new MolTreeWidth();
		
		Polymer pep = null;
		try {
			pep = this.pepDB.getObject("250");
		} catch (NullPointerException e) {
			e.printStackTrace();
		} 
		
		try {
			assertEquals(2, mtw.calculateTreeWidth(SmilesConverter.conv.transform(pep.getSmiles())));
		} catch (InvalidSmilesException e) {
			System.err.println("Impossible to parse smiles");
		}
	}

}
