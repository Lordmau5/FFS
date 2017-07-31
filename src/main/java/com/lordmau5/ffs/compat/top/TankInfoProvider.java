package com.lordmau5.ffs.compat.top;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.block.valves.BlockMetaphaser;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.apiimpl.ProbeConfig;
import mcjty.theoneprobe.apiimpl.elements.ElementProgress;
import mcjty.theoneprobe.config.Config;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

/**
 * Created by Gigabit101 on 31/07/2017.
 */
public class TankInfoProvider implements IProbeInfoProvider
{
    @Override
    public String getID()
    {
        return "ffstank";
    }

    @Override
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, EntityPlayer entityPlayer, World world, IBlockState iBlockState, IProbeHitData iProbeHitData)
    {
        AbstractTankValve valve = null;
        if (iBlockState.getBlock() instanceof BlockMetaphaser)
        {
            valve = (AbstractTankValve) world.getTileEntity(iProbeHitData.getPos());
        } else if (FancyFluidStorage.tankManager.isPartOfTank(world, iProbeHitData.getPos()))
        {
            valve = FancyFluidStorage.tankManager.getValveForBlock(world, iProbeHitData.getPos());
        }

        if (valve != null)
        {
            IProbeInfo vert = iProbeInfo.vertical();
            vert.text(TextFormatting.GRAY + "" + TextFormatting.ITALIC + "Part of a tank");
            addFluidInfo(vert, Config.getDefaultConfig(), valve.getTankConfig().getFluidStack(), valve.getTankConfig().getFluidCapacity());
        }
    }

    private void addFluidInfo(IProbeInfo probeInfo, ProbeConfig config, FluidStack fluidStack, int maxContents)
    {
        int contents = fluidStack == null ? 0 : fluidStack.amount;
        if (fluidStack != null)
        {
            probeInfo.text("Liquid: " + fluidStack.getLocalizedName());
        }
        if (config.getTankMode() == 1)
        {
            probeInfo.progress(contents, maxContents, probeInfo.defaultProgressStyle().suffix("mB").filledColor(Config.tankbarFilledColor).alternateFilledColor(Config.tankbarAlternateFilledColor)
                            .borderColor(Config.tankbarBorderColor).numberFormat(Config.tankFormat));
        }
        else
        {
            probeInfo.text(TextFormatting.GREEN + ElementProgress.format(contents, Config.tankFormat, "mB"));
        }
    }
}
