package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.item.ItemTit;
import com.lordmau5.ffs.item.ItemTitEgg;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class FFSItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, FancyFluidStorage.MOD_ID);

    public static final DeferredHolder<Item, Item> tit = register("tit", () -> new ItemTit(new Item.Properties()));
    public static final DeferredHolder<Item, Item> titEgg = register("tit_egg", () -> new ItemTitEgg(new Item.Properties()));

    private static <T extends Item> DeferredHolder<Item, Item> register(final String name, final Supplier<T> item) {
        return ITEMS.register(name, item);
    }

    public static void register() {
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
