package com.lordmau5.ffs.block.tanktiles;

import com.lordmau5.ffs.block.abstracts.AbstractBlock;
import com.lordmau5.ffs.blockentity.tanktiles.BlockEntityTankComputer;
import com.lordmau5.ffs.holder.FFSBlockEntities;
import com.lordmau5.ffs.util.FFSStateProps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import javax.annotation.Nullable;

public class BlockTankComputer extends AbstractBlock {
    public BlockTankComputer() {
        super(Properties.of().strength(5.0f, 6.0f));

        registerDefaultState(getStateDefinition().any().setValue(FFSStateProps.FACING, Direction.NORTH).setValue(FFSStateProps.TILE_VALID, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FFSStateProps.FACING, FFSStateProps.TILE_VALID);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FFSStateProps.FACING, pContext.getNearestLookingDirection().getOpposite());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return FFSBlockEntities.tankComputer.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == FFSBlockEntities.tankComputer.get() ? BlockEntityTankComputer::tick : null;
    }
}
