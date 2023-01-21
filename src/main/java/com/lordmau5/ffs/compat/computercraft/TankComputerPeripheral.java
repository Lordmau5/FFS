package com.lordmau5.ffs.compat.computercraft;

import com.lordmau5.ffs.blockentity.tanktiles.BlockEntityTankComputer;
import com.lordmau5.ffs.blockentity.util.TankConfig;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.IPeripheral;

import javax.annotation.Nullable;

public record TankComputerPeripheral(BlockEntityTankComputer computer) implements IPeripheral {

    @Override
    public String getType() {
        return "ffs_tank_computer";
    }

    @Nullable
    @Override
    public Object getTarget() {
        return computer;
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return other == this;
    }

    private void ensureValidity() throws LuaException {
        if (!computer.isValid()) {
            throw new LuaException("Tank is invalid.");
        }
    }

    @LuaFunction(mainThread = true)
    public String getFluidName() throws LuaException {
        ensureValidity();

        TankConfig config = computer.getMainValve().getTankConfig();
        if (config.isEmpty()) return null;

        return config.getFluidStack().getDisplayName().getString();
    }

    @LuaFunction(mainThread = true)
    public int getFluidAmount() throws LuaException {
        ensureValidity();

        TankConfig config = computer.getMainValve().getTankConfig();
        if (config.isEmpty()) return 0;

        return config.getFluidAmount();
    }

    @LuaFunction(mainThread = true)
    public int getFluidCapacity() throws LuaException {
        ensureValidity();

        TankConfig config = computer.getMainValve().getTankConfig();
        if (config.isEmpty()) return 0;

        return config.getFluidCapacity();
    }

    @LuaFunction(mainThread = true)
    public boolean isFluidLocked() throws LuaException {
        ensureValidity();

        TankConfig config = computer.getMainValve().getTankConfig();
        return config.isFluidLocked();
    }

    @LuaFunction(mainThread = true)
    public void toggleFluidLock() throws LuaException {
        ensureValidity();

        TankConfig config = computer.getMainValve().getTankConfig();
        toggleFluidLock(!config.isFluidLocked());
    }

    @LuaFunction(mainThread = true)
    public void toggleFluidLock(boolean shouldLock) throws LuaException {
        ensureValidity();

        TankConfig config = computer.getMainValve().getTankConfig();

        if (shouldLock) {
            if (config.isEmpty()) {
                throw new LuaException("Can't lock fluid: No fluid in tank.");
            }

            config.lockFluid(config.getFluidStack());
        } else {
            config.unlockFluid();
        }
    }

    @LuaFunction(mainThread = true)
    public String getLockedFluid() throws LuaException {
        ensureValidity();

        TankConfig config = computer.getMainValve().getTankConfig();
        if (config.isEmpty()) return null;

        return config.getLockedFluid().getDisplayName().getString();
    }
}
