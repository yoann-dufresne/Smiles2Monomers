package io.loaders.json;

import io.parsers.SmilesConverter;

import java.util.Collections;
import java.util.List;

import model.Family;
import model.Family.Link;
import model.Monomer;
import model.Residue;
import model.Rule;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import db.FamilyDB;
import db.MonomersDB;
import db.RulesDB;

public class ResidueJsonLoader extends AbstractJsonLoader<FamilyDB, Family> {

	private RulesDB rules;
	private MonomersDB monos;

	public ResidueJsonLoader (RulesDB rules, MonomersDB monos) {
		this.rules = rules;
		this.monos = monos;
	}

	@Override
	protected Family objectFromJson(JSONObject obj) {
		
		Residue res = new Residue (
				(String)obj.get("mono"),
				(String)obj.get("smarts"),
				true
		);
		res.setIdx(((Number)obj.get("id")).intValue());
		
		JSONArray array = (JSONArray)obj.get("links");
		for (Object o : array) {
			JSONObject jso = (JSONObject)o;
			String name = (String)jso.get("name");
			
			Rule rule = null;
			try {
				rule = this.rules.getObject(name);
			} catch (NullPointerException e) {
				System.err.println("Unknown link " + name);
			}
			
			int idx = ((Number)jso.get("atom")).intValue();
			res.addIdxLink(idx, rule);
		}
		
		
		
		// Family construction
		Family fam = new Family();
		try {
			for (String name : ((String)obj.get("family")).split("€")) {
				Monomer m = this.monos.getObject(name);
				fam.addMonomer(m);
			}
		} catch (NullPointerException e) {
			System.err.println("Unloaded residue " + res.getMonoName());
		}
		fam.addResidue(res);
		
		for (Object jso : (JSONArray)obj.get("depandances")) {
			int idx = ((Number)jso).intValue();
			fam.addDependance(idx, new Integer(res.getId()));
		}
		
		return fam;
	}

	@Override
	protected String getObjectId(Family fam) {
		return fam.getName();
	}

	@Override
	protected FamilyDB createDB() {
		return new FamilyDB();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected JSONArray getArrayOfElements(Family obj) {
		JSONArray array = new JSONArray();
		
		for (Residue res : obj.getResidues()) {
			JSONObject jso = new JSONObject();
			
			jso.put("name", res.getName());
			jso.put("id", new Integer(res.getId()));
			jso.put("mono", res.getMonoName());
			
			List<String> names = obj.getMonoNames();
			Collections.sort(names);
			String familyName = "";
			for (int idx=0 ; idx<names.size() ; idx++) {
				if (idx > 0)
					familyName += '€';
				familyName += names.get(idx);
			}
			jso.put("family", familyName);
			
			JSONArray links = new JSONArray();
			String smiles = this.fillLinksJSO(links, res);
			jso.put("smarts", smiles);
			jso.put("links", links);
			
			JSONArray depandances = new JSONArray();
			for (Link link : obj.getDepandances())
				if (link.getTo().intValue() == new Integer(res.getId()).intValue())
					depandances.add(new Integer(link.getFrom()));
			jso.put("depandances", depandances);
			
			array.add(jso);
		}
		
		return array;
	}

	@SuppressWarnings("unchecked")
	private String fillLinksJSO(JSONArray links, Residue res) {
		IMolecule mol = res.getMolecule();
		AtomContainerManipulator.convertImplicitToExplicitHydrogens(mol);
		String smiles = SmilesConverter.conv.mol2Smiles(mol, false);
		List<IAtom> order = SmilesConverter.conv.getOrder();
		
		for (IAtom a : res.getAtomicLinks().keySet()) {
			JSONObject jso = new JSONObject();
			Rule rule = res.getAtomicLinks().get(a);
			jso.put("name", rule.getName());
			jso.put("atom", order.indexOf(a));
			links.add(jso);
		}
		AtomContainerManipulator.removeHydrogens(mol);

		return smiles;
	}

}
