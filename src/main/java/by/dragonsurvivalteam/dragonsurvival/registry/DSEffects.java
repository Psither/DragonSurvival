package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.active.ToughSkinAbility;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlyingStatus;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSModifiers.SLOW_MOVEMENT;
import static by.dragonsurvivalteam.dragonsurvival.registry.DSModifiers.TOUGH_SKIN;

public class DSEffects {

    public static final DeferredRegister<MobEffect> DS_MOB_EFFECTS = DeferredRegister.create(
            BuiltInRegistries.MOB_EFFECT,
            MODID
    );

    private static class Stress extends MobEffect {

        protected Stress(int color) {
            super(MobEffectCategory.HARMFUL, color);
        }

        @Override
        public boolean applyEffectTick(@NotNull LivingEntity living, int p_76394_2_) {
            if (living instanceof Player player) {
                FoodData food = player.getFoodData();

                if (food.getSaturationLevel() > 0) {
                    int oldFood = food.getFoodLevel();
                    food.eat(1, (float) ((-0.5F * food.getSaturationLevel()) * ServerConfig.forestStressExhaustion));
                    if (oldFood != 20) {
                        food.setFoodLevel((int) (food.getFoodLevel() - 1 * ServerConfig.forestStressExhaustion));
                    }
                }

                player.causeFoodExhaustion((float) (1.0f * ServerConfig.forestStressExhaustion));

                return true;
            }

            return false;
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int pDuration, int pAmplifier) {
            int i = 20 >> pAmplifier;
            if (i > 0) {
                return pDuration % i == 0;
            } else {
                return true;
            }
        }
    }

    public static Holder<MobEffect> STRESS = DS_MOB_EFFECTS.register(
            "stress",
            () -> new Stress(0xf4a2e8)
    );

    private static class WingDisablingEffect extends ModifiableMobEffect {
        protected WingDisablingEffect(MobEffectCategory type, int color, boolean uncurable) {
            super(type, color, uncurable);
        }

