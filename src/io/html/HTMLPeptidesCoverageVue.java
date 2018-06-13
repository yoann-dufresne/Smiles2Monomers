package io.html;

import io.imgs.PictureCoverageGenerator.ColorsMap;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import algorithms.utils.Coverage;
import db.FamilyDB;
import db.MonomersDB;

public class HTMLPeptidesCoverageVue extends HTMLAbstractVue {
	
	public HTMLPeptidesCoverageVue(List<Coverage> coverages, Map<Coverage, ColorsMap> allColors, MonomersDB monoDB, File coverageDir, FamilyDB families) {
		super();
		
		for (Coverage cov : coverages) {
			cov.setFamilies(families);
		}
		Collections.sort(coverages);
		
		for (Coverage cov : coverages) {
			
			ColorsMap cm = allColors.get(cov);
			HTMLColoredCoverageVue ccv = new HTMLColoredCoverageVue(cov, cm, monoDB, coverageDir, families);
			
			// HTML
			this.html += "<div class='coverage'>";
			this.html += ccv.getHTML();
			this.html += "</div>";
			
			// CSS
			this.css.putAll(ccv.css);
			this.addToCSS(".coverage", "border", "black solid 1px");
			this.addToCSS(".coverage", "padding", "10px");
			this.addToCSS(".coverage", "margin-bottom", "20px");
		}
	}

	@Override
	public void updateVue() {}

}
