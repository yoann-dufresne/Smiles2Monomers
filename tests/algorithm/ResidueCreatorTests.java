package algorithm;

import io.loaders.json.ResidueJsonLoader;
import io.loaders.json.RulesJsonLoader;

import java.io.File;
import java.util.Map.Entry;
import java.util.Set;

import model.Family;
import model.Monomer;
import model.Residue;
import model.Rule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtom;

import algorithms.ResidueCreator;
import db.FamilyDB;
import db.MonomersDB;
import db.RulesDB;

public class ResidueCreatorTests {
	
	private ResidueCreator creator;
	private MonomersDB monos;
	private FamilyDB families;
	private RulesDB rules;
	private Family fam;
	private Set<Residue> residues;

	@Before
	public void setUp() throws Exception {
		if (this.fam != null)
			return;
					
		this.monos = new MonomersDB();
		Monomer mono = new Monomer("bOH-Tyr", "bOH-Tyr", "c1cc(ccc1C(C(C(=O)O)N)O)O");
		monos.addObject(mono.getId(), mono);
		mono = new Monomer("Tyr", "Tyr", "c1cc(ccc1CC(C(=O)O)N)O");
		monos.addObject(mono.getId(), mono);
		this.rules = RulesJsonLoader.loader.loadFile("data_tests/rules.json");
		
		this.creator = new ResidueCreator(this.rules);
		this.families = this.creator.createResidues(this.monos);
		
		this.fam = null;
		try {
			this.fam = this.families.getObject("Tyr");
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		this.residues = this.fam.getResidues();
	}
	
	@Test
	public void nbResiduesCreateTest() {
		Assert.assertEquals(31, this.residues.size());
	}
	
	@Test
	public void linksCreationTest () {
		Residue tyrN = null;
		for (Residue res : this.residues)
			if ("Tyr_pepN".equals(res.getName())) {
				tyrN = res;
				break;
			}
		
		Entry<IAtom, Rule> entry = tyrN.getAtomicLinks().entrySet().iterator().next();
		IAtom a = entry.getKey();
		Assert.assertEquals(a.getSymbol(), "N");
	}
	
	@Test
	public void linksLoadingTest () {
		ResidueJsonLoader rjl = new ResidueJsonLoader(this.rules, this.monos);
		rjl.saveFile(this.families, "tmp.json");
		Residue.resetResidues();
		FamilyDB loaded = rjl.loadFile("tmp.json");
		new File("tmp.json").delete();
		
		Family famTyr = null;
		try {
			famTyr = loaded.getObject("Tyr");
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		Residue tyrN = null;
		for (Residue res : famTyr.getResidues())
			if ("Tyr_pepN".equals(res.getName())) {
				tyrN = res;
				break;
			}
		
		Entry<IAtom, Rule> entry = tyrN.getAtomicLinks().entrySet().iterator().next();
		IAtom a = entry.getKey();
		Assert.assertEquals(a.getSymbol(), "N");
	}

}
