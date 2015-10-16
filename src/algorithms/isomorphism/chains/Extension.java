package algorithms.isomorphism.chains;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.openscience.cdk.Atom;
import org.openscience.cdk.Bond;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IBond.Order;

import algorithms.isomorphism.MatchingType;

public class Extension {

	private IBond bond;
	//private static boolean aromaticTest = true;
	
	private static HashMap<String, IAtom> savedAtoms = new HashMap<>();
	private static HashMap<String, IBond> savedBonds = new HashMap<>();
	
	/**
	 * Constructor for an extension
	 * @param ext Bond of the extension
	 */
	public Extension(IBond ext) {
		IAtom a0 = ext.getAtom(0);
		int h0 = 0;
		try {
			h0 = a0.getImplicitHydrogenCount();
		} catch (NullPointerException e) {}
		
		IAtom a1 = ext.getAtom(1);
		int h1 = 0;
		try {
			h1 = a1.getImplicitHydrogenCount();
		} catch (NullPointerException e) {}
		
		this.createBond(
				a0.getSymbol(), h0, a0.getFlag(CDKConstants.ISAROMATIC),
				a1.getSymbol(), h1, a1.getFlag(CDKConstants.ISAROMATIC),
				ext.getOrder()
		);
	}
	
	public Extension (String serial) {
		String[] split = serial.split(",");
		int h0 = 0;
		int h1 = 0;
		Order ord = Order.values()[new Integer(split[1])];
		if (split.length > 4) {
			h0 = new Integer(split[3]);
			h1 = new Integer(split[4]);
		}
		
		String a0 = split[0];
		String a1 = split[2];
		
		boolean aro0 = Character.isLowerCase(a0.charAt(0));
		boolean aro1 = Character.isLowerCase(a1.charAt(0));
		
		this.createBond(a0, h0, aro0, a1, h1, aro1, ord);
	}
	
	private Extension () {
		
	}

	private void createBond (String name0, int h0, boolean arom0, String name1, int h1, boolean arom1, Order ord) {
		if (arom0)
			name0 = name0.toLowerCase();
		if (arom1)
			name1 = name1.toLowerCase();
		
		List<String> names = new ArrayList<>();
		names.add(name0 + ":" + h0);
		names.add(name1 + ":" + h1);
		Collections.sort(names);
		
		//this.inverted = !names.get(0).equals(name0 + ":" + h0);
		
		IAtom c0 = null;
		String n0 = names.get(0);
		if (Extension.savedAtoms.containsKey(n0))
			c0 = Extension.savedAtoms.get(n0);
		else {
			String[] split = n0.split(":");
			char first = Character.toUpperCase(split[0].charAt(0));
			String name = first + split[0].substring(1);
			c0 = new Atom(name);
			c0.setFlag(CDKConstants.ISAROMATIC, Character.isLowerCase(split[0].charAt(0)));
			c0.setImplicitHydrogenCount(new Integer(split[1]));
			Extension.savedAtoms.put(n0, c0);
		}
		
		IAtom c1 = null;
		String n1 = names.get(1);
		if (Extension.savedAtoms.containsKey(n1))
			c1 = Extension.savedAtoms.get(n1);
		else {
			String[] split = n1.split(":");
			char first = Character.toUpperCase(split[0].charAt(0));
			String name = first + split[0].substring(1);
			c1 = new Atom(name);
			c1.setFlag(CDKConstants.ISAROMATIC, Character.isLowerCase(split[0].charAt(0)));
			c1.setImplicitHydrogenCount(new Integer(split[1]));
			Extension.savedAtoms.put(n1, c1);
		}
		
		String key = names.get(0)+ord+names.get(1);
		if (Extension.savedBonds.containsKey(key)) {
			this.bond = Extension.savedBonds.get(key);
		} else {
			this.bond = new Bond(c0, c1, ord);
			Extension.savedBonds.put(key, this.bond);
		}
	}
	
	public IBond getBond() {
		return bond;
	}

