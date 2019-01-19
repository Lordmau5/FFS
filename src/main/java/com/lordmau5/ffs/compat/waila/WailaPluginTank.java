package com.lordmau5.ffs.compat.waila;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.tile.abstracts.AbstractTankTile;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import com.lordmau5.ffs.tile.interfaces.INameableTile;
import com.lordmau5.ffs.util.GenericUtil;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.event.FMLInterModComms;

import java.util.List;

/**
 * Created by max on 7/4/15.
 */
@Optional.Interface(iface = "mcp.mobius.waila.api.IWailaDataProvider", modid = "waila")
public class WailaPluginTank implements IWailaDataProvider {

    public static void init() {
        FMLInterModComms.sendMessage("waila", "register", WailaPluginTank.class.getName() + ".registerPlugin");
    }

    @Optional.Method(modid = "waila")
    public static void registerPlugin(IWailaRegistrar registrar) {
        WailaPluginTank instance = new WailaPluginTank();

        registrar.registerBodyProvider(instance, Block.class);
    }

    @Optional.Method(modid = "waila")
    @Override
    public ItemStack getWailaStack(IWailaDataAccessor iWailaDataAccessor, IWailaConfigHandler iWailaConfigHandler) {
        // Unused, implemented because of the interface
        return null;
    }

    @Optional.Method(modid = "waila")
    @Override
    public List<String> getWailaBody(ItemStack itemStack, List<String> list, IWailaDataAccessor iWailaDataAccessor, IWailaConfigHandler iWailaConfigHandler) {
        TileEntity te = iWailaDataAccessor.getTileEntity();
        AbstractTankValve valve = null;
        if ( te instanceof AbstractTankValve ) { // Continue with Valve stuff
            valve = (AbstractTankValve) te;
        } else if ( te instanceof AbstractTankTile ) {
            valve = ((AbstractTankTile) te).getMasterValve();
            if ( valve != null && valve.isValid() )
                list.add("Part of a tank");
        }

        if ( valve == null && FancyFluidStorage.tankManager.isPartOfTank(iWailaDataAccessor.getWorld(), iWailaDataAccessor.getPosition()) ) {
            valve = FancyFluidStorage.tankManager.getValveForBlock(iWailaDataAccessor.getWorld(), iWailaDataAccessor.getPosition());
            if ( valve != null && valve.isValid() ) {
                list.add("Part of a tank");
            }
        }

        if ( valve == null ) {
            return list;
        }

        int fluidAmount = valve.getTankConfig().getFluidAmount();
        int capacity = valve.getTankConfig().getFluidCapacity();

        if ( !valve.isValid() ) {
            list.add("Invalid tank");
            return list;
        }

        if ( te instanceof INameableTile ) {
            list.add("Name: " + TextFormatting.ITALIC + ((INameableTile) te).getTileName() + TextFormatting.RESET);
        }

        if ( fluidAmount == 0 ) {
            list.add("Fluid: None");
            list.add("Amount: 0/" + GenericUtil.intToFancyNumber(capacity) + " mB");
        } else {
            String fluid = valve.getTankConfig().getFluidStack().getLocalizedName();
            list.add("Fluid: " + fluid);
            list.add("Amount: " + GenericUtil.intToFancyNumber(fluidAmount) + "/" + GenericUtil.intToFancyNumber(capacity) + " mB");
        }

        if ( valve.getTankConfig().isFluidLocked() ) {
            list.add("Fluid locked to: " + valve.getTankConfig().getLockedFluid().getLocalizedName());
        }
        return list;
    }

    @Optional.Method(modid = "waila")
    @Override
    public List<String> getWailaHead(ItemStack itemStack, List<String> list, IWailaDataAccessor iWailaDataAccessor, IWailaConfigHandler iWailaConfigHandler) {
        // Unused, implemented because of the interface
        return list;
    }

    @Optional.Method(modid = "waila")
    @Override
    public List<String> getWailaTail(ItemStack itemStack, List<String> list, IWailaDataAccessor iWailaDataAccessor, IWailaConfigHandler iWailaConfigHandler) {
        // Unused, implemented because of the interface
        return list;
    }

    @Optional.Method(modid = "waila")
    @Override
    public NBTTagCompound getNBTData(EntityPlayerMP entityPlayerMP, TileEntity tileEntity, NBTTagCompound nbtTagCompound, World world, BlockPos pos) {
        // Unused, implemented because of the interface
        return nbtTagCompound;
    }
}
