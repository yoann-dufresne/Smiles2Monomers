package main;

import io.isomorphism.BlocsIOJson;
import io.loaders.json.CoveragesJsonLoader;
import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PeptideJsonLoader;
import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;
import model.Polymer;
import algorithms.MonomericSpliting;
import algorithms.isomorphism.blocs.BlocsDB;
import algorithms.utils.Coverage;
import db.CoveragesDB;
import db.FamilyDB;
import db.MonomersDB;
import db.PeptidesDB;
import db.RulesDB;

public class AtomicToMonomeric {

	public static void main(String[] args) {
		if (args.length < 7) {
			System.err.println("Command line :\n\tjava main.AtomicToMonomeric <monomersFile> <peptidesFile> <rulesFile> <residuesFile> <blocsFile> <residuesMappingFiles> <outfile> [-v]");
			System.exit(42);
		}
		// Parse arguments
		String monoDBname = args[0];
		String pepDBname = args[1];
		String rulesDBname = args[2];
		String residuesDBname = args[3];
		String blocsDBname = args[4];
		String mappingsDBname = args[5];
		String outfile = args[6];
		
		boolean verbose = false;
		if (args.length > 7 && args[7].equals("-v"))
			verbose = true;
		
		// Loading databases
		System.out.println("--- Loading ---");
		// Maybe loading can be faster for the learning base, using serialized molecules instead of CDK SMILES parsing method.
		long loadingTime = System.currentTimeMillis();
		MonomersDB monoDB = MonomersJsonLoader.loader.loadFile(monoDBname);
		PeptideJsonLoader pjl = new PeptideJsonLoader(monoDB);
		PeptidesDB pepDB = pjl.loadFile(pepDBname);
		RulesDB rulesDB = RulesJsonLoader.loader.loadFile(rulesDBname);
		ResidueJsonLoader rjl = new ResidueJsonLoader(rulesDB, monoDB);
		FamilyDB families = rjl.loadFile(residuesDBname);
		BlocsDB blocs = BlocsIOJson.loader.loadBlocs(blocsDBname);
		BlocsIOJson.loader.addMappings(mappingsDBname, blocs, families.getResidues());
		
		// Create monomer splits
		CoveragesDB coverages = new CoveragesDB();

		MonomericSpliting.setVerbose(verbose);
		MonomericSpliting ms = new MonomericSpliting(families, blocs);
		//MonomericSpliting ms = new MonomericSpliting(rulesDBname, residuesDBname, blocsDBname, mappingsDBname, monoDB);
		loadingTime = (System.currentTimeMillis() - loadingTime) / 1000;
		
		System.out.println("--- Calculate coverages ---");
		long totalTime = System.currentTimeMillis();
		for (Polymer pep : pepDB.getObjects()) {
			// Calculate coverage
			long time = System.currentTimeMillis();
			ms.setAllowLightMatchs(true);
			ms.calculateCoverage(pep);
			if (verbose)
				System.out.println("Coverage performed in " + (System.currentTimeMillis() - time));
			Coverage cov = ms.getCoverage();
			
			cov.calculateGreedyCoverage();
			if (verbose)
				System.out.println("Coverage : " + cov.getCoverageRatio());
			coverages.addObject(cov.getId(), cov);
		}
		if (verbose)
			System.out.println();

		long totalMemory = Runtime.getRuntime().totalMemory();
		long currentMemory = totalMemory-Runtime.getRuntime().freeMemory();
		
		System.out.println("Total time to load datas : " + loadingTime + "s");
		System.out.println("Total time to coverage : " + ((System.currentTimeMillis() - totalTime) / 1000) + "s");
		System.out.println("Total space usage : " +
			Math.ceil(100 * new Double(currentMemory)/Math.pow(2, 30))/100.0 + "/" +
			Math.ceil(100 * new Double(totalMemory)/Math.pow(2, 30))/100.0 + " (Go)");
		System.out.println();
		
		System.out.println("--- Create results ---");
		CoveragesJsonLoader cjl = new CoveragesJsonLoader(pepDB, families);
		cjl.saveFile(coverages, outfile);
		
		System.out.println("--- Program ended ---");
	}
	
}
