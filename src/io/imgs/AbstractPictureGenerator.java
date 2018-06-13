package io.imgs;

import io.imgs.coloration.ColoredAtomGenerator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;


public abstract class AbstractPictureGenerator {
	
	protected static int SIZE = 1000;
	
	protected AtomContainerRenderer renderer;
	protected RendererModel model;
	protected IGenerator<IAtomContainer> atomGenerator;
	protected BasicBondGenerator bbg;
	protected BasicSceneGenerator bsg;
	
	public AbstractPictureGenerator(IGenerator<IAtomContainer> atomGenerator) {
		// generators make the image elements
		List<IGenerator<IAtomContainer>> generators = new ArrayList<IGenerator<IAtomContainer>>();
		this.bsg = new BasicSceneGenerator();
		generators.add(this.bsg);
		this.bbg = new BasicBondGenerator();
		generators.add(this.bbg);
		this.atomGenerator = atomGenerator;
		generators.add(this.atomGenerator);
		
		this.renderer = new AtomContainerRenderer(generators, new AWTFontManager());
		this.model = renderer.getRenderer2DModel();
	}

	/**
	 * Create a PNG picture of molecule with width and height sizes
	 * @param molecule Molecule to be print
	 * @param outfile Out file
	 */
	public void createPNG (IMolecule molecule, File outfile) {
		/* Warning : This part of code change IAtoms coordinates
		StructureDiagramGenerator sdg = new StructureDiagramGenerator();
		sdg.setMolecule(molecule);
		try {
			sdg.generateCoordinates();
			molecule = sdg.getMolecule();
		} catch (CDKException e1) {
			e1.printStackTrace();
		}*/
		
		// the draw area and the image should be the same size
		Rectangle drawArea = new Rectangle(0, 0, SIZE, SIZE);
		this.renderer.setup(molecule, drawArea);
		Rectangle diagramBounds = this.renderer.calculateDiagramBounds(molecule);

		int width = diagramBounds.width;
		int height = diagramBounds.height;
		int diff = Math.abs(width - height);
		int max = width > height ? width : height;
		int xshift = width > height ? 0 : diff / 2;
		int yshift = width > height ? diff / 2 : 0;
		
		// Recenter image
		this.renderer.shiftDrawCenter(
				xshift - diagramBounds.x,
				yshift - diagramBounds.y
		);
		
		Image image = new BufferedImage(
				max,
				max,
				BufferedImage.TYPE_INT_RGB
		);
		
		// Drawing options
		this.model.set(ColoredAtomGenerator.KekuleStructure.class, true);
		this.model.set(ColoredAtomGenerator.ColorByType.class, true);
		
		// paint the background
		Graphics2D g2 = (Graphics2D)image.getGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, max, max);
	   
		// the paint method also needs a toolkit-specific renderer
		this.renderer.paint(molecule, new AWTDrawVisitor(g2));
	   
		try {
			ImageIO.write((RenderedImage)image, "PNG", outfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}/**/
	
}
