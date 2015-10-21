package io.loaders.json;

import model.Monomer;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import db.MonomersDB;

public class MonomersJsonLoader extends AbstractJsonLoader<MonomersDB, Monomer> {

	public static MonomersJsonLoader loader = new MonomersJsonLoader();
	
    private MonomersJsonLoader() {}

	@Override
	protected Monomer objectFromJson(JSONObject jso) {
		Monomer mono = new Monomer(
				(String)jso.get("name"),
				(String)jso.get("desc"),
				(String)jso.get("smiles")
		);
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
		jso.put("smiles", obj.getSMILES());
		
		JSONArray array = new JSONArray();
		array.add(jso);
		
		return array;
	}
	
}
