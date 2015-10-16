package main;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


public class Fusion {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			JSONArray peptides = (JSONArray) JSONValue.parse(new FileReader("data/monomers.json"));
			JSONArray old = (JSONArray) JSONValue.parse(new FileReader("data/old_monomers.json"));
			
			JSONArray all = new JSONArray();
			for (Object o : peptides)
				all.add(o);
			
			for (Object oo : old) {
				JSONObject jo = (JSONObject) oo;
				boolean isPresent = false;
				
				for (Object op : peptides) {
					JSONObject jp = (JSONObject) op;
					
					if (jp.get("code").equals(jo.get("name"))) {
						isPresent = true;
						break;
					}
				}
				
				if (!isPresent) {
					all.add(jo);
					System.out.println(jo.get("name"));
				}
			}
			
			BufferedWriter bw = new BufferedWriter(new FileWriter("data/fusion_mono.json"));
			bw.write(all.toJSONString());
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
