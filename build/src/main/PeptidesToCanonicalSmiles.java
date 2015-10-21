package main;

import java.io.File;

import org.openscience.cdk.interfaces.IMolecule;

import model.Polymer;
import io.loaders.json.PeptideJsonLoader;
import io.parsers.SmilesConverter;
import db.MonomersDB;
import db.PeptidesDB;

public class PeptidesToCanonicalSmiles {
	
	public static void modify (String pepFile) 
	{
		// Loading
		PeptideJsonLoader pjl = new PeptideJsonLoader(new MonomersDB());
		PeptidesDB db = pjl.loadFile(pepFile);
		
		// transform all smiles
		for (Polymer pep : db.getObjects()) {
			IMolecule mol = pep.getMolecule();
			String smiles = SmilesConverter.conv.mol2Smiles(mol);
			pep.setSmiles(smiles);
		}
		
		// Saving
		pjl.saveFile(db, pepFile);	
	}
	
	public static void main(String[] args) {
		
		if (args.length < 1) {
			System.err.println("Incorrect usage. Use : java main.PeptidesToCanonicalSmiles <folder>");
			System.exit(1);
		}
		
		 String path = args[0];
		 File folder = new File(path);
		 
		 if (folder.isFile())
		 {
			 modify(path);
			 System.out.println(path);
		 }
		 else
		 {
			 File[] listOfFiles = folder.listFiles(); 
			
			 for (int i = 0; i < listOfFiles.length; i++) 
			 {
			
			  if (listOfFiles[i].isFile()) 
			  {
				  String pepFile = folder + "/" + (listOfFiles[i].getName());
				  modify(pepFile);
				  System.out.println(pepFile);
			  }
			   
			 }
		  }
	}
	
}
