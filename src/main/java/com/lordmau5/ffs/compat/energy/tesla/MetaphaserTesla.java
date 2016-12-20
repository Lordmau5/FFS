package com.lordmau5.ffs.compat.energy.tesla;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.compat.Capabilities;
import com.lordmau5.ffs.tile.valves.TileEntityMetaphaser;
import com.lordmau5.ffs.util.GenericUtil;
import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.api.ITeslaProducer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * Created by Dustin on 01.06.2016.
 */

/**
 * Handles Tesla... power?
 */
public class MetaphaserTesla implements ITeslaConsumer, ITeslaProducer, ITeslaHolder {

	private final TileEntityMetaphaser metaphaser;

	public MetaphaserTesla(TileEntityMetaphaser metaphaser) {
		this.metaphaser = metaphaser;
	}

	private int convertForOutput(int amount) {
		return (int) Math.ceil((double) amount * GenericUtil.calculateEnergyLoss());
	}

	public void outputToTile() {
		if(metaphaser.getTankConfig().getFluidAmount() <= 0) {
			return;
		}

		BlockPos outsidePos = metaphaser.getPos().offset(metaphaser.getTileFacing().getOpposite());
		if(metaphaser.getWorld().isAirBlock(outsidePos)) {
			return;
		}

		TileEntity outsideTile = metaphaser.getWorld().getTileEntity(outsidePos);
		if(outsideTile == null) {
			return;
		}

		if(!metaphaser.containsMetaphasedFlux()) {
			return;
		}

		if(!outsideTile.hasCapability(Capabilities.Tesla.RECEIVER, metaphaser.getTileFacing().getOpposite())) {
			return;
		}

		ITeslaConsumer consumer = outsideTile.getCapability(Capabilities.Tesla.RECEIVER, metaphaser.getTileFacing().getOpposite());
		int maxPowa = Math.min(50, metaphaser.getTankConfig().getFluidAmount());
		maxPowa = (int) Math.max(Integer.MAX_VALUE, consumer.givePower(takePower(maxPowa, true), true));
		if(maxPowa > 0) {
			consumer.givePower(takePower(maxPowa, false), false);
		}
	}

	@Override
	public long givePower(long power, boolean simulated) {
		if(!metaphaser.isValid()) {
			return 0;
		}

		if(getCapacity() - metaphaser.getTankConfig().getFluidAmount() <= 0) {
			return 0;
		}

		int i_power = (int) Math.min(Integer.MAX_VALUE, power);

		i_power = metaphaser.getTankConfig().getFluidTank().fill(FluidRegistry.getFluidStack(FancyFluidStorage.fluidMetaphasedFlux.getName(), i_power), false);

		if(simulated) {
			return i_power;
		}

		return metaphaser.getTankConfig().getFluidTank().fill(FluidRegistry.getFluidStack(FancyFluidStorage.fluidMetaphasedFlux.getName(), i_power), true);
	}

	@Override
	public long takePower(long power, boolean simulated) {
		if(!metaphaser.isValid()) {
			return 0;
		}

		if(metaphaser.getTankConfig().getFluidAmount() <= 0) {
			return 0;
		}

		if(!metaphaser.containsMetaphasedFlux()) {
			return 0;
		}

		int i_power = (int) Math.min(power, metaphaser.getTankConfig().getFluidTank().getFluidAmount());
		int energy = convertForOutput(metaphaser.getTankConfig().getFluidTank().drain(i_power, false).amount);

		if(simulated) {
			return energy;
		}

		return convertForOutput(metaphaser.getTankConfig().getFluidTank().drain(i_power, true).amount);
	}

	@Override
	public long getStoredPower() {
		return (long) Math.ceil((double) metaphaser.getTankConfig().getFluidAmount() * GenericUtil.calculateEnergyLoss());
	}

	@Override
	public long getCapacity() {
		return metaphaser.getTankConfig().getFluidCapacity();
	}
}
