package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.tile.valves.TileEntityFluidValve;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ObjectHolder;

import java.util.function.Supplier;

@ObjectHolder(FancyFluidStorage.MODID)
public class TileEntities {

    public static TileEntityType<TileEntityFluidValve> tileEntityFluidValve = null;

//    public static TileEntityType<TileEntityTankComputer> tileEntityTankComputer = null;

    public static void registerAll() {
        tileEntityFluidValve =
                TileEntityType.Builder.create(TileEntityFluidValve::new, Blocks.fluidValve).build(null);

        register("tile_entity_type_fluid_valve", () -> tileEntityFluidValve);
    }

    private static RegistryObject<TileEntityType<?>> register(final String name, final Supplier<TileEntityType<?>> supplier) {
        return FancyFluidStorage.TILE_ENTITIES.register(name, supplier);
    }
}
