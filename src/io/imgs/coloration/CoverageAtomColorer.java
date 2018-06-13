package io.imgs.coloration;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.renderer.color.IAtomColorer;

public class CoverageAtomColorer implements IAtomColorer {

	private Map<IAtom, Color> coloration;
	
	public CoverageAtomColorer(Map<IAtom, Color> colors) {
		this.coloration = new HashMap<>(colors);
	}
	
	@Override
	public Color getAtomColor(IAtom atom) {
		return this.getAtomColor(atom, Color.black);
	}

	@Override
	public Color getAtomColor(IAtom atom, Color color) {
		Color c = this.coloration.containsKey(atom) ? this.coloration.get(atom) : color;
		return  c;
	}

}
