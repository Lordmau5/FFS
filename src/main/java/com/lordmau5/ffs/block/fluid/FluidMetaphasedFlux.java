package com.lordmau5.ffs.block.fluid;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;

/**
 * Created by Dustin on 19.02.2016.
 */
public class FluidMetaphasedFlux extends Fluid {

	private static final ResourceLocation still = new ResourceLocation("ffs", "blocks/power/fluid/metaphased_flux");
	private static final ResourceLocation flowing = new ResourceLocation("ffs", "blocks/power/fluid/metaphased_flux_flow");

	public FluidMetaphasedFlux() {
		super(FancyFluidStorage.MODID + ".metaphased_flux", still, flowing);
	}

	@Override
	public int getColor() {
		return 0xCCFFFFFF;
	}
}
