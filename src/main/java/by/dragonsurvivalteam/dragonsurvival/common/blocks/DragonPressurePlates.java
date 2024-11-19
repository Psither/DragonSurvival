package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import javax.annotation.Nullable;

public class DragonPressurePlates extends PressurePlateBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public PressurePlateType type;

    public enum PressurePlateType {
        DRAGON,
        HUMAN,
        SEA,
        CAVE,
        FOREST
    }

    public DragonPressurePlates(Properties properties, PressurePlateType type) {
        super(BlockSetType.WARPED, properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false).setValue(WATERLOGGED, false));

        this.type = type;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return PRESSED_AABB;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction dir, BlockState state2, LevelAccessor level, BlockPos pos, BlockPos pos2) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return super.updateShape(state, dir, state2, level, pos, pos2);
    }

    @Override
    protected int getSignalStrength(Level pLevel, BlockPos pPos) {
        net.minecraft.world.phys.AABB axisalignedbb = TOUCH_AABB.move(pPos);
        List<? extends Entity> list = pLevel.getEntities(null, axisalignedbb);

        if (!list.isEmpty()) {
            for (Entity entity : list) {
                if (!entity.isIgnoringBlockTriggers()) {
                    return switch (type) {
                        case DRAGON -> DragonStateProvider.isDragon(entity) ? 15 : 0;
                        case HUMAN -> !DragonStateProvider.isDragon(entity) ? 15 : 0;
                        case SEA -> DragonUtils.isType(entity, DragonTypes.SEA) ? 15 : 0;
                        case FOREST -> DragonUtils.isType(entity, DragonTypes.FOREST) ? 15 : 0;
                        case CAVE -> DragonUtils.isType(entity, DragonTypes.CAVE) ? 15 : 0;
                    };
                }
            }
        }
        return 0;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder);
        pBuilder.add(FACING);
        pBuilder.add(WATERLOGGED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull final BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);

        if (state == null) {
            return null;
        }

        if (state.hasProperty(FACING)) {
            state = state.setValue(FACING, context.getHorizontalDirection());
        }

        return state.setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public @NotNull BlockState rotate(final BlockState state, @NotNull final Rotation rotation) {
        if (state.hasProperty(FACING)) {
            return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
        }

        return state;
    }

    @Override
    public @NotNull BlockState mirror(final BlockState state, @NotNull final Mirror mirror) {
        if (state.hasProperty(FACING)) {
            return state.rotate(mirror.getRotation(state.getValue(FACING)));
        }

        return state;
    }
}