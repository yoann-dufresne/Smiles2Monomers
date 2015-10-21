package main;

import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PeptideJsonLoader;
import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;
import algorithms.isomorphism.blocs.SequenceLearning;
import db.FamilyDB;
import db.MonomersDB;
import db.PeptidesDB;
import db.RulesDB;

public class SequenceCreation {

	public static void main(String[] args) {
		String monomersFile = null;
		String rulesFile = null;
		String peptidesFile = null;
		String residuesFile = null;
		String blocsFile = null;
		int size = 5;
		
		boolean verbose = false;
		
		/* --- Arguments --- */
		if (args.length < 6) {
			System.err.println("Wrong aguments. Arguments must be like :");
			System.err.println("java main.CalculateBlocs <monomers file> <peptides file> <rules file> <residues file> <out sequences file> <blocs size> [-v]");
			System.exit(42);
		} else {
			monomersFile = args[0];
			peptidesFile = args[1];
			rulesFile = args[2];
			residuesFile = args[3];
			blocsFile = args[4];
			size = new Integer(args[5]);
			if (args.length > 7 && args[6].equals("-v"))
				verbose = true;
		}
		
		// --- Loadings --- 
		System.out.println("--- Loading ---");
		MonomersDB monos = MonomersJsonLoader.loader.loadFile(monomersFile);
		RulesDB rules = RulesJsonLoader.loader.loadFile(rulesFile);
		
		ResidueJsonLoader rjl = new ResidueJsonLoader(rules, monos);
		FamilyDB families = rjl.loadFile(residuesFile);
		
		PeptideJsonLoader pjl = new PeptideJsonLoader(monos);
		PeptidesDB learningBase = pjl.loadFile(peptidesFile);
		
		// --- Main part : Algorithms --- 
		System.out.println("--- Learning ---");
		SequenceLearning.verbose = verbose;
		SequenceLearning learning = new SequenceLearning(learningBase);
		learning.setMarkovianSize(size);
		learning.learn(families);
		
		// --- Save ---
		System.out.println("--- Saving ---");
	}
	
}
