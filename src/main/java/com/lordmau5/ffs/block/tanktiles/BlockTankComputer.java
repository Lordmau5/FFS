package com.lordmau5.ffs.block.tanktiles;

import com.lordmau5.ffs.block.abstracts.AbstractBlock;
import com.lordmau5.ffs.blockentity.tanktiles.BlockEntityTankComputer;
import com.lordmau5.ffs.holder.BlockEntities;
import com.lordmau5.ffs.util.FFSStateProps;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;

import javax.annotation.Nullable;

public class BlockTankComputer extends AbstractBlock {
    public BlockTankComputer() {
        super(Block.Properties.of(Material.METAL).strength(5.0f, 6.0f));

        registerDefaultState(getStateDefinition().any().setValue(FFSStateProps.TILE_VALID, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FFSStateProps.TILE_VALID);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntities.tileEntityTankComputer.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntities.tileEntityTankComputer.get() ? BlockEntityTankComputer::tick : null;
    }
}
