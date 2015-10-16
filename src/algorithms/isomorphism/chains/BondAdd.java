package algorithms.isomorphism.chains;

import java.util.List;

import model.Residue;

import org.json.simple.JSONObject;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;

import algorithms.isomorphism.MatchingType;

public class BondAdd extends ChainAdd {
	
	private Extension ext;
	private int idx1;
	private int idx2;
	
	private IMolecule mol;
	private IAtom expected;
	private IAtom neiAtom;
	private IBond bond;
	private int neiHydro;
	private MatchingType matchingType;

	public BondAdd(Residue from, Extension ext, int idx1, int idx2) {
		super(from);
		this.ext = ext;
		this.idx1 = idx1;
		this.idx2 = idx2;
	}
	
	public void searchAtoms (MappedChain mc, MatchingType type) throws AtomNotFound {
		this.mol = mc.getChemObject().getMolecule();
		
		// Init real atom idxs.
		int atomIdx = -1;
		int neiIdx = -1;
		this.expected = null;
		if (this.idx1 == -1) {
			atomIdx = mc.getAtomsMapping().get(this.idx2);
			neiIdx = this.idx1;
			expected = this.ext.getBond().getAtom(0);
		} else {
			atomIdx = mc.getAtomsMapping().get(this.idx1);
			neiIdx = this.idx2;
			this.expected = this.ext.getBond().getAtom(1);
		}
		
		// Search in neighborhood.
		IAtom atom = this.mol.getAtom(atomIdx);
		List<IAtom> neighbors = this.mol.getConnectedAtomsList(atom);
		this.neiAtom = null;
		
		// Adding with two atoms already in the previous mapped chain
		if (neiIdx != -1) {
			this.neiAtom = this.mol.getAtom(mc.getAtomsMapping().get(neiIdx));
			
			if (!neighbors.contains(this.neiAtom)) {
				throw new AtomNotFound();
			}
		}
		// Adding with only one atom in the previous matching
		else {
			for (IAtom a : neighbors) {
				if (mc.getAtomsMapping().contains(this.mol.getAtomNumber(a)))
					continue;
				
				IBond bond = this.mol.getBond(atom, a);
						
				if (this.expected.getSymbol().equals(a.getSymbol())) {
					if (bond.getOrder() == this.ext.getBond().getOrder() &&
							this.expected.getFlag(CDKConstants.ISAROMATIC) == a.getFlag(CDKConstants.ISAROMATIC) &&
								this.expected.getImplicitHydrogenCount().intValue() <= a.getImplicitHydrogenCount().intValue()) {
						this.neiAtom = a;
						this.neiHydro = this.expected.getImplicitHydrogenCount().intValue();
						
						if (this.expected.getImplicitHydrogenCount().intValue() == a.getImplicitHydrogenCount().intValue()) {
							this.matchingType = MatchingType.EXACT;
							break;
						} else if (type != MatchingType.EXACT) {
							this.matchingType = MatchingType.STRONG;
							break;
						}
					} else if (type == MatchingType.LIGHT) {
						this.neiAtom = a;
						this.neiHydro = 0;
						this.matchingType = MatchingType.LIGHT;
						break;
					}
				}
			}
			if (this.neiAtom == null) {
				throw new AtomNotFound();
			}
		}
		
		this.bond = this.mol.getBond(atom, neiAtom);
	}
	
	public void addInMc (MappedChain mc) {
		mc.getAtomsMapping().add(this.mol.getAtomNumber(this.neiAtom));
		mc.getHydrogensMapping().put(this.mol.getAtomNumber(this.neiAtom), this.neiHydro);
		mc.getBondsMapping().add(this.mol.getBondNumber(this.bond));
		mc.getMatchings().add(this.matchingType);
	}

	@Override
	public void apply(MappedChain mc, MatchingType type) throws AtomNotFound {
		this.searchAtoms(mc, type);
		this.addInMc(mc);
	}

	@Override
	public MappedChain applyAndClone(MappedChain mc, MatchingType type) throws AtomNotFound {
		this.searchAtoms(mc, type);
		MappedChain clone = new MappedChain(mc);
		this.addInMc(clone);
		return clone;
	}

	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJSON() {
		JSONObject jso = new JSONObject();
		jso.put("from", this.getFrom().getId());
		jso.put("type", "extension");
		jso.put("ext", this.ext.getSerial());
		jso.put("idx1", this.idx1);
		jso.put("idx2", this.idx2);
		return jso;
	}
	
}
