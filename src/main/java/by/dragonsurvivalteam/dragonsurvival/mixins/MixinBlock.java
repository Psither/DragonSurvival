package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import javax.security.auth.callback.Callback;
import java.util.Stack;

@Mixin(Block.class)
public class MixinBlock {
	@Unique
	private static Stack<Boolean> dragon_Survival$fireImmuneItems = new Stack<>();

	@Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
	private static void storeData(BlockState pState, Level pLevel, BlockPos pPos, @Nullable BlockEntity pBlockEntity, @Nullable Entity pEntity, ItemStack pTool, boolean dropXp, CallbackInfo ci) {
		if (pEntity instanceof Player player && DragonUtils.isDragonType(player, DragonTypes.CAVE)) {
			dragon_Survival$fireImmuneItems.push(true);
		}
	}

	@ModifyVariable(method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "STORE"))
	private static ItemEntity makeDropsSafe(final ItemEntity itemEntity) {
		if (!dragon_Survival$fireImmuneItems.isEmpty() && dragon_Survival$fireImmuneItems.pop()) {
			return new ItemEntity(itemEntity.level(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), itemEntity.getItem()) {
				@Override
				public boolean fireImmune() {
					return true;
				}
			};
		}

		return itemEntity;
	}
}