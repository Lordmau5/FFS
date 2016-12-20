package com.lordmau5.ffs.proxy;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.compat.Compatibility;
import com.lordmau5.ffs.compat.oc.OCCompatibility;
import com.lordmau5.ffs.compat.top.TOPCompatibility;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
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

		if(Loader.isModLoaded("ComputerCraft")) {
			//new CCPeripheralProvider().register();
		}

		if(Compatibility.INSTANCE.isOpenComputersLoaded) {
			new OCCompatibility().init();
		}

		if(Compatibility.INSTANCE.isTOPLoaded) {
			TOPCompatibility.register();
		}
	}
}
