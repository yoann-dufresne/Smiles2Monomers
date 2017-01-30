package io.loaders.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;

import algorithms.utils.Coverage;
import algorithms.utils.Match;
import db.CoveragesDB;
import db.DB;
import db.FamilyDB;
import db.ResiduesDB;
import model.ChemicalObject;
import model.Monomer;
import model.Residue;
import model.graph.ContractedGraph;
import model.graph.MonomerGraph;
import model.graph.MonomerGraph.MonomerLinks;

public class CoveragesJsonLoader extends
		AbstractJsonLoader<CoveragesDB, Coverage> {
	
	private DB<? extends ChemicalObject> db;
	private FamilyDB families;
	private ResiduesDB residues;

	public CoveragesJsonLoader(DB<? extends ChemicalObject> db, FamilyDB families) {
		this.db = db;
		this.families = families;
		this.residues = families.getResidues();
	}

	@Override
	protected CoveragesDB createDB() {
		return new CoveragesDB();
	}
	

	@Override
	protected Coverage objectFromJson(JSONObject obj) {
		ChemicalObject co = null;
		try {
			co = this.db.getObject("" + ((Number)obj.get("peptide")).intValue());
		} catch (NullPointerException e) {
			System.err.println(e.getMessage());
			System.err.println("Maybe coverage json and molecule json don't match");
			System.exit(2);
		}
		Coverage cov = new Coverage(co);
		
		JSONArray array = (JSONArray) obj.get("matches");
		for (Object o : array) {
			JSONObject jso = (JSONObject)o;
			Residue res = null;
			try {
				res = this.residues.getObject("" + ((Number)jso.get("residue")).intValue());
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			
			JSONObject jMatch = (JSONObject)jso.get("match");
			Match match = new Match(res);
				
			JSONArray atoms = (JSONArray) jMatch.get("atoms");
			for (Object atObj : atoms) {
				JSONObject atom = (JSONObject) atObj;
				int idx = ((Number)atom.get("a")).intValue();
				match.addAtom(idx);
				match.addHydrogens(idx, ((Number)atom.get("h")).intValue());
			}
			
			JSONArray bonds = (JSONArray) jMatch.get("bonds");
			for (Object boObj : bonds) {
				int bond = ((Number) boObj).intValue();
				match.addBond(bond);
			}
			
			cov.addMatch(match);
		}
		
		cov.calculateGreedyCoverage();
		
		return cov;
	}

	@Override
	protected String getObjectId(Coverage tObj) {
		return tObj.getId();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONArray getArrayOfElements(Coverage cov) {
		JSONArray array = new JSONArray();
		JSONObject obj = new JSONObject();
		
		obj.put("id", cov.getId());
		//obj.put("pepId", cov.getChemicalObject())
		obj.put("peptide", new Integer(cov.getChemicalObject().getId()));
		obj.put("peptideName", cov.getChemicalObject().getName());
		obj.put("atomic_graph", this.getJSONMatches(cov));
		
		obj.put("monomeric_graph", this.getJSONGraph(cov));
		obj.put("coverage", cov.getCoverageRatio());
		
		array.add(obj);
		return array;
	}

	@SuppressWarnings("unchecked")
	private JSONObject getJSONMatches(Coverage cov) {
		JSONObject graph = new JSONObject();
		
		JSONArray atoms = new JSONArray();
		graph.put("atoms", atoms);
		JSONArray bonds = new JSONArray();
		graph.put("bonds", bonds);
		
		for (Match match : cov.getUsedMatches()) {
			// Atoms
			for (int a : match.getAtoms()) {
				JSONObject atom = new JSONObject();
				// CDK informations
				atom.put("cdk_idx", a);
				// Atom informations
				IAtom ia = cov.getChemicalObject().getMolecule().getAtom(a);
				atom.put("name", ia.getSymbol());
				atom.put("hdrogens", match.getHydrogensFrom(a));
				// Residue informations
				atom.put("res", match.getResidue().getId());
				
				atoms.add(atom);
			}
			
			// Bonds
			for (int b : match.getBonds()) {
				IBond ib = cov.getChemicalObject().getMolecule().getBond(b);
				JSONObject bond = new JSONObject();
				
				// CDK informations
				bond.put("cdk_idx", b);
				
				// atoms linked
				JSONArray linkedAtoms = new JSONArray();
				for (IAtom a : ib.atoms()) {
					linkedAtoms.add(cov.getChemicalObject().getMolecule().getAtomNumber(a));
				}
				bond.put("atoms", linkedAtoms);
				bond.put("res", match.getResidue().getId());
				
				bonds.add(bond);
			}
		}
		
		return graph;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject getJSONGraph(Coverage cov) {
		ContractedGraph cg = new ContractedGraph(cov);
		MonomerGraph mg = cg.toMonomerGraph(families);
		
		JSONObject graph = new JSONObject();
		// Monomers
		JSONArray monos = new JSONArray();
		for (Monomer mono : mg.nodes)
			if (mono != null)
				monos.add(mono.getName());
			else
				monos.add("?");
		graph.put("monos", monos);
		
		// Residues (equivalent to monomers)
		JSONArray residues = new JSONArray();
		for (Residue res : mg.residues)
			if (res != null)
				residues.add(res.getId());
			else
				residues.add("?");
		graph.put("residues", residues);
		
		// Links
		JSONArray links = new JSONArray();
		for (MonomerLinks ml : mg.links) {
			JSONObject link = new JSONObject();
			JSONArray idxs = new JSONArray();
			idxs.add(ml.mono1);
			idxs.add(ml.mono2);
			link.put("idxs", idxs);
			
			links.add(link);
		}
		graph.put("links", links);
		
		return graph;
	}

}
