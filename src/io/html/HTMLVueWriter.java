package io.html;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

public class HTMLVueWriter {

	private HTMLVueWriter () {};
	
	public static void writeFiles (File hTMLfile, File cSSFile, String title, HTMLVue vue) {
		HTMLVueWriter.writeHTML (hTMLfile, cSSFile, title, vue);
		HTMLVueWriter.writeCSS (cSSFile, vue);
	}

	private static void writeCSS(File cSSFile, HTMLVue vue) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(cSSFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Map<String, Map<String, String>> properties = vue.getCSSProperties();
		
		for (String property : properties.keySet()) {
			Map<String, String> values = properties.get(property);
			try {
				bw.write(property + " {\n");
				for (Entry<String, String> value : values.entrySet())
					bw.write(value.getKey() + ": " + value.getValue()+ ";\n");
				bw.write("}\n\n");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeHTML(File hTMLfile, File cSSFile, String title, HTMLVue vue) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(hTMLfile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			bw.write("<!DOCTYPE html>\n");
			bw.write("<html>\n");
			bw.write("	<head>");
			bw.write("		<meta charset=\"utf-8\">\n");
			bw.write("		<title>" + title + "</title>");
			bw.write("		<link rel='stylesheet' type='text/css' href='" + cSSFile.getName() + "' media='screen' />");
			bw.write("	</head>");
			bw.write("	<body>");
			bw.write(vue.getHTML());
			bw.write("	</body>");
			bw.write("<script></script>");
			bw.write("</html>");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
