package com.lordmau5.ffs.block.valves;

import com.lordmau5.ffs.block.abstracts.AbstractBlockValve;
import com.lordmau5.ffs.client.CreativeTabFFS;
import com.lordmau5.ffs.tile.valves.TileEntityMetaphaser;
import com.lordmau5.ffs.util.FFSStateProps;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Created by Dustin on 08.02.2016.
 */
public class BlockMetaphaser extends AbstractBlockValve
{
    public BlockMetaphaser()
    {
        super("block_metaphaser");
        setCreativeTab(CreativeTabFFS.INSTANCE);
    }

    @Override
    public void setDefaultState()
    {
        setDefaultState(blockState.getBaseState().withProperty(FFSStateProps.TILE_VALID, false));
    }

    @Override
    public BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FFSStateProps.TILE_VALID);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile != null && tile instanceof TileEntityMetaphaser)
        {
            TileEntityMetaphaser metaphaser = (TileEntityMetaphaser) tile;

            state = state.withProperty(FFSStateProps.TILE_VALID, metaphaser.isValid());
        }
        return state;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileEntityMetaphaser();
    }
}
