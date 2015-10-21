package nl.uu.cs.treewidth;

import static org.junit.Assert.assertEquals;
import io.loaders.csv.PeptideCsvLoader;
import io.parsers.SmilesConverter;
import model.Polymer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.exception.InvalidSmilesException;

import db.MonomersDB;
import db.PeptidesDB;

public class TreeWidthTests {

	private PeptidesDB pepDB;

	@Before
	public void setUp() throws Exception {
		String pepDBname = "data_tests/peps.csv";
		
		// Loading databases
		MonomersDB monoDB = new MonomersDB();
		PeptideCsvLoader pcl = new PeptideCsvLoader(monoDB);
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
			assertEquals(2, mtw.calculateTreeWidth(SmilesConverter.conv.transform(pep.getSMILES())));
		} catch (InvalidSmilesException e) {
			System.err.println("Impossible to parse smiles");
		}
	}

}
