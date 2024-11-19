package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class HelpButton extends ExtendedButton {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/help_button.png");

    private final String text;
    private final int variation;
    private final AbstractDragonType type;

    private boolean usesVanillaTooltip;

    public HelpButton(int x, int y, int sizeX, int sizeY, String text, int variation) {
        this(DragonUtils.getType(Minecraft.getInstance().player), x, y, sizeX, sizeY, text, variation);
    }

    // This is needed for the DragonScreen, as otherwise we'll get cut out by the scissoring used for the rendering of the player entity in the window
    public HelpButton(int x, int y, int sizeX, int sizeY, String text, int variation, boolean usesVanillaTooltip) {
        this(DragonUtils.getType(Minecraft.getInstance().player), x, y, sizeX, sizeY, text, variation);
        if (usesVanillaTooltip) {
            setTooltip(Tooltip.create(Component.translatable(text)));
        }
        this.usesVanillaTooltip = usesVanillaTooltip;
    }

    public HelpButton(AbstractDragonType type, int x, int y, int sizeX, int sizeY, String text, int variation) {
        super(x, y, sizeX, sizeY, Component.empty(), action -> { /* Nothing to do */ });
        this.text = text;
        this.variation = variation;
        this.type = type;
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.setShaderTexture(0, TEXTURE);

        float size = variation == 0 ? 18f : 22f;
        float xSize = (float) (width + (variation == 0 ? 0 : 2)) / size;
        float ySize = (float) (height + (variation == 0 ? 0 : 2)) / size;

        int i = 0;
        if (isHovered() && !usesVanillaTooltip) {
            i += (int) ((Objects.equals(type, DragonTypes.CAVE) ? 1 : Objects.equals(type, DragonTypes.FOREST) ? 2 : Objects.equals(type, DragonTypes.SEA) ? 3 : 4) * size);
            // Render the tooltip manually since minecraft's tooltip positioner often fails with this button type
            guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, List.of(Component.translatable(text)), mouseX, mouseY);
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(getX() - getX() * xSize, getY() - getY() * ySize, 0);
        guiGraphics.pose().scale(xSize, ySize, 0);

        if (variation == 0) {
            guiGraphics.blit(TEXTURE, getX(), getY(), 0, (float) i, 18, 18, 256, 256);
        } else {
            guiGraphics.blit(TEXTURE, getX() - 1, getY() - 1, 18, (float) i, 22, 22, 256, 256);
        }

        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }
}