	public String getSerial() {
		IAtom a0 = this.bond.getAtom(0);
		IAtom a1 = this.bond.getAtom(1);
		String name0 = a0.getFlag(CDKConstants.ISAROMATIC) ? a0.getSymbol().toLowerCase() : a0.getSymbol();
		String name1 = a1.getFlag(CDKConstants.ISAROMATIC) ? a1.getSymbol().toLowerCase() : a1.getSymbol();
		return name0 + "," + this.bond.getOrder().ordinal() + "," + name1 +
				"," + a0.getImplicitHydrogenCount() + "," + a1.getImplicitHydrogenCount();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Extension))
			return false;
		
		Extension ext = (Extension)obj;
		if (!ext.bond.getOrder().equals(this.bond.getOrder()))
			return false;
		
		IAtom oa0 = ext.bond.getAtom(0);
		int oh0 = oa0.getImplicitHydrogenCount();
		IAtom oa1 = ext.bond.getAtom(1);
		int oh1 = oa1.getImplicitHydrogenCount();
		
		IAtom ta0 = this.bond.getAtom(0);
		int th0 = ta0.getImplicitHydrogenCount();
		IAtom ta1 = this.bond.getAtom(1);
		int th1 = ta1.getImplicitHydrogenCount();
		
		if (oa0.getSymbol().equals(ta0.getSymbol())
				&& oa1.getSymbol().equals(ta1.getSymbol())
				&& th0 == oh0 && th1 == oh1)
			return true;
		
		if (oa0.getSymbol().equals(ta1.getSymbol())
				&& oa1.getSymbol().equals(ta0.getSymbol())
				&& th0 == oh1 && th1 == oh0)
			return true;
		
		return false;
	}
	
	/**
	 * Match "this" extension on the bond from the chemical object with or without strict rules
	 * @param bond Bond to match
	 * @param light False for strict matching, true for aromatic, bond degree and hydrogen approximation.
	 * @return list of matches
	 */
	public List<BondMapping> match (IBond bond, MatchingType type) {
		List<BondMapping> mappings = new ArrayList<>();
		
		IAtom a0 = bond.getAtom(0);
		IAtom a1 = bond.getAtom(1);
		IAtom e0 = this.bond.getAtom(0);
		IAtom e1 = this.bond.getAtom(1);
		
		int h0 = a0.getImplicitHydrogenCount();
		int h1 = a1.getImplicitHydrogenCount();
		int eh0 = e0.getImplicitHydrogenCount();
		int eh1 = e1.getImplicitHydrogenCount();
		
		// Mapping in bond direction
		if (this.areSameAtoms(e0, e1, a0, a1))
			if (this.lightToStrongMatching(e0, e1, eh0, eh1, a0, a1, h0, h1, this.bond, bond)) {
				if (this.strongToExactMatching(eh0, eh1, h0, h1))
					mappings.add(new BondMapping(bond, this, a0, a1, eh0, eh1, MatchingType.EXACT));
				else if (type != MatchingType.EXACT)
					mappings.add(new BondMapping(bond, this, a0, a1, eh0, eh1, MatchingType.STRONG));
			} else if (type == MatchingType.LIGHT) {
				mappings.add(new BondMapping(bond, this, a0, a1, 0, 0, MatchingType.LIGHT));
			}
		
		// Reverse mapping
		if (this.areSameAtoms(e0, e1, a1, a0))
			if (this.lightToStrongMatching(e0, e1, eh0, eh1, a1, a0, h1, h0, this.bond, bond)) {
				if (this.strongToExactMatching(eh0, eh1, h1, h0))
					mappings.add(new BondMapping(bond, this, a1, a0, eh0, eh1, MatchingType.EXACT));
				else if (type != MatchingType.EXACT)
					mappings.add(new BondMapping(bond, this, a1, a0, eh0, eh1, MatchingType.STRONG));
			} else if (type == MatchingType.LIGHT) {
				mappings.add(new BondMapping(bond, this, a1, a0, 0, 0, MatchingType.LIGHT));
			}
		
		return mappings;
	}/**/

	/**
	 * Search only same atom type.
	 * @param a1
	 * @param a2
	 * @param b1
	 * @param b2
	 * @return true if a1 = b1 && a2 = b2
	 */
	private boolean areSameAtoms (IAtom a1, IAtom a2, IAtom b1, IAtom b2) {
		return a1.getSymbol().equals(b1.getSymbol()) && a2.getSymbol().equals(b2.getSymbol());
				
	}
	
	/*
	 * Verify if bonds are same, aromtic flags are same and hydrogens are compatible
	 */
	private boolean lightToStrongMatching (IAtom a1, IAtom a2, int ha1, int ha2,
			IAtom b1, IAtom b2, int hb1, int hb2,
			IBond l1, IBond l2) {
		// Aromatic matching
		if (a1.getFlag(CDKConstants.ISAROMATIC) != b1.getFlag(CDKConstants.ISAROMATIC) ||
				a2.getFlag(CDKConstants.ISAROMATIC) != b2.getFlag(CDKConstants.ISAROMATIC))
			return false;/**/
		
		// Bond type
		if (l1.getOrder() != l2.getOrder())
			return false;
		
		// Hydrogens
		if (ha1 > hb1 || ha2 > hb2)
			return false;
		
		return true;
	}

	/*
	 * Verify exact hydrogens matching
	 */
	private boolean strongToExactMatching(int ah0, int ah1, int bh0, int bh1) {
		return ah0 == bh0 && ah1 == bh1;
	}
	
	@Override
	public String toString() {
		return this.getSerial();
	}
	
	public Extension clone () {
		Extension ext = new Extension();
		
		IAtom a0 = null, a1 = null;
		try {
			a0 = (IAtom) this.bond.getAtom(0).clone();
			a1 = (IAtom) this.bond.getAtom(1).clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		ext.bond = new Bond(a0, a1, this.bond.getOrder());
		
		return ext;
	}
	
	/*public static void setAromacityTest (boolean test) {
		Extension.aromaticTest = test;
	}/**/
	
	
	
	public class BondMapping {
		public IBond bond;
		public Extension ext;
		public IAtom a0;
		public IAtom a1;
		public int h0;
		public int h1;
		public MatchingType matchingType;
		
		
		public BondMapping(IBond bond, Extension ext, IAtom a1, IAtom a2, int h0, int h1, MatchingType matching) {
			this.a0 = a1;
			this.a1 = a2;
			this.bond = bond;
			this.ext = ext;
			this.h0 = h0;
			this.h1 = h1;
			this.matchingType = matching;
		}
	}

}