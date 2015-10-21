package io.html;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import algorithms.utils.Coverage;
import db.FamilyDB;
import db.MonomersDB;
import io.imgs.PictureCoverageGenerator.ColorsMap;

public class Coverages2HTML {
	
	private List<Coverage> coverages;
	private MonomersDB monoDB;
	private FamilyDB familiesDB;

	public Coverages2HTML(List<Coverage> cov, MonomersDB db, FamilyDB famDB) {
		this.coverages = cov;
		this.monoDB = db;
		this.familiesDB = famDB;
	}
	
	public Coverages2HTML(Coverage[] covs, MonomersDB db, FamilyDB famDB) {
		List<Coverage> coverages = new ArrayList<>();
		for (Coverage cov : covs)
			coverages.add(cov);
		this.coverages = coverages;
		this.monoDB = db;
		this.familiesDB = famDB;
	}

	public void createResults (File resultFile, File imgs, Map<Coverage, ColorsMap> allColors) {
		String resultDir = resultFile.getParent();
		File resultImgs = new File(resultDir + "/imgs");
		this.copyDirectory(imgs, resultImgs);
		
		File coverageDir = new File(resultImgs.getPath() + "/peptides");
		
		// Tests HTML
		HTMLPeptidesCoverageVue hpcv = new HTMLPeptidesCoverageVue(coverages, allColors, monoDB, coverageDir, familiesDB);
		
		// Write images
//		File HTMLfile = new File(resultFile.getPath() + "/test.html");
		File HTMLfile = resultFile;
		File CSSFile = new File(resultDir + "/mainStyle.css");
		HTMLVueWriter.writeFiles(HTMLfile, CSSFile, resultFile.getName(), hpcv);
	}

	private void copyDirectory(File src, File dst) {
		if (src.isDirectory()) {
			if (!dst.exists())
				dst.mkdir();
			for (File child : src.listFiles())
				this.copyDirectory(child, new File(dst.getPath() + "/" + child.getName()));
		} else {
			try {
				FileInputStream fis = new FileInputStream(src);
		    	FileChannel inChannel = fis.getChannel();
		    	FileOutputStream fos = new FileOutputStream(dst);
			    FileChannel outChannel = fos.getChannel();
			    
		        inChannel.transferTo(0, inChannel.size(), outChannel);
		        
		        if (inChannel != null)
		            inChannel.close();
		        if (outChannel != null)
		            outChannel.close();
		        
		        fis.close();
		        fos.close();
		    } catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
