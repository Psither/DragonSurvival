package by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.Bolas;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class BolasEntityRenderer extends EntityRenderer<Bolas>{

	// This class is purely for rendering the bolas projectile. The bolas rendered on top of the target when it is trapped is handled elsewhere: renderBolas & renderTrap inside of ClientEvents.java, and thirdPersonPreRender in ClientDragonRender.java

	private static final ResourceLocation BOLAS_TEXTURE = new ResourceLocation("dragonsurvival", "textures/item/dragon_hunting_mesh.png");

	public BolasEntityRenderer(Context p_174198_){
		super(p_174198_);
	}

	@Override
	public void render(final Bolas bolas, float p_225623_2_, float p_225623_3_, @NotNull PoseStack stack, @NotNull MultiBufferSource bufferSource, int p_225623_6_){
		if(bolas.tickCount >= 2 || !(entityRenderDispatcher.camera.getEntity().distanceToSqr(bolas) < 12.25D)){
			stack.pushPose();
			stack.scale(1.2F, 1.2F, 1.2F);
			stack.mulPose(entityRenderDispatcher.cameraOrientation());
			stack.mulPose(Axis.YP.rotationDegrees(180.0F));
			Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(DSItems.huntingNet), ItemDisplayContext.GROUND, p_225623_6_, OverlayTexture.NO_OVERLAY, stack, bufferSource, bolas.level(), 0);
			stack.popPose();
		}

		super.render(bolas, p_225623_2_, p_225623_3_, stack, bufferSource, p_225623_6_);
	}

	@Override
	public ResourceLocation getTextureLocation(Bolas bolas) {
		return BOLAS_TEXTURE;
	}
}