package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.block.valves.BlockFluidValve;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Blocks {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FancyFluidStorage.MOD_ID);

    public static final RegistryObject<Block> fluidValve = register("fluid_valve", BlockFluidValve::new);

//    public static final BlockTankComputer tankComputer = null;

    private static <T extends Block> RegistryObject<T> register(final String name, final Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @SubscribeEvent
    public static void onRegisterItems(final RegisterEvent event) {
        if (!event.getRegistryKey().equals(ForgeRegistries.Keys.ITEMS)) return;

        BLOCKS.getEntries().forEach( (blockRegistryObject) -> {
            Block block = blockRegistryObject.get();
            Item.Properties properties = new Item.Properties().tab(ModCreativeTab.instance);
            Supplier<Item> blockItemFactory = () -> new BlockItem(block, properties);
            event.register(ForgeRegistries.Keys.ITEMS, blockRegistryObject.getId(), blockItemFactory);
        });
    }
}
