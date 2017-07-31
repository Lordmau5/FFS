package com.lordmau5.ffs.compat.cnb;

import mod.chiselsandbits.api.ChiselsAndBitsAddon;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.api.IChiselsAndBitsAddon;
import net.minecraftforge.common.MinecraftForge;

/**
 * Created by Lordmau5 on 08.10.2016.
 */
@ChiselsAndBitsAddon
public class CNBAPIAccess implements IChiselsAndBitsAddon
{
    public static IChiselAndBitsAPI apiInstance;

    @Override
    public void onReadyChiselsAndBits(IChiselAndBitsAPI iChiselAndBitsAPI)
    {
        apiInstance = iChiselAndBitsAPI;

        MinecraftForge.EVENT_BUS.register(CNBCompatibility.INSTANCE);
    }
}
