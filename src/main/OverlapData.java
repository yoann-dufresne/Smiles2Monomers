package main;

import java.util.HashMap;

import org.openscience.cdk.exception.InvalidSmilesException;

import db.MonomersDB;
import db.PolymersDB;
import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.PolymersJsonLoader;
import io.parsers.SmilesConverter;
import model.Monomer;
import model.Polymer;

public class OverlapData {

	public static void main(String[] args) {
		// Norine Loading
		MonomersJsonLoader monosLoader = new MonomersJsonLoader();
		MonomersDB norMonos = monosLoader.loadFile("data/monomers.json");
		PolymersJsonLoader loader = new PolymersJsonLoader(norMonos);
		PolymersDB norine = loader.loadFile("data/peptides_clean.json");
		
		// CCD Loading
		MonomersDB ccdMonos = monosLoader.loadFile("data/pdbe_monos_min.json");
		loader = new PolymersJsonLoader(ccdMonos);
		PolymersDB ccd = loader.loadFile("data/pdbe_polys_clean.json");
		
		// Norine smiles generation
		HashMap<String, Polymer> polymers = new HashMap<>();
		for (Polymer poly : norine.getObjects()) {
			String smiles = null;
			try {
				smiles = SmilesConverter.conv.toCanonicalSmiles(poly.getSmiles());
			} catch (InvalidSmilesException e) {
				e.printStackTrace();
				continue;
			}
			polymers.put(smiles, poly);
		}
		
		// Test overlapping peptides
		int overlap = 0;
		for (Polymer poly : ccd.getObjects()) {
			String smiles = null;
			try {
				smiles = SmilesConverter.conv.toCanonicalSmiles(poly.getSmiles());
			} catch (InvalidSmilesException e) {
				e.printStackTrace();
				continue;
			}
			
			if (polymers.containsKey(smiles)) {
				System.out.println(polymers.get(smiles).getName());
				overlap++;
			}
		}
		
		System.out.println("Total peptides overlapping : " + overlap);
		
		// Norine smiles generation
		HashMap<String, Monomer> monomers = new HashMap<>();
		for (Monomer mono : norMonos.getObjects()) {
			String smiles = null;
			try {
				smiles = SmilesConverter.conv.toCanonicalSmiles(mono.getSmiles());
			} catch (InvalidSmilesException e) {
				e.printStackTrace();
				continue;
			}
			monomers.put(smiles, mono);
		}
		
		// Test overlapping peptides
		overlap = 0;
		for (Monomer mono : ccdMonos.getObjects()) {
			String smiles = null;
			try {
				smiles = SmilesConverter.conv.toCanonicalSmiles(mono.getSmiles());
			} catch (InvalidSmilesException e) {
				e.printStackTrace();
				continue;
			}
			
			if (monomers.containsKey(smiles)) {
				System.out.println(monomers.get(smiles).getName());
				overlap++;
			}
		}
		
		System.out.println("Total monomers overlapping : " + overlap + "/" + ccdMonos.size());
	}
	
}
