package com.lordmau5.ffs.tile.util;

import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class TankConfig {

    private final InternalFluidTank fluidTank;

    private Fluid lockedFluid = Fluids.EMPTY;

    public TankConfig(AbstractTankValve valve) {
        this.fluidTank = new InternalFluidTank(valve);
    }

    private void resetVariables() {
        lockedFluid = Fluids.EMPTY;

        fluidTank.setFluid(FluidStack.EMPTY);
        fluidTank.setCapacity(0);
    }

    public void lockFluid(FluidStack lockedFluid) {
        if (lockedFluid == null) {
            lockedFluid = FluidStack.EMPTY;
        }
        this.lockedFluid = lockedFluid.getFluid();
    }

    public void unlockFluid() {
        this.lockedFluid = Fluids.EMPTY;
    }

    public boolean isFluidLocked() {
        return this.lockedFluid != Fluids.EMPTY;
    }

    public FluidStack getLockedFluid() {
        return new FluidStack(this.lockedFluid, 1000);
    }

    public FluidTank getFluidTank() {
        return this.fluidTank;
    }

    public FluidStack getFluidStack() {
        return this.fluidTank.getFluid();
    }

    public void setFluidStack(FluidStack fluidStack) {
        this.fluidTank.setFluid(fluidStack);
    }

    public int getFluidCapacity() {
        return this.fluidTank.getCapacity();
    }

    public void setFluidCapacity(int fluidCapacity) {
        this.fluidTank.setCapacity(fluidCapacity);
    }

    public int getFluidAmount() {
        return this.fluidTank.getFluidAmount();
    }

    public boolean isEmpty() {
        return this.fluidTank.isEmpty();
    }

    public void readFromNBT(CompoundTag mainTag) {
        resetVariables();

        if ( !mainTag.contains("TankConfig") ) {
            return;
        }

        CompoundTag tag = mainTag.getCompound("TankConfig");

        getFluidTank().readFromNBT(tag);

        if ( tag.contains("LockedFluid") ) {
            CompoundTag base = tag.getCompound("LockedFluid");
            lockFluid(FluidStack.loadFluidStackFromNBT(base));
        }
        setFluidCapacity(tag.getInt("Capacity"));

    }

    public void writeToNBT(CompoundTag mainTag) {
        CompoundTag tag = new CompoundTag();

        if ( isFluidLocked() ) {
            CompoundTag lockedFluidTag = new CompoundTag();

            getLockedFluid().writeToNBT(lockedFluidTag);

            tag.put("LockedFluid", lockedFluidTag);
        }
        tag.putInt("Capacity", getFluidCapacity());

        getFluidTank().writeToNBT(tag);

        mainTag.put("TankConfig", tag);
    }

}
