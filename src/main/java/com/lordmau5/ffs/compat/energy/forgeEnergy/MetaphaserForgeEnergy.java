package com.lordmau5.ffs.compat.energy.forgeEnergy;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.tile.valves.TileEntityMetaphaser;
import com.lordmau5.ffs.util.GenericUtil;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * Created by Lordmau5 on 17.11.2016.
 */
public class MetaphaserForgeEnergy implements IEnergyStorage
{

    private final TileEntityMetaphaser metaphaser;

    public MetaphaserForgeEnergy(TileEntityMetaphaser metaphaser)
    {
        this.metaphaser = metaphaser;
    }

    private int convertForOutput(int amount)
    {
        return (int) Math.ceil((double) amount * GenericUtil.calculateEnergyLoss());
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        if (!metaphaser.isValid())
        {
            return 0;
        }

        if (metaphaser.getTankConfig().getFluidCapacity() - metaphaser.getTankConfig().getFluidAmount() <= 0)
        {
            return 0;
        }

        int i_power = Math.min(Integer.MAX_VALUE, maxReceive);

        i_power = metaphaser.getTankConfig().getFluidTank().fill(FluidRegistry.getFluidStack(FancyFluidStorage.fluidMetaphasedFlux.getName(), i_power), false);

        if (simulate)
        {
            return i_power;
        }

        return metaphaser.getTankConfig().getFluidTank().fill(FluidRegistry.getFluidStack(FancyFluidStorage.fluidMetaphasedFlux.getName(), i_power), true);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        if (!metaphaser.isValid())
        {
            return 0;
        }

        if (metaphaser.getTankConfig().getFluidAmount() <= 0)
        {
            return 0;
        }

        if (!metaphaser.containsMetaphasedFlux())
        {
            return 0;
        }

        int i_power = Math.min(maxExtract, metaphaser.getTankConfig().getFluidAmount());
        int energy = convertForOutput(metaphaser.getTankConfig().getFluidTank().drain(i_power, false).amount);

        if (simulate)
        {
            return energy;
        }

        return convertForOutput(metaphaser.getTankConfig().getFluidTank().drain(i_power, true).amount);
    }

    @Override
    public int getEnergyStored()
    {
        return (int) Math.ceil((double) metaphaser.getTankConfig().getFluidAmount() * GenericUtil.calculateEnergyLoss());
    }

    @Override
    public int getMaxEnergyStored()
    {
        return metaphaser.getTankConfig().getFluidCapacity();
    }

    @Override
    public boolean canExtract()
    {
        return true;
    }

    @Override
    public boolean canReceive()
    {
        return true;
    }
}
