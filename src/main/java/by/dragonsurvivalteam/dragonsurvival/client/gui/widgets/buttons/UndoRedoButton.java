package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ArrowButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.checkerframework.checker.nullness.qual.NonNull;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class UndoRedoButton extends ArrowButton {
    private static final ResourceLocation UNDO = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/arrow_undo.png");
    private static final ResourceLocation REDO = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/arrow_redo.png");

    public UndoRedoButton(int x, int y, int xSize, int ySize, boolean next, OnPress pressable) {
        super(x, y, xSize, ySize, next, pressable);
    }

    @Override
    public void renderWidget(@NonNull final GuiGraphics graphics, int p_230431_2_, int p_230431_3_, float p_230431_4_) {
        if (next) {
            graphics.blit(REDO, getX(), getY(), 0, 0, width, height, width, height);
        } else {
            graphics.blit(UNDO, getX(), getY(), 0, 0, width, height, width, height);
        }
    }
}