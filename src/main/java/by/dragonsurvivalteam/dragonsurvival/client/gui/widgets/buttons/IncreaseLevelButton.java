package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ArrowButton;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.common.passive.PassiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncSkillLevelChangeCost;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class IncreaseLevelButton extends ArrowButton {
    @Translation(type = Translation.Type.MISC, comments = "§aUpgrade the skill for§r %s §alevels§r")
    private static final String LEVEL_UP = Translation.Type.GUI.wrap("level_up");

    public int skillCost;

    private PassiveDragonAbility ability;
    private final int slot;

    public IncreaseLevelButton(int x, int y, int slot) {
        super(x, y, 16, 16, true, Button::onPress);
        this.slot = slot;
    }

    @Override
    public void onPress() {
        DragonStateProvider.getOptional(Minecraft.getInstance().player).ifPresent(cap -> {
            ability = cap.getMagicData().getPassiveAbilityFromSlot(slot);

            if (ability != null) {
                int newLevel = ability.getLevel() + 1;

                if (newLevel <= ability.getMaxLevel()) {
                    if (Minecraft.getInstance().player.experienceLevel >= ability.getLevelCost(1) || Minecraft.getInstance().player.isCreative()) {
                        PacketDistributor.sendToServer(new SyncSkillLevelChangeCost.Data(newLevel, ability.getName(), 1));
                        DragonAbilities.setAbilityLevel(Minecraft.getInstance().player, ability.getClass(), newLevel);
                    }
                }
            }
        });
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderWidget(graphics, pMouseX, pMouseY, pPartialTick);

        if (isHovered()) {
            DragonStateProvider.getOptional(Minecraft.getInstance().player).ifPresent(cap -> {
                ability = cap.getMagicData().getPassiveAbilityFromSlot(slot);

                if (ability != null) {
                    ArrayList<Component> description = new ArrayList<>(List.of(Component.translatable(LEVEL_UP, skillCost)));

                    if (!ability.getLevelUpInfo().isEmpty()) {
                        description.add(Component.empty());
                        description.addAll(ability.getLevelUpInfo());
                    }

                    if (ability.getLevel() < ability.getMaxLevel()) {
                        skillCost = ability.getLevelCost(1);
                        graphics.renderComponentTooltip(Minecraft.getInstance().font, description, pMouseX, pMouseY);
                    }
                }
            });
        }
    }
}