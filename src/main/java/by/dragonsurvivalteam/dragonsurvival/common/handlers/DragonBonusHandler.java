package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.passive.CliffhangerAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonModifiers;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;

@Mod.EventBusSubscriber
public class DragonBonusHandler {
	@SubscribeEvent
	public static void dragonDamageImmunities(final LivingAttackEvent event) {
		LivingEntity living = event.getEntity();
		DamageSource damageSource = event.getSource();

		DragonStateProvider.getCap(living).ifPresent(handler -> {
			if (handler.isDragon()) {
				if (ServerConfig.bonuses) {
					if (ServerConfig.caveFireImmunity && DragonUtils.isDragonType(handler, DragonTypes.CAVE) && damageSource.is(DamageTypeTags.IS_FIRE)) {
						event.setCanceled(true);
					} else if (ServerConfig.forestBushImmunity && DragonUtils.isDragonType(handler, DragonTypes.FOREST) && damageSource == living.damageSources().sweetBerryBush()) {
						event.setCanceled(true);
					} else if (ServerConfig.forestCactiImmunity && damageSource == living.damageSources().cactus()) {
						event.setCanceled(true);
					}
				}

				if (ServerConfig.caveSplashDamage != 0) {
					if (DragonUtils.isDragonType(handler, DragonTypes.CAVE) && !living.hasEffect(DragonEffects.FIRE)) {
						if (damageSource.getDirectEntity() instanceof Snowball) {
							living.hurt(living.damageSources().generic(), ServerConfig.caveSplashDamage.floatValue());
						}
					}
				}
			}
		});
	}

	@SubscribeEvent
	public static void removeLavaFootsteps(PlayLevelSoundEvent.AtEntity event){
		if (!(event.getEntity() instanceof Player player)) {
			return;
		}

		if (event.getSound() != null) {
			boolean isRelevant = event.getSound().get().getLocation().getPath().contains(".step");

			if (isRelevant && ServerConfig.bonuses && ServerConfig.caveLavaSwimming) {
				if (DragonUtils.isDragonType(player, DragonTypes.CAVE) && DragonSizeHandler.getOverridePose(player) == Pose.SWIMMING) {
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void reduceFallDistance(LivingFallEvent livingFallEvent){
		LivingEntity living = livingFallEvent.getEntity();
		DragonStateProvider.getCap(living).ifPresent(dragonStateHandler -> {
			if(dragonStateHandler.isDragon()){
				float distance = livingFallEvent.getDistance();

				if(Objects.equals(dragonStateHandler.getType(),DragonTypes.FOREST)){

					if(ServerConfig.bonuses){
						distance -= ServerConfig.forestFallReduction.floatValue();
					}

					CliffhangerAbility ability = DragonAbilities.getSelfAbility(living, CliffhangerAbility.class);
					distance -= ability.getHeight();
				}

				float gravity = (float) livingFallEvent.getEntity().getAttributeValue(ForgeMod.ENTITY_GRAVITY.get());
				// TODO: Added a fudge factor of 1.5 here. Not sure why it is needed but otherwise you begin to hurt yourself at very high jump heights even though the calculation here is identical to the push force calculation.
				float jumpHeight = (float) DragonModifiers.getJumpBonus(dragonStateHandler) * 1.5f;

				// Calculating the peak of the jump
				distance -= (float) (Math.pow(jumpHeight, 2.0) / (2 * gravity));

				AbstractDragonBody body = dragonStateHandler.getBody();
				if (body != null && body.getGravityMult() <= 1.0) {
					if (body.getGravityMult() == 0) {
						distance = 0;
					} else {
						distance *= body.getGravityMult();
					}
				} else if (body != null) {
					distance *= ((body.getGravityMult() - 1) / 2) + 1;
				}

				livingFallEvent.setDistance(distance);
			}
		});
	}
}