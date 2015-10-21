package io.imgs.coloration;

/*  Copyright (C) 2008  Arvid Berg <goglepox@users.sf.net>
*
*  Contact: cdk-devel@list.sourceforge.net
*
*  This program is free software; you can redistribute it and/or
*  modify it under the terms of the GNU Lesser General Public License
*  as published by the Free Software Foundation; either version 2.1
*  of the License, or (at your option) any later version.
*
*  This program is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU Lesser General Public License for more details.
*
*  You should have received a copy of the GNU Lesser General Public License
*  along with this program; if not, write to the Free Software
*  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
*/

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.annotations.TestClass;
import org.openscience.cdk.annotations.TestMethod;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.color.IAtomColorer;
import org.openscience.cdk.renderer.elements.IRenderingElement;
import org.openscience.cdk.renderer.generators.BasicAtomGenerator;
import org.openscience.cdk.renderer.generators.IGeneratorParameter;
import org.openscience.cdk.renderer.generators.parameter.AbstractGeneratorParameter;

/**
* Generates basic {@link IRenderingElement}s for atoms in an atom container.
*  
* @cdk.module renderbasic
* @cdk.githash
*/
@TestClass("org.openscience.cdk.renderer.generators.BasicAtomGeneratorTest")
public class ColoredAtomGenerator extends BasicAtomGenerator {

   /** The default atom color. */
   private IGeneratorParameter<Color> atomColor = new AtomColor();

   public static class AtomColorer extends AbstractGeneratorParameter<IAtomColorer> {
	   
	   private Map<IAtom, Color> colors;
	   
	   public AtomColorer() {
		this.colors = new HashMap<>();
	   }
	   
       public IAtomColorer getDefault() {
           return new CoverageAtomColorer(this.colors);
       }

       public void setColor (IAtom atom, Color color) {
    	   this.colors.put(atom, color);
       }
       
       public void resetColors () {
    	   this.colors = new HashMap<>();
       }
   }

   /** Converter between atoms and colors. */
   private IGeneratorParameter<IAtomColorer> atomColorer = new AtomColorer();
   
   public AtomColorer getColorer () { return (AtomColorer) this.atomColorer; }

   /** If true, colors atoms by their type. */
   private IGeneratorParameter<Boolean> colorByType = new ColorByType();

   /** If true, explicit hydrogens are displayed. */
   private IGeneratorParameter<Boolean> showExplicitHydrogens = new ShowExplicitHydrogens();

   /** The atom radius on screen. */
   private IGeneratorParameter<Double> atomRadius = new AtomRadius();

   private IGeneratorParameter<Boolean> isCompact = new CompactAtom();

   /**
    * Determines whether structures should be drawn as Kekule structures, thus
    * giving each carbon element explicitly, instead of not displaying the
    * element symbol. Example C-C-C instead of /\.
    */
   private IGeneratorParameter<Boolean> isKekule = new KekuleStructure();

   /** The compact shape used to display atoms when isCompact is true. */
   private IGeneratorParameter<Shape> compactShape = new CompactShape();

   /**
    * Determines whether methyl carbons' symbols should be drawn explicit for
    * methyl carbons. Example C/\C instead of /\.
    */
   private IGeneratorParameter<Boolean> showEndCarbons = new ShowEndCarbons();

   /**
    * Returns the drawing color of the given atom. An atom is colored as
    * highlighted if highlighted. The atom is color marked if in a
    * substructure. If not, the color from the CDK2DAtomColor is used (if
    * selected). Otherwise, the atom is colored black.
    */
   @TestMethod("getAtomColorTest")
	protected Color getAtomColor(IAtom atom, RendererModel model) {
	    Color atomColor = model.get(AtomColor.class);
	    if ((Boolean)model.get(ColorByType.class)) {
	        atomColor = ((IAtomColorer)model.get(AtomColorer.class)).getAtomColor(atom);
       }
       return atomColor;
   }

   /** {@inheritDoc} */
   @TestMethod("getParametersTest")
   public List<IGeneratorParameter<?>> getParameters() {
       return Arrays.asList(
               new IGeneratorParameter<?>[] {
                       atomColor,
                       atomColorer,
                       atomRadius,
                       colorByType,
                       compactShape,
                       isCompact,
                       isKekule,
                       showEndCarbons,
                       showExplicitHydrogens
               }
       );
   }
   
}
