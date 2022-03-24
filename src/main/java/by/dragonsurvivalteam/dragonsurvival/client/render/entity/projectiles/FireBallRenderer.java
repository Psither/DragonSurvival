package by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles;

<<<<<<< HEAD:src/main/java/by/jackraidenph/dragonsurvival/client/render/entity/projectiles/FireBallRenderer.java
import by.jackraidenph.dragonsurvival.DragonSurvivalMod;
import by.jackraidenph.dragonsurvival.common.entity.projectiles.FireBallEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
=======
import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.FireBallEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
>>>>>>> v1.16.x:src/main/java/by/dragonsurvivalteam/dragonsurvival/client/render/entity/projectiles/FireBallRenderer.java
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

@OnlyIn( Dist.CLIENT )
public class FireBallRenderer extends GeoProjectilesRenderer<FireBallEntity>{
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(DragonSurvivalMod.MODID, "textures/entity/fireball_texture.png");
<<<<<<< HEAD:src/main/java/by/jackraidenph/dragonsurvival/client/render/entity/projectiles/FireBallRenderer.java
	
	public FireBallRenderer(EntityRendererProvider.Context renderManager, AnimatedGeoModel<FireBallEntity> modelProvider)
	{
=======

	public FireBallRenderer(EntityRendererManager renderManager, AnimatedGeoModel<FireBallEntity> modelProvider){
>>>>>>> v1.16.x:src/main/java/by/dragonsurvivalteam/dragonsurvival/client/render/entity/projectiles/FireBallRenderer.java
		super(renderManager, modelProvider);
	}

	protected int getBlockLightLevel(FireBallEntity p_225624_1_, BlockPos p_225624_2_){
		return 15;
	}
<<<<<<< HEAD:src/main/java/by/jackraidenph/dragonsurvival/client/render/entity/projectiles/FireBallRenderer.java
	
	public void render(FireBallEntity p_225623_1_, float p_225623_2_, float p_225623_3_, PoseStack stack, MultiBufferSource p_225623_5_, int p_225623_6_) {
=======

	public void render(FireBallEntity p_225623_1_, float p_225623_2_, float p_225623_3_, MatrixStack stack, IRenderTypeBuffer p_225623_5_, int p_225623_6_){
>>>>>>> v1.16.x:src/main/java/by/dragonsurvivalteam/dragonsurvival/client/render/entity/projectiles/FireBallRenderer.java
		super.render(p_225623_1_, p_225623_2_, p_225623_3_, stack, p_225623_5_, p_225623_6_);
	}

	public ResourceLocation getTextureLocation(FireBallEntity p_110775_1_){
		return TEXTURE_LOCATION;
	}
}