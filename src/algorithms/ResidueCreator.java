package algorithms;

import io.parsers.SmilesConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.Family;
import model.Monomer;
import model.Residue;
import model.Rule;
import model.Rule.Replacement;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.isomorphism.mcss.RMap;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import db.FamilyDB;
import db.MonomersDB;
import db.RulesDB;


/**
 * Class to create residues from a rules database.
 * @author Yoann Dufresne
 */
public class ResidueCreator {
	
	private RulesDB dbAlone;
	private RulesDB dbNotAlone;
	
	private SmilesGenerator sg;
	private boolean verbose;

	public ResidueCreator(RulesDB db) {
		this.verbose = false;
		this.sg = new SmilesGenerator();
		this.sg.setUseAromaticityFlag(true);
		
		Residue.resetResidues();
		
		this.dbAlone = new RulesDB();
		this.dbNotAlone = new RulesDB();
		
		for (Rule r : db.getObjects())
		{
			if (r.getAlone())
				this.dbAlone.addObject(r.getName(), r);
			else
				this.dbNotAlone.addObject(r.getName(), r);
		}
	}

	/**
	 * Create all residues from the argument monomers database according to rules entered in paramaters of the constructor.
	 * @param monosDBName A monomers database.
	 * @return All possible residues found.
	 */
	public FamilyDB createResidues(MonomersDB monosDB) {
		FamilyDB famDB = new FamilyDB();
		famDB.init(monosDB);
		
		for (Family family : famDB.getFamilies())
		{
			this.residuesFromMonomers(family);
			for (Residue res : family.getResidues()) {
				IMolecule oldMol = res.getMolecule();
				res.setMol(new Molecule(AtomContainerManipulator.removeHydrogens(res.getMolecule())));
				IMolecule newMol = res.getMolecule();
				
				Map<IAtom, IAtom> conversion = new HashMap<>();
				int idx = 0;
				for (IAtom a : oldMol.atoms()) {
					if (!"H".equals(a.getSymbol())) {
						conversion.put(a, newMol.getAtom(idx));
						idx++;
					}
				}
				
				Map<IAtom, Rule> oldLinks = new HashMap<>(res.getAtomicLinks());
				res.getAtomicLinks().clear();
				for (IAtom oldA : oldLinks.keySet()) {
					Rule rule = oldLinks.get(oldA);
					res.addLink(conversion.get(oldA), rule);
				}
			}
		}
		
		return famDB;
	}

	/*
	 * Create residues from a monomer according to the rules database.
	 */
	private Set<Residue> residuesFromMonomers(Family family) {
		Monomer mono = family.getMonomers().get(0);
		Set<Residue> residues = new HashSet<>();
		RulesDB rules = new RulesDB();
		rules.addDB(this.dbAlone);
		
		// List of residues in witch we search other residues.
		Set<Residue> searchResidues = new HashSet<>();
		IMolecule m = null;
		try {
			m = mono.getMolecule().clone();
			AtomContainerManipulator.convertImplicitToExplicitHydrogens(m);
		} catch (CloneNotSupportedException e1) {
			e1.printStackTrace();
		}
		searchResidues.add(new Residue(mono.getCode(), this.sg.createSMILES(m), true));
		
		
		boolean firstTime = true;
		while (searchResidues.size() != 0)
		{
			Set<Residue> nextLevelResidues = new HashSet<>();
			
			for (Residue res : searchResidues)
			{
				res.setExplicitHydrogens(true);
				Set<Residue> newResidues = new HashSet<>();
				for (Rule rule : rules.getObjects()) {
					Set<Residue> ruleResidues = this.getResiduesWithRule (res, rule);
					
					newResidues.addAll(ruleResidues);
					for (Residue r : ruleResidues)
						nextLevelResidues.add(r);
				}
				for (Residue newRes : newResidues) {
					family.addResidue(newRes);
					if (family.containsMonomer(res.getMonoName()) && res.getAtomicLinks().size() > 0)
						family.addDependance(newRes, res);
				}
				res.setExplicitHydrogens(false);
			}
			
			searchResidues = nextLevelResidues;
			residues.addAll(searchResidues);
			
			if (firstTime) {
				firstTime = false;
				rules.addDB(this.dbNotAlone);
			}
		}
		
		if (residues.size() == 0) {
			String smiles = this.sg.createSMILES(m);
			Residue res = Residue.constructResidue(mono.getName(), smiles);
			family.addResidue(res);
			residues.add(res);
		}
		
		if (this.verbose) {
			System.out.println("Nb residues of " + mono.getCode() + " : " + residues.size());
			for (Residue res : residues)
				System.out.println("  " + res.getSmiles());
			System.out.println();
		}
		
		return residues;
	}

