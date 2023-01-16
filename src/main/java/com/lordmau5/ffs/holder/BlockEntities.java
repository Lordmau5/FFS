package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.tile.valves.TileEntityFluidValve;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

//@ObjectHolder(FancyFluidStorage.MODID)
public class BlockEntities {

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FancyFluidStorage.MOD_ID);

    public static final RegistryObject<BlockEntityType<TileEntityFluidValve>> tileEntityFluidValve = register("fluid_valve",
            () -> BlockEntityType.Builder.of(TileEntityFluidValve::new, Blocks.fluidValve.get()).build(null));

//    public static TileEntityType<TileEntityTankComputer> tileEntityTankComputer = null;

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(final String name, final Supplier<BlockEntityType<T>> tile) {
        return BLOCK_ENTITIES.register(name, tile);
    }

    public static void register() {
        BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
