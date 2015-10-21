package io.isomorphism;

import io.parsers.CSVParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Residue;
import algorithms.isomorphism.MatchingType;
import algorithms.isomorphism.blocs.Bloc;
import algorithms.isomorphism.blocs.BlocsDB;
import algorithms.isomorphism.blocs.MappedBloc;
import db.ResiduesDB;

public class BlocsIOCsv {
	
	public static BlocsIOCsv loader = new BlocsIOCsv();
	
	private BlocsIOCsv() {}
	
	public BlocsDB loadBlocs (String filename) {
		List<Map<String, String>> objects = CSVParser.parse(filename);
		BlocsDB db = new BlocsDB();
		
		for (Map<String, String> obj : objects) {
			Bloc b = new Bloc(obj.get("bloc"));
			String serial = b.getSerial();
			db.addObject(serial, b);
			db.setFrequency(b, new Integer(obj.get("frequence")));
		}
		
		return db;
	}
	
	public void saveAllBlocs (String filename, BlocsDB blocsDB) {
		File file = new File(filename);
		if (file.exists())
			file.delete();
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(filename);
			fw.write("bloc;frequence;performance\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (Bloc b : blocsDB.getAllBlocsOfSize()) {
			try {
				fw.write('"' + b.getSerial() + '"' + ";" + blocsDB.getFrequency(b) + ";" + b.getPerformance(blocsDB) + "\n");
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}
		
		try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void saveAllMappings (String filename, BlocsDB blocsDB) {
		File file = new File(filename);
		if (file.exists())
			file.delete();
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(filename);
			fw.write("bloc;residue;mapping atoms;hydrogens;mapping bonds;mapping type\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (MappedBloc mb : blocsDB.getAllMappedBlocs())
			try {
				fw.write(this.MappedBlocToCSV(mb) + '\n');
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String MappedBlocToCSV (MappedBloc mb) {
		int idx = mb.getAtomsMapping().get(0);
		String atoms = "" + idx;
		String hydrogens = "" + mb.getHydrogensMapping().get(idx);
		for (int i=1 ; i<mb.getAtomsMapping().size() ; i++) {
			idx = mb.getAtomsMapping().get(i);
			atoms += "," + idx;
			hydrogens += "," + mb.getHydrogensMapping().get(idx);
		}
		
		String bonds = "" + mb.getBondsMapping().get(0);
		for (int i=1 ; i<mb.getBondsMapping().size() ; i++)
			bonds += "," + mb.getBondsMapping().get(i);
		
		String types = "" + mb.getMatchings().get(0).ordinal();
		for (int i=1 ; i<mb.getMatchings().size() ; i++)
			types += "," + mb.getMatchings().get(i).ordinal();
		
		return '"' + mb.getBloc().getSerial() + "\";" + mb.getChemObject().getId() + ';' + atoms + ';' + hydrogens + ';' + bonds + ';' + types;
	}

	public void addMappings(String mappingsDBname, BlocsDB blocs, ResiduesDB residues) {
		List<Map<String, String>> objects = CSVParser.parse(mappingsDBname);
		for (Map<String, String> object : objects) {
			Residue res = residues.getResidueAt(new Integer(object.get("residue")));
			
			Bloc bloc;
			try {
				bloc = blocs.getObject(object.get("bloc"));
			} catch (NullPointerException e) {
				System.err.println("Imposible to found bloc with id " + object.get("bloc") + " in the database");
				continue;
			}
			
			List<Integer> mappedAtoms = new ArrayList<>();
			for (String s : object.get("mapping atoms").split(","))
				mappedAtoms.add(new Integer(s));
			
			List<Integer> mappedBonds = new ArrayList<>();
			for (String s : object.get("mapping bonds").split(","))
				mappedBonds.add(new Integer(s));
			
			List<MatchingType> mappingTypes = new ArrayList<>();
			for (String s : object.get("mapping type").split(","))
				mappingTypes.add(MatchingType.values()[new Integer(s)]);
			
			int i=0;
			Map<Integer, Integer> hydrogens = new HashMap<>();
			for (String s : object.get("hydrogens").split(","))
				hydrogens.put(mappedAtoms.get(i++), new Integer(s));
			
			MappedBloc mb = new MappedBloc(res, bloc, mappedAtoms, mappedBonds, mappingTypes, hydrogens);
			blocs.setMapping(mb);
		}
	}

}