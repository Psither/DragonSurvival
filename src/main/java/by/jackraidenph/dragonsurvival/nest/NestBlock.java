package by.jackraidenph.dragonsurvival.nest;

import by.jackraidenph.dragonsurvival.capability.DragonStateHandler;
import by.jackraidenph.dragonsurvival.capability.DragonStateProvider;
import by.jackraidenph.dragonsurvival.handlers.TileEntityTypesInit;
import by.jackraidenph.dragonsurvival.util.DragonLevel;
import by.jackraidenph.dragonsurvival.util.DragonType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class NestBlock extends HorizontalBlock {

    public NestBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(HORIZONTAL_FACING);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return TileEntityTypesInit.nestEntityTile.create();
    }

    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
//        NestEntity nestEntity = getBlockEntity(worldIn, pos);
//        if (!worldIn.isRemote()) {
//        if (nestEntity.damageCooldown <= 0)
//            {
//                double damage = player.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
//                nestEntity.health -= Math.min(damage, 10);
//                DragonSurvivalMod.CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(pos.getX(), pos.getY(), pos.getZ(), 40, worldIn.getDimension().getType())), new SynchronizeNest(nestEntity.getPos(), nestEntity.health, nestEntity.damageCooldown));
//                if (nestEntity.health <= 0) {
//                    worldIn.playSound(player, pos, SoundEvents.BLOCK_ANVIL_DESTROY, SoundCategory.BLOCKS, 1, 1);
//                    worldIn.destroyBlock(pos, false);
//                } else {
//                    worldIn.playSound(player, pos, SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.BLOCKS, 1, 1);
//                    nestEntity.damageCooldown = NestEntity.COOLDOWN_TIME;
//                }
//                nestEntity.markDirty();
//            }
//        }
        super.onBlockClicked(state, worldIn, pos, player);
    }

    public NestEntity getBlockEntity(World world, BlockPos pos) {
        return (NestEntity) world.getTileEntity(pos);
    }

    /**
     * Prevent anyone from breaking the nest
     */
    @Override
    public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
        return 0;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        DragonStateHandler dragonStateHandler = player.getCapability(DragonStateProvider.PLAYER_STATE_HANDLER_CAPABILITY).orElse(null);
        DragonLevel dragonLevel = dragonStateHandler.getLevel();
        DragonType dragonType = dragonStateHandler.getType();
        //TODO transformation
        if (state.getBlock().getClass() == NestBlock.class && dragonLevel == DragonLevel.YOUNG) {
            switch (dragonType) {
                case SEA:
//                    worldIn.setBlockState(pos, BlockInit.mediumSeaNest.getDefaultState());
                    return ActionResultType.SUCCESS;
                case FOREST:
//                    worldIn.setBlockState(pos,BlockInit.mediumForestNest.getDefaultState());
                    return ActionResultType.SUCCESS;
                case CAVE:
//                    worldIn.setBlockState(pos,BlockInit.mediumCaveNest.getDefaultState());
                    return ActionResultType.SUCCESS;
            }
        }
        if (player instanceof ServerPlayerEntity) {
            NetworkHooks.openGui((ServerPlayerEntity) player, getBlockEntity(worldIn, pos), packetBuffer -> packetBuffer.writeBlockPos(pos));
        }
        return ActionResultType.SUCCESS;
    }

    /**
     * Setting owner and type
     */
    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        NestEntity nestEntity = getBlockEntity(worldIn, pos);
        if (placer != null) {
            DragonStateProvider.getCap(placer).ifPresent(dragonStateHandler -> {
                if (dragonStateHandler.isDragon()) {
                    if (nestEntity.ownerUUID == null) {
                        nestEntity.ownerUUID = placer.getUniqueID();
                    }
                    if (nestEntity.type == DragonType.NONE) {
                        nestEntity.type = dragonStateHandler.getType();
                    }
                }
            });
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        return tileentity != null && tileentity.receiveClientEvent(id, param);
    }
}
