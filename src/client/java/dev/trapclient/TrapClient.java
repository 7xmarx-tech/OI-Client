package dev.trapclient;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class TrapClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("TRAP CLIENT");
	public static final List<ClientFeature> FEATURES = ClientFeature.createDefaults();
	private static final Map<ClientFeature.Position, Integer> STACKS = new EnumMap<>(ClientFeature.Position.class);
	private static KeyMapping openGuiKey;

	@Override
	public void onInitializeClient() {
		ClientConfig.load(FEATURES);

		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath("trapclient", "main")
		);
		openGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.trapclient.open_gui",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_RSHIFT,
				category
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openGuiKey.consumeClick()) {
				if (client.screen instanceof ClickGuiScreen clickGui) {
					clickGui.beginClose();
				} else if (client.screen == null) {
					client.setScreen(new ClickGuiScreen());
				}
			}
		});

		HudElementRegistry.attachElementBefore(
				VanillaHudElements.CHAT,
				Identifier.fromNamespaceAndPath("trapclient", "client_hud"),
				TrapClient::renderHud
		);
		LOGGER.info("TRAP CLIENT initialized. Press Right Shift to open the Click GUI.");
	}

	private static void renderHud(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		if (client.options.hideGui) {
			return;
		}

		STACKS.clear();
		int screenWidth = client.getWindow().getGuiScaledWidth();
		int screenHeight = client.getWindow().getGuiScaledHeight();
		for (ClientFeature feature : FEATURES) {
			if (!feature.enabled()) {
				continue;
			}
			String text = feature.displayText();
			if (text == null) {
				continue;
			}

			int textWidth = client.font.width(text);
			int stack = STACKS.merge(feature.position(), 1, Integer::sum) - 1;
			int inset = 8 + feature.offset();
			boolean right = feature.position() == ClientFeature.Position.TOP_RIGHT
					|| feature.position() == ClientFeature.Position.BOTTOM_RIGHT;
			boolean bottom = feature.position() == ClientFeature.Position.BOTTOM_LEFT
					|| feature.position() == ClientFeature.Position.BOTTOM_RIGHT;
			int x = right ? screenWidth - inset - textWidth : inset;
			int y = bottom ? screenHeight - inset - 10 - stack * 13 : inset + stack * 13;
			graphics.text(client.font, Component.literal(text), x, y, feature.accent().color(), true);
		}
	}
}
