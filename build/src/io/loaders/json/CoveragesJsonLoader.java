package io.loaders.json;

import java.util.ArrayList;
import java.util.List;

import model.ChemicalObject;
import model.Family;
import model.Residue;
import model.graph.ContractedGraph;
import model.graph.ContractedGraph.Vertex;

import org._3pq.jgrapht.edge.UndirectedEdge;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import algorithms.utils.Coverage;
import algorithms.utils.Match;
import db.CoveragesDB;
import db.DB;
import db.FamilyDB;
import db.ResiduesDB;

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
		obj.put("peptide", new Integer(cov.getChemicalObject().getId()));
		obj.put("matches", this.getJSONMatches(cov));
		obj.put("graph", this.getJSONGraph(cov));
		
		array.add(obj);
		return array;
	}

	@SuppressWarnings("unchecked")
	private JSONArray getJSONMatches(Coverage cov) {
		JSONArray resMatchs = new JSONArray();
		for (Match match : cov.getUsedMatches()) {
			JSONObject value = new JSONObject();
			
			value.put("residue", new Integer(match.getResidue().getId()));
			
			JSONObject jMatch = new JSONObject();
			
			JSONArray atoms = new JSONArray();
			for (int a : match.getAtoms()) {
				JSONObject atom = new JSONObject();
				atom.put("a", a);
				atom.put("h", match.getHydrogensFrom(a));
				atoms.add(atom);
			}
			jMatch.put("atoms", atoms);
			
			JSONArray bonds = new JSONArray();
			for (int b : match.getBonds())
				bonds.add(b);
			jMatch.put("bonds", bonds);

			value.put("match", jMatch);
			resMatchs.add(value);
		}
		
		return resMatchs;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject getJSONGraph(Coverage cov) {
		ContractedGraph cg = new ContractedGraph(cov);
		JSONObject graph = new JSONObject();
		
		List<Vertex> verticiesOrder = new ArrayList<>();
		JSONArray monos = new JSONArray();
		for (Object o : cg.vertexSet()) {
			Vertex v = (Vertex)o;
			verticiesOrder.add(v);
			Residue res = v.res;
			if (res != null) {
				Family fam = null;
				try {
					fam = this.families.getObject(res.getMonoName());
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				monos.add(fam.getName());
			} else
				monos.add("?");
		}
		graph.put("monos", monos);
		
		JSONArray links = new JSONArray();
		for (Object o : cg.edgeSet()) {
			JSONObject link = new JSONObject();
			
			UndirectedEdge e = (UndirectedEdge)o;
			int mono1 = verticiesOrder.indexOf((Vertex)e.getSource());
			int mono2 = verticiesOrder.indexOf((Vertex)e.getTarget());
			JSONArray idxs = new JSONArray();
			idxs.add(mono1);
			idxs.add(mono2);
			link.put("idxs", idxs);
			
			links.add(link);
		}
		graph.put("links", links);
		
		return graph;
	}

}
