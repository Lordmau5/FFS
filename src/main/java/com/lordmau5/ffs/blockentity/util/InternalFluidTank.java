package com.lordmau5.ffs.blockentity.util;

import com.lordmau5.ffs.blockentity.abstracts.AbstractTankValve;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

class InternalFluidTank extends FluidTank
{
    private final AbstractTankValve valve;

    public InternalFluidTank(AbstractTankValve valve) {
        super(0);

        this.valve = valve;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        valve.setNeedsUpdate();
    }
}
