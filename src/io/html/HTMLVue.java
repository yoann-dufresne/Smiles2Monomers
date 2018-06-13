package io.html;

import io.Vue;

import java.util.Map;

public interface HTMLVue extends Vue {

	public String getHTML ();
	public Map<String, Map<String, String>> getCSSProperties ();
	
}
