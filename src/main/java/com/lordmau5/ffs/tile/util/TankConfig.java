package com.lordmau5.ffs.tile.util;

import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

/**
 * Created by Dustin on 20.01.2016.
 */
public class TankConfig {

    private final InternalFluidTank fluidTank;

    private FluidStack lockedFluid;

    public TankConfig(AbstractTankValve valve) {
        this.fluidTank = new InternalFluidTank(valve);
    }

    private void resetVariables() {
        lockedFluid = null;

        fluidTank.setFluid(null);
        fluidTank.setCapacity(0);
    }

    public void lockFluid(FluidStack lockedFluid) {
        this.lockedFluid = lockedFluid;
    }

    public void unlockFluid() {
        this.lockedFluid = null;
    }

    public boolean isFluidLocked() {
        return this.lockedFluid != null;
    }

    public FluidStack getLockedFluid() {
        return this.lockedFluid;
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

    public void readFromNBT(NBTTagCompound mainTag) {
        resetVariables();

        if ( !mainTag.hasKey("tankConfig") ) {
            return;
        }

        getFluidTank().readFromNBT(mainTag);

        NBTTagCompound tag = mainTag.getCompoundTag("tankConfig");

        if ( tag.hasKey("lockedFluid") ) {
            NBTBase base = tag.getTag("lockedFluid");
            if ( base instanceof NBTTagCompound ) {
                lockFluid(FluidStack.loadFluidStackFromNBT((NBTTagCompound) base));
            }
        }
        setFluidCapacity(tag.getInteger("capacity"));

    }

    public void writeToNBT(NBTTagCompound mainTag) {
        NBTTagCompound tag = new NBTTagCompound();

        if ( getLockedFluid() != null ) {
            NBTTagCompound fluidTag = new NBTTagCompound();
            getLockedFluid().writeToNBT(fluidTag);

            tag.setTag("lockedFluid", fluidTag);
        }
        tag.setInteger("capacity", getFluidCapacity());

        getFluidTank().writeToNBT(mainTag);

        mainTag.setTag("tankConfig", tag);
    }

}
