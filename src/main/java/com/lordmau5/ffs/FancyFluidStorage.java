package com.lordmau5.ffs;

import com.lordmau5.ffs.compat.Compatibility;
import com.lordmau5.ffs.config.ServerConfig;
import com.lordmau5.ffs.holder.FFSBlockEntities;
import com.lordmau5.ffs.holder.FFSBlocks;
import com.lordmau5.ffs.holder.FFSItems;
import com.lordmau5.ffs.holder.FFSSounds;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.util.Config;
import com.lordmau5.ffs.util.GenericUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

@Mod(FancyFluidStorage.MOD_ID)
public class FancyFluidStorage {
    public static final String MOD_ID = "ffs";

    public FancyFluidStorage() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
        bus.addListener(this::setupClient);
        bus.addListener(this::registerCreativeTab);

        FFSBlocks.register();
        FFSItems.register();
        FFSBlockEntities.register();
        FFSSounds.register();

        GenericUtil.init();

        final ModLoadingContext context = ModLoadingContext.get();
        context.registerConfig(ModConfig.Type.SERVER, Config.walkClass(ServerConfig.class, bus));
    }

    private void setup(final FMLCommonSetupEvent event) {
        NetworkHandler.registerChannels();

        Compatibility.init();
    }

    private void setupClient(final FMLClientSetupEvent event) {
        Compatibility.initClient();
    }

    private void registerCreativeTab(RegisterEvent event) {
        ResourceKey<CreativeModeTab> TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(MOD_ID, "creative_tab"));
        event.register(Registries.CREATIVE_MODE_TAB, creativeModeTabRegisterHelper ->
        {
            creativeModeTabRegisterHelper.register(TAB, CreativeModeTab.builder().icon(() -> new ItemStack(FFSBlocks.fluidValve.get()))
                    .title(Component.translatable("itemGroup.ffs"))
                    .displayItems((params, output) -> {
                        FFSItems.ITEMS.getEntries().forEach(itemRegistryObject -> output.accept(new ItemStack(itemRegistryObject.get())));
                        FFSBlocks.BLOCKS.getEntries().forEach(itemRegistryObject -> output.accept(new ItemStack(itemRegistryObject.get())));
                    })
                    .build());
        });
    }
}
