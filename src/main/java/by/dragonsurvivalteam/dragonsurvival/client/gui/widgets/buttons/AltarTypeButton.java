package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonAltarScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.DietComponent;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.CaveDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.ForestDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.SeaDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class AltarTypeButton extends Button {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dragon_altar_icons.png");

    @Translation(type = Translation.Type.MISC, comments = "You have awakened from your sleep, and become a human.")
    private static final String CHOICE_HUMAN = Translation.Type.GUI.wrap("altar.choice.human");

    @Translation(type = Translation.Type.MISC, comments = {
            "■ §nHuman.§r",
            "■ Homo sapiens.§r",
            "Travelers, builders, and creators."
    })
    private static final String HUMAN = Translation.Type.GUI.wrap("altar.info.human");

    @Translation(type = Translation.Type.MISC, comments = {
            "§c■ Cave dragon.§r",
            "§2■ Features:§f§r fire resistance, pickaxe claws, fire magic, faster movement on stone and magma blocks.",
            "§4■ Weakness:§r water.",
            "§6■ Diet:§r"
    })
    private static final String CAVE_DRAGON = Translation.Type.GUI.wrap("altar.info.cave");

    @Translation(type = Translation.Type.MISC, comments = {
            "§3■ Sea dragon.§r",
            "§2■ Features:§f§r underwater breathing, shovel claws, electric magic, faster movement on ice and beach blocks.",
            "§4■ Weakness:§r dehydration.",
            "§6■ Diet:§r"
    })
    private static final String SEA_DRAGON = Translation.Type.GUI.wrap("altar.info.sea");

    @Translation(type = Translation.Type.MISC, comments = {
            "§a■ Forest dragon.§r",
            "§2■ Features:§f§r soft fall, axe claws, poison magic, faster movement on wooden and grass blocks.",
            "§4■ Weakness:§r dark caves.",
            "§6■ Diet:§r"
    })
    private static final String FOREST_DRAGON = Translation.Type.GUI.wrap("altar.info.forest");

    public AbstractDragonType type;

    private final DragonAltarScreen parent;
    private int scroll;
    private boolean resetScroll;

    public AltarTypeButton(DragonAltarScreen parent, AbstractDragonType type, int x, int y) {
        super(x, y, 49, 147, Component.empty(), Button::onPress, DEFAULT_NARRATION);
        this.parent = parent;
        this.type = type;

        scroll = 0;
    }

    @Override
    public void onPress() {
        initiateDragonForm(type);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isHovered() && isBottomOrTop(mouseY)) {
            scroll += (int) -scrollY; // invert the value so that scrolling down shows further entries
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHovered() && isBottomOrTop(mouseY)) {
            if (resetScroll) {
                resetScroll = false;
                scroll = 0;
            }

            List<Either<FormattedText, TooltipComponent>> components = new ArrayList<>();

            if (type != null) {
                List<Item> foods = DragonFoodHandler.getEdibleFoods(type);

                int maxItems = 5;

                if (foods.size() < 1 + maxItems) {
                    scroll = 0;
                } else {
                    scroll = Math.clamp(scroll, 0, foods.size() - 1 - maxItems);
                }

                int max = Math.min(foods.size() - 1, scroll + maxItems);

                String translationKey = switch (type) {
                    case ForestDragonType ignored -> FOREST_DRAGON;
                    case CaveDragonType ignored -> CAVE_DRAGON;
                    case SeaDragonType ignored -> SEA_DRAGON;
                    default -> throw new IllegalArgumentException("Invalid dragon type [" + type + "]");
                };

                // TODO : could append a scroll-icon here?
                // Using the color codes in the translation doesn't seem to apply the color to the entire text - therefor we create the [shown / max_items] tooltip part here
                MutableComponent shownFoods = Component.literal(" [" + (scroll + maxItems) + " / " + (foods.size() - 1) + "]").withStyle(ChatFormatting.DARK_GRAY);
                components.addFirst(Either.left(Component.translatable(translationKey).append(shownFoods)));

                for (int i = scroll; i < max; i++) {
                    components.add(Either.right(new DietComponent(type, foods.get(i))));
                }

                graphics.renderComponentTooltipFromElements(Minecraft.getInstance().font, components, mouseX, mouseY, ItemStack.EMPTY);
            } else {
                components.addFirst(Either.left(Component.translatable(HUMAN)));
                graphics.renderComponentTooltipFromElements(Minecraft.getInstance().font, components, mouseX, mouseY, ItemStack.EMPTY);
            }
        } else {
            resetScroll = true;
        }

        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);

        int uOffset = 3;

        if (DragonUtils.isType(type, DragonTypes.CAVE)) {
            uOffset = 0;
        } else if (DragonUtils.isType(type, DragonTypes.FOREST)) {
            uOffset = 1;
        } else if (DragonUtils.isType(type, DragonTypes.SEA)) {
            uOffset = 2;
        }

        graphics.fill(getX() - 1, getY() - 1, getX() + width, getY() + height, new Color(0.5f, 0.5f, 0.5f).getRGB());
        graphics.blit(BACKGROUND_TEXTURE, getX(), getY(), uOffset * 49, isHovered ? 0 : 147, 49, 147, 512, 512);
    }

    private boolean isBottomOrTop(double mouseY) {
        return mouseY > getY() + 6 && mouseY < getY() + 26 || mouseY > getY() + 133 && mouseY < getY() + 153;
    }

    private void initiateDragonForm(AbstractDragonType type) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null)
            return;

        if (type == null) {
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable(CHOICE_HUMAN));

            DragonStateProvider.getOptional(player).ifPresent(cap -> {
                player.level().playSound(player, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 0.7f);
                cap.revertToHumanForm(player, false);
                PacketDistributor.sendToServer(new SyncComplete.Data(player.getId(), cap.serializeNBT(player.registryAccess())));
            });
            player.closeContainer();
        } else {
            Minecraft.getInstance().setScreen(new DragonEditorScreen(parent, type));
        }
    }
}