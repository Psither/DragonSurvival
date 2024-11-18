package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;


/** Mixture of vanilla implementation from RespawnAnchorBlock.java and TheEndGateWayBlockEntity.java */
@SuppressWarnings({"removal", "deprecation"})
public class PrimordialAnchorBlock extends Block {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final BooleanProperty CHARGED = BooleanProperty.create("charged");

    public PrimordialAnchorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CHARGED, false));
    }

    private static boolean canBeCharged(BlockState state) {
        return !state.getValue(CHARGED);
    }

    private static void charge(@Nullable Entity entity, Level level, BlockPos pos, BlockState state) {
        BlockState blockstate = state.setValue(CHARGED, true);
        level.setBlock(pos, blockstate, 3);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(entity, blockstate));
        level.playSound(
                null,
                (double)pos.getX() + 0.5,
                (double)pos.getY() + 0.5,
                (double)pos.getZ() + 0.5,
                SoundEvents.RESPAWN_ANCHOR_CHARGE,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );
    }

    @Override
    public void animateTick(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
        if (state.getValue(CHARGED)) {
            if (random.nextInt(100) == 0) {
                level.playLocalSound(pos, SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }

            double d0 = (double)pos.getX() + 0.5 + (0.5 - random.nextDouble());
            double d1 = (double)pos.getY() + 1.0;
            double d2 = (double)pos.getZ() + 0.5 + (0.5 - random.nextDouble());
            double d3 = (double)random.nextFloat() * 0.04;
            level.addParticle(ParticleTypes.REVERSE_PORTAL, d0, d1, d2, 0.0, d3, 0.0);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGED);
    }

    @Override
    protected boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, @NotNull Level level, @NotNull BlockPos pos) {
        return blockState.getValue(CHARGED) ? 15 : 0;
    }

    private static boolean isWaterThatWouldFlow(BlockPos pos, Level level) {
        FluidState fluidstate = level.getFluidState(pos);
        if (!fluidstate.is(FluidTags.WATER)) {
            return false;
        } else if (fluidstate.isSource()) {
            return true;
        } else {
            float f = (float)fluidstate.getAmount();
            if (f < 2.0F) {
                return false;
            } else {
                FluidState fluidstate1 = level.getFluidState(pos.below());
                return !fluidstate1.is(FluidTags.WATER);
            }
        }
    }

    private static BlockPos findTallestBlock(BlockGetter level, BlockPos pos, int radius, boolean allowBedrock) {
        BlockPos blockpos = null;

        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                if (i != 0 || j != 0 || allowBedrock) {
                    for (int k = level.getMaxBuildHeight() - 1; k > (blockpos == null ? level.getMinBuildHeight() : blockpos.getY()); k--) {
                        BlockPos blockpos1 = new BlockPos(pos.getX() + i, k, pos.getZ() + j);
                        BlockState blockstate = level.getBlockState(blockpos1);
                        if (blockstate.isCollisionShapeFullBlock(level, blockpos1) && (allowBedrock || !blockstate.is(Blocks.BEDROCK))) {
                            blockpos = blockpos1;
                            break;
                        }
                    }
                }
            }
        }

        return blockpos == null ? pos : blockpos;
    }

    @Nullable private static BlockPos findValidSpawnInChunk(LevelChunk chunk) {
        ChunkPos chunkpos = chunk.getPos();
        BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX(), 30, chunkpos.getMinBlockZ());
        int i = chunk.getHighestSectionPosition() + 16 - 1;
        BlockPos blockpos1 = new BlockPos(chunkpos.getMaxBlockX(), i, chunkpos.getMaxBlockZ());
        BlockPos blockpos2 = null;
        double d0 = 0.0;

        for (BlockPos blockpos3 : BlockPos.betweenClosed(blockpos, blockpos1)) {
            BlockState blockstate = chunk.getBlockState(blockpos3);
            BlockPos blockpos4 = blockpos3.above();
            BlockPos blockpos5 = blockpos3.above(2);
            if (blockstate.is(Blocks.END_STONE)
                    && !chunk.getBlockState(blockpos4).isCollisionShapeFullBlock(chunk, blockpos4)
                    && !chunk.getBlockState(blockpos5).isCollisionShapeFullBlock(chunk, blockpos5)) {
                double d1 = blockpos3.distToCenterSqr(0.0, 0.0, 0.0);
                if (blockpos2 == null || d1 < d0) {
                    blockpos2 = blockpos3;
                    d0 = d1;
                }
            }
        }

        return blockpos2;
    }

    private static LevelChunk getChunk(Level level, Vec3 pos) {
        return level.getChunk(Mth.floor(pos.x / 16.0), Mth.floor(pos.z / 16.0));
    }

    private static boolean isChunkEmpty(ServerLevel level, Vec3 pos) {
        return getChunk(level, pos).getHighestFilledSectionIndex() == -1;
    }

    private static Vec3 findExitPortalXZPosTentative(ServerLevel level, BlockPos pos) {
        Vec3 vec3 = new Vec3(pos.getX(), 0.0, pos.getZ()).normalize();
        int i = 1024;
        Vec3 vec31 = vec3.scale(1024.0);

        for (int j = 16; !isChunkEmpty(level, vec31) && j-- > 0; vec31 = vec31.add(vec3.scale(-16.0))) {
            LOGGER.debug("Skipping backwards past nonempty chunk at {}", vec31);
        }

        for (int k = 16; isChunkEmpty(level, vec31) && k-- > 0; vec31 = vec31.add(vec3.scale(16.0))) {
            LOGGER.debug("Skipping forward past empty chunk at {}", vec31);
        }

        LOGGER.debug("Found chunk at {}", vec31);
        return vec31;
    }

    private static BlockPos findOrCreateValidTeleportPos(ServerLevel level, BlockPos pos) {
        Vec3 vec3 = findExitPortalXZPosTentative(level, pos);
        LevelChunk levelchunk = getChunk(level, vec3);
        BlockPos validSpawn = findValidSpawnInChunk(levelchunk);
        if (validSpawn == null) {
            BlockPos blockpos1 = BlockPos.containing(vec3.x + 0.5, 75.0, vec3.z + 0.5);
            LOGGER.debug("Failed to find a suitable block to teleport to, spawning an island on {}", blockpos1);
            level.registryAccess()
                    .registry(Registries.CONFIGURED_FEATURE)
                    .flatMap(configuredFeatureRegistry -> configuredFeatureRegistry.getHolder(EndFeatures.END_ISLAND))
                    .ifPresent(
                            configuredFeatureHolder -> configuredFeatureHolder.value()
                                    .place(level, level.getChunkSource().getGenerator(), RandomSource.create(blockpos1.asLong()), blockpos1)
                    );
            validSpawn = blockpos1;
        } else {
            LOGGER.debug("Found suitable block to teleport to: {}", validSpawn);
        }

        return findTallestBlock(level, validSpawn, 16, true);
    }

    private static boolean isChargeFuel(ItemStack stack) {
        return stack.is(Items.ENDER_PEARL);
    }

    private static boolean canUseTeleport(Level level) {
        return level.dimension() == Level.END;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hitResult) {
        if (!state.getValue(CHARGED)) {
            return InteractionResult.PASS;
        } else if (!canUseTeleport(level)) {
            return InteractionResult.PASS;
        } else {
            if (!level.isClientSide) {
                DragonStateHandler handler = DragonStateProvider.getData(player);
                if (!handler.isDragon()) {
                    level.playSound(
                            null,
                            (double) pos.getX() + 0.5,
                            (double) pos.getY() + 0.5,
                            (double) pos.getZ() + 0.5,
                            SoundEvents.FIRE_EXTINGUISH,
                            SoundSource.BLOCKS,
                            1.0F,
                            1.0F);

                    player.hurt(level.damageSources().magic(), 1.0F);

                    return InteractionResult.PASS;
                }

                state.setValue(CHARGED, false);

                BlockPos blockpos = findOrCreateValidTeleportPos((ServerLevel) level, pos);
                blockpos = blockpos.above(5);
                DimensionTransition transition = new DimensionTransition((ServerLevel) level, blockpos.getCenter(), player.getDeltaMovement(), player.getYRot(), player.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND);
                player.changeDimension(transition);

                level.playSound(
                        null,
                        (double) pos.getX() + 0.5,
                        (double) pos.getY() + 0.5,
                        (double) pos.getZ() + 0.5,
                        SoundEvents.RESPAWN_ANCHOR_SET_SPAWN,
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F
                );
            }

            DragonStateHandler handler = DragonStateProvider.getData(player);
            if (!handler.isDragon()) {
                for (int i = 0; i < 10; i++) {
                    double d0 = (double)pos.getX() + 0.5 + (0.5 - level.random.nextDouble());
                    double d1 = (double)pos.getY() + 1.0;
                    double d2 = (double)pos.getZ() + 0.5 + (0.5 - level.random.nextDouble());
                    double d3 = (double)level.random.nextFloat() * 0.04;
                    level.addParticle(ParticleTypes.LARGE_SMOKE, d0, d1, d2, 0.0, d3, 0.0);
                }

                return InteractionResult.PASS;
            }

            return InteractionResult.CONSUME;
        }
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(
            @NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult
    ) {
        if (isChargeFuel(stack) && canBeCharged(state)) {
            charge(player, level, pos, state);
            stack.consume(1, player);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return hand == InteractionHand.MAIN_HAND && isChargeFuel(player.getItemInHand(InteractionHand.OFF_HAND)) && canBeCharged(state)
                    ? ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION
                    : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType pathComputationType) {
        return false;
    }
}
