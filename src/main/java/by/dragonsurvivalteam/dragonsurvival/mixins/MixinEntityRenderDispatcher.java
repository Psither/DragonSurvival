package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.joml.Vector3f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;


@Mixin( EntityRenderDispatcher.class )
public class MixinEntityRenderDispatcher{
	@Inject(method = "renderShadow", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;last()Lcom/mojang/blaze3d/vertex/PoseStack$Pose;"))
	private static void renderShadowOffset(PoseStack poseStack, MultiBufferSource buffer, Entity entity, float weight, float partialTicks, LevelReader level, float size, CallbackInfo ci){
		if(DragonUtils.isDragon(entity)) {
			Vector3f offset = Functions.getDragonCameraOffset(entity).negate();
			poseStack.pushPose();
			poseStack.translate(offset.x(), offset.y(), offset.z());
		}
	}

	@Inject(method = "renderShadow", at = @At(value = "TAIL"))
	private static void renderShadowOffsetEnd(PoseStack poseStack, MultiBufferSource buffer, Entity entity, float weight, float partialTicks, LevelReader level, float size, CallbackInfo ci){
		if(DragonUtils.isDragon(entity)) {
			poseStack.popPose();
		}
	}

	// TODO: Dragon shadows disappear if you are too big (since you are too far from the ground). To fix this we would need to mixin to the weight calculation in renderShadow.
}