package dev.trapclient;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class ClickGuiScreen extends Screen {
	private static final int PANEL_WIDTH = 900;
	private static final int PANEL_HEIGHT = 540;
	private static final int SIDEBAR_WIDTH = 174;
	private static final int HEADER_HEIGHT = 76;
	private static final int CARD_HEIGHT = 148;
	private static final int CARD_GAP = 12;

	private FeatureCategory selectedCategory = FeatureCategory.ALL;
	private String search = "";
	private int scrollOffset;
	private ClientFeature draggingFeature;
	private long openedAt;
	private long closingAt;
	private int panelX;
	private int panelY;
	private int panelWidth;
	private int panelHeight;

	public ClickGuiScreen() {
		super(Component.literal("TRAP CLIENT"));
	}

	@Override
	protected void init() {
		openedAt = System.currentTimeMillis();
		panelWidth = Math.min(PANEL_WIDTH, Math.max(320, width - 28));
		panelHeight = Math.min(PANEL_HEIGHT, Math.max(280, height - 28));
		panelX = (width - panelWidth) / 2;
		panelY = (height - panelHeight) / 2;
		scrollOffset = 0;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		long now = System.currentTimeMillis();
		float animation = Math.min(1.0F, (now - openedAt) / 220.0F);
		if (closingAt != 0) {
			animation = Math.max(0.0F, 1.0F - (now - closingAt) / 180.0F);
		}
		int slide = Math.round((1.0F - animation) * 12);
		fill(graphics, 0, 0, width, height, 0xAA08060D);

		int x = panelX;
		int y = panelY + slide;
		roundedPanel(graphics, x, y, panelWidth, panelHeight, 0xF512101B);
		graphics.fill(x + 1, y + 1, x + SIDEBAR_WIDTH, y + panelHeight - 1, 0xF017141F);
		graphics.fill(x + SIDEBAR_WIDTH, y + HEADER_HEIGHT, x + SIDEBAR_WIDTH + 1, y + panelHeight - 16, 0xFF282333);

		renderBrand(graphics, x, y, animation);
		renderSidebar(graphics, x, y, mouseX, mouseY);
		renderHeader(graphics, x, y, mouseX, mouseY);
		renderFeatureCards(graphics, x, y, mouseX, mouseY);
	}

	public void beginClose() {
		if (closingAt == 0) {
			closingAt = System.currentTimeMillis();
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (closingAt != 0 && System.currentTimeMillis() - closingAt >= 180) {
			minecraft.setScreen(null);
		}
	}

	private void renderBrand(GuiGraphicsExtractor graphics, int x, int y, float animation) {
		graphics.fill(x + 20, y + 21, x + 47, y + 48, 0xFF8A55D7);
		graphics.text(font, "T", x + 28, y + 26, 0xFFFFFFFF, true);
		graphics.text(font, "TRAP", x + 57, y + 21, 0xFFF7F3FF, true);
		graphics.text(font, "CLIENT", x + 57, y + 37, 0xFFAE8BE5, true);
		graphics.text(font, "CLIENT-SIDE  /  26.1.2", x + SIDEBAR_WIDTH + 22, y + 26, 0xFF8F899B, false);
		graphics.text(font, "01", x + panelWidth - 36, y + 25, 0xFF9B7EC5, false);
	}

	private void renderSidebar(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
		graphics.text(font, "WORKSPACE", x + 20, y + 87, 0xFF777184, false);
		for (int index = 0; index < FeatureCategory.values().length; index++) {
			FeatureCategory category = FeatureCategory.values()[index];
			int itemY = y + 105 + index * 39;
			boolean selected = selectedCategory == category;
			boolean hovered = inside(mouseX, mouseY, x + 12, itemY, SIDEBAR_WIDTH - 24, 32);
			if (selected) {
				graphics.fill(x + 12, itemY, x + SIDEBAR_WIDTH - 12, itemY + 32, 0xFF332440);
				graphics.fill(x + 12, itemY + 5, x + 14, itemY + 27, 0xFFB181F1);
			} else if (hovered) {
				graphics.fill(x + 12, itemY, x + SIDEBAR_WIDTH - 12, itemY + 32, 0xFF211D2A);
			}
			graphics.text(font, category.icon(), x + 24, itemY + 9, selected ? 0xFFC59AFB : 0xFF898394, false);
			graphics.text(font, category.label(), x + 46, itemY + 9,
					selected ? 0xFFF3EBFF : 0xFFAAA5B4, false);
			if (category != FeatureCategory.ALL) {
				graphics.text(font, Integer.toString(count(category)), x + SIDEBAR_WIDTH - 35, itemY + 9,
						0xFF787283, false);
			}
		}

		int footerY = y + panelHeight - 54;
		graphics.fill(x + 20, footerY - 13, x + SIDEBAR_WIDTH - 20, footerY - 12, 0xFF2C2835);
		graphics.text(font, "RIGHT SHIFT", x + 20, footerY, 0xFFC19AEF, false);
		graphics.text(font, "TOGGLE MENU", x + 20, footerY + 14, 0xFF777181, false);
	}

	private void renderHeader(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
		int contentX = x + SIDEBAR_WIDTH;
		int searchX = x + panelWidth - 224;
		int searchY = y + 18;
		graphics.text(font, selectedCategory.label(), contentX + 22, y + 17, 0xFFF1EDF7, true);
		graphics.text(font, "Curated client-side tools for your everyday play.", contentX + 22, y + 35,
				0xFF8F8999, false);
		graphics.fill(searchX, searchY, searchX + 196, searchY + 30, 0xFF211D2B);
		graphics.outline(searchX, searchY, 196, 30, inside(mouseX, mouseY, searchX, searchY, 196, 30)
				? 0xFF7759A5 : 0xFF302A3A);
		graphics.text(font, "⌕", searchX + 10, searchY + 8, 0xFFAA93D1, false);
		String shownSearch = search.isEmpty() ? "Search features..." : search;
		graphics.text(font, shownSearch, searchX + 29, searchY + 9,
				search.isEmpty() ? 0xFF787283 : 0xFFEDE7F7, false);
		graphics.text(font, "ESC", x + panelWidth - 37, y + 56, 0xFF6F6A78, false);
	}

	private void renderFeatureCards(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY) {
		List<ClientFeature> features = filteredFeatures();
		int areaX = x + SIDEBAR_WIDTH + 20;
		int areaY = y + HEADER_HEIGHT;
		int areaWidth = panelWidth - SIDEBAR_WIDTH - 40;
		int columnGap = 12;
		int cardWidth = (areaWidth - columnGap) / 2;
		int visibleHeight = panelHeight - HEADER_HEIGHT - 23;
		graphics.enableScissor(areaX, areaY, areaX + areaWidth, areaY + visibleHeight);

		if (features.isEmpty()) {
			graphics.text(font, "No features match your search.", areaX + 12, areaY + 20, 0xFF918B9C, false);
		}
		for (int index = 0; index < features.size(); index++) {
			ClientFeature feature = features.get(index);
			int column = index % 2;
			int row = index / 2;
			int cardX = areaX + column * (cardWidth + columnGap);
			int cardY = areaY + 12 + row * (CARD_HEIGHT + CARD_GAP) - scrollOffset;
			if (cardY + CARD_HEIGHT < areaY || cardY > areaY + visibleHeight) {
				continue;
			}
			renderCard(graphics, feature, cardX, cardY, cardWidth, mouseX, mouseY);
		}
		graphics.disableScissor();
	}

	private void renderCard(GuiGraphicsExtractor graphics, ClientFeature feature, int x, int y, int cardWidth,
			int mouseX, int mouseY) {
		boolean hovered = inside(mouseX, mouseY, x, y, cardWidth, CARD_HEIGHT);
		roundedPanel(graphics, x, y, cardWidth, CARD_HEIGHT, hovered ? 0xFF211D2B : 0xFF1C1925);
		graphics.outline(x, y, cardWidth, CARD_HEIGHT, hovered ? 0xFF393144 : 0xFF2A2633);
		graphics.fill(x + 15, y + 15, x + 18, y + 34, feature.enabled() ? 0xFFAA78E9 : 0xFF625C6B);
		graphics.text(font, feature.name(), x + 26, y + 14, 0xFFF2EDF8, true);
		graphics.text(font, feature.description(), x + 26, y + 31, 0xFF8F8999, false);

		int toggleX = x + cardWidth - 53;
		int toggleY = y + 16;
		int toggleColor = feature.enabled() ? 0xFF8052B8 : 0xFF3B3744;
		graphics.fill(toggleX, toggleY, toggleX + 37, toggleY + 19, toggleColor);
		int knobX = feature.enabled() ? toggleX + 21 : toggleX + 3;
		graphics.fill(knobX, toggleY + 3, knobX + 13, toggleY + 16, 0xFFF5F0FA);

		int fieldY = y + 61;
		int fieldGap = 8;
		int fieldWidth = (cardWidth - 40 - fieldGap) / 2;
		drawChoice(graphics, x + 16, fieldY, fieldWidth, "ACCENT", feature.accent().label(), mouseX, mouseY);
		drawChoice(graphics, x + 16 + fieldWidth + fieldGap, fieldY, fieldWidth, "POSITION",
				feature.position().label(), mouseX, mouseY);

		int sliderY = y + 117;
		int sliderLeft = x + 16;
		int sliderRight = x + cardWidth - 16;
		graphics.text(font, "EDGE OFFSET", sliderLeft, y + 101, 0xFF888292, false);
		String offsetText = feature.offset() + " px";
		graphics.text(font, offsetText, sliderRight - font.width(offsetText), y + 101, 0xFFC2B9CF, false);
		graphics.fill(sliderLeft, sliderY, sliderRight, sliderY + 3, 0xFF3D3748);
		int knob = sliderLeft + Math.round((sliderRight - sliderLeft) * feature.offset() / 48.0F);
		graphics.fill(sliderLeft, sliderY, knob + 1, sliderY + 3, 0xFF9E6BE0);
		graphics.fill(knob - 4, sliderY - 3, knob + 5, sliderY + 7, 0xFFEDE5F8);
	}

	private void drawChoice(GuiGraphicsExtractor graphics, int x, int y, int width, String label, String value,
			int mouseX, int mouseY) {
		graphics.fill(x, y, x + width, y + 31, 0xFF17151F);
		graphics.outline(x, y, width, 31, inside(mouseX, mouseY, x, y, width, 31)
				? 0xFF7759A5 : 0xFF302B38);
		graphics.text(font, label, x + 8, y + 4, 0xFF777181, false);
		graphics.text(font, value + "  ▾", x + 8, y + 16, 0xFFD8D1E2, false);
	}

	private int count(FeatureCategory category) {
		return (int) TrapClient.FEATURES.stream().filter(feature -> feature.category() == category).count();
	}

	private List<ClientFeature> filteredFeatures() {
		String query = search.toLowerCase(Locale.ROOT).trim();
		List<ClientFeature> result = new ArrayList<>();
		for (ClientFeature feature : TrapClient.FEATURES) {
			boolean categoryMatches = selectedCategory == FeatureCategory.ALL || feature.category() == selectedCategory;
			boolean searchMatches = query.isEmpty() || feature.name().toLowerCase(Locale.ROOT).contains(query)
					|| feature.description().toLowerCase(Locale.ROOT).contains(query);
			if (categoryMatches && searchMatches) {
				result.add(feature);
			}
		}
		return result;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
			return super.mouseClicked(event, doubleClick);
		}

		int areaX = panelX + SIDEBAR_WIDTH + 20;
		int areaY = panelY + HEADER_HEIGHT;
		for (int index = 0; index < FeatureCategory.values().length; index++) {
			int itemY = panelY + 105 + index * 39;
			if (inside(mouseX, mouseY, panelX + 12, itemY, SIDEBAR_WIDTH - 24, 32)) {
				selectedCategory = FeatureCategory.values()[index];
				scrollOffset = 0;
				return true;
			}
		}

		int searchX = panelX + panelWidth - 224;
		if (inside(mouseX, mouseY, searchX, panelY + 18, 196, 30)) {
			searchFocused = true;
			return true;
		}
		searchFocused = false;

		int areaWidth = panelWidth - SIDEBAR_WIDTH - 40;
		int cardWidth = (areaWidth - 12) / 2;
		List<ClientFeature> features = filteredFeatures();
		for (int index = 0; index < features.size(); index++) {
			ClientFeature feature = features.get(index);
			int column = index % 2;
			int row = index / 2;
			int cardX = areaX + column * (cardWidth + 12);
			int cardY = areaY + 12 + row * (CARD_HEIGHT + CARD_GAP) - scrollOffset;
			int toggleX = cardX + cardWidth - 53;
			if (inside(mouseX, mouseY, toggleX, cardY + 16, 37, 19)) {
				feature.setEnabled(!feature.enabled());
				ClientConfig.save(TrapClient.FEATURES);
				return true;
			}

			int fieldY = cardY + 61;
			int fieldWidth = (cardWidth - 48) / 2;
			if (inside(mouseX, mouseY, cardX + 16, fieldY, fieldWidth, 31)) {
				feature.setAccent(feature.accent().next());
				ClientConfig.save(TrapClient.FEATURES);
				return true;
			}
			if (inside(mouseX, mouseY, cardX + 16 + fieldWidth + 8, fieldY, fieldWidth, 31)) {
				feature.setPosition(feature.position().next());
				ClientConfig.save(TrapClient.FEATURES);
				return true;
			}

			int sliderY = cardY + 110;
			if (inside(mouseX, mouseY, cardX + 16, sliderY, cardWidth - 32, 17)) {
				draggingFeature = feature;
				updateSlider(feature, mouseX, cardX, cardWidth);
				ClientConfig.save(TrapClient.FEATURES);
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	private boolean searchFocused;

	private void updateSlider(ClientFeature feature, double mouseX, int cardX, int cardWidth) {
		double progress = (mouseX - (cardX + 16.0)) / (cardWidth - 32.0);
		feature.setOffset((int) Math.round(Math.max(0.0, Math.min(1.0, progress)) * 48));
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
		double mouseX = event.x();
		if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && draggingFeature != null) {
			int areaWidth = panelWidth - SIDEBAR_WIDTH - 40;
			int cardWidth = (areaWidth - 12) / 2;
			for (int index = 0; index < filteredFeatures().size(); index++) {
				if (filteredFeatures().get(index) == draggingFeature) {
					int cardX = panelX + SIDEBAR_WIDTH + 20 + (index % 2) * (cardWidth + 12);
					updateSlider(draggingFeature, mouseX, cardX, cardWidth);
					return true;
				}
			}
		}
		return super.mouseDragged(event, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (draggingFeature != null) {
			ClientConfig.save(TrapClient.FEATURES);
			draggingFeature = null;
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		int rows = (filteredFeatures().size() + 1) / 2;
		int visibleHeight = panelHeight - HEADER_HEIGHT - 23;
		int contentHeight = rows * (CARD_HEIGHT + CARD_GAP);
		int maxScroll = Math.max(0, contentHeight - visibleHeight + 12);
		scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int) Math.signum(verticalAmount) * 42));
		return true;
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		int codepoint = event.codepoint();
		if (!Character.isValidCodePoint(codepoint) || Character.isISOControl(codepoint)) {
			return super.charTyped(event);
		}
		if (searchFocused && search.length() < 40) {
			search += Character.toChars(codepoint);
			scrollOffset = 0;
			return true;
		}
		return super.charTyped(event);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (searchFocused && event.key() == InputConstants.KEY_ESCAPE) {
			searchFocused = false;
			return true;
		}
		if (searchFocused && event.key() == InputConstants.KEY_BACKSPACE && !search.isEmpty()) {
			search = search.substring(0, search.length() - 1);
			scrollOffset = 0;
			return true;
		}
		if (event.key() == InputConstants.KEY_ESCAPE) {
			beginClose();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public void onClose() {
		searchFocused = false;
		beginClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
		return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	private static void fill(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color) {
		graphics.fill(x1, y1, x2, y2, color);
	}

	private static void roundedPanel(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int color) {
		graphics.fill(x + 5, y, x + width - 5, y + height, color);
		graphics.fill(x, y + 5, x + width, y + height - 5, color);
		graphics.fill(x + 2, y + 2, x + width - 2, y + 4, color);
		graphics.fill(x + 2, y + height - 4, x + width - 2, y + height - 2, color);
	}
}
