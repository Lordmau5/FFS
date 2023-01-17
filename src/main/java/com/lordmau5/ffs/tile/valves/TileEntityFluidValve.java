package com.lordmau5.ffs.tile.valves;


import com.lordmau5.ffs.holder.TileEntities;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Direction;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityFluidValve extends AbstractTankValve {

    private final LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> getTankConfig().getFluidTank());

    public TileEntityFluidValve() {
        super(TileEntities.tileEntityFluidValve);
    }

    @Override
    public void tick() {
        super.tick();

        if ( getLevel().isClientSide ) {
            return;
        }

        if ( !isValid() ) {
            return;
        }

        FluidStack fluidStack = getTankConfig().getFluidStack();
        if ( fluidStack.isEmpty() ) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        if ( fluid == null ) {
            return;
        }

        if ( fluid == Fluids.WATER ) {
            if ( getLevel().isRaining() ) {
                int rate = (int) Math.floor(getLevel().rainLevel * 5 * getLevel().getBiome(getBlockPos()).getDownfall());
                if ( getBlockPos().getY() == getLevel().getHeightmapPos(Heightmap.Type.WORLD_SURFACE, getBlockPos()).getY() - 1 ) {
                    FluidStack waterStack = fluidStack.copy();
                    waterStack.setAmount(rate * 10);
                    getTankConfig().getFluidTank().fill(waterStack, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY)
            return holder.cast();
        return super.getCapability(cap, side);
    }
}
