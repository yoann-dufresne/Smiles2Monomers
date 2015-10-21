package io.html;

import io.imgs.PictureCoverageGenerator.ColorsMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;

import algorithms.utils.Coverage;
import db.FamilyDB;
import db.MonomersDB;

public class Coverages2HTML {
	
	private List<Coverage> coverages;
	private MonomersDB monoDB;
	private FamilyDB familiesDB;

	public Coverages2HTML(List<Coverage> cov, MonomersDB db, FamilyDB famDB) {
		this.coverages = cov;
		this.monoDB = db;
		this.familiesDB = famDB;
	}
	
	public void createResults (File resultDir, File imgs, Map<Coverage, ColorsMap> allColors) {
		File resultImgs = new File(resultDir.getPath() + "/imgs");
		this.copyDirectory(imgs, resultImgs);
		
		File coverageDir = new File(resultImgs.getPath() + "/peptides");
		
		// Tests HTML
		HTMLPeptidesCoverageVue hpcv = new HTMLPeptidesCoverageVue(coverages, allColors, monoDB, coverageDir, familiesDB);
		
		// Write images
		File HTMLfile = new File(resultDir.getPath() + "/test.html");
		File CSSFile = new File(resultDir.getPath() + "/mainStyle.css");
		HTMLVueWriter.writeFiles(HTMLfile, CSSFile, "test HTML", hpcv);
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
