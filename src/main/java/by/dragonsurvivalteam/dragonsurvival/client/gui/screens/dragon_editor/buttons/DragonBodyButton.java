package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.TextureManagerAccess;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

public class DragonBodyButton extends Button {
    public static final String LOCATION_PREFIX = "textures/gui/body/";

    public static final int HOVERED = 1;
    public static final int SELECTED = 2;
    private static final int LOCKED = 3;

    private final DragonEditorScreen screen;
    private final Holder<DragonBody> dragonBody;
    private final ResourceLocation location;
    private final boolean locked;

    public DragonBodyButton(DragonEditorScreen screen, int x, int y, int xSize, int ySize, Holder<DragonBody> dragonBody, boolean locked) {
        super(x, y, xSize, ySize, Component.literal(dragonBody.toString()), action -> {
            if (!locked) {
                screen.actionHistory.add(new DragonEditorScreen.EditorAction<>(screen.dragonBodySelectAction, dragonBody));
            }
        }, DEFAULT_NARRATION);

        //noinspection DataFlowIssue -> key is present
        ResourceLocation bodyLocation = dragonBody.getKey().location();
        setTooltip(Tooltip.create(Component.translatable(Translation.Type.BODY_DESCRIPTION.wrap(bodyLocation.getNamespace(), bodyLocation.getPath()))));
        ResourceLocation iconLocation = ResourceLocation.fromNamespaceAndPath(bodyLocation.getNamespace(), LOCATION_PREFIX + bodyLocation.getPath() + "/" + screen.dragonType.getTypeNameLowerCase() + ".png");
        ResourceManager manager = ((TextureManagerAccess) Minecraft.getInstance().getTextureManager()).dragonSurvival$getResourceManager();

        if (manager.getResource(iconLocation).isEmpty()) {
            iconLocation = ResourceLocation.fromNamespaceAndPath(DragonBody.center.location().getNamespace(), LOCATION_PREFIX + DragonBody.center.location().getPath() + "/" + screen.dragonType.getTypeNameLowerCase() + ".png");
        }

        this.location = iconLocation;
        this.screen = screen;
        this.dragonBody = dragonBody;
        this.locked = locked;
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        active = visible = screen.showUi;
        int state = 0;

        if (DragonUtils.isBody(dragonBody, screen.dragonBody)) {
            state = SELECTED;
        } else if (locked) {
            state = LOCKED;
        } else if (isHoveredOrFocused()) {
            state = HOVERED;
        }

        graphics.blit(location, getX(), getY(), 0, state * this.height, this.width, this.height, 32, 104);
    }
}
