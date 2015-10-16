package main;

import io.loaders.json.PolymersJsonLoader;
import io.parsers.SmilesConverter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import model.Polymer;
import nl.uu.cs.treewidth.MolTreeWidth;

import org.openscience.cdk.exception.InvalidSmilesException;

import db.MonomersDB;
import db.PolymersDB;

public class PolymersTreeWidths {

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Command line : java main.PeptidesTreeWidths <peptidesFile> [outfile]");
			System.exit(-1);
		}
		
		// Parse arguments
		String pepDBname = args[0];
		BufferedWriter bw = null;
		if (args.length > 1) {
			File out = new File(args[1]);
			if (out.exists())
				out.delete();
			
			try {
				bw = new BufferedWriter(new FileWriter(out));
				bw.write("peptide;treewidth\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Loading databases
		MonomersDB monoDB = new MonomersDB();
		PolymersJsonLoader pcl = new PolymersJsonLoader(monoDB);
		PolymersDB pepDB = pcl.loadFile(pepDBname);
		
		// Tools for treewidth
		MolTreeWidth mtw = new MolTreeWidth();
		
		for (Polymer pep : pepDB.getObjects()) {
			int tw;
			try {
				tw = mtw.calculateTreeWidth(SmilesConverter.conv.transform(pep.getSmiles()));
			} catch (InvalidSmilesException e) {
				System.err.println("Impossible to parse " + pep.getName() + " id:" + pep.getId());
				continue;
			}
			if (args.length > 1) {
				try {
					bw.write(pep.getName()+";"+tw+"\n");
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println(pep.getName() + " : " + tw);
			}
		}
		
		if (args.length > 1) {
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
