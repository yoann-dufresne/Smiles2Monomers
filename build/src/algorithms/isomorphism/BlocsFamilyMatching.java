package algorithms.isomorphism;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;

import model.ChemicalObject;
import model.Family;
import model.Residue;
import algorithms.isomorphism.blocs.Bloc;
import algorithms.isomorphism.blocs.BlocsDB;
import algorithms.isomorphism.blocs.MappedBloc;
import algorithms.utils.Coverage;
import algorithms.utils.Match;

public class BlocsFamilyMatching implements FamilyMatcher {
	
	private ResidueIsomorphism ri;
	private BlocsDB blocs;
	private ChemicalObject co;
	private Map<Bloc, List<MappedBloc>> mappings;
	private Map<Integer, Bloc> blocsSearched;
	private Coverage coverage;
	private Set<Residue> markSet;
	private MatchingType matchingType;
	
	public Map<Residue, MappedBloc> residuesOrdered;
	

	public BlocsFamilyMatching(BlocsDB db) {
		this.blocs = db;
		this.blocsSearched = new HashMap<>();
		this.mappings = new HashMap<>();
		this.matchingType = MatchingType.STRONG;
		this.residuesOrdered = new HashMap<>();
		this.ri = new ResidueIsomorphism();
	}
	
	@Override
	public Coverage matchFamilly(Family family) {
		if (this.co == null)
			return null;
		this.coverage = new Coverage(co);
		
		// Init
		Stack<Residue> searchStack = new Stack<>();
		searchStack.addAll(family.getRoots());
		this.markSet = new HashSet<>();
		
		while (!searchStack.isEmpty()) {
			
			Residue res = searchStack.pop();
			//System.out.println(res.getName() + " : " + res.getId());
			Bloc bloc = this.createBloc (family, res);
			List<MappedBloc> mappings = this.searchResidue (bloc);
			mappings = Isomorphism.filter(mappings);
			this.removeNeighborhoodViolationMappings(mappings, res);
			
			for (MappedBloc mb : mappings) {
				Match m = new Match(res);
				m.addAtoms(mb.getAtomsMapping());
				m.addHydrogens(mb.getHydrogensMapping());
				m.addBonds(mb.getBondsMapping());
				
				this.coverage.addMatch(m);
			}
			
			// Mark current residue to possibly add children.
			if (mappings.size() > 0) {
				markSet.add(res);
				this.removeParentsFromStack(searchStack, family, res);
				this.addChildrenToStack (searchStack, family, res);
			}
		}

		return this.coverage;
	}

	/*
	 * Search incorrect mappings to remove them. An incorrect mapping is a mapping with
	 * linked atoms when they theoretically can't be mapped.
	 */
	private void removeNeighborhoodViolationMappings(List<MappedBloc> mappings, Residue res) {
		MappedBloc order = this.residuesOrdered.get(res);
		for (int i=mappings.size()-1 ; i>=0 ; i--) {
			boolean next = false;
			MappedBloc mb = mappings.get(i);
			
			for (int idx=0 ; idx<mb.getAtomsMapping().size() ; idx++) {
				int aIdx = order.getAtomsMapping().get(idx);
				IAtom a = res.getMolecule().getAtom(aIdx); 
				IMolecule mol = mb.getChemObject().getMolecule();
				IAtom aFromCo = mol.getAtom(mb.getAtomsMapping().get(idx));
				
				// If a is allowed to be linked
				if (res.getLinks().keySet().contains(a)) {
					continue;
				} else {
					for (IAtom neighbor : mol.getConnectedAtomsList(aFromCo)) {
						int nIdx = mol.getAtomNumber(neighbor);
						if (!mb.getAtomsMapping().contains(nIdx)) {
							next = true;
							break;
						}
					}
				}
				

				if (next) {
					mappings.remove(i);
					break;
				}
			}
		}
	}

	private void addChildrenToStack(Stack<Residue> searchStack, Family family,
			Residue res) {
		// Add children with all parents marked
		Set<Residue> children = family.getChildrenOf(res);
		for (Residue child : children) {
			if (!this.markSet.contains(child))
				searchStack.add(child);
		}
	}
	
	private void removeParentsFromStack(Stack<Residue> searchStack, Family family,
			Residue res) {
		// Add children with all parents marked
		Set<Residue> parents = family.getParentsOf(res);
		for (Residue parent : parents) {
			searchStack.remove(parent);
		}
	}

	private List<MappedBloc> searchResidue(Bloc b) {
		if (this.mappings.containsKey(b))
			return this.mappings.get(b);
		else if (b.getSize() == 1) {
			List<MappedBloc> mbs = Isomorphism.searchFromPreviousMapping(new MappedBloc(co), b.getExt(), this.matchingType);
			this.mappings.put(b, mbs);
			return mbs;
		} else {
			List<MappedBloc> prevMappings = this.searchResidue(b.getSubBlc());
			List<MappedBloc> mappings = new ArrayList<>();
			for (MappedBloc mb : prevMappings) {
				List<MappedBloc> found = Isomorphism.searchFromPreviousMapping(mb, b.getExt(), this.matchingType);
				for (MappedBloc newMb : found)
					if (b.equals(newMb.getBloc()))
						mappings.add(newMb);
			}
			
			this.mappings.put(b, mappings);
			
			return mappings;
		}
	}

	private Bloc createBloc(Family fam, Residue res) {
		int resIdx = new Integer(res.getId());
		Set<Residue> parents = fam.getParentsOf(res);
		if (parents.size() == 0) {
			Bloc b = null;
			if (this.blocsSearched.containsKey(resIdx)) {
				b = this.blocsSearched.get(resIdx);
			} else {
				b = this.ri.createCoveringBloc(res, blocs);
				this.residuesOrdered.put(res, this.ri.orderedResidueAtoms);
			}
			this.blocsSearched.put(resIdx, b);
			return b;
		}
		
		Residue dady = null;
		for (Residue parent : parents) {
			if (this.blocsSearched.containsKey(new Integer(parent.getId()))) {
				dady = parent;
				//System.out.println(parent.getId() + " -> " + dady.getId());
			}
		}
		
		Bloc dadyBloc = this.blocsSearched.get(new Integer(dady.getId()));
		dadyBloc.getSmiles();
		List<MappedBloc> mbs = Isomorphism.searchABloc(dadyBloc, res, MatchingType.STRONG);
		MappedBloc mb = mbs.get(0);
		mb.addAllHydrogens();
		mb = this.ri.greedyBlocFromMB(mb, blocs);
		
		this.blocsSearched.put(resIdx, mb.getBloc());
		
		this.residuesOrdered.put(res, mb);
		
		return mb.getBloc();
	}

	@Override
	public void setChemicalObject(ChemicalObject co) {
		this.co = co;
		this.mappings = new HashMap<>();
		this.blocsSearched = new HashMap<>();
		this.markSet = new HashSet<>();
	}

	@Override
	public void setAllowLightMatch(MatchingType type) {
		this.matchingType = type;
	}
	
	

}
