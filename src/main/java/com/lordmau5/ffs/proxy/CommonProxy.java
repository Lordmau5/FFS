package com.lordmau5.ffs.proxy;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.compat.waila.WailaPluginTank;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.relauncher.libraries.ModList;

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
