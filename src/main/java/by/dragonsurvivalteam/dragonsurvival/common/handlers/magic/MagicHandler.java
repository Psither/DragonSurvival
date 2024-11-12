package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.MagicCap;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.particles.SeaSweepParticleOption;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.passive.BurnAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.FrostDragon.BlizzardAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.FrostDragon.HealingColdAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active.RevealingTheSoulAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.active.StormBreathAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.passive.SpectralImpactAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ActiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDamageTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEffectTags;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlyingStatus;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.TargetingFunctions;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

@EventBusSubscriber
public class MagicHandler {
	private static final ResourceLocation FULLY_FROZEN_MOVEMENT_SPEED = ResourceLocation.fromNamespaceAndPath(MODID, "fully_frozen_movement_speed");
	private static final ResourceLocation FULLY_FROZEN_SWIM_SPEED = ResourceLocation.fromNamespaceAndPath(MODID, "fully_frozen_movement_speed");

    @SubscribeEvent // TODO :: is this needed?
    public static void setPlayerForAbilities(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();

        DragonStateProvider.getOptional(player).ifPresent(data -> {
            if (!data.isDragon()) {
                return;
            }

            for (DragonAbility ability : data.getMagicData().abilities.values()) {
                ability.player = player;
            }
        });
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

            if(entity.hasEffect(DSEffects.BURN)){
                if(entity.isEyeInFluid(FluidTags.WATER) || entity.isInWaterRainOrBubble()){
                    entity.removeEffect(DSEffects.BURN);
                }
                if (entity.hasEffect(DSEffects.FROSTED)) {
                    entity.removeEffect(DSEffects.FROSTED);
                    entity.removeEffect(DSEffects.BURN);
                    entity.setTicksFrozen(0);
                } if (entity.hasEffect(DSEffects.HEALING_COLD)) {
                    entity.removeEffect(DSEffects.HEALING_COLD);
                    entity.removeEffect(DSEffects.BURN);
                    entity.setTicksFrozen(0);
                }
            }
    
            if (!entity.level().isClientSide()) {
                if (entity.isOnFire()) {
                    if (entity.hasEffect(DSEffects.FROSTED))
                        entity.removeEffect(DSEffects.FROSTED);
                    if (entity.hasEffect(DSEffects.HEALING_COLD))
                        entity.removeEffect(DSEffects.HEALING_COLD);
                }
                if (entity.hasEffect(DSEffects.FROSTED)) {
                    entity.setTicksFrozen(entity.getTicksFrozen() + 2);
                    if (entity.isFullyFrozen()) {
                        entity.addEffect(new MobEffectInstance(DSEffects.FULLY_FROZEN, entity.getEffect(DSEffects.FROSTED).getDuration(), 0));
                        if (entity.hasEffect(DSEffects.BRITTLE)) {
                            entity.addEffect(new MobEffectInstance(DSEffects.BRITTLE, 20, entity.getEffect(DSEffects.BRITTLE).getAmplifier()));
                        }
                    }
                }
                if (entity.hasEffect(DSEffects.HEALING_COLD)) {
                    entity.addEffect(new MobEffectInstance(DSEffects.FULLY_FROZEN, entity.getEffect(DSEffects.HEALING_COLD).getDuration(), 0));
                    entity.heal((float) (HealingColdAbility.healingColdHealStrength / 20.0f * (entity.getEffect(DSEffects.HEALING_COLD).getAmplifier() + 1)));
                }
                AttributeInstance moveSpeed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
                AttributeInstance swimSpeed = entity.getAttribute(NeoForgeMod.SWIM_SPEED);
    
                if (entity.hasEffect(DSEffects.FULLY_FROZEN)){
                    if (moveSpeed != null && moveSpeed.getModifier(FULLY_FROZEN_MOVEMENT_SPEED) == null)
                        moveSpeed.addTransientModifier(new AttributeModifier(FULLY_FROZEN_MOVEMENT_SPEED, -1.0f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                    if (swimSpeed != null && swimSpeed.getModifier(FULLY_FROZEN_SWIM_SPEED) == null)
                        swimSpeed.addTransientModifier(new AttributeModifier(FULLY_FROZEN_SWIM_SPEED, -1.0f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    
                    if (entity instanceof Player player) {
                        DragonStateHandler handler = DragonStateProvider.getData(player);
                        if (handler.isDragon()) {
                            handler.setWingsSpread(false);
                            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncFlyingStatus.Data(player.getId(), false));
                        }
                    }
                } else {
                    if (moveSpeed != null && moveSpeed.getModifier(FULLY_FROZEN_MOVEMENT_SPEED) != null)
                        moveSpeed.removeModifier(FULLY_FROZEN_MOVEMENT_SPEED);
                    if (swimSpeed != null && swimSpeed.getModifier(FULLY_FROZEN_SWIM_SPEED) != null)
                        swimSpeed.removeModifier(FULLY_FROZEN_SWIM_SPEED);
                }
            }
    
            MobEffectInstance blizzardEffect = entity.getEffect(DSEffects.BLIZZARD);
            if (blizzardEffect != null) {
                BlizzardAbility.inflictDamageOnNearbyEntities(entity, blizzardEffect.getAmplifier());
            }

            if (entity.tickCount % 20 == 0) {
                MobEffectInstance drainEffect = entity.getEffect(DSEffects.DRAIN);

                if (drainEffect != null) {
                    if (!DragonUtils.isDragonType(entity, DragonTypes.FOREST)) {
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
                    if (!DragonUtils.isDragonType(entity, DragonTypes.SEA)) {
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

                if(DragonUtils.isDragonType(cap, DragonTypes.SEA)){
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
                MobEffectInstance modifiedInstance = new MobEffectInstance(instance.getEffect(), instance.getDuration(), amplifier);

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

                if (DragonUtils.isDragonType(handler, DragonTypes.SEA)) {
                    SpectralImpactAbility spectralImpact = DragonAbilities.getSelfAbility(player, SpectralImpactAbility.class);
                    boolean hit = player.getRandom().nextInt(100) <= spectralImpact.getChance(); // TODO Check :: Can the next int be 0? In that case the effect would trigger

                    if (hit) {
                        event.getEntity().hurt(new DamageSource(DSDamageTypes.get(player.level(), DSDamageTypes.SPECTRAL_IMPACT), player), (float) (event.getAmount() * 0.15));
                        double d0 = -Mth.sin(player.getYRot() * ((float) Math.PI / 180F));
                        double d1 = Mth.cos(player.getYRot() * ((float) Math.PI / 180F));

                        if (player.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(new SeaSweepParticleOption(0), player.getX() + d0, player.getY(0.5D), player.getZ() + d1, 0, d0, 0.0D, d1, 0.0D);
                        }
                    }
                } else if (DragonUtils.isDragonType(handler, DragonTypes.CAVE)) {
                    BurnAbility burnAbility = DragonAbilities.getSelfAbility(player, BurnAbility.class);
                    boolean hit = player.getRandom().nextInt(100) < burnAbility.getChance();

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
            DragonStateProvider.getOptional(player).ifPresent(cap -> {
                if (!cap.isDragon()) {
                    return;
                }

                double expMult = 1.0;
                AbstractDragonBody body = DragonUtils.getDragonBody(player);
                if (body != null) {
                    expMult = body.getExpMult();
                }

                if (player.hasEffect(DSEffects.REVEALING_THE_SOUL)) {
                    int extra = (int) Math.min(RevealingTheSoulAbility.revealingTheSoulMaxEXP, event.getDroppedExperience() * RevealingTheSoulAbility.revealingTheSoulMultiplier);
                    event.setDroppedExperience((int) ((event.getDroppedExperience() + extra) * expMult));
                }
            });
        }
    }
}