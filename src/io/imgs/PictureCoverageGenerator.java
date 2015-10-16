package io.imgs;

import io.imgs.coloration.ColoredAtomGenerator;
import io.imgs.coloration.ColoredAtomGenerator.AtomColorer;
import io.imgs.coloration.ColorsGenerator;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import model.Residue;

import org.openscience.cdk.interfaces.IMolecule;

import algorithms.utils.Coverage;
import algorithms.utils.Match;


public class PictureCoverageGenerator extends AbstractPictureGenerator {
	
	protected ColoredAtomGenerator cag;
	
	public PictureCoverageGenerator() {
		super(new ColoredAtomGenerator());
		this.cag = (ColoredAtomGenerator)this.atomGenerator;
	}
	
	public ColorsMap createPNG (Coverage coverage, File outfile) {

		ColorsMap coverageColors = new ColorsMap();
		
		AtomColorer ac = this.cag.getColorer();
		IMolecule mol = coverage.getMolecule(false);
		List<Color> colors = ColorsGenerator.HsbColorsGeneration(coverage.nbMatchesForCoverage());
		int i = 0;
		for (Match match : coverage.getUsedMatches())
		{
			Residue res = match.getResidue();
			List<Color> matchesColor = coverageColors.containsKey(res) ?
					coverageColors.get(res) :
					new ArrayList<Color>();
			
			for (int idx : match.getAtoms())
			{
				if (!"H".equals(coverage.getMolecule(true).getAtom(idx).getSymbol()))
					ac.setColor(
							mol.getAtom(idx),
							colors.get(i)
					);
			}
			matchesColor.add(colors.get(i));
			coverageColors.put(res, matchesColor);
			i++;
		}
		
		this.createPNG(mol, outfile);
		ac.resetColors();
		
		return coverageColors;
	}/**/
	
	@SuppressWarnings("serial")
	public class ColorsMap extends HashMap<Residue, List<Color>> {}
}
