package io.loaders.json;

import model.Monomer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import db.MonomersDB;

public class MonomersJsonLoader extends AbstractJsonLoader<MonomersDB, Monomer> {

	public MonomersJsonLoader () {};
	
    public MonomersJsonLoader(boolean coordinates) {
    	Monomer.setComputeCoordinates(coordinates);
    }

	@Override
	protected Monomer objectFromJson(JSONObject jso) {
		// afl: utilisation du fichier monomers.json(code,nameAA,smiles...) au lieu de (id,name,smiles) 
		// pour avoir + d'infos et renvoyer le code des monos plutot que leur nom
		
		//	String name = jso.containsKey("name") ? (String)jso.get("name") :
		//		jso.containsKey("code") ? (String)jso.get("code") : null; 
		//	String desc = jso.containsKey("desc") ? (String)jso.get("desc") :
		//		jso.containsKey("code") ? (String)jso.get("code") : null;
		
		String code = jso.containsKey("id") ? (String)jso.get("id") :
			jso.containsKey("code") ? (String)jso.get("code") : null; 
		String name = jso.containsKey("name") ? (String)jso.get("name") :
			jso.containsKey("nameAA") ? (String)jso.get("nameAA") : null; 
			
		if ("".equals((String)jso.get("smiles"))) {
			System.err.println("No smiles for " + ((String)jso.get("name")));
			return null;
		} else if (((String)jso.get("smiles")).contains(".")) {
			System.err.println("The smiles for " + ((String)jso.get("name")) + " contains character '.'");
			System.err.println("The '.' means that the smiles is composed of more than one molecule.");
			System.err.println("Please split the smiles in two distinct smiles.");
			return null;
		}
		
		Monomer mono = new Monomer(
				code,
				name,
				(String)jso.get("smiles")
		);
		if (mono.getMolecule().getAtomCount() < 2)
			return null;
		//System.out.println(mono);
		return mono;
	}

	@Override
	protected String getObjectId(Monomer tObj) {
		return tObj.getId();
	}

	@Override
	protected MonomersDB createDB() {
		return new MonomersDB();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONArray getArrayOfElements(Monomer obj) {
		JSONObject jso = new JSONObject();
		
		jso.put("name", obj.getCode());
		jso.put("desc", obj.getName());
		jso.put("smiles", obj.getSmiles());
		
		JSONArray array = new JSONArray();
		array.add(jso);
		
		return array;
	}
	
}
