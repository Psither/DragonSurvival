package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ActiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.Objects;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    protected PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    public void dragonSurvival$disableSuffocationDamage(DamageSource source, CallbackInfoReturnable<Boolean> callback) {
        if (ServerConfig.disableDragonSuffocation && source == damageSources().inWall() && DragonStateProvider.isDragon(this)) {
            callback.setReturnValue(true);
        }
    }

    /** Disables the mining speed penalty for not being on the ground (for sea dragons that are in the water) */
    @WrapOperation(method = "getDigSpeed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;onGround()Z"))
    private boolean dragonSurvival$disablePenalty(final Player instance, final Operation<Boolean> original) {
        if (instance.isInWater() && DragonUtils.isType(instance, DragonTypes.SEA)) {
            return true;
        }

        if (instance.isInLava() && DragonUtils.isType(instance, DragonTypes.CAVE)) {
            return true;
        }

        return original.call(instance);
    }

    /** Prevent the player from moving when casting certain abilities or using emotes */
    @Inject(method = "isImmobile", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$preventMovement(CallbackInfoReturnable<Boolean> callback) {
        if (!isDeadOrDying() && !isSleeping()) {
            DragonStateHandler data = DragonStateProvider.getData((Player) (Object) this);

            if (!ServerConfig.canMoveWhileCasting) {
                ActiveDragonAbility casting = data.getMagicData().getCurrentlyCasting();

                if (casting != null && casting.requiresStationaryCasting()) {
                    callback.setReturnValue(true);
                }
            }

            if (!ServerConfig.canMoveInEmote && Arrays.stream(data.getEmoteData().currentEmotes).noneMatch(Objects::nonNull)) {
                callback.setReturnValue(true);
            }
        }
    }

    /** Allow treasure blocks to trigger sleep logic */
    @Inject(method = "isSleepingLongEnough", at = @At("HEAD"), cancellable = true)
    public void dragonSurvival$isSleepingLongEnough(CallbackInfoReturnable<Boolean> callback) {
        DragonStateProvider.getOptional(this).ifPresent(handler -> {
            if (handler.isDragon() && handler.treasureResting && handler.treasureSleepTimer >= 100) {
                callback.setReturnValue(true);
            }
        });
    }

    /** Make sure to consider the actual dragon hitbox when doing checks like these */
    @ModifyReturnValue(method = "canPlayerFitWithinBlocksAndEntitiesWhen", at = @At("RETURN"))
    private boolean dragonSurvival$checkDragonHitbox(boolean returnValue, @Local(argsOnly = true) Pose pose) {
        if (DragonStateProvider.isDragon(this)) {
            return DragonSizeHandler.canPoseFit((Player) (Object) this, pose);
        } else {
            return returnValue;
        }
    }
}