package algorithms.isomorphism.blocs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.ChemicalObject;
import model.Family;
import model.Polymer;
import model.Residue;

import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;

import algorithms.isomorphism.Isomorphism;
import algorithms.isomorphism.MatchingType;
import db.FamilyDB;
import db.PeptidesDB;

public class BlocsLearning {

	private BlocsDB blocs;
	private List<Map<Polymer, List<MappedBloc>>> peptidesMappings;
	
	private List<Map<Bloc, Bloc>> redirections;
	
	private PeptidesDB peptides;
	private List<Residue> residues;
	
	public static boolean verbose = false;
	
	public BlocsLearning(FamilyDB families, PeptidesDB peptides) {
		this.residues = new ArrayList<>();
		this.peptides = peptides;
		for (Family fam : families.getFamilies())
			for (Residue res : fam.getRoots()) {
				this.residues.add(res);
			}
		
		this.blocs = new BlocsDB();
		this.redirections = new ArrayList<>();
		
		this.peptidesMappings = new ArrayList<>();
	}
	
	
	/**
	 * Search blocks of size size
	 * @param size Size of blocs
	 */
	public void searchForSize (int size) {
		List<MappedBloc> prevMbs = null;
		
		if (size < 1) {
			System.err.println("Size can't be less than 1");
			System.exit(42);
		} else if (size != 1) {
			this.searchForSize(size-1);
			
			prevMbs = this.blocs.getAllMappedBlocsOfSize(size - 1);
		} else {
			Set<Residue> residues = new HashSet<>(this.residues);
			prevMbs = this.initResidueMappedBlocs(residues);
		}
		
		// Objects creation
		this.redirections.add(new HashMap<Bloc, Bloc>());
		
		// Blocs creation
		List<MappedBloc> newMappedBlocs= this.createBlocs(prevMbs);
		this.blocs.setMappings(Isomorphism.filter(newMappedBlocs));
		Map<Bloc, Integer> frqs = this.calculateFrequencies(this.blocs.getAllBlocsOfSize(size));
		this.blocs.setFrequencies(frqs);
	}
	
	/**
	 * First initialization of mapped blocs on residues.
	 * @return A list of mapped blocs on residues associated with the 'null' bloc.
	 */
	private List<MappedBloc> initResidueMappedBlocs (Set<? extends ChemicalObject> objs) {
		List<MappedBloc> mbs = new ArrayList<>();
		
		for (ChemicalObject co : objs) {
			mbs.add(new MappedBloc(co));
		}

		return mbs;
	}
	
	/**
	 * First initialization of mapped blocs on peptides.
	 * @return A list of mapped blocs on peptides associated with the 'null' bloc.
	 */
	private Map<Polymer, List<MappedBloc>> initPeptidesMappedBlocs (List<Polymer> objs) {
		Map<Polymer, List<MappedBloc>> mbs = new HashMap<Polymer, List<MappedBloc>>();
		
		for (Polymer pep : objs) {
			List<MappedBloc> mbsList = new ArrayList<>();
			mbsList.add(new MappedBloc(pep));
			mbs.put(pep, mbsList);
		}

		return mbs;
	}
	
	/**
	 * Create all blocs
	 * @param prevMbs
	 */
	private List<MappedBloc> createBlocs (List<MappedBloc> prevMbs) {
		int size = prevMbs.get(0).getBondsMapping().size() + 1;
		
		List<MappedBloc> newBlocs = new ArrayList<>();
				
		int m=0;
		for (MappedBloc mb : prevMbs) {
			m++;
			
			ChemicalObject co = mb.getChemObject();
			IMolecule mol = co.getMolecule();
			
			if (verbose) {
				System.out.println("  Size search : " + size);
				System.out.println("    Mapped bloc " + m + "/" + prevMbs.size());
				System.out.println("    id : " + co.getName());
			}
			
			List<Integer> neighbors = mb.getNeighborsBonds(mol);
			// Create a new bloc for each neighbor
			for (int idx : neighbors) {
				// Create bloc
				IBond nb = mol.getBond(idx);
				Extension ext = new Extension(nb);
				
				List<MappedBloc> newMbs = Isomorphism.searchFromPreviousMapping (mb, ext, MatchingType.EXACT);
				
				// Keep only good blocs
				for (MappedBloc newMb : newMbs)
					if(this.canBeAdd (newMb, newBlocs)) {
						if (!newBlocs.contains(newMb)) {
							newBlocs.add(newMb);
						}
					}
			}

		}
		
		return newBlocs;
	}
	
