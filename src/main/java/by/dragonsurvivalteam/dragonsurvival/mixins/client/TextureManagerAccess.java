package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureManager.class)
public interface TextureManagerAccess {
    @Accessor("resourceManager")
    ResourceManager dragonSurvival$getResourceManager();
}
