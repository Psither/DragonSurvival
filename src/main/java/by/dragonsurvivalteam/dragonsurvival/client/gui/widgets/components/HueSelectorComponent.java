package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.settings.ResetSettingsButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.dropdown.DropdownList;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.DSSlider;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ExtendedCheckbox;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorRegistry;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.EnumSkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonEditorObject;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.LayerSettings;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

public class HueSelectorComponent extends AbstractContainerEventHandler implements Renderable {
	private final ExtendedButton hueReset;
	private final ExtendedButton saturationReset;
	private final ExtendedButton brightnessReset;
	private final Checkbox glowing;
	private final DragonEditorScreen screen;
	private final int x;
	private final int y;
	private final int xSize;
	private final int ySize;
	private final Supplier<LayerSettings> settings;
	public boolean visible;
	private DSSlider hueSlider;
	private DSSlider saturationSlider;
	private DSSlider brightnessSlider;

	public HueSelectorComponent(DragonEditorScreen screen, int x, int y, int xSize, int ySize, EnumSkinLayer layer){
		this.screen = screen;
		this.x = x;
		this.y = y;
		this.xSize = xSize;
		this.ySize = ySize;

		settings = () -> screen.preset.skinAges.get(screen.level).get().layerSettings.get(layer).get();
		LayerSettings set = settings.get();
		DragonEditorObject.Texture text = DragonEditorHandler.getSkin(FakeClientPlayerUtils.getFakePlayer(0, screen.handler), layer, set.selectedSkin, screen.handler.getType());

		glowing = new ExtendedCheckbox(x + 3, y, xSize - 5, 10, 10, Component.translatable("ds.gui.dragon_editor.glowing"), set.glowing, s -> {
			settings.get().glowing = s.selected();
			screen.handler.getSkinData().compileSkin();
		});

		float[] hsb = new float[]{set.hue, set.saturation, set.brightness};

		if(text == null){
			hsb[0] = 0.5f;
			hsb[1] = 0.5f;
			hsb[2] = 0.5f;
		} else if (!set.modifiedColor) {
			hsb[0] = text.average_hue;
			hsb[1] = 0.5f;
			hsb[2] = 0.5f;
		}

		hueSlider = new DSSlider(x + 3, y + 12, xSize - 26, 20, Component.empty(), Component.empty(), 0, 360, hsb[0] * 360.0f, true){
			@Override
			protected void applyValue(){
				super.applyValue();

				float value = (hueSlider.getValueInt()) / 360f;
				float value1 = (saturationSlider.getValueInt()) / 360f;
				float value2 = (brightnessSlider.getValueInt()) / 360f;

				settings.get().hue = value;
				settings.get().saturation = value1;
				settings.get().brightness = value2;
                settings.get().modifiedColor = text != null && (Float.compare(Math.round(value * 360), Math.round(text.average_hue * 360)) != 0 || !(Math.abs(value1 - 0.5f) < 0.05) || !(Math.abs(value2 - 0.5f) < 0.05));

				DragonEditorScreen.handler.getSkinData().compileSkin();
				screen.update();
			}

			@Override
			public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partial){
				if(visible){
					this.isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
					RenderingUtils.renderPureColorSquare(guiGraphics.pose(), getX(), getY(), getWidth(), getHeight());
					guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 0, this.getHandleTextureY(), 8, this.height, 200, 20, 2, 3, 2, 2);
				}
			}
		};

		hueReset = new ExtendedButton(x + 3 + xSize - 26, y + 12, 20, 20, Component.empty(), button -> hueSlider.setValue(text != null ? Math.round(text.average_hue * 360f) : 180)) {
			@Override
			public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partial){
				super.renderWidget(guiGraphics, mouseX, mouseY, partial);
				guiGraphics.blit(ResetSettingsButton.texture, getX() + 2, getY() + 2, 0, 0, 16, 16, 16, 16);
			}
		};

		saturationSlider = new DSSlider(x + 3, y + 22 + 12, xSize - 26, 20, Component.empty(), Component.empty(), 0, 360, hsb[1] * 360, true){
			@Override
			protected void applyValue(){
				super.applyValue();

				float value = (hueSlider.getValueInt()) / 360f;
				float value1 = (saturationSlider.getValueInt()) / 360f;
				float value2 = (brightnessSlider.getValueInt()) / 360f;

				settings.get().hue = value;
				settings.get().saturation = value1;
				settings.get().brightness = value2;

				settings.get().modifiedColor = text != null && (Float.compare(Math.round(value * 360), Math.round(text.average_hue * 360)) != 0 || !(Math.abs(value1 - 0.5f) < 0.05) || !(Math.abs(value2 - 0.5f) < 0.05));

				DragonEditorScreen.handler.getSkinData().compileSkin();
				screen.update();
			}

			@Override
			public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partial){
				if(visible){
					this.isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
					float value1 = (hueSlider.getValueInt()) / 360f;

					int col1 = Color.getHSBColor(value1, 0f, 1f).getRGB();
					int col2 = Color.getHSBColor(value1, 1f, 1f).getRGB();

					RenderingUtils.drawGradientRect(guiGraphics.pose().last().pose(), 0, getX(), getY(), getX() + getWidth(), getY() + getHeight(), new int[]{col2, col1, col1, col2});
					guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 0, this.getHandleTextureY(), 8, this.height, 200, 20, 2, 3, 2, 2);
				}
			}
		};

		saturationReset = new ExtendedButton(x + 3 + xSize - 26, y + 22 + 12, 20, 20, Component.empty(), button -> saturationSlider.setValue(180)) {
			@Override
			public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partial){
				super.renderWidget(guiGraphics, mouseX, mouseY, partial);
				guiGraphics.blit(ResetSettingsButton.texture, getX() + 2, getY() + 2, 0, 0, 16, 16, 16, 16);
			}
		};

		brightnessReset = new ExtendedButton(x + 3 + xSize - 26, y + 44 + 12, 20, 20, Component.empty(), button -> brightnessSlider.setValue(180)) {
			@Override
			public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partial){
				super.renderWidget(guiGraphics, mouseX, mouseY, partial);
				guiGraphics.blit(ResetSettingsButton.texture, getX() + 2, getY() + 2, 0, 0, 16, 16, 16, 16);
			}
		};

		brightnessSlider = new DSSlider(x + 3, y + 44 + 12, xSize - 26, 20, Component.empty(), Component.empty(), 0, 360, hsb[2] * 360, true){
			@Override
			protected void applyValue(){
				super.applyValue();

				float value = (hueSlider.getValueInt()) / 360f;
				float value1 = (saturationSlider.getValueInt()) / 360f;
				float value2 = (brightnessSlider.getValueInt()) / 360f;

				settings.get().hue = value;
				settings.get().saturation = value1;
				settings.get().brightness = value2;
				settings.get().modifiedColor = text != null && (Float.compare(Math.round(value * 360), Math.round(text.average_hue * 360)) != 0 || !(Math.abs(value1 - 0.5f) < 0.05) || !(Math.abs(value2 - 0.5f) < 0.05));

				DragonEditorScreen.handler.getSkinData().compileSkin();
				screen.update();
			}

			@Override
			public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partial){
				if(visible){
					this.isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
					float value1 = (hueSlider.getValueInt()) / 360f;

					int col1 = Color.getHSBColor(value1, 1f, 0f).getRGB();
					int col2 = Color.getHSBColor(value1, 1f, 1f).getRGB();

					RenderingUtils.drawGradientRect(guiGraphics.pose().last().pose(), 0, getX(), getY(), getX() + getWidth(), getY() + getHeight(), new int[]{col2, col1, col1, col2});
					guiGraphics.blitWithBorder(SLIDER_LOCATION, this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 0, this.getHandleTextureY(), 8, this.height, 200, 20, 2, 3, 2, 2);
				}
			}
		};
	}

	@Override
	public boolean isMouseOver(double pMouseX, double pMouseY){
		return visible && pMouseY >= (double) y - 3 && pMouseY <= (double)y + ySize + 3 && pMouseX >= (double)x && pMouseX <= (double)x + xSize;
	}

	@Override
	public @NotNull List<? extends GuiEventListener> children() {
		return ImmutableList.of(hueSlider, saturationSlider, brightnessSlider, hueReset, saturationReset, brightnessReset, glowing);
	}

	@Override
	public void render(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks){
		guiGraphics.pose().pushPose();
		// Render pop-up menu content above the other elements
		guiGraphics.pose().translate(0, 0, 150);
		guiGraphics.blitWithBorder(DropdownList.BACKGROUND_TEXTURE, x, y - 3, 0, 0, xSize, ySize + 6, 32, 32, 10, 10, 10, 10);

		glowing.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);

		hueReset.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
		saturationReset.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
		brightnessReset.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);

		hueSlider.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
		saturationSlider.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
		brightnessSlider.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
		guiGraphics.pose().popPose();
	}
}