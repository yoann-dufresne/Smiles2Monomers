package main;

import io.html.Coverages2HTML;
import io.imgs.ImagesGeneration;
import io.imgs.PictureCoverageGenerator.ColorsMap;
import io.loaders.json.CoveragesJsonLoader;
import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PolymersJsonLoader;
import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;
import io.loaders.serialization.MonomersSerialization;
import io.zip.OutputZiper;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import algorithms.utils.Coverage;
import db.CoveragesDB;
import db.FamilyDB;
import db.MonomersDB;
import db.PolymersDB;
import db.RulesDB;

public class Json2HTML {

	public static void main(String[] args) {
		if (args.length < 6) {
			System.err.println("Command line :\n\tjava main.AtomicToMonomeric <monomersFile> <peptidesFile> <rulesFile> <residuesFile> <coveragesFile> <outType> [outFile]  [-v]");
			System.err.println("  outType can be \"-zip\" or \"-html\"");
			System.exit(42);
		}
		
		String monoDBname = args[0];
		String pepDBname = args[1];
		String rulesDBname = args[2];
		String residuesDBname = args[3];
		String covsFile = args[4];
		boolean zip = args[5].equals("-zip") ? true : false;
		String outFile = null;
		
		if (zip && args.length < 7) {
			System.err.println("Command line :\n\tjava main.AtomicToMonomeric <monomersFile> <peptidesFile> <rulesFile> <residuesFile> <coveragesFile> <outType> [outFile]  [-v]");
			System.err.println("  outType can be \"-zip\" or \"-html\"");
			System.exit(42);
		} else if (zip && args.length >= 7) 
			outFile = args[6];
		
		
		// Loading databases
		System.out.println("--- Loading ---");
		// Maybe loading can be faster for the learning base, using serialized molecules instead of CDK SMILES parsing method.
		long loadingTime = System.currentTimeMillis();
		MonomersDB monoDB = new MonomersJsonLoader(false).loadFile(monoDBname);
		MonomersSerialization ms = new MonomersSerialization();
        ms.deserialize(monoDB, "data/serials/monos.serial");
        
		PolymersJsonLoader pcl = new PolymersJsonLoader(monoDB, true);
		PolymersDB pepDB = pcl.loadFile(pepDBname);
		RulesDB rules = RulesJsonLoader.loader.loadFile(rulesDBname);
		ResidueJsonLoader rjl = new ResidueJsonLoader(rules, monoDB);
		FamilyDB families = rjl.loadFile(residuesDBname);
		loadingTime = (System.currentTimeMillis() - loadingTime) / 1000;
		
		System.out.println("--- Json to HTML ---");
		long creationTime = System.currentTimeMillis();
		CoveragesJsonLoader cl = new CoveragesJsonLoader(pepDB, families);
		CoveragesDB covs = cl.loadFile(covsFile);
		List<Coverage> covsList = covs.getObjects();
		Collections.sort(covsList);
		
		// Common generations
		File imgs = new File("tmp_imgs_" + covsFile.substring(covsFile.lastIndexOf("/")+1, covsFile.lastIndexOf(".")));
		if (!imgs.exists())
			imgs.mkdir();
		ImagesGeneration ig = new ImagesGeneration();
		Map<Coverage, ColorsMap> allColors = ig.generate(imgs, monoDB, covsList);
		
		if (!zip) {
			// HTML
			File resultDir = null;
			if (covsFile.contains("/"))
				resultDir = new File(covsFile.substring(0, covsFile.lastIndexOf("/")));
			else
				resultDir = new File(".");
			
			if (!resultDir.exists())
				resultDir.mkdir();
			
			Coverages2HTML c2h = new Coverages2HTML(covsList, monoDB, families);
			File htmlFile  = new File(resultDir.getPath()+"/test.html");
			c2h.createResults(htmlFile, imgs, allColors);
		} else {
			// Zip File
			OutputZiper oz = new OutputZiper(outFile);
			oz.createZip(imgs.getPath(), covsFile, pepDBname, monoDBname, residuesDBname, allColors);
		}
		
		ig.cleanTmp(imgs);
		
		creationTime = (System.currentTimeMillis() - creationTime) / 1000;
		
		System.out.println();
		System.out.println("Total time to load datas : " + loadingTime + "s");
		System.out.println("Total time to create HTML : " + creationTime + "s");
		System.out.println();
		System.out.println("--- Program ended ---");
		
	}
	
}
