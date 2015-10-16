package main;

import io.loaders.json.FamilyChainIO;
import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PolymersJsonLoader;
import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;
import algorithms.isomorphism.chains.ChainLearning;
import db.FamilyDB;
import db.MonomersDB;
import db.PolymersDB;
import db.RulesDB;

public class ChainsCreation {

	public static void main(String[] args) {
		String monomersFile = null;
		String rulesFile = null;
		String peptidesFile = null;
		String residuesFile = null;
		String chainsFile = null;
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
			chainsFile = args[4];
			size = new Integer(args[5]);
			if (args.length > 7 && args[6].equals("-v"))
				verbose = true;
		}
		
		// --- Loadings --- 
		System.out.println("--- Loading ---");
		MonomersDB monos = new MonomersJsonLoader().loadFile(monomersFile);
		RulesDB rules = RulesJsonLoader.loader.loadFile(rulesFile);
		
		ResidueJsonLoader rjl = new ResidueJsonLoader(rules, monos);
		FamilyDB families = rjl.loadFile(residuesFile);
		
		PolymersJsonLoader pjl = new PolymersJsonLoader(monos);
		PolymersDB learningBase = pjl.loadFile(peptidesFile);
		
		// --- Main part : Algorithms --- 
		System.out.println("--- Learning ---");
		ChainLearning.verbose = verbose;
		ChainLearning learning = new ChainLearning(learningBase);
		learning.setMarkovianSize(size);
		learning.learn(families);
		
		// --- Save ---
		System.out.println("--- Saving ---");
		FamilyChainIO fcio = new FamilyChainIO(families);
		fcio.saveFile(learning.getDb(), chainsFile);
	}
	
}
