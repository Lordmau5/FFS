package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.block.valves.BlockFluidValve;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.ObjectHolder;

import java.util.function.Supplier;

@ObjectHolder(FancyFluidStorage.MODID)
public class Blocks {

//    @ObjectHolder("fluid_valve")
    public static BlockFluidValve fluidValve;

//    @ObjectHolder("tank_computer")
//    public static final BlockTankComputer tankComputer = null;

    // Initialization
    public static void registerAll() {
        fluidValve = new BlockFluidValve();

        register("fluid_valve", () -> fluidValve);
//        register("tank_computer", () -> new BlockTankComputer());
    }

    public static RegistryObject<Block> register(final String name, final Supplier<Block> supplier) {
        return FancyFluidStorage.BLOCKS.register(name, supplier);
    }

}
