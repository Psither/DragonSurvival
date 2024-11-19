package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.MagicCap;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SeaSweepParticleOption;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.passive.BurnAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active.RevealingTheSoulAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active.StormBreathAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.passive.SpectralImpactAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ActiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDamageTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEffectTags;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.TargetingFunctions;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@EventBusSubscriber
public class MagicHandler {
    @SubscribeEvent // TODO :: is it needed to set the player here?
    public static void setPlayerForAbilities(PlayerTickEvent.Pre event) {
        DragonStateHandler data = DragonStateProvider.getData(event.getEntity());

        if (!data.isDragon()) {
            return;
        }

        for (DragonAbility ability : data.getMagicData().abilities.values()) {
            ability.player = event.getEntity();
        }
    }

    @SubscribeEvent
    public static void magicUpdate(PlayerTickEvent.Post event) {
        DragonStateProvider.getOptional(event.getEntity()).ifPresent(data -> {
            if (!data.isDragon()) {
                return;
            }

            if (data.getMagicData().abilities.isEmpty() || data.getMagicData().innateDragonAbilities.isEmpty() || data.getMagicData().activeDragonAbilities.isEmpty()) {
                data.getMagicData().initAbilities(data.getType());
            }

            for (int i = 0; i < MagicCap.activeAbilitySlots; i++) {
                ActiveDragonAbility ability = data.getMagicData().getAbilityFromSlot(i);

                if (ability != null) {
                    ability.tickCooldown();
                }
            }
        });
    }

