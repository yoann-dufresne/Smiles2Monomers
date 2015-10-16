package algorithms.isomorphism;

import model.Residue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import algorithms.isomorphism.chains.Chain;
import algorithms.isomorphism.chains.MappedChain;

public class MappedBlocsTest {
	
	private Residue res;
	private MappedChain mb;
	private Chain prevBloc;
	

	@Before
	public void setUp() throws Exception {
		this.prevBloc = new Chain("C,0,S,0,0,-1,-1;C,0,C,0,1,0,-1;C,0,N,1,0,2,-1");
		this.prevBloc.getSerial();
		this.res = Residue.constructResidue("MonoTest", "[H]NC([H])(C=O)C([H])([H])S");
		this.mb = Isomorphism.searchAChain(this.prevBloc, res, MatchingType.STRONG).get(0);
	}
	
	@Test
	public void createIsomorphicMBTest() {
		Assert.assertNotEquals(this.mb, null);
	}
	
	@Test
	public void correctIsomorphicMBTest() {
		String value = this.mb.toString();
		Assert.assertTrue(value.startsWith("C,0,S,0,0,-1,-1;C,0,C,0,1,0,-1;C,0,N,1,0,2,-1;")
				&& value.contains(";4,5,1,0;4,3,0;"));
	}
	
	@Test
	public void addHydrogensSmallTest() {
		this.mb.addAllHydrogens();
		String value = this.mb.toString();
		Assert.assertTrue(this.mb.toString().startsWith("C,0,S,2,0,-1,-1;C,0,C,2,1,0,-1;C,0,N,1,1,2,-1;")
				&& value.contains(";4,5,1,0;4,3,0;"));
	}

}
