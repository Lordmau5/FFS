package com.lordmau5.ffs.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;

/**
 * Created by Lordmau5 on 19.06.2016.
 */
public interface IProxy {
    void preInit();

    void init(FMLInitializationEvent event);
}
