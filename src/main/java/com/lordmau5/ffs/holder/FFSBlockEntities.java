package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.blockentity.tanktiles.BlockEntityTankComputer;
import com.lordmau5.ffs.blockentity.valves.BlockEntityFluidValve;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class FFSBlockEntities {


    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, FancyFluidStorage.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityFluidValve>> tileEntityFluidValve = BLOCK_ENTITIES.register("fluid_valve",
            () -> BlockEntityType.Builder.of(BlockEntityFluidValve::new, FFSBlocks.fluidValve.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BlockEntityTankComputer>> tankComputer = BLOCK_ENTITIES.register("tank_computer",
            () -> BlockEntityType.Builder.of(BlockEntityTankComputer::new, FFSBlocks.tankComputer.get()).build(null));


    public static void register() {
        BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
