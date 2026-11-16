package dev.trapclient;

import java.util.List;

public final class ClientFeature {
	public enum Position {
		TOP_LEFT("Top left"),
		TOP_RIGHT("Top right"),
		BOTTOM_LEFT("Bottom left"),
		BOTTOM_RIGHT("Bottom right");

		private final String label;

		Position(String label) {
			this.label = label;
		}

		public String label() {
			return label;
		}

		public Position next() {
			return values()[(ordinal() + 1) % values().length];
		}
	}

	public enum Accent {
		VIOLET("Violet", 0xFFB78AFF),
		CYAN("Cyan", 0xFF74E4FF),
		WHITE("White", 0xFFF2F0FA),
		AMBER("Amber", 0xFFFFC875);

		private final String label;
		private final int color;

		Accent(String label, int color) {
			this.label = label;
			this.color = color;
		}

		public String label() {
			return label;
		}

		public int color() {
			return color;
		}

		public Accent next() {
			return values()[(ordinal() + 1) % values().length];
		}
	}

	private final String id;
	private final String name;
	private final String description;
	private final FeatureCategory category;
	private boolean enabled;
	private Position position = Position.TOP_LEFT;
	private Accent accent = Accent.VIOLET;
	private int offset = 0;

	public ClientFeature(String id, String name, String description, FeatureCategory category, boolean enabled) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.category = category;
		this.enabled = enabled;
	}

	public String id() {
		return id;
	}

	public String name() {
		return name;
	}

	public String description() {
		return description;
	}

	public FeatureCategory category() {
		return category;
	}

	public boolean enabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Position position() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public Accent accent() {
		return accent;
	}

	public void setAccent(Accent accent) {
		this.accent = accent;
	}

	public int offset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = Math.max(0, Math.min(48, offset));
	}

	public String displayText() {
		return switch (id) {
			case "coordinates" -> {
				var player = net.minecraft.client.Minecraft.getInstance().player;
				yield player == null ? null : "XYZ  " + player.getBlockX() + "  " + player.getBlockY() + "  " + player.getBlockZ();
			}
			case "fps" -> "FPS  " + net.minecraft.client.Minecraft.getInstance().getFps();
			case "clock" -> "LOCAL TIME  " + java.time.LocalTime.now()
					.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
			case "player-status" -> {
				var player = net.minecraft.client.Minecraft.getInstance().player;
				yield player == null ? null : "HEALTH  " + Math.round(player.getHealth()) + "   FOOD  "
						+ player.getFoodData().getFoodLevel();
			}
			case "armor" -> {
				var player = net.minecraft.client.Minecraft.getInstance().player;
				yield player == null ? null : "ARMOR  " + player.getArmorValue();
			}
			case "world-time" -> {
				var level = net.minecraft.client.Minecraft.getInstance().level;
				if (level == null) {
					yield null;
				}
				long time = Math.floorMod(level.getDefaultClockTime(), 24_000L);
				yield "WORLD TIME  " + String.format("%02d:%02d", (time / 1_000 + 6) % 24, (time % 1_000) * 60 / 1_000);
			}
			default -> null;
		};
	}

	public static List<ClientFeature> createDefaults() {
		return List.of(
				new ClientFeature("coordinates", "Coordinates", "Your position, always in view.", FeatureCategory.HUD, true),
				new ClientFeature("fps", "FPS Counter", "Live frame-rate readout.", FeatureCategory.HUD, true),
				new ClientFeature("clock", "Local Clock", "A small real-world clock.", FeatureCategory.HUD, false),
				new ClientFeature("player-status", "Player Status", "Your own health and hunger.", FeatureCategory.HUD, false),
				new ClientFeature("armor", "Armor", "Your own armor rating.", FeatureCategory.VISUAL, false),
				new ClientFeature("world-time", "World Time", "Time of day in your current world.", FeatureCategory.UTILITY, false)
		);
	}
}
