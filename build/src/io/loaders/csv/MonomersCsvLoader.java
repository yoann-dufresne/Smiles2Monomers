package io.loaders.csv;

import java.util.Map;

import model.Monomer;
import db.MonomersDB;


public class MonomersCsvLoader extends AbstractCsvLoader<MonomersDB, Monomer> {
	
	public static MonomersCsvLoader loader = new MonomersCsvLoader();

	private MonomersCsvLoader() {}

	@Override
	protected Monomer objectFromCSV(Map<String, String> obj) {
		return new Monomer(
				obj.get("code"),
				obj.get("name_aa"),
				obj.get("smiles")
			);
	}

	@Override
	protected String getObjectId(Monomer tObj) {
		return tObj.getId();
	}

	@Override
	protected MonomersDB createDB() {
		return new MonomersDB();
	}

	@Override
	protected StringBuffer toCsv(Monomer obj) {
		StringBuffer sb = new StringBuffer();
		
		sb.append(obj.getCode() + ";");
		sb.append(obj.getName_aa() + ";");
		sb.append(obj.getSMILES());
		
		return sb;
	}

	@Override
	protected String getHeader() {
		return "code;name_aa;smiles";
	}
	
}
