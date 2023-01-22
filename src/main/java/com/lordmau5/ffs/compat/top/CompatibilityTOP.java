package com.lordmau5.ffs.compat.top;

import net.minecraftforge.fml.InterModComms;

public class CompatibilityTOP {
    public static void register() {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", DataProviderTOP::new);
    }
}
