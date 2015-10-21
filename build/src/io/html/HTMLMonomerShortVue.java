package io.html;

import java.awt.Color;
import java.util.List;

import model.Monomer;

public class HTMLMonomerShortVue extends HTMLAbstractVue {
	
	private Monomer monomer;
	private int nb;
	private List<Color> colors;

	public HTMLMonomerShortVue(Monomer monomer, int nb, List<Color> colors) {
		super();
		this.monomer = monomer;
		this.nb = nb;
		this.colors = colors;
		
		this.html = "<div class='monomer'>\n";
		this.html += "	<p>" + this.nb + " " + this.monomer.getCode() + "</p>\n";
		if (this.colors.size() > 0) {
			this.html += "<div class='colors'>\n";
			for (Color color : this.colors) {
				this.html += "<div class='color' style='background-color:rgb(" + color.getRed() +"," + color.getGreen() + "," + color.getBlue() + ")'></div>\n";
			}
			this.html += "</div>\n";
		}
		this.html += "	<img src='imgs/monomers/" + this.monomer.getCode() + ".png' class='miniImg' />\n";
		this.html += "</div>\n";
		
		this.addToCSS(".colors>.color", "width", "20px");
		this.addToCSS(".colors>.color", "height", "20px");
		this.addToCSS(".colors>.color", "display", "inline-block");
		this.addToCSS(".colors", "text-align", "center");
		this.addToCSS(".miniImg", "width", "150px");
		this.addToCSS(".miniImg", "height", "150px");
		this.addToCSS(".monomer>p", "text-align", "center");
	}

	@Override
	public void updateVue() {}
	
	public Monomer getMonomer() {
		return monomer;
	}

}
