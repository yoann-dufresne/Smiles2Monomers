package io.loaders.json;

import java.util.ArrayList;
import java.util.List;

import model.Monomer;
import model.Polymer;
import model.graph.MonomerGraph;
import model.graph.MonomerGraph.MonomerLinks;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import db.MonomersDB;
import db.PeptidesDB;

public class PeptideJsonLoader extends AbstractJsonLoader<PeptidesDB, Polymer> {

	private MonomersDB monos;

	public PeptideJsonLoader(MonomersDB monos) {
		this.monos = monos;
	}

	@Override
	protected PeptidesDB createDB() {
		return new PeptidesDB();
	}

	
	@Override
	protected Polymer objectFromJson(JSONObject obj) {
		JSONArray verticies;
		JSONArray edges;
		if (obj.containsKey("graph")) {
			JSONObject graph = (JSONObject)obj.get("graph");
			verticies = (JSONArray)graph.get("V");
			edges = (JSONArray)graph.get("E");
		} else {
			verticies = new JSONArray();
			edges = new JSONArray();
		}
		
		Monomer[] monomers = new Monomer[verticies.size()];
		for (int i=0 ; i<monomers.length ; i++) {
			String name = (String) verticies.get(i);
			try {
				monomers[i] = this.monos.contains(name) ?
						this.monos.getObject(name) :
						new Monomer(name, "", "");
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
		
		MonomerGraph g = new MonomerGraph(monomers);
		for (Object o : edges) {
			JSONArray link = (JSONArray)o;
			g.createLink (
					((Number)link.get(0)).intValue(),
					((Number)link.get(1)).intValue()
			);
		}
		
		Polymer pep =  new Polymer(
				((Number)obj.get("id")).intValue(),
				(String)obj.get("name"),
				(String)obj.get("smiles"),
				monomers
		);
		
		pep.setGraph(g);
		
		return pep;
	}

	
	
	@SuppressWarnings("unchecked")
	public JSONObject graphToJson(MonomerGraph g) {
		JSONObject jso = new JSONObject();
		
		JSONArray nodes = new JSONArray();
		for (Monomer m : g.nodes) {
			nodes.add(m.getCode());
		}
		jso.put("V", nodes);
		
		JSONArray edges = new JSONArray();
		List<MonomerLinks> added = new ArrayList<>(g.links.size());
		for (MonomerLinks ml : g.links) {
			boolean contains = false;
			for (MonomerLinks old : added)
				if ((old.mono1 == ml.mono1 && old.mono2 == ml.mono2) ||
					(old.mono2 == ml.mono1 && old.mono1 == ml.mono2)) {
					contains = true;
					break;
				}
			
			if (!contains) {
				added.add(ml);
				
				JSONArray edge = new JSONArray();
				edge.add(ml.mono1);
				edge.add(ml.mono2);
				
				edges.add(edge);
			}
		}
		jso.put("E", edges);
		
		return jso;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected JSONArray getArrayOfElements(Polymer pep) {
		JSONObject jso = new JSONObject();
		
		jso.put("id", new Integer(pep.getId()));
		jso.put("name", pep.getName());
		jso.put("smiles", pep.getSMILES());
		jso.put("graph", this.graphToJson(pep.getGraph()));
		
		JSONArray array = new JSONArray();
		array.add(jso);
		
		return array;
	}

	@Override
	protected String getObjectId(Polymer tObj) {
		return tObj.getId();
	}
}
