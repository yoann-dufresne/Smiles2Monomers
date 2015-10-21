package io.loaders.serialization;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.vecmath.Point2d;

import org.openscience.cdk.interfaces.IAtom;

import db.MonomersDB;
import model.Monomer;

public class MonomersSerialization {

	
	public void serialize (MonomersDB db, String filename) {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (Monomer mono : db.getObjects())
			this.serializeObject (mono, oos);
		
		try {
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void serializeObject(Monomer mono, ObjectOutputStream oos) {
		try {
			oos.writeInt(mono.getMolecule().getAtomCount());
			for (IAtom a : mono.getMolecule().atoms()) {
				Point2d p = a.getPoint2d();
				oos.writeDouble(p.x);
				oos.writeDouble(p.y);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void deserialize (MonomersDB db, String filename) {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (Monomer mono : db.getObjects())
			this.deserializeObject (mono, ois);
		
		try {
			ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void deserializeObject(Monomer mono, ObjectInputStream ois) {
		try {
			int nbAtoms = ois.readInt();
			if (nbAtoms != mono.getMolecule().getAtomCount()) {
				System.err.println("Bad serialized value for atom count in " + mono.getCode());
			}
			
			for (IAtom a : mono.getMolecule().atoms()) {
				Point2d p = new Point2d(ois.readDouble(), ois.readDouble());
				a.setPoint2d(p);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
