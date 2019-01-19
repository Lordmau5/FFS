package com.lordmau5.ffs.proxy;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

/**
 * Created by Dustin on 29.06.2015.
 */
public class CommonProxy implements IProxy {
    public void preInit() {
    }

    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new FancyFluidStorage());
        MinecraftForge.EVENT_BUS.register(FancyFluidStorage.tankManager);
    }
}
