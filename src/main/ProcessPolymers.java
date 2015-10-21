package main;

import algorithms.MonomericSpliting;
import algorithms.isomorphism.chains.ChainsDB;
import db.CoveragesDB;
import db.FamilyDB;
import db.MonomersDB;
import db.PolymersDB;
import db.RulesDB;
import io.loaders.json.FamilyChainIO;
import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PolymersJsonLoader;
import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;

public class ProcessPolymers {

	public static void main(String[] args) {
		//----------------- Parameters ---------------------------
		String monoDBname = "data/monomers.json";
		String pepDBname = "data/peptides.json";
		String rulesDBname = "data/rules.json";
		String residuesDBname = "data/residues.json";
		String chainsDBFile = "data/chains.json";
		String outfile = "results/coverages.json";
		
		String serialFolder = "data/serials/";
		
		boolean lightMatch = true;
		boolean verbose = false;
		int removeDistance = 2;
		int retryCount = 2;
		int modulationDepth = 2;
		
		// Parsing
		loop:
		for (int idx=0 ; idx<args.length ; idx++) {
			if (args[idx].startsWith("-")) {
				switch (args[idx]) {
				case "-rul":
					rulesDBname = args[idx+1];
					break;
				case "-mono":
					monoDBname = args[idx+1];
					break;
				case "-poly":
					pepDBname = args[idx+1];
					break;
				case "-res":
					residuesDBname = args[idx+1];
					break;
				case "-cha":
					chainsDBFile = args[idx+1];
					break;
				case "-serial":
					serialFolder = args[idx+1];
					break;
				case "-strict":
					lightMatch = false;
					continue loop;
				case "-v":
					verbose = true;
					continue loop;

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
		
		
		//------------------- Loadings ------------------------
		System.out.println("--- Loading ---");
		// Maybe loading can be faster for the learning base, using serialized molecules instead of CDK SMILES parsing method.
		long loadingTime = System.currentTimeMillis();
		MonomersDB monoDB = new MonomersJsonLoader().loadFile(monoDBname);
		PolymersJsonLoader pjl = new PolymersJsonLoader(monoDB);
		PolymersDB pepDB = pjl.loadFile(pepDBname);
		RulesDB rulesDB = RulesJsonLoader.loader.loadFile(rulesDBname);
		ResidueJsonLoader rjl = new ResidueJsonLoader(rulesDB, monoDB);
		FamilyDB families = rjl.loadFile(residuesDBname);
		FamilyChainIO fcio = new FamilyChainIO(families);
		ChainsDB chains = fcio.loadFile(chainsDBFile);
		
		// Create monomer splits
		CoveragesDB coverages = new CoveragesDB();

		MonomericSpliting.setVerbose(verbose);
		MonomericSpliting ms = new MonomericSpliting(families, chains, removeDistance, retryCount, modulationDepth);
		loadingTime = System.currentTimeMillis() - loadingTime;
	}

}
