package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.block.tanktiles.BlockTankComputer;
import com.lordmau5.ffs.block.valves.BlockFluidValve;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FFSBlocks {

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, FancyFluidStorage.MOD_ID);

    public static final RegistryObject<Block> fluidValve = register("fluid_valve", BlockFluidValve::new);

    public static final RegistryObject<Block> tankComputer = register("tank_computer", BlockTankComputer::new);

    private static <T extends Block> RegistryObject<T> register(final String name, final Supplier<T> block) {
        return BLOCKS.register(name, block);
    }

    public static void register() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @SubscribeEvent
    public static void onRegisterItems(final RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();

        BLOCKS.getEntries().stream().map(RegistryObject::get).forEach( (block) -> {
            final Item.Properties properties = new Item.Properties().tab(ModCreativeTab.instance);
            final BlockItem blockItem = new BlockItem(block, properties);
            blockItem.setRegistryName(block.getRegistryName());
            registry.register(blockItem);
        });
    }
}
