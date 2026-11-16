package dev.trapclient;

public enum FeatureCategory {
	ALL("All", "✦"),
	HUD("HUD", "◈"),
	VISUAL("Visual", "◉"),
	UTILITY("Utility", "⌁");

	private final String label;
	private final String icon;

	FeatureCategory(String label, String icon) {
		this.label = label;
		this.icon = icon;
	}

	public String label() {
		return label;
	}

	public String icon() {
		return icon;
	}
}