	/*
	 * Search Residues for one monomer and one rule.
	 */
	private Set<Residue> getResiduesWithRule(Residue res, Rule rule) {
		Set<Residue> residues = new HashSet<>();
		
		IMolecule ruleMol = null;
		try {
			ruleMol = SmilesConverter.conv.transform(rule.getFormula(), false, false, true);
		} catch (InvalidSmilesException e) {
			System.err.println("Impossible to parse " + rule.getName() + " rule");
			return residues;
		}
		boolean status = false;
		try {
			status = UniversalIsomorphismTester.isSubgraph(res.getMolecule(), ruleMol);
		} catch (CDKException e) {
			e.printStackTrace();
		}
		
		// If rule is found
		if (status)
		{
			List<List<RMap>> matches = null;
			try {
				matches = UniversalIsomorphismTester.getSubgraphAtomsMaps(res.getMolecule(), ruleMol);
			} catch (CDKException e) {
				e.printStackTrace();
			}
			
			if (!"".equals(rule.getExclusion()))
				for (String exclusion : rule.getExclusion())
					this.removeExclusions(matches, exclusion, res.getMolecule());
			
			for (List<RMap> match : matches) {
				Set<Residue> residuesByMatch = this.createResidue (match, rule, res);
				residues.addAll(residuesByMatch);
			}
		}
		
		return residues;
	}

	//Create residue when it was found
	private Set<Residue> createResidue(List<RMap> match, Rule rule, Residue res) {
		Set<Residue> residues = new HashSet<>();
		
		// Create index
		int[] index = new int[match.size()];
		for (RMap rm : match) {
			index[rm.getId2()] = rm.getId1();
		}
	
		for (Replacement replace : rule.getReplacements())
		{
			// Clone molecule
			Molecule oldMol = res.getMolecule();
			Molecule mol = null;
			try {
				mol = (Molecule) oldMol.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			// Save links atoms
			Map<IAtom, IAtom> convesionAtom = new HashMap<>();
			for (IAtom a : res.getAtomicLinks().keySet()) {
				int idx = oldMol.getAtomNumber(a);
				IAtom newA = mol.getAtom(idx);
				convesionAtom.put(a, newA);
			}
			
			
			// Prepare deletions
			List<IAtom> deletedAtoms = new ArrayList<>();
			List<IAtom> linkedAtoms = new ArrayList<>();
			for (int i : replace.toDelete)
				deletedAtoms.add(mol.getAtom(index[i]));
			for (int i : replace.toReplace) {
				IAtom atom = mol.getAtom(index[i]);
				deletedAtoms.add(atom);
				
				for (IAtom neighbor : mol.getConnectedAtomsList(atom))
					if (!neighbor.getSymbol().equals("H") && !deletedAtoms.contains(neighbor))
						linkedAtoms.add(neighbor);
			}
			
			// Delete atoms
			for (IAtom a : deletedAtoms) {
				for (IBond b : mol.getConnectedBondsList(a))
					mol.removeBond(b);
				mol.removeAtom(a);
			}

			String smarts = this.sg.createSMILES(mol);
			
			if (!Residue.existingResidue(smarts, res.getMonoName())) {
				Residue residue = Residue.constructResidue (res.getMonoName(), smarts);
				residue.setMol(mol);
				
				// Add old links
				for (IAtom oldA : convesionAtom.keySet()) {
					IAtom newA = convesionAtom.get(oldA);
					//int oldIdx = oldMol.getAtomNumber(oldA);
					//int newIdx = mol.getAtomNumber(newA);
					residue.getAtomicLinks().put(newA, res.getAtomicLinks().get(oldA));
				}
				// Add new Links
				for (IAtom a : linkedAtoms)
					residue.addLink(a, rule);
				
				residues.add(residue);
			} else {
				Residue residue = Residue.constructResidue (res.getMonoName(), smarts);
				residues.add(residue);
			}
		}
		
		return residues;
	}

	/*
	 * Remove results corresponding to an exclusion in rule
	 */
	private void removeExclusions(List<List<RMap>> matches, String ex, IMolecule mol) {
		// Verifying exclusions
		boolean areExclusion = false;
		IMolecule exclusionMol = null;
		try {
			exclusionMol = SmilesConverter.conv.transform(ex, false, false, true);
		} catch (InvalidSmilesException e) {
			System.err.println("Impossible to parse " + ex);
			return;
		}
		try {
			areExclusion = UniversalIsomorphismTester.isSubgraph(mol, exclusionMol);
		} catch (CDKException e) {
			e.printStackTrace();
		}
		
		if (areExclusion)
		{
			List<RMap> toExclude = null;
			boolean flag = false;
			List<List<RMap>> exclusions = null;
			try {
				exclusions = UniversalIsomorphismTester.getSubgraphAtomsMaps(mol, exclusionMol);
			} catch (CDKException e) {
				e.printStackTrace();
			}
			for (List<RMap> exclusion : exclusions)
			{
				for (List<RMap> match : matches)
				{
					for (RMap rmMatch : match)
					{
						for (RMap exId : exclusion) {
							if (rmMatch.getId1() == exId.getId1())
							{
								flag = true;
								break;
							}
						}
						if (flag)
							break;
					}
					
					if (flag)
					{
						toExclude = match;
						break;
					}
				}
				
				if (flag)
				{
					matches.remove(toExclude);
					toExclude = null;
					flag = false;
				}
			}
		}/**/
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
