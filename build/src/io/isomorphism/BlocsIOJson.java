package io.isomorphism;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Residue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import algorithms.isomorphism.MatchingType;
import algorithms.isomorphism.blocs.Bloc;
import algorithms.isomorphism.blocs.BlocsDB;
import algorithms.isomorphism.blocs.MappedBloc;
import db.ResiduesDB;

public class BlocsIOJson {
	
	public static BlocsIOJson loader = new BlocsIOJson();
	
	private BlocsIOJson() {}
	
	// ---------- Loading ----------
	
	public BlocsDB loadBlocs (String filename) {
		BlocsDB db = new BlocsDB();
		JSONArray array = null;
		try {
			array = (JSONArray) JSONValue.parse(new FileReader(new File(filename)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for (Object obj : array) {
			JSONObject jso = (JSONObject)obj;

			Bloc b = new Bloc((String)jso.get("bloc"));
			String serial = b.getSerial();
			db.addObject(serial, b);
			db.setFrequency(b, ((Number)jso.get("frequence")).intValue());
		}
		
		return db;
	}

	public void addMappings(String mappingsDBname, BlocsDB blocs, ResiduesDB residues) {
		JSONArray array = null;
		try {
			array = (JSONArray) JSONValue.parse(new FileReader(new File(mappingsDBname)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for (Object obj : array) {
			JSONObject jso = (JSONObject)obj;
			Residue res = residues.getResidueAt(((Number)jso.get("residue")).intValue());
			
			Bloc bloc;
			try {
				bloc = blocs.getObject((String)jso.get("bloc"));
			} catch (NullPointerException e) {
				System.err.println("Imposible to found bloc with id " + (String)jso.get("bloc") + " in the database");
				continue;
			}
			
			List<Integer> mappedAtoms = new ArrayList<>();
			for (Object id : (JSONArray)jso.get("mapping atoms"))
				mappedAtoms.add(((Number)id).intValue());
			
			List<Integer> mappedBonds = new ArrayList<>();
			for (Object id : (JSONArray)jso.get("mapping bonds"))
				mappedBonds.add(((Number)id).intValue());
			
			List<MatchingType> mappingTypes = new ArrayList<>();
			for (Object id : (JSONArray)jso.get("mapping type"))
				mappingTypes.add(MatchingType.values()[((Number)id).intValue()]);
			
			int i=0;
			Map<Integer, Integer> hydrogens = new HashMap<>();
			for (Object id : (JSONArray)jso.get("hydrogens"))
				hydrogens.put(mappedAtoms.get(i++), ((Number)id).intValue());
			
			MappedBloc mb = new MappedBloc(res, bloc, mappedAtoms, mappedBonds, mappingTypes, hydrogens);
			blocs.setMapping(mb);
		}
	}
	
	// ---------- Saving ----------
	
	@SuppressWarnings("unchecked")
	public void saveAllBlocs (String filename, BlocsDB blocsDB) {
		File file = new File(filename);
		if (file.exists())
			file.delete();
		
		JSONArray array = new JSONArray();
		
		for (Bloc b : blocsDB.getAllBlocsOfSize()) {
			JSONObject jso = new JSONObject();
			
			jso.put("bloc", b.getSerial());
			jso.put("frequence", blocsDB.getFrequency(b));
			jso.put("performance", b.getPerformance(blocsDB));
			
			array.add(jso);
		}
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(filename);
			fw.write(array.toJSONString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void saveAllMappings (String filename, BlocsDB blocsDB) {
		File file = new File(filename);
		if (file.exists())
			file.delete();
		
		JSONArray array = new JSONArray();
		
		for (MappedBloc mb : blocsDB.getAllMappedBlocs())
			array.add(this.MappedBlocToJson(mb));
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(filename);
			fw.write(array.toJSONString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	@SuppressWarnings("unchecked")
	private JSONObject MappedBlocToJson (MappedBloc mb) {
		JSONObject jso = new JSONObject();
		
		jso.put("bloc", mb.getBloc().getSerial());
		jso.put("residue", new Integer(mb.getChemObject().getId()));
		
		JSONArray atoms = new JSONArray();
		JSONArray hydrogens = new JSONArray();
		for (int i=0 ; i<mb.getAtomsMapping().size() ; i++) {
			int idx = mb.getAtomsMapping().get(i);
			atoms.add(idx);
			hydrogens.add(mb.getHydrogensMapping().get(idx));
		}
		jso.put("mapping atoms", atoms);
		jso.put("hydrogens", hydrogens);
		
		
		JSONArray bonds = new JSONArray();
		for (int i=0 ; i<mb.getBondsMapping().size() ; i++)
			bonds.add(mb.getBondsMapping().get(i));
		jso.put("mapping bonds", bonds);
		
		JSONArray types =  new JSONArray();
		for (int i=0 ; i<mb.getMatchings().size() ; i++)
			types.add(mb.getMatchings().get(i).ordinal());
		jso.put("mapping type", types);
		
		return jso;
	}

}