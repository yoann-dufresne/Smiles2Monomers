package main;

import io.loaders.csv.PeptideCsvLoader;
import io.parsers.SmilesConverter;
import model.Polymer;

import org._3pq.jgrapht.graph.SimpleGraph;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.MoleculeGraphs;
import org.openscience.cdk.interfaces.IMolecule;

import algorithms.Planarity;
import db.MonomersDB;
import db.PeptidesDB;

public class PlanarityTest {

	public static void main(String[] args) {
		String pepDBname = "datas/peptides.csv";
		
		// Loading databases
		PeptideCsvLoader pcl = new PeptideCsvLoader(new MonomersDB());
		PeptidesDB pepDB = pcl.loadFile(pepDBname);
		
		//Tools
		Planarity pt = new Planarity();
		
		for (Polymer pep : pepDB.getObjects()) {
			System.out.println(pep.getName() + " : ");
			
			IMolecule mol = null;
			try {
				mol = SmilesConverter.conv.transform(pep.getSMILES());
			} catch (InvalidSmilesException e) {
				System.err.println("Impossible to parse " + pep.getName() + " id:" + pep.getId());
				continue;
			}
			SimpleGraph g = MoleculeGraphs.getMoleculeGraph(mol);
	
			System.out.println("planar : " + pt.isPlanar(g) + "\n");
		}
	}
}
