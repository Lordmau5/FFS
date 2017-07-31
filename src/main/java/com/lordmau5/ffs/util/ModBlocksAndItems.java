package com.lordmau5.ffs.util;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.block.fluid.FluidMetaphasedFlux;
import com.lordmau5.ffs.block.tanktiles.BlockTankComputer;
import com.lordmau5.ffs.block.valves.BlockFluidValve;
import com.lordmau5.ffs.block.valves.BlockMetaphaser;
import com.lordmau5.ffs.item.ItemTit;
import com.lordmau5.ffs.item.ItemTitEgg;
import com.lordmau5.ffs.tile.tanktiles.TileEntityTankComputer;
import com.lordmau5.ffs.tile.valves.TileEntityFluidValve;
import com.lordmau5.ffs.tile.valves.TileEntityMetaphaser;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import reborncore.RebornRegistry;

/**
 * Created by Lordmau5 on 19.06.2016.
 */
public class ModBlocksAndItems
{

    public static void preInit(FMLPreInitializationEvent event)
    {
        registerBlockWithItemAndTile(FancyFluidStorage.blockFluidValve = new BlockFluidValve(), TileEntityFluidValve.class);
        registerBlockWithItemAndTile(FancyFluidStorage.blockMetaphaser = new BlockMetaphaser(), TileEntityMetaphaser.class);
        registerBlockWithItemAndTile(FancyFluidStorage.blockTankComputer = new BlockTankComputer(), TileEntityTankComputer.class);

        FancyFluidStorage.itemTitEgg = new ItemTitEgg();
        FancyFluidStorage.itemTit = new ItemTit();

        FluidRegistry.registerFluid(FancyFluidStorage.fluidMetaphasedFlux = new FluidMetaphasedFlux());
    }

    private static void registerBlockWithItemAndTile(Block block, Class<? extends TileEntity> tileClass)
    {
        RebornRegistry.registerBlock(block);
//		RebornRegistry.re(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        GameRegistry.registerTileEntity(tileClass, block.getUnlocalizedName());
    }

}
