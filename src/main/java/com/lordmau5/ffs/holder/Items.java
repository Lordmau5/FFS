package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.item.ItemTit;
import com.lordmau5.ffs.item.ItemTitEgg;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ObjectHolder;

import java.util.function.Supplier;

@ObjectHolder(FancyFluidStorage.MODID)
public class Items {

    @ObjectHolder("tit")
    public static final ItemTit tit = null;

    @ObjectHolder("tit_egg")
    public static final ItemTitEgg titEgg = null;

    // Initialization
    public static void registerAll() {
        register("tit", () -> new ItemTit(new Item.Properties()));
        register("tit_egg", () -> new ItemTitEgg(new Item.Properties()));

        register("fluid_valve", () -> new BlockItem(Blocks.fluidValve, new Item.Properties().group(FancyFluidStorage.ITEM_GROUP)));
    }

    public static RegistryObject<Item> register(final String name, final Supplier<Item> supplier) {
        return FancyFluidStorage.ITEMS.register(name, supplier);
    }

}
