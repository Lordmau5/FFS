package com.lordmau5.ffs.compat.energy.rf;

import cofh.api.energy.IEnergyReceiver;
import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.tile.valves.TileEntityMetaphaser;
import com.lordmau5.ffs.util.GenericUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * Created by Dustin on 02.06.2016.
 */

/**
 * Handles Redstone Flux
 */
public enum MetaphaserRF {

	INSTANCE;

	public void outputToTile(TileEntityMetaphaser metaphaser) {
		if(metaphaser.getTankConfig().getFluidAmount() <= 0) {
			return;
		}

		BlockPos outsidePos = metaphaser.getPos().offset(metaphaser.getTileFacing().getOpposite());
		if(metaphaser.getWorld().isAirBlock(outsidePos)) {
			return;
		}

		TileEntity outsideTile = metaphaser.getWorld().getTileEntity(outsidePos);
		if(outsideTile == null || !(outsideTile instanceof IEnergyReceiver)) {
			return;
		}

		if(!metaphaser.containsMetaphasedFlux()) {
			return;
		}

		IEnergyReceiver receiver = (IEnergyReceiver) outsideTile;
		int maxReceive = receiver.receiveEnergy(metaphaser.getTileFacing(), extractEnergy(metaphaser, EnumFacing.DOWN, metaphaser.getTankConfig().getFluidAmount(), true), true);
		if(maxReceive > 0) {
			receiver.receiveEnergy(metaphaser.getTileFacing(), extractEnergy(metaphaser, EnumFacing.DOWN, maxReceive, false), false);
		}
	}

	private int getMaxEnergyBuffer(TileEntityMetaphaser metaphaser) {
		return (int) Math.ceil((float) metaphaser.getTankConfig().getFluidCapacity() / 200f);
	}

	public int convertForOutput(int amount) {
		return (int) Math.ceil((double) amount * GenericUtil.calculateEnergyLoss());
	}

	public int getMaxEnergyStored(TileEntityMetaphaser metaphaser, EnumFacing facing) {
		return metaphaser.getTankConfig().getFluidCapacity();
	}

	public boolean canConnectEnergy(TileEntityMetaphaser metaphaser, EnumFacing facing) {
		return metaphaser.isValid();
	}

	public int extractEnergy(TileEntityMetaphaser metaphaser, EnumFacing facing, int maxExtract, boolean simulate) {
		if(!metaphaser.isValid()) {
			return 0;
		}

		if(metaphaser.getTankConfig().getFluidAmount() <= 0) {
			return 0;
		}

		if(!metaphaser.containsMetaphasedFlux()) {
			return 0;
		}

		maxExtract = Math.min(maxExtract, metaphaser.getTankConfig().getFluidAmount());

		int energy = convertForOutput(metaphaser.getTankConfig().getFluidTank().drain(maxExtract, false).amount);

		if(simulate) {
			return energy;
		}

		return convertForOutput(metaphaser.getTankConfig().getFluidTank().drain(maxExtract, true).amount);
	}

	public int receiveEnergy(TileEntityMetaphaser metaphaser, EnumFacing facing, int maxReceive, boolean simulate) {
		if(!metaphaser.isValid()) {
			return 0;
		}

		if(metaphaser.getTankConfig().getFluidCapacity() - metaphaser.getTankConfig().getFluidAmount() <= 0) {
			return 0;
		}

		maxReceive = Math.min(maxReceive, getMaxEnergyBuffer(metaphaser));

		maxReceive = metaphaser.getTankConfig().getFluidTank().fill(FluidRegistry.getFluidStack(FancyFluidStorage.fluidMetaphasedFlux.getName(), maxReceive), false);

		if(simulate) {
			return maxReceive;
		}

		return metaphaser.getTankConfig().getFluidTank().fill(FluidRegistry.getFluidStack(FancyFluidStorage.fluidMetaphasedFlux.getName(), maxReceive), true);
	}

}
