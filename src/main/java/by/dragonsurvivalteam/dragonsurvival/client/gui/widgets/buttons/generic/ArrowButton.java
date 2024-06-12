package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import com.mojang.blaze3d.systems.RenderSystem;

public class ArrowButton extends Button {
	public static final ResourceLocation texture = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/magic_gui.png");
	public boolean next;

	public ArrowButton(int x, int y, int xSize, int ySize, boolean next, OnPress pressable){
		super(x, y, xSize, ySize, Component.empty(), pressable, DEFAULT_NARRATION);
		this.next = next;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int p_230431_2_, int p_230431_3_, float p_230431_4_){
		RenderSystem.setShaderTexture(0, texture);

		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, 0, 0);
		float xSize = (float)width / 34F;
		float ySize = (float)height / 34F;

		guiGraphics.pose().translate(getX() - getX() * xSize, getY() - getY() * ySize, 0);
		guiGraphics.pose().scale(xSize, ySize, 0);

		if(next)
			if(isHovered){
				guiGraphics.blit(texture, getX(), getY(), 34, 34, 34, 34);
			}else{
				guiGraphics.blit(texture, getX(), getY(), 0, 34, 34, 34);
			}
		else if(isHovered){
			guiGraphics.blit(texture,getX(), getY(), 34, 0, 34, 34);
		}else{
			guiGraphics.blit(texture, getX(), getY(), 0, 0, 34, 34);
		}
		guiGraphics.pose().popPose();
	}
}