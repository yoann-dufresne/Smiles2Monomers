package io.zip;

import io.imgs.PictureCoverageGenerator.ColorsMap;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import model.Residue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import algorithms.utils.Coverage;



public class OutputZiper {
	
	private String zipPath;
	private ZipManager tools;

	public OutputZiper(String zipPath) {
		this.zipPath = zipPath;
		this.tools = new ZipManager(this.zipPath);
	}
	
	public void createZip (String imgsPath, String coveragePath, String pepPath, String monoPath,
			String residuesPath, Map<Coverage, ColorsMap> allColors) {
		this.tools.addDirectory(imgsPath, "imgs", "");
		
		this.tools.addFile(coveragePath, "coverage.json");
		
		this.tools.addFile(pepPath, "matched.json");
		this.tools.addFile(monoPath, "monomers.json");
		this.tools.addFile(residuesPath, "residues.json");
		
		File coverages = new File(coveragePath);
		String path = coverages.getParentFile().getPath() + "/supplementaries.json";
		this.createSupplementaries (path, allColors);
		
		this.tools.addFile(path, "suplementaries.json");
		
		this.tools.writeZip();
	}

	@SuppressWarnings("unchecked")
	private void createSupplementaries(String path, Map<Coverage, ColorsMap> allColors) {
		File sup = new File(path);
		if (sup.exists())
			sup.delete();
		
		JSONArray array = new JSONArray();
		for (Coverage cov : allColors.keySet()) {
			JSONObject jso = new JSONObject();
			
			jso.put("id", cov.getId());
			jso.put("ratio", cov.getCoverageRatio());
			
			ColorsMap cm = allColors.get(cov);
			JSONArray colorsArray = new JSONArray();
			jso.put("colors", colorsArray);
			for (Residue res : cm.keySet()) {
				JSONObject resObj = new JSONObject();
				JSONArray resColors = new JSONArray();
				for (Color col : cm.get(res)) {
					resColors.add(Integer.toHexString(col.getRGB()));
				}
				resObj.put(res.getId(), resColors);
				colorsArray.add(resObj);
			}
			array.add(jso);
		}
		
		try {
			FileWriter fw = new FileWriter(sup);
			fw.write(array.toJSONString());
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
