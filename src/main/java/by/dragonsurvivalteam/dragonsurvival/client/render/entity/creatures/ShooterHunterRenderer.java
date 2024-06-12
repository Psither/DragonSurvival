package by.dragonsurvivalteam.dragonsurvival.client.render.entity.creatures;

import by.dragonsurvivalteam.dragonsurvival.client.models.HunterModel;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.Shooter;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ShooterHunterRenderer extends MobRenderer<Shooter, HunterModel<Shooter>>{ // TODO :: Use Geckolib and CustomBlockAndItemGeoLayer?
	private static final ResourceLocation TEXTURE = new ResourceLocation("dragonsurvival", "textures/dragon_hunter.png");

	public ShooterHunterRenderer(EntityRendererProvider.Context rendererManager){
		super(rendererManager, new HunterModel<>(rendererManager.bakeLayer(ModelLayers.EVOKER)), 0.5F);
		addLayer(new CustomHeadLayer<>(this, rendererManager.getModelSet(), rendererManager.getItemInHandRenderer()));
		addLayer(new ItemInHandLayer<>(this, rendererManager.getItemInHandRenderer()));
	}

	@Override
	public @NotNull ResourceLocation getTextureLocation(@NotNull final Shooter ignored) {
		return TEXTURE;
	}

	@Override
	protected void scale(Shooter shooter, PoseStack matrixStack, float p_225620_3_){
		float f = 0.9375F;
		matrixStack.scale(f, f, f);
	}
}