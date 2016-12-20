package com.lordmau5.ffs.compat.top;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.block.valves.BlockMetaphaser;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import mcjty.theoneprobe.api.*;
import mcjty.theoneprobe.apiimpl.ProbeConfig;
import mcjty.theoneprobe.apiimpl.elements.ElementProgress;
import mcjty.theoneprobe.config.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import javax.annotation.Nullable;

/**
 * Created by Lordmau5 on 07.10.2016.
 */
public class TOPCompatibility {
	private static boolean registered;

	public static void register() {
		if(registered) {
			return;
		}

		registered = true;
		FMLInterModComms.sendFunctionMessage("theoneprobe", "getTheOneProbe", "com.lordmau5.ffs.compat.top.TOPCompatibility$GetTheOneProbe");
	}

	public static class GetTheOneProbe implements com.google.common.base.Function<ITheOneProbe, Void> {

		public static ITheOneProbe probe;

		@Nullable
		@Override
		public Void apply(ITheOneProbe theOneProbe) {
			probe = theOneProbe;
			probe.registerProvider(new IProbeInfoProvider() {
				@Override
				public String getID() {
					return FancyFluidStorage.MODID;
				}

				@Override
				public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
//					if(blockState.getBlock() instanceof BlockFluidValve) {
//						return;
//					}

					AbstractTankValve valve = null;
					if(blockState.getBlock() instanceof BlockMetaphaser) {
						valve = (AbstractTankValve) world.getTileEntity(data.getPos());
					}
					else if(FancyFluidStorage.tankManager.isPartOfTank(world, data.getPos())) {
						valve = FancyFluidStorage.tankManager.getValveForBlock(world, data.getPos());
					}

					if(valve != null) {
						IProbeInfo vert = probeInfo.vertical();
						vert.text(TextFormatting.GRAY + "" + TextFormatting.ITALIC + "Part of a tank");
						addFluidInfo(vert, Config.getDefaultConfig(), valve.getTankConfig().getFluidStack(), valve.getTankConfig().getFluidCapacity());
					}
				}

				private void addFluidInfo(IProbeInfo probeInfo, ProbeConfig config, FluidStack fluidStack, int maxContents) {
					int contents = fluidStack == null ? 0 : fluidStack.amount;
					if(fluidStack != null) {
						probeInfo.text("Liquid: " + fluidStack.getLocalizedName());
					}
					if(config.getTankMode() == 1) {
						probeInfo.progress(contents, maxContents,
										   probeInfo.defaultProgressStyle()
												   .suffix("mB")
												   .filledColor(Config.tankbarFilledColor)
												   .alternateFilledColor(Config.tankbarAlternateFilledColor)
												   .borderColor(Config.tankbarBorderColor)
												   .numberFormat(Config.tankFormat));
					}
					else {
						probeInfo.text(TextFormatting.GREEN + ElementProgress.format(contents, Config.tankFormat, "mB"));
					}
				}
			});
			return null;
		}
	}
}
