package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemBlockRenderTypes.class)
public class MixinItemBlockRenderTypes {
    @ModifyReturnValue(method = "getRenderLayer", at = @At(value = "RETURN"))
    private static RenderType getRenderLayerReturnValue(RenderType renderType, @Local(argsOnly = true) FluidState fluidState) {
        RenderType modifiedRenderType = ClientEvents.onRenderFluidLayer(fluidState);
        return modifiedRenderType != null ? modifiedRenderType : renderType;
    }
}
