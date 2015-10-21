package io.imgs;

import io.imgs.PictureCoverageGenerator.ColorsMap;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.Monomer;
import algorithms.utils.Coverage;
import db.MonomersDB;

public class ImagesGeneration {
	
	private PictureCoverageGenerator pg;
	
	public ImagesGeneration() {
		this.pg = new PictureCoverageGenerator();
	}

	public Map<Coverage, ColorsMap> generate (File imgsDirectory, MonomersDB monos, List<Coverage> coverages) {
		// Images directory
		this.generateMonomerImages(imgsDirectory, monos);
		return this.generatePeptidesImages(imgsDirectory, coverages);
	}

	private void generateMonomerImages(File imgsDirectory, MonomersDB monoDB) {
		// Monomer images directory
		File monoImgsDirectory = new File(imgsDirectory.getPath() + "/monomers");
		if (!monoImgsDirectory.exists())
			monoImgsDirectory.mkdir();
		for (File f : monoImgsDirectory.listFiles())
			f.delete();
		
		// Monomers imgages generation.
		PictureGenerator pg = new PictureGenerator();
		
		for (Monomer m : monoDB.getObjects()) {
			File monoImg = new File(monoImgsDirectory.getPath() + "/" + m.getName() + ".png");
			pg.createPNG(
					m.getMolecule(),
					monoImg
			);
		}
	}
	
	private Map<Coverage, ColorsMap> generatePeptidesImages(File imgsDirectory, List<Coverage> coverages) {
		// Coverage images directory
		File coverageDir = new File(imgsDirectory.getPath() + "/peptides");
		if (!coverageDir.exists())
			coverageDir.mkdir();
		for (File f : coverageDir.listFiles())
			f.delete();
				
		Map<Coverage, ColorsMap> covsColors = new HashMap<>();
		
		for (Coverage cov : coverages) {
			File png = new File(coverageDir.getPath() + "/" + cov.getChemicalObject().getId() + ".png");
			ColorsMap colors = pg.createPNG(cov, png);
			covsColors.put(cov, colors);
		}
	
		return covsColors;
	}
	
	public void cleanTmp (File tree) {
		if (tree.isDirectory())
			for (File son : tree.listFiles())
				this.cleanTmp(son);
		tree.delete();
	}
	
}
