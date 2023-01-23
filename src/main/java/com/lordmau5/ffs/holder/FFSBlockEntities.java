package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.blockentity.tanktiles.BlockEntityTankComputer;
import com.lordmau5.ffs.blockentity.valves.BlockEntityFluidValve;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class FFSBlockEntities {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, FancyFluidStorage.MOD_ID);

    public static final RegistryObject<BlockEntityType<BlockEntityFluidValve>> tileEntityFluidValve = register("fluid_valve",
            () -> BlockEntityType.Builder.of(BlockEntityFluidValve::new, FFSBlocks.fluidValve.get()).build(null));

    public static final RegistryObject<BlockEntityType<BlockEntityTankComputer>> tankComputer = register("tank_computer",
            () -> BlockEntityType.Builder.of(BlockEntityTankComputer::new, FFSBlocks.tankComputer.get()).build(null));

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(final String name, final Supplier<BlockEntityType<T>> tile) {
        return BLOCK_ENTITIES.register(name, tile);
    }

    public static void register() {
        BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
