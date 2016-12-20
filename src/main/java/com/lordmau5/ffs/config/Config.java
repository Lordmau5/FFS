package com.lordmau5.ffs.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

/**
 * Created by Lordmau5 on 10.11.2016.
 */
public class Config {

	public static Configuration config;

	public static int MB_PER_TANK_BLOCK = 16000;
	public static int MAX_AIR_BLOCKS = 2197;

	public static boolean TANK_OVERLAY_RENDER = true;
	public static int METAPHASED_FLUX_ENERGY_LOSS = 10;

	public Config(Configuration _config) {
		config = _config;
		config.load();
		syncConfig();
	}

	public static void syncConfig() {
		Property mbPerTankProp = config.get(Configuration.CATEGORY_GENERAL, "mbPerVirtualTank", 16000);
		mbPerTankProp.setComment("How many millibuckets can each block within the tank store? (Has to be higher than 1!)\nDefault: 16000");
		MB_PER_TANK_BLOCK = Math.max(1, Math.min(Integer.MAX_VALUE, mbPerTankProp.getInt(16000)));
		if(mbPerTankProp.getInt(16000) < 1 || mbPerTankProp.getInt(16000) > Integer.MAX_VALUE) {
			mbPerTankProp.set(16000);
		}

		Property maxAirBlocksProp = config.get(Configuration.CATEGORY_GENERAL, "maxAirBlocks", 2197);
		maxAirBlocksProp.setComment("Define the maximum number of air blocks a tank can have. 2197 have been tested to not cause any feelable lag.!\nMinimum: 1, Maximum: 2197\nDefault: 2197");
		MAX_AIR_BLOCKS = Math.max(1, Math.min(maxAirBlocksProp.getInt(2197), 2197));
		if(maxAirBlocksProp.getInt(2197) < 3 || maxAirBlocksProp.getInt(1) > 2197) {
			maxAirBlocksProp.set(2197);
		}

		Property tankOverlayRender = config.get(Configuration.CATEGORY_CLIENT, "tankOverlayRender", true);
		tankOverlayRender.setComment("Should a semi-transparent tank overlay be rendered on the tank when you look at it?\nDefault: true");
		TANK_OVERLAY_RENDER = tankOverlayRender.getBoolean(true);

		Property metaphasedFluxEnergyLoss = config.get(Configuration.CATEGORY_GENERAL, "metaphasedFluxEnergyLoss", 10);
		metaphasedFluxEnergyLoss.setComment("The amount of energy loss you have when you extract energy / metaphased flux from the tank.\nDefault: 10\nValue needs to be between 0 and 50!\n0 to disable.");
		METAPHASED_FLUX_ENERGY_LOSS = Math.max(0, metaphasedFluxEnergyLoss.getInt(10));
		if(metaphasedFluxEnergyLoss.getInt(10) < 0 || metaphasedFluxEnergyLoss.getInt(10) > 50) {
			metaphasedFluxEnergyLoss.set(10);
		}

		if(config.hasChanged()) {
			config.save();
		}
	}

}
