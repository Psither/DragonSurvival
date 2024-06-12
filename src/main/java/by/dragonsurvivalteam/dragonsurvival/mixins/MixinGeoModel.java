package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.model.CoreGeoModel;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.util.RenderUtils;

@Mixin(value = GeoModel.class, remap = false)
public abstract class MixinGeoModel<T extends GeoAnimatable> implements CoreGeoModel<T> {

    // The dragon entity doesn't tick, so we apply the time it would've taken for it to tick in this mixin for GeckoLib to work properly
    @ModifyArg(method = "handleAnimations", at = @At(value = "INVOKE", target = "Lsoftware/bernie/geckolib/core/animation/AnimatableManager;updatedAt(D)V"))
    private double applyDragonTickTime(double updateTime, @Local(argsOnly = true) T animatable, @Local(argsOnly = true) long instanceId) {
        if (animatable instanceof DragonEntity) {
            AnimatableManager<GeoAnimatable> manager = animatable.getAnimatableInstanceCache().getManagerForId(instanceId);
            return RenderUtils.getCurrentTick() - manager.getFirstTickTime();
        }

        return updateTime;
    }
}
