package io.loaders.json;

import model.Family;
import model.Residue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import algorithms.isomorphism.chains.BondAdd;
import algorithms.isomorphism.chains.Chain;
import algorithms.isomorphism.chains.ChainAdd;
import algorithms.isomorphism.chains.ChainsDB;
import algorithms.isomorphism.chains.Extension;
import algorithms.isomorphism.chains.FamilyChainsDB;
import algorithms.isomorphism.chains.HydrogenAdd;
import db.FamilyDB;
import db.ResiduesDB;

public class FamilyChainIO extends AbstractJsonLoader<ChainsDB, FamilyChainsDB> {

	private FamilyDB families;
	private ResiduesDB residues;

	public FamilyChainIO(FamilyDB families) {
		this.families = families;
		this.residues = this.families.getResidues();
	}
	
	@Override
	protected FamilyChainsDB objectFromJson(JSONObject jso) {
		// FC creation
		String famName = (String)jso.get("family");
		if (famName.contains("€"))
			famName = famName.split("€")[0];
		Family fam = this.families.getObject(famName);
		FamilyChainsDB fc = new FamilyChainsDB(fam);
		
		// Roots
		JSONObject jsonRoots = (JSONObject)jso.get("roots");
		for (Object objIdx : jsonRoots.keySet()) {
			String idx = (String)objIdx;
			fc.addRootChain(this.residues.getObject(idx), new Chain((String)jsonRoots.get(objIdx)));
		}
		
		// Adds
		JSONObject jsonAdds = (JSONObject)jso.get("extensions");
		for (Object objIdx : jsonAdds.keySet()) {
			String idx = (String)objIdx;
			Residue current = this.residues.getObject(idx);
			
			JSONArray jsonAddsList = (JSONArray)jsonAdds.get(objIdx);
			for (Object obj : jsonAddsList) {
				JSONObject add = (JSONObject)obj;
				
				Residue from = this.residues.getObject((String)add.get("from"));
				ChainAdd ca = null;
				
				// TODO : Change it for better usage of genericity.
				switch ((String)add.get("type")) {
				case "hydrogen":
					ca = new HydrogenAdd(
							from,
							((Number)add.get("idx")).intValue(),
							((Number)add.get("num")).intValue()
					);
					break;
				case "extension":
					ca = new BondAdd(
							from,
							new Extension((String)add.get("ext")),
							((Number)add.get("idx1")).intValue(),
							((Number)add.get("idx2")).intValue()
					);
					break;
				default:
					break;
				}
				fc.addAnAdd(current, ca);
			}
		}
		return fc;
	}

	@Override
	protected String getObjectId(FamilyChainsDB fc) {
		return fc.getFamily().getJsonName();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONArray getArrayOfElements(FamilyChainsDB fc) {
		JSONArray jsa = new JSONArray();
		jsa.add(fc.toJSON());
		return jsa;
	}

	@Override
	protected ChainsDB createDB() {
		return new ChainsDB();
	}

}
