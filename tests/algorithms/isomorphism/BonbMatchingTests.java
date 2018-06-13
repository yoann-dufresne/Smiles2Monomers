package algorithms.isomorphism;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.Atom;
import org.openscience.cdk.Bond;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond.Order;

import algorithms.isomorphism.chains.Extension;
import algorithms.isomorphism.chains.Extension.BondMapping;

public class BonbMatchingTests {

	private Bond bond1;

	@Before
	public void setUp() throws Exception {
		//cNH
		IAtom a2 = new Atom("C");
		a2.setImplicitHydrogenCount(0);
		a2.setFlag(CDKConstants.ISAROMATIC, true);
		IAtom a1 = new Atom("N");
		a1.setImplicitHydrogenCount(1);
		this.bond1 = new Bond(a1, a2, Order.DOUBLE);
	}
	
	@Test
	public void perfectMatching() {
		Extension ext = new Extension("c,1,N,0,1");
		List<BondMapping> matchs = ext.match(this.bond1, MatchingType.EXACT);
		Assert.assertTrue(matchs.size() == 1);
	}
	
	@Test
	public void noPerfectMatching() {
		Extension ext = new Extension("c,1,N,0,0");
		List<BondMapping> matchs = ext.match(this.bond1, MatchingType.EXACT);
		Assert.assertTrue(matchs.size() == 0);
	}
	
	@Test
	public void strongMatching() {
		Extension ext = new Extension("c,1,N,0,1");
		List<BondMapping> matchs = ext.match(this.bond1, MatchingType.STRONG);
		Assert.assertTrue(matchs.size() == 1);
	}
	
	@Test
	public void strongMatching2() {
		Extension ext = new Extension("c,1,N,0,0");
		List<BondMapping> matchs = ext.match(this.bond1, MatchingType.STRONG);
		Assert.assertTrue(matchs.size() == 1);
	}
	
	@Test
	public void lightMatching() {
		Extension ext = new Extension("C,1,N,0,0");
		List<BondMapping> matchs = ext.match(this.bond1, MatchingType.LIGHT);
		Assert.assertTrue(matchs.size() == 1);
	}
	
	@Test
	public void aromaticNoMatching() {
		Extension ext = new Extension("C,1,N,0,1");
		List<BondMapping> matchs = ext.match(this.bond1, MatchingType.STRONG);
		Assert.assertTrue(matchs.size() == 0);
	}
	
	@Test
	public void noHydrogenMatching() {
		Extension ext = new Extension("c,1,N,0,0");
		List<BondMapping> matchs = ext.match(this.bond1, MatchingType.STRONG);
		Assert.assertTrue(matchs.size() == 1);
	}
	
	@Test
	public void hydrogenNoMatching() {
		Extension ext = new Extension("c,1,N,1,1");
		List<BondMapping> matchs = ext.match(this.bond1, MatchingType.STRONG);
		Assert.assertTrue(matchs.size() == 0);
	}

}
