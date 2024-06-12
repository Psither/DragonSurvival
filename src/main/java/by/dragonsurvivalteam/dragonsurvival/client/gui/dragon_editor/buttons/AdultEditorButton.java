package by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class AdultEditorButton extends Button{
	private final DragonEditorScreen dragonEditorScreen;

	public AdultEditorButton(DragonEditorScreen dragonEditorScreen){
		super(dragonEditorScreen.width / 2 + 60, dragonEditorScreen.guiTop - 30, 120, 20, Component.translatable("ds.level.adult"), btn -> {
			dragonEditorScreen.level = DragonLevel.ADULT;
			dragonEditorScreen.dragonRender.zoom = dragonEditorScreen.level.size * 2;
			dragonEditorScreen.handler.getSkinData().compileSkin();
			dragonEditorScreen.update();
		}, DEFAULT_NARRATION);
		this.dragonEditorScreen = dragonEditorScreen;
	}

	@Override
	public void render(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks){
		active = visible = dragonEditorScreen.showUi;
		super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
	}

	@Override
	public void renderWidget(@NotNull final GuiGraphics guiGraphics, int p_230431_2_, int p_230431_3_, float p_230431_4_){
		int j = isHovered || dragonEditorScreen.level == DragonLevel.ADULT ? 16777215 : 10526880;
		TextRenderUtil.drawCenteredScaledText(guiGraphics, getX() + width / 2, getY() + 4, 1.5f, getMessage().getString(), j | Mth.ceil(alpha * 255.0F) << 24);
	}
}