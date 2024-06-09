package by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.BlizzardSpikeEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class BlizzardSpikeRenderer extends ArrowRenderer<BlizzardSpikeEntity> {
    public BlizzardSpikeRenderer(EntityRendererProvider.Context pContext) { super(pContext); }

    @Override
    public ResourceLocation getTextureLocation(BlizzardSpikeEntity entity){
        return new ResourceLocation(DragonSurvivalMod.MODID, "textures/entity/blizzard_" + entity.getArrow_level() + ".png");
    }
}
