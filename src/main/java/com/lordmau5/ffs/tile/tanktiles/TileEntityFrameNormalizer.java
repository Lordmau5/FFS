package com.lordmau5.ffs.tile.tanktiles;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

/**
 * Created by Lordmau5 on 25.11.2016.
 */

// TODO: Remove legacy support

@Deprecated
public class TileEntityFrameNormalizer extends TileEntity implements ITickable {
	private IBlockState camoBlockState;

	@Override
	public void update() {
		if(getWorld() == null || getWorld().isRemote) {
			return;
		}

		if(camoBlockState != null) {
			getWorld().setBlockState(getPos(), camoBlockState);
		}
		else {
			getWorld().setBlockToAir(getPos());
		}
	}

	public void setBlockState(IBlockState blockState) {
		this.camoBlockState = blockState;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);

		if(tag.hasKey("blockState")) {
			setBlockState(Block.getStateById(tag.getInteger("blockState")));
		}
	}
}
