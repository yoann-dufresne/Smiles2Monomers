package io.imgs.coloration;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ColorsGenerator {

	public static List<Color> HsbColorsGeneration (int colorsNumber) {
		List<Color> colors = new ArrayList<Color>();
		
		for (int i=0 ; i<colorsNumber+1 ; i++) {
			float h = new Float(i) / new Float(colorsNumber);
			float s = 1f;
			float b = 1f;
			
			//System.out.println(h + "," + s + "," + b);
			colors.add(Color.getHSBColor(h, s, b));
		}
		
		return colors;
	}
	
}
