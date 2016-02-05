package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GLPK_test {

	public static void main(String[] args) {
		Runtime rt = Runtime.getRuntime();
		try {
			Process pr = rt.exec("lib/glpk-4.57/examples/glpsol --version");
			BufferedReader br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
			String line = null;
			while ((line = br.readLine()) != null)
				System.out.println(line);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
