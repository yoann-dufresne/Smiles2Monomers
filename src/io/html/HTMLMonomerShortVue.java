package io.html;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HTMLMonomerShortVue extends HTMLAbstractVue {
	
	private int nb;
	private List<Color> colors;
	

	public HTMLMonomerShortVue(String code, int nb, List<Color> colors) {
		super();
		this.nb = nb;
		this.colors = colors;
		
		this.html = "<div class='monomer'>\n";
		try {
			this.html += "	<p>" + this.nb +
					" <a href=\"http://bioinfo.lifl.fr/norine/res_amino.jsp?code=" +
					URLEncoder.encode(code, StandardCharsets.UTF_8.toString())
					+ "\">" + code + "</a></p>\n";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			this.html += "	<p>" + this.nb + " " + code + "</a></p>\n";
		}
		if (this.colors.size() > 0) {
			this.html += "<div class='colors'>\n";
			for (Color color : this.colors) {
				this.html += "<div class='color' style='background-color:rgb(" + color.getRed() +"," + color.getGreen() + "," + color.getBlue() + ")'></div>\n";
			}
			this.html += "</div>\n";
		}
		String src = "http://upload.wikimedia.org/wikipedia/commons/f/f8/Question_mark_alternate.svg";
		if (!code.endsWith("_unloaded")) {
			src = "imgs/monomers/" + code + ".png";
		}
		this.html += "	<img src='" + src + "' class='miniImg' />\n";
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

}