        @Override
        public void onEffectStarted(LivingEntity living, int strength) {
            if (!living.level().isClientSide()) {
                if (living instanceof Player player) {
                    DragonStateHandler handler = DragonStateProvider.getData(player);
                    if (handler.isDragon()) {
                        handler.setWingsSpread(false);
                        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncFlyingStatus.Data(player.getId(), false));
                    }
                }
            }
        }
    }

    // There are some missing effects here, since they are handled elsewhere:
    // -The player can't jump (DragonBonusHandler.java)
    // -The player can't activate their wings (ClientFlightHandler.java)
    public static Holder<MobEffect> TRAPPED = DS_MOB_EFFECTS.register(
            "trapped",
            () -> new WingDisablingEffect(MobEffectCategory.HARMFUL, 0xdddddd, true)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, SLOW_MOVEMENT, -0.5, Operation.ADD_MULTIPLIED_TOTAL)
    );

    // There are some missing effects here, since they are handled elsewhere:
    // -The player can't activate their wings (ClientFlightHandler.java)
    public static Holder<MobEffect> WINGS_BROKEN = DS_MOB_EFFECTS.register(
            "wings_broken",
            () -> new WingDisablingEffect(MobEffectCategory.HARMFUL, 0x0, true)
    );

    private static class ModifiableMobEffect extends MobEffect {
        private final boolean incurable;

        protected ModifiableMobEffect(MobEffectCategory type, int color, boolean incurable) {
            super(type, color);
            this.incurable = incurable;
        }

        @Override
        public void fillEffectCures(@NotNull Set<EffectCure> cures, @NotNull MobEffectInstance effectInstance) {
            if (incurable) {
                cures.clear();
            } else {
                super.fillEffectCures(cures, effectInstance);
            }
        }
    }

    public static Holder<MobEffect> MAGIC_DISABLED = DS_MOB_EFFECTS.register(
            "magic_disabled",
            () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, false)
    );

    public static Holder<MobEffect> HUNTER_OMEN = DS_MOB_EFFECTS.register(
            "hunter_omen",
            () -> new ModifiableMobEffect(MobEffectCategory.NEUTRAL, 0x0, true)
    );

    public static Holder<MobEffect> PEACE = DS_MOB_EFFECTS.register(
            "peace",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
    );

    public static Holder<MobEffect> MAGIC = DS_MOB_EFFECTS.register(
            "magic",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
    );

    public static Holder<MobEffect> FIRE = DS_MOB_EFFECTS.register(
            "fire",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
    );

    public static Holder<MobEffect> ANIMAL_PEACE = DS_MOB_EFFECTS.register(
            "animal_peace",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
    );

    public static Holder<MobEffect> SOURCE_OF_MAGIC = DS_MOB_EFFECTS.register(
            "source_of_magic",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
    );

    private static class TradeEffect extends MobEffect {

        protected TradeEffect(MobEffectCategory type, int color) {
            super(type, color);
        }

        // Make this uncurable
        @Override
        public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
            cures.clear();
        }

        @Override
        public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
            return true;
        }
    }

    public static Holder<MobEffect> ROYAL_DEPARTURE = DS_MOB_EFFECTS.register(
            "royal_departure",
            () -> new TradeEffect(MobEffectCategory.HARMFUL, -3407617)
    );

    public static Holder<MobEffect> WATER_VISION = DS_MOB_EFFECTS.register(
            "water_vision",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
    );

    public static Holder<MobEffect> LAVA_VISION = DS_MOB_EFFECTS.register(
            "lava_vision",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
    );

    public static Holder<MobEffect> HUNTER = DS_MOB_EFFECTS.register(
        "hunter",
        () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
                // Same value as vanilla speed effect
                .addAttributeModifier(Attributes.MOVEMENT_SPEED, DragonSurvival.res("hunter_speed_multiplier"), 0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
    );

    public static Holder<MobEffect> REVEALING_THE_SOUL = DS_MOB_EFFECTS.register(
            "revealing_the_soul",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
    );

    public static Holder<MobEffect> BURN = DS_MOB_EFFECTS.register(
            "burn",
            () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, false)
    );

    public static Holder<MobEffect> CHARGED = DS_MOB_EFFECTS.register(
            "charged",
            () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, false)
    );

    public static Holder<MobEffect> DRAIN = DS_MOB_EFFECTS.register(
            "drain",
            () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, false)
    );

    public static Holder<MobEffect> STRONG_LEATHER = DS_MOB_EFFECTS.register(
            "strong_leather",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
                    .addAttributeModifier(Attributes.ARMOR, TOUGH_SKIN, ToughSkinAbility.toughSkinArmorValue, Operation.ADD_VALUE)
    );


    public static Holder<MobEffect> FROSTED = DS_MOB_EFFECTS.register(
            "frosted",
            () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, false)
    );

    public static Holder<MobEffect> BRITTLE = DS_MOB_EFFECTS.register(
            "brittle",
            () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, false)
    );

    public static Holder<MobEffect> FULLY_FROZEN = DS_MOB_EFFECTS.register(
            "fully_frozen",
            () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, false)
    );

    public static Holder<MobEffect> HEALING_COLD = DS_MOB_EFFECTS.register(
            "healing_cold",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
    );

    public static Holder<MobEffect> BLIZZARD = DS_MOB_EFFECTS.register(
            "blizzard",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
    );

    public static Holder<MobEffect> BLOOD_SIPHON = DS_MOB_EFFECTS.register(
            "blood_siphon",
            () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, false)
    );

    public static Holder<MobEffect> REGEN_DELAY = DS_MOB_EFFECTS.register(
            "regen_delay",
            () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, true)
    );

    public static Holder<MobEffect> cave_wings = DS_MOB_EFFECTS.register(
            "wings_cave",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, true)
    );

    public static Holder<MobEffect> sea_wings = DS_MOB_EFFECTS.register(
            "wings_sea",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, true)
    );

    public static Holder<MobEffect> forest_wings = DS_MOB_EFFECTS.register(
            "wings_forest",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, true)
    );
}