package com.lordmau5.ffs.config;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("CanBeFinal")
@Config(modid = FancyFluidStorage.MODID)
public class ModConfig {
    @Config.Name("General")
    @Config.Comment("General settings for configuring the mod.")
    public static final General general = new General();

    public static class General {
        @Config.Name("mB Per Virtual Tank")
        @Config.Comment({
                "How many millibuckets can each block within the tank store?",
                "(Has to be higher than 1!)"
        })
        @Config.RangeInt(min = 1)
        public int mbPerTankBlock = 16000;

        @Config.Name("Maximum Air Blocks")
        @Config.Comment({
                "Define the maximum number of air blocks a tank can have.",
                "2197 have been tested to not cause any noticeable lag."
        })
        @Config.RangeInt(min = 3, max = 2197)
        public int maxAirBlocks = 2197;

        @Config.Name("Block Blacklist")
        @Config.Comment({
                "Define a blacklist of blocks that can't be used as a frame in a tank.",
                "Registry names need to be used, example: minecraft:stone, minecraft:wool@3"
        })
        public String[] blockBlacklist = {};

        @Config.Name("Block Blacklist Invert")
        @Config.Comment({
                "When enabled, this will turn the blacklist into a whitelist."
        })
        public boolean blockBlacklistInvert = false;
    }

    @Mod.EventBusSubscriber(modid = FancyFluidStorage.MODID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if ( event.getModID().equals(FancyFluidStorage.MODID) ) {
                ConfigManager.sync(FancyFluidStorage.MODID, Config.Type.INSTANCE);
            }
        }
    }
}