package io.loaders.csv;

import java.util.Map;

import model.Family;
import model.Family.Link;
import model.Monomer;
import model.Residue;
import db.FamilyDB;
import db.MonomersDB;
import db.RulesDB;

@Deprecated
public class ResiduesIO extends AbstractCsvLoader<FamilyDB, Family> {

	//private RulesDB rules;
	private MonomersDB monos;

	public ResiduesIO(RulesDB rules, MonomersDB monos) {
		//sthis.rules = rules;
		this.monos = monos;
	}
	

	@Override
	protected FamilyDB createDB() {
		return new FamilyDB();
	}
	
	/* ---------------------------------------------------------------------
	 * ---------------------------------------------------------------------
	 * ------------------------------Load-----------------------------------
	 * ---------------------------------------------------------------------
	 * ---------------------------------------------------------------------
	 */

	@Override
	protected Family objectFromCSV(Map<String, String> obj) {
		// Residue Construction
		Residue res = Residue.constructResidue(obj.get("monomer"), obj.get("smarts"));
		res.setIdx(new Integer(obj.get("idx")));
		
		/*for (String name : obj.get("links").split(",")) {
			Rule rule;
			try {
				rule = this.rules.getObject(name);
				// TODO : rules back
				//res.addLink(rule);
			} catch (UnknownObjectException e) {
				System.err.println("Unknown link " + name);
			}
		}*/
		
		
		// Family construction
		Family fam = new Family();
		try {
			for (String name : obj.get("family").split(",")) {
				Monomer m = this.monos.getObject(name);
				fam.addMonomer(m);
			}
		} catch (NullPointerException e) {
			System.err.println("Unloaded residue " + res.getMonoName());
		}
		fam.addResidue(res);
		
		if (!"".equals(obj.get("depandances")))
			for (String idx : obj.get("depandances").split(","))
				fam.addDependance(new Integer (idx), new Integer(res.getId()));
		
		return fam;
	}

	@Override
	protected String getObjectId(Family tObj) {
		return tObj.getName();
	}
	
	
	/* ---------------------------------------------------------------------
	 * ---------------------------------------------------------------------
	 * ------------------------------Save-----------------------------------
	 * ---------------------------------------------------------------------
	 * ---------------------------------------------------------------------
	 */

	@Override
	protected String getHeader() {
		return "idx;name;monomer;smarts;links;family;depandances";
	}

	@Override
	protected StringBuffer toCsv(Family obj) {
		StringBuffer sb = new StringBuffer();
		
		for (Residue res : obj.getResidues())
			sb.append(this.residueToCSV(res, obj) + "\n");
		
		return sb;
	}
	
	private String residueToCSV (Residue res, Family family) {
		String str = "";
		
		str += res.getId() + ";";
		str += res.getName() + ";";
		str += res.getMonoName() + ";";
		str += res.getSMILES() + ";";
		boolean first = true;
		// TODO : rules back
		/*for (Rule r : res.getLinks()) {
			if (first)
				first = false;
			else
				str += ",";
			str += r.getName();
		}*/
		
		if (family != null) {
			first = true;
			
			str += ";" + family.getMonomers().get(0).getCode();
			for  (int i=1 ; i<family.getMonomers().size() ; i++)
				str += "," + family.getMonomers().get(i).getCode();
			str += ";";
			for (Link link : family.getDepandances())
				if (link.getTo().intValue() == new Integer(res.getId()).intValue()) {
					if (!first)
						str += ",";
					else
						first = false;
					
					str += link.getFrom();
				}
		}
		
		return str;
	}
	
}
