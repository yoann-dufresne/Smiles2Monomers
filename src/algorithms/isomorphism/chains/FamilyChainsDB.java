package algorithms.isomorphism.chains;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import model.Family;
import model.Residue;

public class FamilyChainsDB {

	private Family family;
	private Map<Residue, Chain> rootChains;
	private Map<Residue, List<ChainAdd>> adds;

	public FamilyChainsDB(Family fam) {
		this.family = fam;
		this.rootChains = new HashMap<>();
		this.adds = new HashMap<>();
		for (Residue res : this.family.getResidues())
			this.adds.put(res, new ArrayList<ChainAdd>());
	}
	
	public void addRootChain (Residue root, Chain chain) {
		if (!this.family.getRoots().contains(root)) {
			System.err.println("Residue " + root.getId() + " is not a root of the family " + this.family.getName());
			return;
		}
		this.rootChains.put(root, chain);
	}
	
	public void addAnAdd (Residue res, ChainAdd add) {
		if (!this.family.getResidues().contains(res)) {
			System.err.println("Residue " + res.getId() + " is not a residue of the family " + this.family.getName());
		}
		this.adds.get(res).add(add);
	}
	
	public List<ChainAdd> getAdds (Residue res) {
		if (!this.adds.containsKey(res)) {
			System.err.println("Family " + this.family.getName() + " do not contain residue " + res.getName());
			return new ArrayList<>(1);
		}
		
		return this.adds.get(res);
	}
	
	public Family getFamily() {
		return family;
	}
	
	public Map<Residue, Chain> getRootChains() {
		return rootChains;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject jso = new JSONObject();
		
		// Id
		jso.put("family", this.family.getJsonName());
		
		// Roots
		JSONObject roots = new JSONObject();
		for (Residue res : this.rootChains.keySet()) {
			roots.put(res.getId(), this.rootChains.get(res).getSerial());
		}
		jso.put("roots", roots);
		
		// Extensions
		JSONObject exts = new JSONObject();
		for (Residue res : this.adds.keySet()) {
			JSONArray adds = new JSONArray();
			for (ChainAdd add : this.adds.get(res)) {
				adds.add(add.toJSON());
			}
			exts.put(res.getId(), adds);
		}
		jso.put("extensions", exts);
		
		return jso;
	}

}
