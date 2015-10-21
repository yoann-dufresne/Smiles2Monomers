package algorithms.isomorphism;

import io.parsers.SmilesConverter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.Bond;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.silent.Atom;

import algorithms.isomorphism.blocs.Bloc;
import algorithms.isomorphism.blocs.Extension;

public class BlocsTests {
	
	private Extension ext1;
	private Extension ext2;
	private Extension ext3;
	private Extension ext4;
	private Extension ext5;
	private Extension ext6;
	
	private Bloc blc1;
	private Bloc blc2;
	private Bloc blc3;
	private Bloc blc4;
	private Bloc blc5;
	private Bloc blc6;
	
	private SmilesConverter sc;

	@Before
	public void setUp() throws Exception {
		this.sc = SmilesConverter.conv;
		//String smiles = "C(C(=O)O)C(=O)N";
		IAtom a1;
		IAtom a2;
		
		a1 = new Atom("C");
		a1.setFlag(CDKConstants.ISAROMATIC, true);
		a2 = new Atom("N");
		a2.setFlag(CDKConstants.ISAROMATIC, true);
		this.ext1 = new Extension(new Bond(a1, a2, Order.SINGLE)); // NC
		
		a2 = new Atom("O");
		this.ext2 = new Extension(new Bond(a1, a2, Order.DOUBLE)); // NC=O
		
		a2 = new Atom("C");
		a2.setFlag(CDKConstants.ISAROMATIC, true);
		this.ext3 = new Extension(new Bond(a1,a2, Order.SINGLE)); // NC(C)=O
		
		a1 = new Atom("C");
		a1.setFlag(CDKConstants.ISAROMATIC, true);
		this.ext4 = new Extension(new Bond(a1, a2, Order.SINGLE)); // NC(CC)=O
		
		a2 = new Atom("C");
		a2.setFlag(CDKConstants.ISAROMATIC, true);
		this.ext5 = new Extension(new Bond(a1, a2, Order.SINGLE)); // NC(CCC)=O
		
		a1 = new Atom("C");
		a1.setFlag(CDKConstants.ISAROMATIC, true);
		a2 = new Atom("N");
		a2.setFlag(CDKConstants.ISAROMATIC, true);
		this.ext6 = new Extension(new Bond(a1, a2, Order.SINGLE)); // N1C(CCC1)=O
		
		this.blc1 = new Bloc(null, ext1, -1, -1); // NC
		this.blc2 = new Bloc(blc1, ext2, -1, 0); // NC=O
		this.blc3 = new Bloc(blc2, ext3, 0, -1); // NC(C)=O
		this.blc4 = new Bloc(blc3, ext4, 3, -1); // NC(CC)=O
		this.blc5 = new Bloc(blc4, ext5, 4, -1); // NC(CCC)=O
		this.blc6 = new Bloc(blc5, ext6, 5, 1); // N1C(CC1)=O
	}
	
	@Test
	public void SerialisationTest() {
		Assert.assertTrue("c,0,n,0,0,-1,-1;O,1,c,0,0,-1,0;c,0,c,0,0,-1,0;c,0,c,0,0,-1,3;c,0,c,0,0,-1,4;c,0,n,0,0,5,1".equals(this.blc6.getSerial()));
	}
	
	@Test
	public void MoleculeTest() {
		Assert.assertTrue("O=C1CCCN1".equals(this.sc.mol2Smiles(this.blc6.getMolecule())));
	}
	
	@Test
	public void serialsTest() {
		Bloc b0 = new Bloc(this.ext3);
		Bloc b00 = new Bloc(b0, ext1, 0, -1);
		Bloc b01 = new Bloc(b0, ext1, 1, -1);
		
		Assert.assertNotEquals(b00.getSerial(), b01.getSerial());
	}

}
