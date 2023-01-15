package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.tile.valves.TileEntityFluidValve;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.ObjectHolder;

import java.util.function.Supplier;

@ObjectHolder(FancyFluidStorage.MODID)
public class TileEntities {

    public static RegistryObject<BlockEntityType<TileEntityFluidValve>> tileEntityFluidValve = null;

//    public static TileEntityType<TileEntityTankComputer> tileEntityTankComputer = null;

    public static void registerAll() {
        tileEntityFluidValve = register("tile_entity_type_fluid_valve", () -> BlockEntityType.Builder.of(TileEntityFluidValve::new, Blocks.fluidValve).build(null));
    }

    private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(final String name, final Supplier<BlockEntityType<T>> tile) {
        return FancyFluidStorage.BLOCK_ENTITIES.register(name, tile);
    }
}
