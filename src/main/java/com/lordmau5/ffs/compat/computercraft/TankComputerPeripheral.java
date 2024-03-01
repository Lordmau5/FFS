//package com.lordmau5.ffs.compat.computercraft;
//
//import com.lordmau5.ffs.blockentity.tanktiles.BlockEntityTankComputer;
//import com.lordmau5.ffs.blockentity.util.TankConfig;
//import dan200.computercraft.api.lua.LuaException;
//import dan200.computercraft.api.lua.LuaFunction;
//import dan200.computercraft.api.peripheral.IPeripheral;
//
//import javax.annotation.Nullable;
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Optional;
//
//public record TankComputerPeripheral(BlockEntityTankComputer computer) implements IPeripheral {
//
//    @Override
//    public String getType() {
//        return "ffs_tank_computer";
//    }
//
//    @Nullable
//    @Override
//    public Object getTarget() {
//        return computer;
//    }
//
//    @Override
//    public boolean equals(@Nullable IPeripheral other) {
//        return other == this;
//    }
//
//    private void ensureValidity() throws LuaException {
//        if (!computer.isValid()) {
//            throw new LuaException("Tank is invalid.");
//        }
//    }
//
//    private double roundToDecimals(double value, int decimals) {
//        BigDecimal bd = BigDecimal.valueOf(value);
//        bd = bd.setScale(decimals, RoundingMode.HALF_UP);
//
//        return bd.doubleValue();
//    }
//
//    @LuaFunction(mainThread = true)
//    public Map<?, ?> getTankInfo() throws LuaException {
//        ensureValidity();
//
//        HashMap<String, Object> table = new HashMap<>();
//
//        TankConfig config = computer.getMainValve().getTankConfig();
//
//        table.put("fluid", config.isEmpty() ? null : config.getFluidStack().getDisplayName().getString());
//        if (!config.isEmpty()) {
//            int amount = config.getFluidAmount();
//            int capacity = config.getFluidCapacity();
//
//            table.put("amount", amount);
//            table.put("capacity", capacity);
//
//            table.put("fillPercentage", roundToDecimals((double) amount / capacity, 2));
//        }
//
//        return table;
//    }
//
//    @LuaFunction(mainThread = true)
//    public boolean isFluidLocked() throws LuaException {
//        ensureValidity();
//
//        TankConfig config = computer.getMainValve().getTankConfig();
//        return config.isFluidLocked();
//    }
//
//    @LuaFunction(mainThread = true)
//    public void toggleFluidLock(Optional<Boolean> shouldLockOpt) throws LuaException {
//        ensureValidity();
//
//        TankConfig config = computer.getMainValve().getTankConfig();
//
//        boolean shouldLock = shouldLockOpt.orElse(!config.isFluidLocked());
//
//        if (shouldLock) {
//            if (config.isEmpty()) {
//                throw new LuaException("Can't lock fluid: No fluid in tank.");
//            }
//
//            config.lockFluid(config.getFluidStack());
//        } else {
//            config.unlockFluid();
//        }
//
//        computer.markForUpdateNow();
//    }
//
//    @LuaFunction(mainThread = true)
//    public String getLockedFluid() throws LuaException {
//        ensureValidity();
//
//        TankConfig config = computer.getMainValve().getTankConfig();
//        if (config.isEmpty() || config.getLockedFluid().isEmpty()) return null;
//
//        return config.getLockedFluid().getDisplayName().getString();
//    }
//}
