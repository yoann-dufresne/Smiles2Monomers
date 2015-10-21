package io.html;

import io.imgs.PictureCoverageGenerator.ColorsMap;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import model.Monomer;
import model.Residue;
import algorithms.utils.Coverage;
import db.FamilyDB;
import db.MonomersDB;

public class HTMLColoredCoverageVue extends HTMLAbstractVue {

	private Coverage coverage;
	private MonomersDB monosDB;
	private File coverageDir;
	private FamilyDB families;
	private ColorsMap colors;

	public HTMLColoredCoverageVue(Coverage cov, ColorsMap colors, MonomersDB monoDB, File coverageDir, FamilyDB families) {
		super();
		this.coverage = cov;
		this.monosDB = monoDB;
		this.coverageDir = coverageDir;
		this.families = families;
		this.colors = colors;
		
		this.html += this.createPeptideInfos();
		this.html += this.createMonomerLists();
	}

	private String createPeptideInfos() {
		String pepHTML = "";
		
		// Create coverage image
		String path = this.coverageDir.getPath();
		int splitIdx = 0; int slash = 0;
		for (int i=path.length()-1 ; i>=0 ; i--) {
			splitIdx = i;
			if (path.charAt(i) == '/') {
				slash++;
				if (slash == 2)
					break;
			}
		}
		path = this.coverageDir.getPath().substring(splitIdx+1);
		
		// Create HTML
		pepHTML += "<div class='peptide'>\n";
		pepHTML += "	<image src='" + path + "/" + this.coverage.getChemicalObject().getId() + ".png'"
				+ " class='covImage' />\n";
		double coverageRatio = Math.floor(this.coverage.getCoverageRatio()*1000.0)/1000.0;
		pepHTML += "<div>"
				+ "	<p>" + this.coverage.getChemicalObject().getName() + "</p>"
				+ "	<p>Atomic coverage : " + coverageRatio + "</p>"
				+ "</div>";
		pepHTML += "</div>\n";
		
		// Create CSS
		this.addToCSS(".covImage", "width", "500px");
		this.addToCSS(".covImage", "height", "500px");
		this.addToCSS(".peptide>*", "display", "inline-block");
		this.addToCSS(".peptide>*", "vertical-align", "middle");
		
		return pepHTML;
	}

	private String createMonomerLists() {
		String monosHTML = "";
		Map<String, Integer> correctMonomers = this.coverage.getCorrectMonomers (this.families);
		Map<String, List<Color>> correctColors = this.calculateColorsof(correctMonomers.keySet());
		Map<String, Integer> incorrectMonomers = this.coverage.getIncorrectMonomers(this.families);
		Map<String, List<Color>> incorrectColors = this.calculateColorsof(incorrectMonomers.keySet());
		Map<String, Integer> notfoundMonomers = this.coverage.getNotFoundMonomers(this.families);
		
		// Correct
		monosHTML += "<p>Correct monomers</p>";
		monosHTML += this.createColoredList("correct", correctMonomers, correctColors);
		
		// Incorrect
		monosHTML += "<p>Incorrect monomers</p>";
		monosHTML += this.createColoredList("incorrect", incorrectMonomers, incorrectColors);
		
		// Not found
		monosHTML += "<p>Not found monomers</p>";
		monosHTML += this.createColoredList("notFound", notfoundMonomers);
		
		// CSS
		this.addToCSS(".list>div", "display", "inline-block");
		this.addToCSS(".list", "white-space", "nowrap");
		this.addToCSS(".list", "overflow", "auto");
		
		return monosHTML;
	}

	private Map<String, List<Color>> calculateColorsof(Set<String> monoNames) {
		Map<String, List<Color>> colors = new HashMap<>();
		for (String name : monoNames) {
			List<Color> c = colors.containsKey(name) ? colors.get(name) : new ArrayList<Color>();
			for (Residue res : this.colors.keySet()) {
				if (this.families.areInSameFamily(name, res.getMonoName())) {
					c.addAll(this.colors.get(res));
					//break;
				}
			}
			colors.put(name, c);
		}
		
		return colors;
	}

	private String createColoredList(String string, Map<String, Integer> notfoundMonomers) {
		return this.createColoredList(string, notfoundMonomers, new HashMap<String, List<Color>>());
	}

	private String createColoredList (String listName, Map<String, Integer> monomers, Map<String, List<Color>> colors) {
		String monosHTML = "";
		
		monosHTML += "<div class='list' id='" + listName + "'>\n";
		for (String name : monomers.keySet()) {
			// HTML for each Residue matched.
			Monomer monomer = null;
			try {
				monomer = this.monosDB.getObject(name);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			List<Color> col = colors.containsKey(name) ? colors.get(name) : new ArrayList<Color>();
			HTMLMonomerShortVue hmsv = new HTMLMonomerShortVue(monomer, monomers.get(name), col);
			monosHTML += hmsv.getHTML();
			
			// CSS for each Residue matched.
			Map<String, Map<String, String>> properties = hmsv.getCSSProperties();
			for (String property : properties.keySet()) {
				Map<String, String> localProperty = this.css.containsKey(property) ? this.css.get(property) : new HashMap<String, String>();
				for (Entry<String, String> value : properties.get(property).entrySet())
					localProperty.put(value.getKey(), value.getValue());
				this.css.put(property, localProperty);
			}
		}
		monosHTML += "</div>\n";
		
		return monosHTML;
	}

	@Override
	public void updateVue() {}
	
}
