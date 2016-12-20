package com.lordmau5.ffs.compat.cnb;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import com.lordmau5.ffs.util.GenericUtil;
import mod.chiselsandbits.api.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by Lordmau5 on 08.10.2016.
 */
public enum CNBCompatibility {

	INSTANCE;

	public boolean isValid(World world, BlockPos pos, EnumFacing facing) {
		IChiselAndBitsAPI cbAPI = CNBAPIAccess.apiInstance;
		if(cbAPI.isBlockChiseled(world, pos)) {
			try {
				Walls p = Walls.valueOf(facing.toString().toUpperCase());

				IBitAccess bitAccess = cbAPI.getBitAccess(world, pos);
				BitQueryResults results = bitAccess.queryBitRange(p.posA, p.posB);
				if(results.solid == 256) {
					return true;
				}
			}
			catch(APIExceptions.CannotBeChiseled cannotBeChiseled) {
				cannotBeChiseled.printStackTrace();
			}
		}
		return false;
	}

	@SubscribeEvent
	public void onBlockBitModified(EventBlockBitPostModification event) {
		if(event.getWorld().isRemote) {
			return;
		}

		if(!FancyFluidStorage.tankManager.isPartOfTank(event.getWorld(), event.getPos())) {
			return;
		}

		AbstractTankValve valve = FancyFluidStorage.tankManager.getValveForBlock(event.getWorld(), event.getPos());
		if(valve == null || !valve.isValid()) {
			return;
		}

		EnumFacing inside = GenericUtil.getInsideForTankFrame(valve.getAirBlocks(), event.getPos());
		if(!isValid(event.getWorld(), event.getPos(), inside)) {
			valve.breakTank();
		}
	}

	private enum Walls {
		DOWN(new BlockPos(0, 0, 0), new BlockPos(15, 0, 15)),
		UP(new BlockPos(0, 15, 0), new BlockPos(15, 15, 15)),
		NORTH(new BlockPos(0, 0, 0), new BlockPos(15, 15, 0)),
		SOUTH(new BlockPos(0, 0, 15), new BlockPos(15, 15, 15)),
		WEST(new BlockPos(0, 0, 0), new BlockPos(0, 15, 15)),
		EAST(new BlockPos(15, 0, 0), new BlockPos(15, 15, 15));

		private final BlockPos posA;
		private final BlockPos posB;

		Walls(BlockPos a, BlockPos b) {
			this.posA = a;
			this.posB = b;
		}
	}

}
