package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.CaveDragonConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.DragonBonusConfig;
import by.dragonsurvivalteam.dragonsurvival.config.server.dragon.ForestDragonConfig;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncPlayerJump;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSModifiers;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;


@EventBusSubscriber
public class DragonBonusHandler {
    @SubscribeEvent
    public static void dragonDamageImmunities(final LivingIncomingDamageEvent event) {
        LivingEntity living = event.getEntity();
        DamageSource damageSource = event.getSource();

        DragonStateProvider.getOptional(living).ifPresent(handler -> {
            if (handler.isDragon()) {
                if (DragonBonusConfig.bonusesEnabled) {
                    if (CaveDragonConfig.caveFireImmunity && DragonUtils.isDragonType(handler, DragonTypes.CAVE) && damageSource.is(DamageTypeTags.IS_FIRE)) {
                        event.setCanceled(true);
                    } else if (ForestDragonConfig.bushImmunity && DragonUtils.isDragonType(handler, DragonTypes.FOREST) && damageSource == living.damageSources().sweetBerryBush()) {
                        event.setCanceled(true);
                    } else if (ForestDragonConfig.cactusImmunity && DragonUtils.isDragonType(handler, DragonTypes.FOREST) && damageSource == living.damageSources().cactus()) {
                        event.setCanceled(true);
                    }
                }

                if (CaveDragonConfig.caveSplashDamage != 0) {
                    if (DragonUtils.isDragonType(handler, DragonTypes.CAVE) && !living.hasEffect(DSEffects.FIRE)) {
                        if (damageSource.getDirectEntity() instanceof Snowball) {
                            living.hurt(living.damageSources().generic(), CaveDragonConfig.caveSplashDamage.floatValue());
                        }
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public static void removeLavaFootsteps(PlayLevelSoundEvent.AtEntity event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (event.getSound() != null) {
            boolean isRelevant = event.getSound().value().getLocation().getPath().contains(".step");

            if (isRelevant && DragonBonusConfig.bonusesEnabled && CaveDragonConfig.caveLavaSwimming) {
                if (DragonUtils.isDragonType(player, DragonTypes.CAVE) && DragonSizeHandler.getOverridePose(player) == Pose.SWIMMING) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onJump(LivingEvent.LivingJumpEvent jumpEvent) {
        final LivingEntity living = jumpEvent.getEntity();

        if(living.getEffect(DSEffects.TRAPPED) != null){
            Vec3 deltaMovement = living.getDeltaMovement();
            living.setDeltaMovement(deltaMovement.x, deltaMovement.y < 0 ? deltaMovement.y : 0, deltaMovement.z);
            living.setJumping(false);
            return;
        }

        DragonStateProvider.getOptional(living).ifPresent(dragonStateHandler -> {
            if (dragonStateHandler.isDragon()) {
                if (living instanceof ServerPlayer) {
                    PacketDistributor.sendToAllPlayers(new SyncPlayerJump.Data(living.getId(), 10));
                }
            }
        });
    }

    @SubscribeEvent
    public static void addFireProtectionToCaveDragonDrops(BlockDropsEvent dropsEvent) {
        if (dropsEvent.getBreaker() == null) return;

        // TODO :: also handle experience? would need a hook in 'CommonHooks#handleBlockDrops' to store some context and then modify the experience orb in 'ExperienceOrb#award'
        if (DragonUtils.isDragonType(dropsEvent.getBreaker(), DragonTypes.CAVE)) {
            dropsEvent.getDrops().forEach(drop -> drop.getData(DragonSurvival.ENTITY_HANDLER).isFireImmune = true);
        }
    }

    @SubscribeEvent
    public static void flagPlayersAsJumping(PlayerTickEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            if (handler.isDragon()) {
                handler.isJumping = true;
            }
        }
    }

    // We want to make sure this fires last so that we do the jump handling first before we set the flag to false again
    @SubscribeEvent(receiveCanceled = true, priority = EventPriority.LOWEST)
    public static void flagPlayersAsNotJumping(LivingFallEvent event) {
        if(event.getEntity() instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            if(handler.isDragon() && handler.isJumping) {
                handler.isJumping = false;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void reduceFallDamageFromExtraJumpHeight(LivingFallEvent event) {
        if(event.getEntity() instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            if(handler.isDragon() && handler.isJumping) {
                double gravity = player.getGravity();
                if(gravity <= 0) return;

                // Don't allow a negative jump penalty to cause a negative safe fall distance
                double jumpMod = DSModifiers.buildJumpMod(DragonStateProvider.getData(player)) + handler.getBody().getJumpBonus();
                if(jumpMod <= 0) return;

                // Calculate the extra jump height that the dragon gains based off of the jumpMod and gravity
                // Use vertical projectile motion equation to do this; it isn't exactly accurate to the game's physics but it is very close
                float extraJumpHeight = (float)((jumpMod * jumpMod) / (2 * gravity));
                event.setDistance(event.getDistance() - extraJumpHeight);
            }
        }
    }
}