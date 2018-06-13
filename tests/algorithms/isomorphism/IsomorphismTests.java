package algorithms.isomorphism;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.Monomer;
import model.Polymer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.Bond;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.silent.Atom;

import algorithms.isomorphism.chains.Chain;
import algorithms.isomorphism.chains.Extension;
import algorithms.isomorphism.chains.MappedChain;

public class IsomorphismTests {

	private Extension ext1;
	private Extension ext2;
	private MappedChain mb0;
	private Chain bloc;


	@Before
	public void setUp() throws Exception {
		// Database
		Monomer[] monos = new Monomer[1];
		Polymer pepTest = new Polymer(0, "malformin A1", "O=C1NC2C(=O)NC(C(=O)NC(C(=O)NC(C(=O)NC1CSSC2)C(C)CC)CC(C)C)C(C)C", monos);
		
		// Extensions
		IAtom a = new Atom("C");
		IBond b1 = new Bond(new Atom("S"), a, Order.SINGLE);
		this.ext1 = new Extension(b1);
		a = new Atom("C");
		IAtom a2 = new Atom("C");
		IBond b2 = new Bond(a, a2, Order.SINGLE);
		this.ext2 = new Extension(b2);
		
		// Mapped blocs
		this.mb0 = new MappedChain(pepTest, null, new ArrayList<Integer>(), new ArrayList<Integer>(), new ArrayList<MatchingType>(), new HashMap<Integer, Integer>());
		
		// For blocs Tests
		this.bloc = new Chain("S,0,c,0,0,-1,-1;c,0,c,0,0,-1,1");
	}
	
	
	@Test
	public void matchingAromaticTest () {
		Polymer pep = new Polymer(0, "", "Sc1ccccc1", null);
		List<MappedChain> mbs = Isomorphism.searchAChain(this.bloc, pep, MatchingType.STRONG);
		Assert.assertTrue(mbs.size() > 0);
	}
	
	@Test
	public void notMatchingAromaticTest () {
		Polymer pep = new Polymer(0, "", "SC1CCCCC1", null);
		List<MappedChain> mbs = Isomorphism.searchAChain(this.bloc, pep, MatchingType.STRONG);
		Assert.assertFalse(mbs.size() > 0);
	}
	
	@Test
	public void lightMatchingAromaticTest () {
		Polymer pep = new Polymer(0, "", "SC1CCCCC1", null);
		List<MappedChain> mbs = Isomorphism.searchAChain(this.bloc, pep, MatchingType.LIGHT);
		Assert.assertTrue(mbs.size() > 0);
	}
	
	@Test
	public void initMappingTest() {
		List<MappedChain> mbs = Isomorphism.searchFromPreviousMapping(this.mb0, this.ext1, MatchingType.STRONG);
		if (mbs.size() != 2)
			fail("2 matches needed");
		
		boolean isGood = true;
		for (MappedChain mb : mbs) {
			IMolecule mol = mb.getChemObject().getMolecule();
			IAtom a1 = (mol.getAtom(mb.getAtomsMapping().get(0)));
			IAtom a2 = (mol.getAtom(mb.getAtomsMapping().get(1)));
			if (!((a1.getSymbol().equals("C") && a2.getSymbol().equals("S")) ||
					(a1.getSymbol().equals("S") && a2.getSymbol().equals("C"))))
				isGood = false;
		}
		
		Assert.assertTrue(isGood);
	}
	
	@Test
	public void MappingTest () {
		List<MappedChain> mbs = Isomorphism.searchFromPreviousMapping(this.mb0, this.ext1, MatchingType.STRONG);
		
		boolean isGood = true;
		for (MappedChain mb : mbs) {
			List<MappedChain> extendedMbs = Isomorphism.searchFromPreviousMapping(mb, this.ext2, MatchingType.STRONG);
			MappedChain newMb = extendedMbs.get(0);
			
			IMolecule mol = mb.getChemObject().getMolecule();
			IAtom newA = (mol.getAtom(newMb.getAtomsMapping().get(2)));
			
			if (!(
					(newMb.getAtomsMapping().get(0) == mb.getAtomsMapping().get(0)) && // Same first atom
					(newMb.getAtomsMapping().get(1) == mb.getAtomsMapping().get(1)) && // Same second atom
					(newA.getSymbol().equals("C")) // Extended by C atom
					))
				isGood = false;
		}
		
		Assert.assertTrue(isGood);
	}

}
