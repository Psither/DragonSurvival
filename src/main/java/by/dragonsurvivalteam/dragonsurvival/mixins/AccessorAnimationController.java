package by.dragonsurvivalteam.dragonsurvival.mixins;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import software.bernie.geckolib.core.animation.AnimationController;

@Mixin(AnimationController.class)
public interface AccessorAnimationController {
    @Dynamic
    @Accessor("tickOffset")
    void setTickOffset(double tickOffset);

    @Dynamic
    @Accessor
    double getTickOffset();
}
