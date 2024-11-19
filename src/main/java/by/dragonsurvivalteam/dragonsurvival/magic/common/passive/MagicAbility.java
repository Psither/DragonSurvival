package by.dragonsurvivalteam.dragonsurvival.magic.common.passive;


import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DSAttributeModifier;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ManaHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonBody;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

import java.util.List;

public abstract class MagicAbility extends PassiveDragonAbility {
    @Override
    public Component getDescription() {
        if (player == null) {
            return Component.empty();
        }

        String manaFromExperience = "0";

        if (ServerConfig.consumeExperienceAsMana) {
            int experienceMana = Math.min(99, ManaHandler.getManaFromExperience(player));

            if (experienceMana > 0) {
                manaFromExperience = "+" + experienceMana;
            }
        }

        int abilityLevel = Math.min(99, getLevel());
        String manaFromAbility = abilityLevel > 0 ? "+" + abilityLevel : "0";

        Holder<DragonBody> body = DragonStateProvider.getData(player).getBody();
        List<DSAttributeModifier> manaModifiers = body.value().modifiers().stream().filter(modifier -> modifier.attribute().is(DSAttributes.MANA)).toList();

        AttributeInstance dummyInstance = new AttributeInstance(DSAttributes.MANA, instance -> { /* Nothing to do */ });
        manaModifiers.forEach(modifier -> dummyInstance.addTransientModifier(modifier.modifier()));
        int bodyMana = (int) Math.min(99, dummyInstance.getValue() - DSAttributes.MANA.value().getDefaultValue());

        String manaFromBody = "0";

        if (bodyMana > 0) {
            manaFromBody = "+" + bodyMana;
        }

        return Component.translatable(Translation.Type.ABILITY_DESCRIPTION.wrap(getName()), Math.min(99, ManaHandler.getMaxMana(player)), manaFromAbility, manaFromBody, manaFromExperience);
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public int getMinLevel() {
        return 0;
    }

    public int getMana() {
        return getLevel();
    }

    @Override
    public ResourceLocation getIcon() {
        return getPlayer() == null ? super.getIcon() : getSkillTextures()[Mth.clamp(getLevel() + Math.max(0, (Math.min(50, getPlayer().experienceLevel) - 5) / 5), 0, getSkillTextures().length - 1)];
    }
}