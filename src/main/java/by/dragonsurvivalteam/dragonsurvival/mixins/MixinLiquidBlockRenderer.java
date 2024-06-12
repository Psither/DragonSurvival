package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin( LiquidBlockRenderer.class )
public class MixinLiquidBlockRenderer {
    @ModifyVariable(method = "tesselate", at = @At(value = "STORE"), ordinal = 0)
    private float modifyAlphaForLavaVision(float value, @Local(ordinal = 0) boolean isLava) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        // The first bool in tesselate is pFluidState.is(FluidTags.LAVA), so we can just reuse it here instead of trying to read the args of this function
        if(player.hasEffect(DragonEffects.LAVA_VISION) && isLava) {
            return value * 0.25F;
        }

        return value;
    }
}
