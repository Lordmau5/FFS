package com.lordmau5.ffs.tile.util;

import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import net.minecraftforge.fluids.FluidTank;

/**
 * Created by Lordmau5 on 21.11.2016.
 */
class InternalFluidTank extends FluidTank
{
    private final AbstractTankValve valve;

    public InternalFluidTank(AbstractTankValve valve)
    {
        super(0);

        this.valve = valve;
    }

    @Override
    protected void onContentsChanged()
    {
        super.onContentsChanged();
        valve.setNeedsUpdate();
    }
}
