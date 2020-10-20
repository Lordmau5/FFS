package com.lordmau5.ffs.config;

import com.lordmau5.ffs.util.Config;

public class ServerConfig {
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

        @Config.Name("Block Blacklist Invert")
        @Config.Comment({
                "When enabled, this will turn the blacklist into a whitelist."
        })
        public boolean blockBlacklistInvert = false;
    }
}