package com.lordmau5.ffs.block.tanktiles;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.tile.tanktiles.TileEntityFrameNormalizer;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Created by Lordmau5 on 25.11.2016.
 */

// TODO: Remove legacy support

@Deprecated
public class BlockFrameNormalizer extends Block {
	public BlockFrameNormalizer(boolean opaque) {
		super(Material.ROCK, MapColor.STONE);

		setUnlocalizedName(FancyFluidStorage.MODID + ".block_frame_normalizer" + (opaque ? "_opaque" : ""));
		setRegistryName("block_frame_normalizer" + (opaque ? "_opaque" : ""));
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityFrameNormalizer();
	}
}