	/**
	 * Verify if a mapped bloc can be add to the new ones
	 * @param newMb
	 * @param mbs
	 * @return
	 */
	private boolean canBeAdd(MappedBloc newMb, List<MappedBloc> mbs) {
		int size = newMb.getBondsMapping().size();
		Bloc newBloc = newMb.getBloc();
		
		Map<Bloc, Bloc> equivalences = this.redirections.get(size-1);
		
		// If bloc have a redirection, then peptides mappings is already good
		if (equivalences.containsKey(newBloc)) {
			return false;
		}
		
		String smiles = newBloc.getSmiles();
		Bloc toDelete = null;
		for (MappedBloc mb : mbs) {
			Bloc bloc = mb.getBloc();
			// Search similar blocs
			if (smiles.equals(bloc.getSmiles())) {
				// If it's already existing
				if (newBloc.getSerial().equals(bloc.getSerial()))
					return true;
					
				// New bloc best
				int newScore = newBloc.getPerformance(this.blocs);
				if (newScore == 0)
					return true;
				
				int oldScore = bloc.getPerformance(this.blocs);
				if (newScore != 0 && oldScore != 0)
					if (newScore < oldScore) {
						equivalences.put(bloc, newBloc);
						toDelete = bloc;
					}
					// Old bloc best or equivalent
					else {
						equivalences.put(newBloc, bloc);
						return false;
					}
			}
		}
		
		// Deletion of old bloc if necessary
		if (null != toDelete) {
			List<MappedBloc> cpy = new ArrayList<>(mbs);
			for (MappedBloc mb : cpy)
				if (mb.getBloc().equals(toDelete))
					mbs.remove(mb);
		}
		
		return true;
	}

	/**
	 * Calculate all frequencies
	 * @param newBlocs
	 * @return
	 */
	private Map<Bloc, Integer> calculateFrequencies (Set<Bloc> blocs) {
		// Get values
		int size = blocs.iterator().next().getSize();
		Map<Polymer, List<MappedBloc>> pepMbsOfSize_1 = null;
		if (size > 1)
			pepMbsOfSize_1 = this.peptidesMappings.get(size - 2);
		else {
			pepMbsOfSize_1 = this.initPeptidesMappedBlocs(this.peptides.getObjects());
		}
		
		// Create new map for new blocs
		Map<Polymer, List<MappedBloc>> pepMbsOfSize = new HashMap<>();
		this.peptidesMappings.add(pepMbsOfSize);
		
		// Create new map for new frequencies.
		Map<Bloc, Integer> frqs = new HashMap<>();
		
		// Calculate
		int n=0;
		for (Bloc b : blocs) {
			n++;
			if (verbose) {
				System.out.println("  Size search : " + size);
				System.out.println("    Calculate frequencies " + n + "/" + blocs.size());
			}
			int frq = this.calculateFrequency (b, pepMbsOfSize_1, pepMbsOfSize);
			frqs.put(b, frq);
		}
		
		return frqs;
	}
	
	/**
	 * Calculate frequency apparition in peptides learning base. 
	 * @param bloc Search bloc
	 * @param pepMbsOfSize_1 Mappings of sub-blocs
	 * @param pepMbsOfSize 
	 * @return
	 */
	private int calculateFrequency(Bloc bloc, Map<Polymer, List<MappedBloc>> pepMbsOfSize_1,
			Map<Polymer, List<MappedBloc>> pepMbsOfSize) {
		int frq = 0;

		// Search frequency in all peptides
		for (Polymer pep : pepMbsOfSize_1.keySet()) {
			List<MappedBloc> pepMbs = pepMbsOfSize_1.get(pep);
			List<MappedBloc> countMbs = new ArrayList<MappedBloc>();
			List<MappedBloc> newMbs = pepMbsOfSize.containsKey(pep) ?
					pepMbsOfSize.get(pep) : new ArrayList<MappedBloc>();
			
			// For each mapped subBloc
			for (MappedBloc mb : pepMbs) {
				if (mb.getBloc() != null && !mb.getBloc().equals(bloc.getSubBlc()))
					continue;
				//System.out.println("    " + mb.toString());
				List<MappedBloc> mbs = Isomorphism.searchFromPreviousMapping(mb, bloc.getExt(), MatchingType.STRONG);
				
				// Add new mappings
				for (MappedBloc newMb : mbs) {
					if (newMb.getBloc().equals(bloc) && !newMbs.contains(newMb)) {
						newMbs.add(newMb);
						countMbs.add(newMb);
					}
				}
			}
			pepMbsOfSize.put(pep, newMbs);
			frq += Isomorphism.filter(countMbs).size();
		}

		return frq;
	}
	
	public BlocsDB getBlocs() {
		return blocs;
	}


	public static void setVerbose(boolean verbose) {
		BlocsLearning.verbose = verbose;
	}
	
}
