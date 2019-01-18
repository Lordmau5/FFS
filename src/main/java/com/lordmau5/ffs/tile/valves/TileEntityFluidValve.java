package com.lordmau5.ffs.tile.valves;


import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

/**
 * Created by Dustin on 28.06.2015.
 */
public class TileEntityFluidValve extends AbstractTankValve {

    @Override
    public void update() {
        super.update();

        if ( getWorld().isRemote ) {
            return;
        }

        if ( !isValid() ) {
            return;
        }

        FluidStack fluidStack = getTankConfig().getFluidStack();
        if ( fluidStack == null ) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        if ( fluid == null ) {
            return;
        }

        if ( fluid == FluidRegistry.WATER ) {
            if ( getWorld().isRaining() ) {
                int rate = (int) Math.floor(getWorld().rainingStrength * 5 * getWorld().getBiomeForCoordsBody(getPos()).getRainfall());
                if ( getPos().getY() == getWorld().getPrecipitationHeight(getPos()).getY() - 1 ) {
                    FluidStack waterStack = fluidStack.copy();
                    waterStack.amount = rate * 10;
                    getTankConfig().getFluidTank().fill(waterStack, true);
                }
            }
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if ( getTileFacing() == null ) {
            return null;
        }

        if ( capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY ) {
            if ( getTankConfig() != null ) {
                return (T) getTankConfig().getFluidTank();
            }
        }

        return super.getCapability(capability, facing);
    }

}
