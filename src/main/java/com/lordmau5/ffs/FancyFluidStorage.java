package com.lordmau5.ffs;

import com.lordmau5.ffs.compat.Compatibility;
import com.lordmau5.ffs.config.ServerConfig;
import com.lordmau5.ffs.holder.BlockEntities;
import com.lordmau5.ffs.holder.Blocks;
import com.lordmau5.ffs.holder.Items;
import com.lordmau5.ffs.holder.Sounds;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.util.Config;
import com.lordmau5.ffs.util.GenericUtil;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FancyFluidStorage.MOD_ID)
public class FancyFluidStorage {
    public static final String MOD_ID = "ffs";

    public FancyFluidStorage() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
        bus.addListener(this::setupClient);

        Blocks.register();
        Items.register();
        BlockEntities.register();
        Sounds.register();

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
}
