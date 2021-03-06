package io.loaders.json;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import db.MonomersDB;
import db.PolymersDB;
import model.Monomer;
import model.Polymer;
import model.graph.MonomerGraph;
import model.graph.MonomerGraph.MonomerLinks;

public class PolymersJsonLoader extends AbstractJsonLoader<PolymersDB, Polymer> {

	private MonomersDB monos;

	public PolymersJsonLoader(MonomersDB monos) {
		this.monos = monos;
	}
	
	public PolymersJsonLoader(MonomersDB monos, boolean coordinates) {
		this.monos = monos;
		Polymer.setComputeCoordinates(true);
	}

	@Override
	protected PolymersDB createDB() {
		return new PolymersDB();
	}

	
	@SuppressWarnings("unchecked")
	@Override
	protected Polymer objectFromJson(JSONObject obj) {
		if ("".equals((String)obj.get("smiles"))) {
			System.err.println("No smiles for " + ((String)obj.get("name")));
			return null;
		} else if (((String)obj.get("smiles")).contains(".")) {
			System.err.println("The smiles for " + ((String)obj.get("name")) + " contains character '.'");
			System.err.println("The '.' means that the smiles is composed of more than one molecule.");
			System.err.println("Please split the smiles in two distinct smiles.");
			return null;
		}
		
		// --- Parse the graph ---
		JSONArray verticies;
		JSONArray edges;
		if (obj.containsKey("graph")) {
			JSONObject graph = (JSONObject)obj.get("graph");
			// Parse the s2m graph format
			verticies = (JSONArray)graph.get("V");
			edges = (JSONArray)graph.get("E");
		} else if (obj.containsKey("structure")) {
			verticies = new JSONArray();
			edges = new JSONArray();
			
			// Parse the Norine graph format
			String text = (String) ((JSONObject)obj.get("structure")).get("graph");
			String[] split = text.split("@");
			
			// The verticies
			verticies = new JSONArray();
			for (String val : split[0].split(","))
				verticies.add(val);
			
			// The edges
			edges = new JSONArray();
			for (int idx=1 ; idx<split.length ; idx++) {
				String[] links = split[idx].split(",");
				for (String val : links) {
					int link_to = new Integer(val);
					
					if (idx-1 < link_to) {
						JSONArray edge = new JSONArray();
						edge.add(idx-1);
						edge.add(link_to);
						edges.add(edge);
					}
				}
			}
		} else {
			verticies = new JSONArray();
			edges = new JSONArray();
		}
		
		
		// --- Parse the monomer list ---
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
		
		// Create the graph in memory
		MonomerGraph g = new MonomerGraph(monomers);
		for (Object o : edges) {
			JSONArray link = (JSONArray)o;
			g.createLink (
					((Number)link.get(0)).intValue(),
					((Number)link.get(1)).intValue()
			);
		}
		
		int id = 0;
		if (obj.get("id") instanceof String) {
			String sid = (String) obj.get("id");
			id = sid.startsWith("NOR") ? new Integer(sid.substring(3)) : sid.hashCode();
		} else
			id = ((Number)obj.get("id")).intValue();
		Polymer pep =  new Polymer(
				id,
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
		jso.put("smiles", pep.getSmiles());
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
