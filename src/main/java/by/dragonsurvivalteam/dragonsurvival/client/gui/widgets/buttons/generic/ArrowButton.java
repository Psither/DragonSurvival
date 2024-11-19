package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class ArrowButton extends Button {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/magic_gui.png");

    public boolean next;

    public ArrowButton(int x, int y, int xSize, int ySize, boolean next, OnPress pressable) {
        super(x, y, xSize, ySize, Component.empty(), pressable, DEFAULT_NARRATION);
        this.next = next;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
        RenderSystem.setShaderTexture(0, TEXTURE);

        float xSize = (float) width / 34F;
        float ySize = (float) height / 34F;

        graphics.pose().pushPose();
        graphics.pose().translate(getX() - getX() * xSize, getY() - getY() * ySize, 0);
        graphics.pose().scale(xSize, ySize, 0);

        if (next) {
            if (isHovered) {
                graphics.blit(TEXTURE, getX(), getY(), 34, 34, 34, 34);
            } else {
                graphics.blit(TEXTURE, getX(), getY(), 0, 34, 34, 34);
            }
        } else if (isHovered) {
            graphics.blit(TEXTURE, getX(), getY(), 34, 0, 34, 34);
        } else {
            graphics.blit(TEXTURE, getX(), getY(), 0, 0, 34, 34);
        }

        graphics.pose().popPose();
    }
}