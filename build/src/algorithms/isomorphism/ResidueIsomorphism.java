package algorithms.isomorphism;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.ChemicalObject;
import model.Residue;

import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;

import algorithms.isomorphism.blocs.Bloc;
import algorithms.isomorphism.blocs.BlocsDB;
import algorithms.isomorphism.blocs.Extension;
import algorithms.isomorphism.blocs.MappedBloc;

public class ResidueIsomorphism {
	
	public MappedBloc orderedResidueAtoms;

	public List<MappedBloc> searchResidueWhithAnnotations (ChemicalObject co,
			Residue res, BlocsDB annotations) {
		Bloc b = createCoveringBloc(res, annotations);
		List<MappedBloc> coMappings = Isomorphism.searchABloc(b, co, MatchingType.STRONG);
		
		return coMappings;
	}
	
	public Bloc createCoveringBloc (Residue res, BlocsDB annotations) {
		MappedBloc bestMb = this.getBestMappedBloc(res, annotations);

		MappedBloc greedy = this.greedyBlocFromMB(bestMb, annotations);
		this.orderedResidueAtoms = greedy;
		return greedy.getBloc();
	}
	
	/**
	 * Get maximum size bloc of residue with minimum frequency.
	 * @param res
	 * @param annotations
	 * @return
	 */
	private MappedBloc getBestMappedBloc (Residue res, BlocsDB annotations) {
		List<MappedBloc> resMbs = new ArrayList<>(annotations.getMappedblocsOf(res));
		List<MappedBloc> mbs = annotations.getMappedblocsOf(res);
		for (MappedBloc mb : mbs)
			if (annotations.getFrequency(mb.getBloc()) == 0)
				resMbs.remove(mb);
		
		Collections.sort(resMbs);
		Collections.reverse(resMbs);
		
		MappedBloc best = null;
		for (MappedBloc mb : resMbs) {
			if (best == null)
				best = mb;
			else {
				if (best.getBloc().getSize() > mb.getBloc().getSize())
					break;
				else {
					int mbScore = mb.getBloc().getPerformance(annotations);
					int bestScore = best.getBloc().getPerformance(annotations);
					if (mbScore < bestScore) {
						best = mb;
					}
				}
			}
		}

		return best;
	}
	
	/**
	 * Create a coverage bloc of the chemical object of mb from the current coverage.
	 * Use a gready algorithm based on frequency of single bonds.
	 * @param mb
	 * @param annotations
	 * @return
	 */
	public MappedBloc greedyBlocFromMB (MappedBloc mb, BlocsDB annotations) {
		ChemicalObject co = mb.getChemObject();
		IMolecule mol = co.getMolecule();
		List<Integer> neighbors = mb.getNeighborsBonds(mol);
		
		if (neighbors.size() == 0)
			return mb;
		
		Extension best = null;
		for (int n : neighbors) {
			IBond b = mol.getBond(n);
			Extension ext = new Extension(b);
			
			if (best == null) {
				best = ext;
			} else {
				int bestFrq = annotations.getFrequency(new Bloc(best));
				
				if (bestFrq == 0)
					best = ext;
				else {
					int extFrq = annotations.getFrequency(new Bloc(ext));
					if (extFrq < bestFrq)
						best = ext;
				}
			}
		}
		
		List<MappedBloc> mbs = Isomorphism.searchFromPreviousMapping(mb, best, MatchingType.STRONG);
		
		return greedyBlocFromMB(mbs.get(0), annotations);
	}

}
