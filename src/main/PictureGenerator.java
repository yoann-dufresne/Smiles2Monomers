package main;

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

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.renderer.AtomContainerRenderer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.font.AWTFontManager;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.BasicBondGenerator;
import org.openscience.cdk.renderer.generators.BasicSceneGenerator;
import org.openscience.cdk.renderer.generators.IGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.openscience.cdk.smiles.SmilesParser;

public class PictureGenerator {
		
	protected static int SIZE = 1000;
	
	protected AtomContainerRenderer renderer;
	protected RendererModel model;
	protected IGenerator<IAtomContainer> atomGenerator;
	protected BasicBondGenerator bbg;
	protected BasicSceneGenerator bsg;

	private SmilesParser sp;
	
	public PictureGenerator() {
		// generators make the image elements
		List<IGenerator<IAtomContainer>> generators = new ArrayList<IGenerator<IAtomContainer>>();
		this.bsg = new BasicSceneGenerator();
		generators.add(this.bsg);
		this.bbg = new BasicBondGenerator();
		generators.add(this.bbg);
		this.atomGenerator = new BasicAtomGenerator();
		generators.add(this.atomGenerator);
		
		this.renderer = new AtomContainerRenderer(generators, new AWTFontManager());
		this.model = renderer.getRenderer2DModel();
		
		// For smiles convertion
		this.sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
		this.sp.setPreservingAromaticity(true);
	}

	/**
	 * Create a PNG picture of molecule with width and height sizes
	 * @param smiles SMILES of the molecule to be print
	 * @param outfile Out file
	 * @throws InvalidSmilesException 
	 */
	public void createPNG (String smiles, File outfile) throws InvalidSmilesException {
		IMolecule molecule = this.transform(smiles);
		
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
		this.model.set(BasicAtomGenerator.KekuleStructure.class, true);
		this.model.set(BasicAtomGenerator.ColorByType.class, true);
		
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
	
	
	public Molecule transform (String smiles) throws InvalidSmilesException {
		Molecule imol = null;
		imol = new Molecule(this.sp.parseSmiles(smiles));
		
		for (IAtom a : imol.atoms())
			a.setImplicitHydrogenCount(0);
		
		StructureDiagramGenerator sdg = new StructureDiagramGenerator(imol);
		try {
			sdg.generateCoordinates();
		} catch (CDKException e) {
			System.err.println(smiles);
			e.printStackTrace();
		}
		imol = new Molecule(sdg.getMolecule());
		
		return imol;
	}
	

	public static void main(String[] args) throws InvalidSmilesException {
		PictureGenerator pg = new PictureGenerator();
		pg.createPNG("NCCCCCc1ccc(=O)cc1", new File("/tmp/imgTest.png"));
	}
}