    @SubscribeEvent
    public static void livingTick(EntityTickEvent.Post event){
        if(event.getEntity() instanceof LivingEntity entity) {
            EntityStateHandler data = entity.getData(DragonSurvival.ENTITY_HANDLER);

            if (entity.hasEffect(DSEffects.BURN)) {
                if (entity.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value()) || entity.isInWaterRainOrBubble()) {
                    entity.removeEffect(DSEffects.BURN);
                }
            }

            if (entity.tickCount % 20 == 0) {
                MobEffectInstance drainEffect = entity.getEffect(DSEffects.DRAIN);

                if (drainEffect != null) {
                    if (!DragonUtils.isType(entity, DragonTypes.FOREST)) {
                        Player player = data.lastAfflicted != -1 && entity.level().getEntity(data.lastAfflicted) instanceof Player ? (Player) entity.level().getEntity(data.lastAfflicted) : null;

                        if (player != null) {
                            TargetingFunctions.attackTargets(player, ent -> ent.hurt(new DamageSource(DSDamageTypes.get(player.level(), DSDamageTypes.FOREST_DRAGON_DRAIN), player), drainEffect.getAmplifier() + 1), entity);
                        } else {
                            entity.hurt(entity.damageSources().magic(), drainEffect.getAmplifier() + 1);
                        }
                    }
                }

                MobEffectInstance chargedEffect = entity.getEffect(DSEffects.CHARGED);

                if (chargedEffect != null) {
                    Player player = data.lastAfflicted != -1 && entity.level().getEntity(data.lastAfflicted) instanceof Player ? (Player) entity.level().getEntity(data.lastAfflicted) : null;
                    if (!DragonUtils.isType(entity, DragonTypes.SEA)) {
                        StormBreathAbility.chargedEffectSparkle(player, entity, StormBreathAbility.chargedChainRange, StormBreathAbility.chargedEffectChainCount, (chargedEffect.getAmplifier() + 1) * StormBreathAbility.chargedEffectDamageMultiplier);
                    }
                }

                MobEffectInstance burnEffect = entity.getEffect(DSEffects.BURN);

                if (burnEffect != null) {
                    if (!entity.fireImmune()) {
                        if (data.lastPos != null) {
                            double distance = entity.distanceToSqr(data.lastPos);
                            float damage = (burnEffect.getAmplifier() + 1) * Mth.clamp((float) distance, 0, 10);

                            if (damage > 0) {
                                if (!entity.isOnFire()) {
                                    // Short enough fire duration to not cause fire damage but still drop cooked items
                                    entity.setRemainingFireTicks(1);
                                }
                                Player player = data.lastAfflicted != -1 && entity.level().getEntity(data.lastAfflicted) instanceof Player ? (Player) entity.level().getEntity(data.lastAfflicted) : null;
                                if (player != null) {
                                    TargetingFunctions.attackTargets(player, ent -> ent.hurt(new DamageSource(DSDamageTypes.get(player.level(), DSDamageTypes.CAVE_DRAGON_BURN), player), damage), entity);
                                } else {
                                    entity.hurt(entity.damageSources().onFire(), damage);
                                }
                            }
                        }
                    }
                }

                data.lastPos = entity.position();
            }
        }
    }

    @SubscribeEvent
    public static void playerStruckByLightning(EntityStruckByLightningEvent event) {
        if (event.getEntity() instanceof Player player) {

            DragonStateProvider.getOptional(player).ifPresent(cap -> {
                if (!cap.isDragon()) {
                    return;
                }

                if(DragonUtils.isType(cap, DragonTypes.SEA)){
                    event.setCanceled(true);
                }
            });
        }
    }

    public static MobEffectInstance modifyEffect(final Player affected, final MobEffectInstance instance, @Nullable final Entity applier) {
        if (instance == null || Objects.equals(affected, applier)) {
            return instance;
        }

        int amplifier = instance.getAmplifier();

        if (instance.getEffect().value().getCategory().equals(MobEffectCategory.HARMFUL)) {
            if (applier instanceof LivingEntity livingApplier && !instance.getEffect().is(DSEffectTags.OVERWHELMING_MIGHT_BLACKLIST)) {
                amplifier += EnchantmentUtils.getLevel(livingApplier, DSEnchantments.OVERWHELMING_MIGHT);
            }

            if (!instance.getEffect().is(DSEffectTags.UNBREAKABLE_SPIRIT_BLACKLIST)) {
                amplifier -= EnchantmentUtils.getLevel(affected, DSEnchantments.UNBREAKABLE_SPIRIT);
            }

            amplifier = Mth.clamp(amplifier, 0, 255);

            if (amplifier != instance.getAmplifier()) {
                MobEffectInstance modifiedInstance = new MobEffectInstance(instance.getEffect(), instance.getDuration(), amplifier, instance.isAmbient(), instance.isVisible(), instance.showIcon());

                if (affected.hasEffect(instance.getEffect())) {
                    affected.removeEffect(instance.getEffect());
                }

                return modifiedInstance;
            }
        }

        return instance;
    }

    @SubscribeEvent
    public static void livingHurt(final LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            if (event.getSource().getEntity() instanceof LivingEntity source) {
                if (entity.hasEffect(DSEffects.BLOOD_SIPHON)) {
                    source.heal(event.getAmount() * 0.1f);
                }
                if (event.getEntity().level().registryAccess().registry(Registries.ENCHANTMENT).isPresent()) {
                    Registry<Enchantment> enchantments = event.getEntity().level().registryAccess().registry(Registries.ENCHANTMENT).get();
                    if (event.getSource().is(DSDamageTypeTags.DRAGON_MAGIC)) {
                        Optional<Holder.Reference<Enchantment>> draconicSuperiority = enchantments.getHolder(DSEnchantments.DRACONIC_SUPERIORITY);
                        if (draconicSuperiority.isPresent()) {
                            EnchantmentHelper.getEnchantmentLevel(draconicSuperiority.get(), source);
                            event.setAmount(event.getAmount() * 1.2f + (0.08f * EnchantmentHelper.getEnchantmentLevel(draconicSuperiority.get(), source)));
                        }
                    }
                    if (event.getEntity().getHealth() == event.getEntity().getMaxHealth()) {
                        Optional<Holder.Reference<Enchantment>> murderersCunning = enchantments.getHolder(DSEnchantments.MURDERERS_CUNNING);
                        murderersCunning.ifPresent(enchantmentReference -> event.setAmount(event.getAmount() * 1.4f + (0.2f * EnchantmentHelper.getEnchantmentLevel(enchantmentReference, source))));
                    }
                }
            }
        }

        if (event.getSource().is(DSDamageTypeTags.DRAGON_BREATH)) {
            return;
        }

        if (event.getSource().getEntity() instanceof Player player) {
            DragonStateProvider.getOptional(player).ifPresent(handler -> {
                if (!handler.isDragon()) {
                    return;
                }

                if (DragonUtils.isType(handler, DragonTypes.SEA)) {
                    int chance = DragonAbilities.getAbility(player, SpectralImpactAbility.class).map(SpectralImpactAbility::getChance).orElse(0);
                    // rolls between 1 and 100 (incl.) (chance of 0 means the ability should not trigger)
                    boolean hit = 1 + player.getRandom().nextInt(101) < chance;

                    if (hit) {
                        event.getEntity().hurt(new DamageSource(DSDamageTypes.get(player.level(), DSDamageTypes.SPECTRAL_IMPACT), player), (float) (event.getAmount() * 0.15));
                        double d0 = -Mth.sin(player.getYRot() * ((float) Math.PI / 180F));
                        double d1 = Mth.cos(player.getYRot() * ((float) Math.PI / 180F));

                        if (player.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(new SeaSweepParticleOption(0), player.getX() + d0, player.getY(0.5D), player.getZ() + d1, 0, d0, 0.0D, d1, 0.0D);
                        }
                    }
                } else if (DragonUtils.isType(handler, DragonTypes.CAVE)) {
                    int chance = DragonAbilities.getAbility(player, BurnAbility.class).map(BurnAbility::getChance).orElse(0);
                    // rolls between 1 and 100 (incl.) (chance of 0 means the ability should not trigger)
                    boolean hit = 1 + player.getRandom().nextInt(101) < chance;

                    if (hit) {
                        event.getEntity().getData(DragonSurvival.ENTITY_HANDLER).lastAfflicted = player.getId();

                        if (!player.level().isClientSide()) {
                            event.getEntity().addEffect(new MobEffectInstance(DSEffects.BURN, Functions.secondsToTicks(30)));
                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void experienceDrop(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();

        if (player != null) {
            int droppedExperience = event.getDroppedExperience();

            if (player.hasEffect(DSEffects.REVEALING_THE_SOUL)) {
                droppedExperience += (int) Math.min(RevealingTheSoulAbility.revealingTheSoulMaxEXP, event.getDroppedExperience() * RevealingTheSoulAbility.revealingTheSoulMultiplier);
            }

            event.setDroppedExperience((int) (droppedExperience * player.getAttributeValue(DSAttributes.EXPERIENCE)));
        }
    }
}