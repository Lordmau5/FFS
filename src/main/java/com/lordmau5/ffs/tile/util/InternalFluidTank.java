package com.lordmau5.ffs.tile.util;

import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import net.minecraftforge.fluids.capability.templates.FluidTank;

class InternalFluidTank extends FluidTank {
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
