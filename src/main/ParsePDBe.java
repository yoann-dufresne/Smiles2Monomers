package main;

import io.parsers.SmilesConverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;

public class ParsePDBe {
	
	public static final ConnectivityChecker connectivity = new ConnectivityChecker(); 

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		File directory = new File("/mnt/data/PDBe/mmcif");
		Map<String, JSONObject> names = new HashMap<>();

		JSONArray poly = new JSONArray();
		JSONArray mono = new JSONArray();
		JSONArray nonpoly = new JSONArray();
		
		for (File molfile : directory.listFiles()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader(molfile));
				JSONObject jso = new JSONObject();
				
				String line;
				while ((line = br.readLine()) != null) {
					
					if (line.startsWith("_chem_comp.pdbx_subcomponent_list")) {
						if (line.contains("?")) {
							mono.add(jso);
							jso.put("desc", jso.get("name"));
							jso.put("name", jso.get("id"));
						} else {
							// Graph spliting
							String[] split = line.split("\"")[1].split(" ");
							JSONObject graph = new JSONObject();
							JSONArray v = new JSONArray();
							graph.put("V", v);
							JSONArray e = new JSONArray();
							graph.put("E", e);
							for (int i=0 ; i<split.length ; i++) {
								v.add(split[i]);
								if (i>0) {
									JSONArray bond = new JSONArray();
									bond.add(i-1);
									bond.add(i);
									e.add(bond);
								}
							}
							jso.put("graph", graph);

							// Add in polymers
							poly.add(jso);
						}
					}
					else if (line.startsWith("_chem_comp.id")) {
						System.out.println(line);
						line = line.replaceAll("_chem_comp.id", "");
						line = line.replaceAll(" ", "").replaceAll("\t", "");
						jso.put("id", line);
						names.put(line, jso);
					}
					else if (line.startsWith("_chem_comp.name")) {
						System.out.println(line);
						line = line.replaceAll("_chem_comp.name", "");
						line = line.replaceAll(" ", "").replaceAll("\t", "");
						jso.put("name", line);
						jso.put("desc", line);
					}
					else if (jso.containsKey("id") && line.startsWith((String)jso.get("id"))
							&& line.contains("SMILES") && line.contains("ACDLabs")) {
						System.out.println(line);
						if (line.contains("\"")) {
							String[] split = line.split("\"");
							line = split[split.length-2];
						} else {
							String[] split = line.split(" ");
							line = split[split.length-1];
						}
						try {
							IMolecule mol = SmilesConverter.conv.transform(line);
							if (ConnectivityChecker.isConnected(mol)) {
								StructureDiagramGenerator sdg = new StructureDiagramGenerator(mol);
								sdg.generateCoordinates();
								jso.put("smiles", line);
							}
						} catch (InvalidSmilesException e) {}
						catch (CDKException e) {}
						catch (IllegalArgumentException e) {}
					} else if (jso.containsKey("id") && line.startsWith((String)jso.get("id"))
							&& line.contains("SMILES") && !jso.containsKey("smiles")) {
						if (line.contains("\"")) {
							String[] split = line.split("\"");
							if (split.length > 1)
								line = split[1];
						} else {
							String[] split = line.split(" ");
							line = split[split.length-1];
						}
						try {
							IMolecule mol = SmilesConverter.conv.transform(line);
							if (ConnectivityChecker.isConnected(mol)) {
								StructureDiagramGenerator sdg = new StructureDiagramGenerator(mol);
								sdg.generateCoordinates();
								jso.put("smiles", line);
							}
						} catch (InvalidSmilesException e) {}
						catch (CDKException e) {}
						catch (IllegalArgumentException e) {}
					} else if (line.startsWith("_chem_comp.type")) {
						if (line.contains("NON-POLYMER"))
							nonpoly.add(jso);
					}
				}
				
				if (!jso.containsKey("smiles") || jso.get("smiles") == null) {
					poly.remove(jso);
					mono.remove(jso);
					nonpoly.remove(jso);
					names.remove(jso);
					System.err.println(jso.toJSONString());
				}
					
				System.out.println();
			
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		HashSet<String> needed = new HashSet<>(); 
		for (Object o : poly) {
			JSONObject p = (JSONObject)o;
			JSONObject graph = (JSONObject) p.get("graph");
			JSONArray v = (JSONArray) graph.get("V");
			for (Object o2 : v) {
				String name = (String)o2;
				needed.add(name);
			}
		}

		JSONArray minMonos = new JSONArray();
		for (String name : needed) {
			if (names.containsKey(name)) {
				minMonos.add(names.get(name));
				System.out.println(name + " added");
			} else {
				System.err.println("Impossible to add " + name);
			}
		}
		
		System.out.println(poly.size());
		System.out.println(mono.size());
		System.out.println(nonpoly.size());
		System.out.flush();
		
		// Save files
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("data/pdbe_monos.json"));
			bw.write(nonpoly.toJSONString());
			bw.close();
			bw = new BufferedWriter(new FileWriter("data/pdbe_polys.json"));
			bw.write(poly.toJSONString());
			bw.close();
			bw = new BufferedWriter(new FileWriter("data/pdbe_monos_extended.json"));
			bw.write(mono.toJSONString());
			bw.close();
			bw = new BufferedWriter(new FileWriter("data/pdbe_monos_min.json"));
			bw.write(minMonos.toJSONString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
