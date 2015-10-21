package io.loaders.csv;

import java.util.Map;

import model.Rule;
import model.Rule.Replacement;
import db.RulesDB;

public class RulesCsvLoader extends AbstractCsvLoader<RulesDB, Rule> {
	
	public static RulesCsvLoader loader = new RulesCsvLoader();

	private RulesCsvLoader() {}

	@Override
	protected Rule objectFromCSV(Map<String, String> obj) {
		String[] sWeights = obj.get("weights").split("\\|");
		int[] weights = new int[sWeights.length];
		for (int i=0 ; i<sWeights.length ; i++)
			weights[i] = new Integer(sWeights[i]);
		
		return new Rule(
				obj.get("name"),
				obj.get("formula"),
				obj.get("transformations").split("\\|"),
				weights,
				obj.get("exclusion").split(","),
				new Boolean(obj.get("alone"))
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

	@Override
	protected StringBuffer toCsv(Rule obj) {
		StringBuffer sb = new StringBuffer();
		
		sb.append(obj.getName() + ";");
		sb.append(obj.getFormula() + ";");
		
		boolean first = true;
		for (Replacement rep : obj.getReplacements()) {
			if (first)
				first = false;
			else
				sb.append(',');
			sb.append("|" + rep.formula);
		}
		sb.append(';');
		
		sb.append(obj.getExclusion() + ";");
		
		first = true;
		for (int val : obj.getWeights()) {
			if (first)
				first = false;
			else
				sb.append(',');
			sb.append("," + val);
		}
		sb.append(';');
		
		sb.append(obj.getAlone());
		
		return sb;
	}

	@Override
	protected String getHeader() {
		return "name;formula;transformations;exclusion;weights;alone";
	}
	
}
