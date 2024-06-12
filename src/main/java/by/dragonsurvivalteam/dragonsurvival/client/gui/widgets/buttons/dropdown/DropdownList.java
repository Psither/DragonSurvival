package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.dropdown;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class DropdownList extends AbstractSelectionList<DropdownEntry> {
	public static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/textbox.png");
	public int listWidth;
	public boolean visible;

	public DropdownList(int x, int y, int xSize, int ySize, int itemHeight) {
		super(Minecraft.getInstance(), 0, 0, 0, 0, itemHeight);
		listWidth = xSize;
		setRenderBackground(false);
		setRenderTopAndBottom(false);
		reposition(x, y, xSize, ySize);
	}

	public void reposition(int x, int y, int xSize, int ySize){
		x0 = x;
		y0 = y + 3;

		width = xSize;
		height = ySize;

		height = Math.min(height, Minecraft.getInstance().getWindow().getGuiScaledHeight() - (y + 6));

		x1 = x + width;
		y1 = y + 3 + height;
	}

	@Override
	public int addEntry(@NotNull final DropdownEntry entry) {
		return super.addEntry(entry);
	}

	@Override
	public void updateNarration(@NotNull final NarrationElementOutput narration) { /* Nothing to do */ }

	@Override
	public void centerScrollOn(@NotNull final DropdownEntry entry) {
		setScrollAmount(children().indexOf(entry) * itemHeight + (double) itemHeight / 2 - (double) (y1 - y0) / 2);
	}

	@Override
	public int getScrollbarPosition(){
		return x1 - 6 - 3;
	}

	@Override
	protected void renderBackground(@NotNull final GuiGraphics guiGraphics) {
		// Renders the black background
		guiGraphics.blitWithBorder(BACKGROUND_TEXTURE, x0, y0 - 3, 0, 0, width, height + 6, 32, 32, 10, 10, 10, 10);

		if (!children().isEmpty()) {
			RenderSystem.enableScissor((int)(x0 * Minecraft.getInstance().getWindow().getGuiScale()), (int)(Minecraft.getInstance().getWindow().getScreenHeight() - (y1 - 3) * Minecraft.getInstance().getWindow().getGuiScale()), (int)(width * Minecraft.getInstance().getWindow().getGuiScale()), (int)((height - 6) * Minecraft.getInstance().getWindow().getGuiScale()));
		}
	}

	@Override
	protected int getMaxPosition() {
		return getItemCount() * itemHeight + headerHeight + itemHeight / 4;
	}

	// For some reason the parent function for this returns a constant of 220 instead of width
	@Override
	public int getRowWidth() {
		return width;
	}

	@Override
	public void render(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (!visible) {
			return;
		}

		int zTranslation = 150;

		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, 0, zTranslation);
		// Background square which contains all dragon editor part buttons
		renderBackground(guiGraphics);
		int i = getScrollbarPosition();
		int j = i + 6;
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		renderList(guiGraphics, mouseX, mouseY, partialTicks);

		if (!children().isEmpty()) {
			RenderSystem.disableScissor();
		}

		int k1 = getMaxScroll();

		// Render the scroll bar
		if (k1 > 0) {
			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			int l1 = (int) ((float) ((y1 - y0) * (y1 - y0)) / (float) getMaxPosition());
			l1 = Mth.clamp(l1, itemHeight, y1 - y0 - 8);
			int i2 = Math.max(y0, (int) getScrollAmount() * (y1 - y0 - l1) / k1 + y0);
			double z = zTranslation + 10;

			bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
			bufferbuilder.vertex(i, y1, z).color(0, 0, 0, 255).endVertex();
			bufferbuilder.vertex(j, y1, z).color(0, 0, 0, 255).endVertex();
			bufferbuilder.vertex(j, y0, z).color(0, 0, 0, 255).endVertex();
			bufferbuilder.vertex(i, y0, z).color(0, 0, 0, 255).endVertex();
			bufferbuilder.vertex(i, i2 + l1, z).color(128, 128, 128, 255).endVertex();
			bufferbuilder.vertex(j, i2 + l1, 0.0D).color(128, 128, 128, 255).endVertex();
			bufferbuilder.vertex(j, i2, z).color(128, 128, 128, 255).endVertex();
			bufferbuilder.vertex(i, i2, z).color(128, 128, 128, 255).endVertex();
			bufferbuilder.vertex(i, i2 + l1 - 1, z).color(192, 192, 192, 255).endVertex();
			bufferbuilder.vertex(j - 1, i2 + l1 - 1, z).color(192, 192, 192, 255).endVertex();
			bufferbuilder.vertex(j - 1, i2, z).color(192, 192, 192, 255).endVertex();
			bufferbuilder.vertex(i, i2, z).color(192, 192, 192, 255).endVertex();
			tesselator.end();
		}

		renderDecorations(guiGraphics, mouseX, mouseY);
		RenderSystem.disableBlend();
		guiGraphics.pose().popPose();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return visible && mouseY >= (double) y0 - 3 && mouseY <= (double) y1 + 3 && mouseX >= (double) x0 && mouseX <= (double) x1;
	}
}