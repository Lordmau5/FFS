package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.block.tanktiles.BlockTankComputer;
import com.lordmau5.ffs.block.valves.BlockFluidValve;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FFSBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, FancyFluidStorage.MOD_ID);

    public static final DeferredHolder<Block, Block> fluidValve = register("fluid_valve", BlockFluidValve::new);

    public static final  DeferredHolder<Block, Block> tankComputer = register("tank_computer", BlockTankComputer::new);

    private static <T extends Block>  DeferredHolder<Block, Block> register(final String name, final Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @SubscribeEvent
    public static void onRegisterItems(final RegisterEvent event) {
        if (!event.getRegistryKey().equals(BuiltInRegistries.ITEM.key())) return;

        BLOCKS.getEntries().forEach((blockRegistryObject) -> {
            Block block = blockRegistryObject.get();
            Item.Properties properties = new Item.Properties();
            Supplier<Item> blockItemFactory = () -> new BlockItem(block, properties);
            event.register(BuiltInRegistries.ITEM.key(), blockRegistryObject.getId(), blockItemFactory);
        });
    }
}
