package main;

import io.imgs.PictureCoverageGenerator;
import io.imgs.PictureCoverageGenerator.ColorsMap;
import io.loaders.json.FamilyChainIO;
import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PolymersJsonLoader;
import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Polymer;
import algorithms.MonomericSpliting;
import algorithms.isomorphism.chains.ChainsDB;
import algorithms.utils.Coverage;
import db.CoveragesDB;
import db.FamilyDB;
import db.MonomersDB;
import db.PolymersDB;
import db.RulesDB;

public class GeneratePeptideImage {

	public static void main(String[] args) {
		File coverageDir = new File("/tmp/imgs");
		if (!coverageDir.exists())
			coverageDir.mkdir();
		
		for (File f : coverageDir.listFiles())
			f.delete();
				
		Map<Coverage, ColorsMap> covsColors = new HashMap<>();
		
		MonomersDB monoDB = new MonomersJsonLoader(true).loadFile("data/monomers.json");
		PolymersJsonLoader pjl = new PolymersJsonLoader(monoDB, true);
		PolymersDB pepDB = pjl.loadFile("data/surePeptides.json");
		RulesDB rulesDB = RulesJsonLoader.loader.loadFile("data/rules.json");
		ResidueJsonLoader rjl = new ResidueJsonLoader(rulesDB, monoDB);
		FamilyDB families = rjl.loadFile("data/residues.json");
		FamilyChainIO fcio = new FamilyChainIO(families);
		ChainsDB chains = fcio.loadFile("data/chains.json");
		
		MonomericSpliting.setVerbose(false);
		MonomericSpliting ms = new MonomericSpliting(families, chains, 2, 2, 2);
		
		CoveragesDB cdb = new CoveragesDB();
		for (Polymer pol : pepDB.getObjects()) {
			ms.calculateCoverage(pol);
			cdb.addObject(pol.getId(), ms.getCoverage());
		}
		
		List<Coverage> coverages =  cdb.getObjects();
		
		PictureCoverageGenerator pg = new PictureCoverageGenerator();
		
		for (Coverage cov : coverages) {
			
			
			String norId = "NOR"+String.format("%05d", Integer.parseInt(cov.getChemicalObject().getId()));
			System.out.println("Génération png peptide #" +norId+ ".png...");
			File png = new File(coverageDir.getPath() + "/" + norId + ".png");			
			
//			try {
			ColorsMap colors = pg.createPNG(cov, png);
			covsColors.put(cov, colors);
//			} catch (Exception ex) {
//				System.err.println("--------> Une erreur est survenu avec le peptide "+cov.getChemicalObject().getName() +
//						"("+cov.getChemicalObject().getId()+")");
//			}
		}
		
		
	}

}
