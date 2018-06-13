package model;

import java.util.ArrayList;
import java.util.List;

public class Rule implements Comparable<Rule> {

	private String name;
	private String formula;
	private List<Replacement> transformations;
	private String[] exclusions;
	private int[] weights;
	private Boolean alone;
	
	public Rule (String name) {
		this.name = name;
	}

	public Rule (String name, String formula, String[] transformations, int[] weights, String[] exclusions, Boolean alone) {
		this.name = name;
		this.formula = formula;
		this.transformations = new ArrayList<>();
		for (String transformation : transformations)
			this.transformations.add(new Replacement(transformation));
		this.weights = weights;
		if (exclusions.length == 1 && exclusions[0].equals(""))
			this.exclusions = new String[0];
		else
			this.exclusions = exclusions;
		this.alone = alone;
	}

	public String getName() {
		return name;
	}
	
	public String getFormula() {
		return formula;
	}
	
	public List<Replacement> getReplacements() {
		return transformations;
	}
	
	public int[] getWeights() {
		return weights;
	}
	
	public Boolean getAlone() {
		return alone;
	}
	
	@Override
	public int hashCode() {
		return this.name.hashCode();
	}
	
public String[] getExclusion() {
		return exclusions;
	}
	
	public class Replacement {
		
		public String formula;
		public List<Integer> toDelete;
		public List<Integer> toReplace;
		
		public Replacement(String formula) {
			this.toDelete = new ArrayList<>();
			this.toReplace = new ArrayList<>();
			this.formula = formula;
			
			String pure = formula.replaceAll("[1-9\\[\\]\\(\\)#=\\-]", "");
			int lowerCases = 0;
			for (int i=0 ; i<pure.length() ; i++) {
				char current = pure.charAt(i);
				if (current == '.')
					toReplace.add(i-lowerCases);
				else if (current =='x' || current =='X')
					toDelete.add(i-lowerCases);
				else if ('n' != current && 'c' != current && Character.isLowerCase(current))
					lowerCases++;
			}
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Rule))
			return false;
		
		Rule r = (Rule)obj;
		return this.name.equals(r.name);
	}

	@Override
	public int compareTo(Rule rule) {
		return this.name.compareTo(rule.name);
	}
	
	
	
}
