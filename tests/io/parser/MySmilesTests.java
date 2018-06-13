package io.parser;

import io.parsers.MySMILES;
import io.parsers.SmilesConverter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IMolecule;

public class MySmilesTests {
	
	private MySMILES ms;

	@Before
	public void setUp() throws Exception {
		this.ms = new MySMILES();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void oxydoAromaticTest() {
		String smiles = "c1ccccc1O";
		String result = "O(c(c(c(c(c[1]))))(c[1]))";
		
		IMolecule mol = null;
		try {
			mol = SmilesConverter.conv.transform(smiles);
		} catch (InvalidSmilesException e) {
			e.printStackTrace();
		}
		
		Assert.assertEquals(result, this.ms.convert(mol, false));
	}
	
	@Test
	public void doubleLinkTest() {
		String smiles = "CC(=O)O";
		String result = "C(=O)(C)(O)";
		
		IMolecule mol = null;
		try {
			mol = SmilesConverter.conv.transform(smiles);
		} catch (InvalidSmilesException e) {
			e.printStackTrace();
		}
		
		Assert.assertEquals(result, this.ms.convert(mol, false));
	}

}
