package main;

import io.loaders.json.MonomersJsonLoader;
import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import model.Family;
import model.Residue;
import db.FamilyDB;
import db.MonomersDB;
import db.RulesDB;

public class ResiduesInfosGeneration {

	public static void main(String[] args) {
		MonomersDB monoDB = new MonomersJsonLoader().loadFile("data/monomers.json");
		RulesDB rules = RulesJsonLoader.loader.loadFile("data/rules.json");
		ResidueJsonLoader rjl = new ResidueJsonLoader(rules, monoDB);
		FamilyDB families = rjl.loadFile("data/residues.json");
		
		StringBuffer sb = new StringBuffer();
		for (Family fam : families.getFamilies()) {
			sb.append(fam.getName() + "\n");
			sb.append("Norine link : http://bioinfo.lifl.fr/norine/res_amino.jsp?code=" + fam.getMonomers().get(0).getCode() + "\n");
			sb.append("Num of residues : " + fam.getResidues().size() + "\n");
			sb.append("Root residues (with max links) :" + "\n");
			for (Residue res : fam.getRoots())
				sb.append("\t" + res.getName() + " : " + res.getAtomicLinks().size() + "\n");
			sb.append("\n");
		}
		
		File out = new File("results/infosMonos.txt");
		try {
			FileWriter fw = new FileWriter(out);
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
