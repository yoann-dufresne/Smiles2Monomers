package io.html;

import java.util.HashMap;
import java.util.Map;

public abstract class HTMLAbstractVue implements HTMLVue {

	protected String html;
	protected Map<String, Map<String, String>> css;

	public HTMLAbstractVue() {
		this.html = "";
		this.css = new HashMap<>();
	}
	
	@Override
	public abstract void updateVue();

	@Override
	public String getHTML() {
		return this.html;
	}
	
	@Override
	public Map<String, Map<String, String>> getCSSProperties() {
		return this.css;
	}
	
	protected void addToCSS (String balise, String property, String value) {
		Map<String, String> properties = this.css.containsKey(balise) ? this.css.get(balise) : new HashMap<String, String>();
		properties.put(property, value);
		this.css.put(balise, properties);
	}

}
