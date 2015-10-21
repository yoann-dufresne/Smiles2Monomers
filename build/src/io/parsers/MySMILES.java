package io.parsers;

import java.util.HashSet;
import java.util.Set;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;

/**
 * @author Yoann Dufresne
 * 
 * Temporary class before I find a way to create partial aromatic query
 *
 */
public class MySMILES {

	/**
	 * 
	 * @param molecule
	 * @return
	 */
	public static String convert (IMolecule molecule) {
		// TODO
		return null;
	}
	
	private class TemporaryString {
		private Set<Integer> coveredAtoms;
		private String smiles;
		
		private IMolecule mol;
		private IAtom currentAtom;
		
		public TemporaryString(IMolecule mol) {
			this.coveredAtoms = new HashSet<Integer>();
			this.smiles = "";
			
			this.mol = mol;
		}
	}
}
