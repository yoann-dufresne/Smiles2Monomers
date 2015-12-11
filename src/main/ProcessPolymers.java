package main;

import java.io.File;
import java.util.Map;

import algorithms.MonomericSpliting;
import algorithms.isomorphism.chains.ChainsDB;
import algorithms.utils.Coverage;
import db.FamilyDB;
import db.MonomersDB;
import db.PolymersDB;
import db.RulesDB;
import io.html.Coverages2HTML;
import io.imgs.ImagesGeneration;
import io.imgs.PictureCoverageGenerator.ColorsMap;
import io.loaders.json.CoveragesJsonLoader;
import io.loaders.json.FamilyChainIO;
import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PolymersJsonLoader;
import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;
import io.loaders.serialization.MonomersSerialization;
import io.zip.OutputZiper;

public class ProcessPolymers {

	public static void main(String[] args) {
		//----------------- Parameters ---------------------------
		String monoDBname = "data/monomers.json";
		String pepDBname = "data/polymers.json";
		String rulesDBname = "data/rules.json";
		String residuesDBname = "data/residues.json";
		String chainsDBFile = "data/chains.json";
		
		String outfile = "results/coverages.json";
		String outfolderName = "results/";
		String imgsFoldername = "images/";
		boolean html = false;
		boolean zip = false;
		
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
				case "-outfile":
					outfile = args[idx+1];
					break;
				case "-outfolder":
					outfolderName = args[idx+1];
					break;
				case "-imgs":
					imgsFoldername = args[idx+1];
					break;
				case "-strict":
					lightMatch = false;
					continue loop;
				case "-v":
					verbose = true;
					continue loop;
				case "-html":
					html = true;
					continue loop;
				case "-zip":
					zip = true;
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
		MonomersDB monoDB = new MonomersJsonLoader(false).loadFile(monoDBname);
		MonomersSerialization ms = new MonomersSerialization();
		ms.deserialize(monoDB, serialFolder + "monos.serial");
		
		boolean d2 = html || zip;
		PolymersJsonLoader pjl = new PolymersJsonLoader(monoDB, d2);
		PolymersDB polDB = pjl.loadFile(pepDBname);
		
		RulesDB rulesDB = RulesJsonLoader.loader.loadFile(rulesDBname);
		
		ResidueJsonLoader rjl = new ResidueJsonLoader(rulesDB, monoDB);
		
		FamilyDB families = rjl.loadFile(residuesDBname); // Need optimizations
		FamilyChainIO fcio = new FamilyChainIO(families);
		ChainsDB chains = fcio.loadFile(chainsDBFile);
		
		loadingTime = System.currentTimeMillis() - loadingTime;
		System.out.println("Loading time : " + (loadingTime/1000) + "s");
		
		
		
		//------------------- Spliting ------------------------
		System.out.println("--- Monomers search ---");
		long searchTime = System.currentTimeMillis();

		MonomericSpliting.setVerbose(verbose);
		MonomericSpliting split = new MonomericSpliting(families, chains, removeDistance, retryCount, modulationDepth);
		split.setAllowLightMatchs(lightMatch);
		Coverage[] covs = split.computeCoverages(polDB);
		
		searchTime = System.currentTimeMillis() - searchTime;
		System.out.println("Search time : " + (searchTime/1000) + "s");
		
		
		
		//------------------- Output ------------------------
		System.out.println("--- Output creations ---");
		long outputTime = System.currentTimeMillis();
		
		// Creation of the out directory
		File outfolder = new File(outfolderName);
		if (!outfolder.exists())
			outfolder.mkdir();
		
		CoveragesJsonLoader cjl = new CoveragesJsonLoader(polDB, families);
		cjl.saveFile(covs, outfile);
		
		// Images generation
		if (html || zip) {
			File imgsFolder = new File(imgsFoldername);
			if (!imgsFolder.exists())
				imgsFolder.mkdir();
			
			ImagesGeneration ig = new ImagesGeneration();
			ig.generateMonomerImages(imgsFolder, monoDB);
			
			Map<Coverage, ColorsMap> colors = ig.generatePeptidesImages(imgsFolder, covs);
			
			if (html) {
				// HTML
				Coverages2HTML c2h = new Coverages2HTML(covs, monoDB, families);
				File htmlFile  = new File(outfolderName + "/s2m.html");
				c2h.createResults(htmlFile, imgsFolder, colors);
			}
			
			if (zip) {
				// Zip File
				OutputZiper oz = new OutputZiper(outfolderName + "/s2m.zip");
				oz.createZip(imgsFolder.getPath(), outfile, pepDBname, monoDBname, residuesDBname, colors);
			}
		}
			
		
		outputTime = System.currentTimeMillis() - outputTime;
		System.out.println("Ouputing time : " + (outputTime/1000) + "s");
		System.out.println("--- Ended ---");
	}

}
