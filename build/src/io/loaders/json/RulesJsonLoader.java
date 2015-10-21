package io.loaders.json;

import model.Rule;
import model.Rule.Replacement;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import db.RulesDB;

public class RulesJsonLoader extends AbstractJsonLoader<RulesDB, Rule>{

	public static RulesJsonLoader loader = new RulesJsonLoader();

	private RulesJsonLoader() {}
	
	@Override
	protected Rule objectFromJson(JSONObject obj) {
		JSONArray sWeights = (JSONArray)obj.get("weights");
		int[] weights = new int[sWeights.size()];
		for (int i=0 ; i<weights.length ; i++)
			weights[i] = ((Number)sWeights.get(i)).intValue();
		
		JSONArray array = (JSONArray)obj.get("transformations");
		String[] transformations = new String[array.size()];
		for (int i=0 ; i<transformations.length ; i++)
			transformations[i] = (String)array.get(i);
		
		array = (JSONArray)obj.get("exclusion");
		String[] exclusions = new String[array.size()];
		for (int i=0 ; i<array.size() ; i++)
			exclusions[i] = (String)array.get(i);
		
		return new Rule(
				(String)obj.get("name"),
				(String)obj.get("formula"),
				transformations,
				weights,
				exclusions,
				(Boolean)obj.get("alone")
			);
	}

	@Override
	protected String getObjectId(Rule tObj) {
		return tObj.getName();
	}

	@Override
	protected RulesDB createDB() {
		return new RulesDB();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONArray getArrayOfElements(Rule obj) {
		JSONObject jso = new JSONObject();
		
		jso.put("name", obj.getName());
		jso.put("formula", obj.getFormula());
		jso.put("alone", obj.getAlone());
		
		JSONArray exclusions = new JSONArray();
		for (String exclu : obj.getExclusion())
			exclusions.add(exclu);
		jso.put("exclusion", exclusions);
		
		JSONArray transformations = new JSONArray();
		for (Replacement rep : obj.getReplacements())
			transformations.add(rep.formula);
		jso.put("transformations", transformations);
		
		JSONArray weights = new JSONArray();
		for (int w : obj.getWeights())
			weights.add(new Integer(w));
		jso.put("weights", weights);
		
		JSONArray array = new JSONArray();
		array.add(jso);
		
		return array;
	}
	
}
