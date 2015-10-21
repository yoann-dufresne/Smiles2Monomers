package main;

import io.isomorphism.BlocsIOJson;
import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PeptideJsonLoader;
import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;
import algorithms.isomorphism.blocs.BlocsLearning;
import db.FamilyDB;
import db.MonomersDB;
import db.PeptidesDB;
import db.RulesDB;

public class CalculateBlocs {

	public static void main(String[] args) {
		String monomersFile = null;
		String rulesFile = null;
		String peptidesFile = null;
		String residuesFile = null;
		String blocsFile = null;
		String mappingsFile = null;
		int size = 5;
		
		boolean verbose = false;
		
		/* --- Arguments --- */
		if (args.length < 7) {
			System.err.println("Wrong aguments. Arguments must be like :");
			System.err.println("java main.CalculateBlocs <monomers file> <peptides file> <rules file> <residues file> <out blocs file> <out mappings file> <blocs size> [-v]");
			System.exit(42);
		} else {
			monomersFile = args[0];
			peptidesFile = args[1];
			rulesFile = args[2];
			residuesFile = args[3];
			blocsFile = args[4];
			mappingsFile = args[5];
			size = new Integer(args[6]);
			if (args.length > 7 && args[7].equals("-v"))
				verbose = true;
		}
		
		System.out.println("--- Loading ---");
		MonomersDB monos = MonomersJsonLoader.loader.loadFile(monomersFile);
		RulesDB rules = RulesJsonLoader.loader.loadFile(rulesFile);
		ResidueJsonLoader rjl = new ResidueJsonLoader(rules, monos);
		FamilyDB families = rjl.loadFile(residuesFile);
		PeptideJsonLoader pjl = new PeptideJsonLoader(monos);
		PeptidesDB learningBase = pjl.loadFile(peptidesFile);
		
		System.out.println("--- Learning ---");
		BlocsLearning.setVerbose(verbose);
		BlocsLearning learning = new BlocsLearning(families, learningBase);
		learning.searchForSize(size);
		
		System.out.println("--- Saving ---");
		// JSON !!
		BlocsIOJson.loader.saveAllBlocs(blocsFile, learning.getBlocs());
		BlocsIOJson.loader.saveAllMappings(mappingsFile, learning.getBlocs());
	}
	
}
