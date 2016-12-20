package com.lordmau5.ffs.block.valves;

import com.lordmau5.ffs.block.abstracts.AbstractBlockValve;
import com.lordmau5.ffs.tile.valves.TileEntityFluidValve;
import com.lordmau5.ffs.util.FFSStateProps;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Created by Dustin on 28.06.2015.
 */
public class BlockFluidValve extends AbstractBlockValve {

	public BlockFluidValve() {
		super("block_fluid_valve");
	}

	@Override
	public void setDefaultState() {
		setDefaultState(blockState.getBaseState()
								.withProperty(FFSStateProps.TILE_VALID, false)
								.withProperty(FFSStateProps.TILE_MASTER, false));
	}

	@Override
	public BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FFSStateProps.TILE_VALID, FFSStateProps.TILE_MASTER);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if(tile != null && tile instanceof TileEntityFluidValve) {
			TileEntityFluidValve valve = (TileEntityFluidValve) tile;

			state = state.withProperty(FFSStateProps.TILE_VALID, valve.isValid())
					.withProperty(FFSStateProps.TILE_MASTER, valve.isMaster());
		}
		return state;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityFluidValve();
	}
}
