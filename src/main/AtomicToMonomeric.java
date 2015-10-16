package main;

import io.loaders.json.CoveragesJsonLoader;
import io.loaders.json.FamilyChainIO;
import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PolymersJsonLoader;
import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;
import model.Polymer;
import algorithms.MonomericSpliting;
import algorithms.isomorphism.chains.ChainsDB;
import algorithms.utils.Coverage;
import db.CoveragesDB;
import db.FamilyDB;
import db.MonomersDB;
import db.PolymersDB;
import db.RulesDB;

public class AtomicToMonomeric {

	public static void main(String[] args) {
		if (args.length < 6) {
			System.err.println("Command line :\n\tjava main.AtomicToMonomeric <monomersFile> <peptidesFile> <rulesFile> <residuesFile> <chainsFile> <outfile> [-v]");
			System.exit(42);
		}
		// Parse arguments
		String monoDBname = args[0];
		String pepDBname = args[1];
		String rulesDBname = args[2];
		String residuesDBname = args[3];
		String chainsDBFile = args[4];
		String outfile = args[5];
		
		boolean lightMatch = true;
		int removeDistance = 2;
		int retryCount = 2;
		int modulationDepth = 2;
		
		boolean verbose = true;
		if (args.length > 6 && args[6].equals("-v"))
			verbose = true;
		
		// Loading databases
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

		System.out.println("--- Compute coverages ---");
		
		long algoTime = 0;
		int maxSize = 500;
		long[] times = new long[maxSize];
		int[] nbs = new int[maxSize];
		double couv = 0;
		double cor = 0;
		double totalSize = 0;
		int nbTests = 1;
		for (int test=0 ; test<nbTests ; test++) {
			algoTime = System.currentTimeMillis();
			coverages = new CoveragesDB();
			//System.out.println(test);
			int idx=0;
			for (Polymer pep : pepDB.getObjects()) {
				long pepTime = System.currentTimeMillis();
				// Calculate coverage
				if (verbose)
					System.out.println("Coverage of " + pep.getName());
				idx++;
				long time = System.currentTimeMillis();
				ms.setAllowLightMatchs(lightMatch);
				ms.calculateCoverage(pep);
				Coverage cov = ms.getCoverage();
				
				cov.calculateGreedyCoverage();
				if (verbose) {
					System.out.println("Coverage performed in " + (System.currentTimeMillis() - time));
					System.out.println("Coverage : " + cov.getCoverageRatio());
				} else {
					System.out.print("[");
					int percent = idx*100/pepDB.size();
					for (int i=0 ; i<100 ; i++)
						if (i<percent)
							System.out.print("=");
						else
							System.out.print(" ");
					System.out.print("]\r");
				}
				coverages.addObject(cov.getId(), cov);
				
				couv += cov.getCoverageRatio();
				cor += cov.getCorrectness(families);
				totalSize += pep.getMolecule().getAtomCount();
				pepTime = System.currentTimeMillis() - pepTime;
				nbs[pep.getMolecule().getAtomCount()]++;
				times[pep.getMolecule().getAtomCount()] += pepTime;
			}
	
			System.out.println("\n");
		}
		
		double avgSize = 0;
		for (Polymer p : pepDB.getObjects())
			avgSize += p.getMolecule().getAtomCount();
		System.out.println(avgSize / pepDB.size());
		
		System.out.println();
		System.out.println("Total time to load datas : " + (loadingTime/1000) + "s");
		System.out.println("Loading time per polymer : " + (new Double(loadingTime) / new Double(pepDB.size())));
		long computationTime = System.currentTimeMillis() - algoTime;
		System.out.println("Total time to coverage : " + computationTime/1000 + "s");
		System.out.println("Computation time per polymer : " + (new Double(computationTime) / new Double(pepDB.size())));
		System.out.println();
		
		System.out.println("--- Create results ---");
		CoveragesJsonLoader cjl = new CoveragesJsonLoader(pepDB, families);
		cjl.saveFile(coverages, outfile);
		
		long totalTime = 0;
		int totalNb = 0;
		for (int size=1 ; size<maxSize ; size++) {
			totalTime += times[size];
			totalNb += nbs[size];
			
			if (nbs[size] != 0)
				System.out.println(size + "\t" + (new Double(times[size]) / new Double(nbs[size])));
		}
		System.out.println("\nAvg time : " + (new Double(totalTime) / new Double(totalNb)));
		System.out.println("Avg size : " + (new Double(totalSize) / new Double(totalNb)));
		System.out.println("Coverage : " + (couv/totalNb));
		System.out.println("Corectness : " + (cor/totalNb));
		
		System.out.println("\n--- Program ended ---");
	}
	
	/*private static void createJson (List<ChemicalObject> objs) {
		try {
			FileWriter fw = new FileWriter("data/surePeptides.json");
			JSONArray array = new JSONArray();
			for (ChemicalObject co : objs) {
				JSONObject jso = new JSONObject();
				String id = ""+co.getId();
				for (int i=id.length() ; i<5 ; i++)
					id = "0" + id;
				id = "NOR" + id;
				jso.put("id", id);
				jso.put("name", co.getName());
				jso.put("smiles", co.getSmiles());
				array.add(jso);
			}
			fw.write(array.toJSONString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}/**/
	
}
