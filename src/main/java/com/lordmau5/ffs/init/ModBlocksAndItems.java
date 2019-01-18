package com.lordmau5.ffs.init;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.block.tanktiles.BlockTankComputer;
import com.lordmau5.ffs.block.valves.BlockFluidValve;
import com.lordmau5.ffs.item.ItemTit;
import com.lordmau5.ffs.item.ItemTitEgg;
import com.lordmau5.ffs.tile.tanktiles.TileEntityTankComputer;
import com.lordmau5.ffs.tile.valves.TileEntityFluidValve;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by Lordmau5 on 19.06.2016.
 */
public class ModBlocksAndItems {

    public static void preInit(FMLPreInitializationEvent event) {
        registerBlockWithItemAndTile(FancyFluidStorage.blockFluidValve = new BlockFluidValve(), TileEntityFluidValve.class);
        registerBlockWithItemAndTile(FancyFluidStorage.blockTankComputer = new BlockTankComputer(), TileEntityTankComputer.class);

        ForgeRegistries.ITEMS.register(FancyFluidStorage.itemTitEgg = new ItemTitEgg());
        ForgeRegistries.ITEMS.register(FancyFluidStorage.itemTit = new ItemTit());
    }

    private static void registerBlockWithItemAndTile(Block block, Class<? extends TileEntity> tileClass) {
        ForgeRegistries.BLOCKS.register(block);
        ForgeRegistries.ITEMS.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        GameRegistry.registerTileEntity(tileClass, block.getUnlocalizedName());
    }

}
