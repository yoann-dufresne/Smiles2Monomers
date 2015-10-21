package io.loaders.csv;

import java.util.Map;

import model.Monomer;
import model.Polymer;
import model.graph.MonomerGraph;
import model.graph.MonomerGraph.MonomerLinks;
import db.MonomersDB;
import db.PeptidesDB;



public class PeptideCsvLoader extends AbstractCsvLoader<PeptidesDB, Polymer> {
	
	private MonomersDB monos;
	
	public PeptideCsvLoader(MonomersDB monos) {
		this.monos = monos;
	}
	

	@Override
	protected Polymer objectFromCSV(Map<String, String> obj) {
		Monomer[] monomers = parseMonomers (obj.get("monomeres_code"), obj.get("monomeres_smiles"));
		MonomerGraph g = new MonomerGraph(monomers);
		
		// Parse Graph
		String[] split = obj.get("graph").split("@");
		for (int i=1 ; i<split.length ; i++) {
			String[] idxs = split[i].split(",");
			for (int j=0 ; j<idxs.length ; j++) {
				int mono1 = i-1;
				int mono2 = new Integer(idxs[j].replaceAll(" ", ""));
				boolean exists = false;
				
				for (MonomerLinks link : g.links)
					if ((link.mono1 == mono1 && link.mono2 == mono2) ||
						(link.mono2 == mono1 && link.mono1 == mono2))
						exists = true;
				
				if (!exists)
					g.createLink(mono1, mono2);
			}
		}
		
		Polymer pep = new Polymer(
				new Integer(obj.get("id_peptide")),
				obj.get("name"),
				obj.get("smiles"),
				monomers
		);
		pep.setGraph(g);
		
		return pep;
	}

	@Override
	protected String getObjectId(Polymer tObj) {
		return tObj.getId();
	}

	@Override
	protected PeptidesDB createDB() {
		return new PeptidesDB();
	}
	
	
	private Monomer[] parseMonomers(String monoCodes, String smiles) {
		String[] names = monoCodes.split("\\|");
		Monomer[] monomers = new Monomer[names.length];
		
		for (int i=0 ; i<names.length ; i++)
			if (this.monos.contains(names[i]))
				try {
					monomers[i] = this.monos.getObject(names[i]);
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			else
				monomers[i] = new Monomer(
						names[i],
						"",
						""
				);
		
		return monomers;
	}

	public String graphToCSV(MonomerGraph g) {
		// Apv,Hiv,Ala,dh-Ala@1,3@0,2@1,3@0,2
		String graph = "";
		for (Monomer m : g.nodes) {
			if (!"".equals(graph))
				graph += ',';
			graph += m.getCode();
		}
		
		for (int id=0 ; id < g.nodes.length ; id++) {
			graph += "@";
			for (MonomerLinks ml : g.links) {
				if (ml.mono1 == id)
					graph += ml.mono2 + ",";
				else if (ml.mono2 == id)
					graph += ml.mono1 + ",";
			}
			graph = graph.substring(0, graph.length()-1);
		}
		
		return graph;
	}

	@Override
	protected StringBuffer toCsv(Polymer obj) {
		StringBuffer sb = new StringBuffer();
		sb.append(obj.getId() + ";");
		sb.append(obj.getName() + ";");
		sb.append(this.graphToCSV(obj.getGraph()) + ";");
		
		String monosCodes = "";
		String monosSmiles = "";
		for (Monomer mono : obj.getMonomeres()) {
			monosCodes += "|" + mono.getCode();
			monosSmiles += "|" + mono.getSMILES();
		}
		
		sb.append(monosCodes.substring(1) + ";");
		sb.append(monosSmiles.substring(1));
		
		return sb;
	}


	@Override
	protected String getHeader() {
		return "id_peptide;name;graph;smilesmonomeres_code;monomeres_smiles";
	}
	
}
