package algorithms.isomorphism.chains;

import java.util.Map;

import model.Residue;

import org.json.simple.JSONObject;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;

import algorithms.isomorphism.MatchingType;

public class HydrogenAdd extends ChainAdd {
	
	private int idx;
	private int nbHydrogens;
	
	//private static SaturationChecker sc = new SaturationChecker();

	public HydrogenAdd(Residue from, int idx, int nbHydrogens) {
		super(from);
		this.idx = idx;
		this.nbHydrogens = nbHydrogens;
	}
	
	public int getIdx() {
		return idx;
	}
	
	public int getNbHydrogens() {
		return nbHydrogens;
	}

	@Override
	public void apply(MappedChain mc, MatchingType type) throws AtomNotFound {
		int free = this.getFreeHydrogens(mc);
		if (free < this.nbHydrogens) {
			if (type == MatchingType.LIGHT)
				return;
			else
				throw new AtomNotFound();
		}
		
		this.commonApplying(mc);
	}

	@Override
	public MappedChain applyAndClone(MappedChain mc, MatchingType type) throws AtomNotFound {
		int free = this.getFreeHydrogens(mc);
		if (free < this.nbHydrogens) {
			throw new AtomNotFound();/**/
		}
		
		MappedChain clone = new MappedChain(mc);
		this.commonApplying(clone);
		return clone;
	}
	
	/**
	 * Apply the hydrogen add on the mc
	 * @param mc
	 */
	private void commonApplying (MappedChain mc) {
		int atomIdx = mc.getAtomsMapping().get(this.idx);
		Map<Integer, Integer> mapping = mc.getHydrogensMapping();
		int val = mapping.containsKey(atomIdx) ? mapping.get(atomIdx) : 0;
		mc.getHydrogensMapping().put(atomIdx, val+this.nbHydrogens);
	}

	/**
	 * Transform mc parameter to correct parameters for arity calculation
	 * @param mc
	 * @return
	 */
	private int getFreeHydrogens(MappedChain mc) {
		int atomIdx = mc.getAtomsMapping().get(this.idx);
		IMolecule mol = mc.getChemObject().getMolecule();
		IAtom a = mol.getAtom(atomIdx);
		
		int mappedHydrogens = mc.getHydrogensMapping().get(atomIdx);
		int presentHydrogens = a.getImplicitHydrogenCount();
		
		return presentHydrogens - mappedHydrogens;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJSON() {
		JSONObject jso = new JSONObject();
		jso.put("from", this.getFrom().getId());
		jso.put("type", "hydrogen");
		jso.put("idx", this.idx);
		jso.put("num", this.nbHydrogens);
		return jso;
	}
	
}
