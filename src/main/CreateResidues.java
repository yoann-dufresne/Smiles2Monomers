package main;

import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;
import algorithms.ResidueCreator;
import db.FamilyDB;
import db.MonomersDB;
import db.RulesDB;

public class CreateResidues {

	public static void main(String[] args) {
		boolean verbose = false;
		
		if (args.length < 3) {
			System.err.println("Incorrect usage. Use : java main.CreateSMARTS <monomersDB> <rulesDB> <outfile> [-v]");
			System.exit(1);
		}
		
		for (int i=3 ; i<args.length ; i++)
			if ("-v".equals(args[i]))
				verbose = true;
		
		String monosDBName = args[0];
		String rulesDBName = args[1];
		String outFile = args[2];
		
		System.out.println("--- Loading ---");
		RulesDB rules = RulesJsonLoader.loader.loadFile(rulesDBName);
		ResidueCreator rc = new ResidueCreator(rules);
		MonomersDB monos = new MonomersJsonLoader().loadFile(monosDBName);
		rc.setVerbose(verbose);
		
		System.out.println("--- Residues creation ---");
		
		FamilyDB families = rc.createResidues(monos);
		
		System.out.println("--- Saving ---");
		
		ResidueJsonLoader rjl = new ResidueJsonLoader(rules, monos);
		rjl.saveFile(families, outFile);
	}
	
}
