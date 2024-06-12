package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.SourceOfMagicBlock;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicPlaceholder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Supplier;


@Mixin( ClientLevel.class )
public abstract class MixinClientWorld extends Level {

	protected MixinClientWorld(WritableLevelData pLevelData, ResourceKey<Level> pDimension, RegistryAccess pRegistryAccess, Holder<DimensionType> pDimensionTypeRegistration, Supplier<ProfilerFiller> pProfiler, boolean pIsClientSide, boolean pIsDebug, long pBiomeZoomSeed, int pMaxChainedNeighborUpdates) {
		super(pLevelData, pDimension, pRegistryAccess, pDimensionTypeRegistration, pProfiler, pIsClientSide, pIsDebug, pBiomeZoomSeed, pMaxChainedNeighborUpdates);
	}

	@ModifyArg(method = "destroyBlockProgress", at = @At( value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;destroyBlockProgress(ILnet/minecraft/core/BlockPos;I)V"), index = 1)
	private BlockPos modifyDestroyBlockProgress(BlockPos pos){
		BlockState state = getBlockState(pos);

		if(state.getBlock() instanceof SourceOfMagicBlock){
			if(!state.getValue(SourceOfMagicBlock.PRIMARY_BLOCK)){
				BlockEntity blockEntity = getBlockEntity(pos);
				BlockPos pos1 = pos;

				if(blockEntity instanceof SourceOfMagicPlaceholder){
					pos1 = ((SourceOfMagicPlaceholder)blockEntity).rootPos;
				}

				return pos1;
			}
		}

		return pos;
	}
}