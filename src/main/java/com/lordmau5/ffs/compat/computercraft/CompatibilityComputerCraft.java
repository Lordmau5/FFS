//package com.lordmau5.ffs.compat.computercraft;
//
//import com.lordmau5.ffs.blockentity.tanktiles.BlockEntityTankComputer;
//import dan200.computercraft.api.peripheral.IPeripheral;
//import net.minecraftforge.common.capabilities.Capability;
//import net.minecraftforge.common.capabilities.CapabilityManager;
//import net.minecraftforge.common.capabilities.CapabilityToken;
//
//public class CompatibilityComputerCraft {
//
//    public static Capability<IPeripheral> CAPABILITY_PERIPHERAL;
//
//    public static void initialize() {
//        CAPABILITY_PERIPHERAL = CapabilityManager.get(new CapabilityToken<>() {
//        });
//    }
//
//    public static TankComputerPeripheral getPeripheral(BlockEntityTankComputer tankComputer) {
//        return new TankComputerPeripheral(tankComputer);
//    }
//}
