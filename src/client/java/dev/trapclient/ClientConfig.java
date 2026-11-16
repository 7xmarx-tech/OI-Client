package dev.trapclient;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

public final class ClientConfig {
	private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("trapclient.properties");

	private ClientConfig() {
	}

	public static void load(List<ClientFeature> features) {
		if (!Files.exists(FILE)) {
			return;
		}

		Properties properties = new Properties();
		try (var reader = Files.newBufferedReader(FILE)) {
			properties.load(reader);
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not read TRAP CLIENT settings from " + FILE, exception);
		}

		for (ClientFeature feature : features) {
			String key = feature.id() + ".";
			feature.setEnabled(readBoolean(properties, key + "enabled", feature.enabled()));
			feature.setOffset(readInt(properties, key + "offset", feature.offset()));
			feature.setPosition(readEnum(properties, key + "position", ClientFeature.Position.class, feature.position()));
			feature.setAccent(readEnum(properties, key + "accent", ClientFeature.Accent.class, feature.accent()));
		}
	}

	public static void save(List<ClientFeature> features) {
		Properties properties = new Properties();
		for (ClientFeature feature : features) {
			String key = feature.id() + ".";
			properties.setProperty(key + "enabled", Boolean.toString(feature.enabled()));
			properties.setProperty(key + "offset", Integer.toString(feature.offset()));
			properties.setProperty(key + "position", feature.position().name());
			properties.setProperty(key + "accent", feature.accent().name());
		}

		try {
			Files.createDirectories(FILE.getParent());
			try (var writer = Files.newBufferedWriter(FILE)) {
				properties.store(writer, "TRAP CLIENT settings");
			}
		} catch (IOException exception) {
			throw new UncheckedIOException("Could not save TRAP CLIENT settings to " + FILE, exception);
		}
	}

	private static boolean readBoolean(Properties properties, String key, boolean fallback) {
		String value = properties.getProperty(key);
		if (value == null) {
			return fallback;
		}
		if (value.equalsIgnoreCase("true")) {
			return true;
		}
		if (value.equalsIgnoreCase("false")) {
			return false;
		}
		TrapClient.LOGGER.warn("Ignoring invalid boolean setting '{}={}'", key, value);
		return fallback;
	}

	private static int readInt(Properties properties, String key, int fallback) {
		String value = properties.getProperty(key);
		if (value == null) {
			return fallback;
		}
		try {
			return Math.max(0, Math.min(48, Integer.parseInt(value)));
		} catch (NumberFormatException exception) {
			TrapClient.LOGGER.warn("Ignoring invalid integer setting '{}={}'", key, value);
			return fallback;
		}
	}

	private static <T extends Enum<T>> T readEnum(Properties properties, String key, Class<T> type, T fallback) {
		String value = properties.getProperty(key);
		if (value == null) {
			return fallback;
		}
		try {
			return Enum.valueOf(type, value);
		} catch (IllegalArgumentException exception) {
			TrapClient.LOGGER.warn("Ignoring invalid setting '{}={}'", key, value);
			return fallback;
		}
	}
}
