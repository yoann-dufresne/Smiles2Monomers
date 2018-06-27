package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import algorithms.ResidueCreator;
import algorithms.isomorphism.chains.ChainLearning;
import db.FamilyDB;
import db.MonomersDB;
import db.PolymersDB;
import db.ResiduesDB;
import db.RulesDB;
import io.loaders.json.FamilyChainIO;
import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PolymersJsonLoader;
import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;
import io.loaders.serialization.MonomersSerialization;
import model.Family;
import model.Monomer;
import model.Residue;
import model.Rule;

public class PreComputation {

	public static void main(String[] args) {
		//----------------- Parameters ---------------------------
		String rulesDBName = "data/rules.json";
		String monosDBName = "data/monomers.json";
		String jsonPolymers = "data/learning.json";
		
		String serialFolder = "data/serials/";
		String jsonResidues = "data/residues.json";
		String jsonChains = "data/chains.json";
		
		int markovianSize = 3;
		boolean loadResidue = true;
		
		// Parsing
		for (int idx=0 ; idx<args.length ; idx++) {
			if (args[idx].startsWith("-")) {
				switch (args[idx]) {
				case "-rul":
					rulesDBName = args[idx+1];
					break;
				case "-mono":
					monosDBName = args[idx+1];
					break;
				case "-poly":
					jsonPolymers = args[idx+1];
					break;
				case "-res":
					jsonResidues = args[idx+1];
					break;
				case "-cha":
					jsonChains = args[idx+1];
					break;
				case "-serial":
					serialFolder = args[idx+1];
					break;
				case "-markovian":
					markovianSize = new Integer(args[idx+1]);
					break;
				case "-load_residues":
					loadResidue = new Boolean(args[idx+1]);
					break;

				default:
					System.err.println("Wrong option " + args[idx]);
					System.exit(1);
					break;
				}
				
				idx++;
			} else {
				System.err.println("Wrong parameter " + args[idx]);
				System.exit(1);
			}
		}
		
		// File existence
		File file = new File(rulesDBName);
		if (!file.exists()) {
			System.err.println("No file at " + rulesDBName);
			System.exit(1);
		}
		file = new File(monosDBName);
		if (!file.exists()) {
			System.err.println("No file at " + monosDBName);
			System.exit(1);
		}
		file = new File(jsonPolymers);
		if (!file.exists()) {
			System.err.println("No file at " + jsonPolymers);
			System.exit(1);
		}
		
		
		//------------------- Loadings ------------------------
		
		System.out.println("--- Loading ---");
        RulesDB rules = RulesJsonLoader.loader.loadFile(rulesDBName);
        MonomersDB monos = new MonomersJsonLoader(true).loadFile(monosDBName);
        PolymersJsonLoader pjl = new PolymersJsonLoader(monos, false);
        PolymersDB learningBase = pjl.loadFile(jsonPolymers);
        
		
		//----------------- Serializations --------------------
		
        System.out.println("--- Data serialisation ---");
        File folder = new File(serialFolder);
        if (!folder.exists())
        	folder.mkdir();
        	
        MonomersSerialization ms = new MonomersSerialization();
        ms.serialize(monos, serialFolder + "monos.serial");
        
		
		//----------------- residues --------------------------
        
        FamilyDB families = null;
        
        if (!loadResidue) {
	        ResidueCreator rc = new ResidueCreator(rules);
	        rc.setVerbose(false);
	
	        System.out.println("--- Residues creation ---");
	        families = rc.createResidues(monos);
	
	        System.out.println("--- Saving residues ---");
	        ResidueJsonLoader rjl = new ResidueJsonLoader(rules, monos);
	        rjl.saveFile(families, jsonResidues);    
        } else {
        	
        	try {
        		System.out.println("--- Loading residues.json ---");
				JSONArray residues = (JSONArray) JSONValue.parse(new FileReader(new File(jsonResidues)));
								
				families = new FamilyDB();
				families.init(monos);
				
				ResiduesDB residuesDB = new ResiduesDB();
				
				int unloaded = 0;
				
				String previousMono ="";
				
				// Family construction
				Family family = new Family();
				
				
				// remplissage de familiesSet
				for (int i=0; i< residues.size(); i++) {
					JSONObject jsonObject = (JSONObject) residues.get(i);					
					
					
					
					if (!previousMono.equals((String)jsonObject.get("mono"))&&i!=0) {
						
						families.addToFamiliesList(family);	
						family = new Family();						
												
					} 
					
					previousMono = (String)jsonObject.get("mono");
					
					go(family, jsonObject, monos);
					

					
				}
				
				// set ResidueDB
				// pour chaque family et chaque residue
				for(Family fam : families.getObjects()) {					
					for (Residue res : fam.getResidues()) {						
						residuesDB.addObject(res.getId(), res);						
					}					
				}
				
		        families.setResiduesDB(residuesDB);
		        
		        System.out.println(" residuesdb size = "+families.getResidues().size());
		        
		        System.out.println("families size =" + families.size());
				
	        	System.out.println("monos : "+ monos.size());
	            System.out.println("unloaded residues : " + unloaded);
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
        	

        }


        //----------------- chains ----------------------------
        System.out.println("--- Learning chains ---");
        // Adapt residue structures
        
        for (Residue res : families.getResidues().getObjects() ) {
        	res.explicitToImplicitHydrogens();
        }
        
        
        ChainLearning learning = new ChainLearning(learningBase);
        learning.setMarkovianSize(markovianSize);
        learning.learn(families);

        // --- Save ---
        System.out.println("--- Saving chains ---");
        FamilyChainIO fcio = new FamilyChainIO(families);
        fcio.saveFile(learning.getDb(), jsonChains);
        
        System.out.println("--- Ended ---");
        
        System.out.println(families.getResidues().size());
	}
	
	private static void go(Family family, JSONObject jsonObject, MonomersDB monos) {
		
		
		// creation objet Residue à partir de l'objet JSON
		Residue residue = new Residue ((String)jsonObject.get("mono"),(String)jsonObject.get("smarts"),true);					
		residue.setIdx(((Number)jsonObject.get("id")).intValue());

		
		
//		String f = (String)jsonObject.get("family");
//		if (f.contains("€")) {
//
//			for (String name : ((String)jsonObject.get("family")).split("€")) {
//				Monomer m = monos.getObject(name.trim());
//				family.addMonomer(m);
//			}						
//		
//		} else {
//
//			String name = (String)jsonObject.get("family");
//			Monomer m = monos.getObject(name);
//			family.addMonomer(m);
////			System.err.println("Unloaded residue " + residue.getMonoName());
//		}
		
		String name = (String)jsonObject.get("mono");
		Monomer m = monos.getObject(name);
		family.addMonomer(m);
		
		family.addResidue(residue);					
		
		for (Object jso : (JSONArray)jsonObject.get("depandances")) {
			int idx = ((Number)jso).intValue();
			family.addDependance(idx, new Integer(residue.getId()));
		}
		
	}

}
