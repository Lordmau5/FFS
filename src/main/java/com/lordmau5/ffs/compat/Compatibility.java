package com.lordmau5.ffs.compat;

import com.lordmau5.ffs.compat.top.TOPCompatibility;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ModList;

public class Compatibility {

    public static boolean isTOPLoaded;

    public static void init() {
        isTOPLoaded = ModList.get().isLoaded("theoneprobe");
    }

    @OnlyIn(Dist.CLIENT)
    public static void initClient() {
        if (isTOPLoaded) {
            TOPCompatibility.register();
        }
    }

}
