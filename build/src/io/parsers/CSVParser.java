package io.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVParser {

	private CSVParser () {}
	
	/**
	 * Parse a CSV file where values are separated by semi-commas
	 * @param filename CSV filename
	 * @return List of each CSV lines where values are associated with correct parameters names
	 */
	public static List<Map<String, String>> parse (String inner) {
		List<Map<String, String>> objects = new ArrayList<>();
		int numLine = 0;
		String[] names = null;
		
		for (String line : inner.split("\n")) {
			numLine++;
			
			if (line.startsWith("#"))
				continue;
			
			if (!line.contains(";"))
				continue;
			
			if (names == null) {
				names = splitLine(line);
			}
			else
				try {
					objects.add(CSVParser.parseLine(names, line));
				} catch (Exception e) {
					System.err.println("Invalid format at line " + numLine);
				}
		}
		
		return objects;
	}
	
	private static String[] splitLine(String line) {
		List<String> splitLine = new ArrayList<>();
		String currentSplit = "";
		char state = 's';
		
		for (int i=0 ; i<line.length() ; i++) {
			char c = line.charAt(i);
			
			switch (c) {
			case ';':
				if (state == 's') {
					splitLine.add(currentSplit);
					currentSplit = "";
				} else {
					currentSplit += ';';
				}
				break;

			case '"':
				switch (state) {
				case '"':
					state = 's';
					break;
				default:
					state = '"';
					break;
				}
				break;
			default:
				currentSplit += c;
			}
		}
		splitLine.add(currentSplit);
		
		String[] table = new String[splitLine.size()];
		for (int i=0 ; i<table.length ; i++)
			table[i] = splitLine.get(i);
		
		return table;
	}

	/**
	 * Parse a CSV line
	 * @param names CSV parameters names
	 * @param line CSV line
	 * @return A map which associate parameter names with line values
	 * @throws InvalidFormatException If there is no correct number of arguments in the line
	 */
	public static Map<String, String> parseLine (String[] names, String line) throws Exception {
		Map<String, String> object = new HashMap<>();
		
		String[] split = CSVParser.splitLine(line);
		
		if (split.length != names.length)
			throw new Exception();
		
		for (int i=0 ; i<names.length ; i++)
			object.put(names[i], split[i]);
		
		return object;
	}

}
