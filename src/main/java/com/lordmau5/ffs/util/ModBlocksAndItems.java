package com.lordmau5.ffs.util;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.block.fluid.FluidMetaphasedFlux;
import com.lordmau5.ffs.block.tanktiles.BlockFrameNormalizer;
import com.lordmau5.ffs.block.tanktiles.BlockTankComputer;
import com.lordmau5.ffs.block.valves.BlockFluidValve;
import com.lordmau5.ffs.block.valves.BlockMetaphaser;
import com.lordmau5.ffs.item.ItemTit;
import com.lordmau5.ffs.item.ItemTitEgg;
import com.lordmau5.ffs.tile.tanktiles.TileEntityFrameNormalizer;
import com.lordmau5.ffs.tile.tanktiles.TileEntityTankComputer;
import com.lordmau5.ffs.tile.valves.TileEntityFluidValve;
import com.lordmau5.ffs.tile.valves.TileEntityMetaphaser;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by Lordmau5 on 19.06.2016.
 */
public class ModBlocksAndItems {

	public static void preInit(FMLPreInitializationEvent event) {
		registerBlockWithItemAndTile(FancyFluidStorage.blockFluidValve = new BlockFluidValve(), TileEntityFluidValve.class, "tileEntityFluidValve");
		registerBlockWithItemAndTile(FancyFluidStorage.blockMetaphaser = new BlockMetaphaser(), TileEntityMetaphaser.class, "tileEntityMetaphaser");
		registerBlockWithItemAndTile(FancyFluidStorage.blockTankComputer = new BlockTankComputer(), TileEntityTankComputer.class, "tileEntityTankComputer");
		registerBlockWithItemAndTile(FancyFluidStorage.blockFrameNormalizer = new BlockFrameNormalizer(false), TileEntityFrameNormalizer.class, "tileEntityTankFrame"); // TODO: Remove legacy support
		registerBlockWithItemAndTile(FancyFluidStorage.blockFrameNormalizerOpaque = new BlockFrameNormalizer(true), TileEntityFrameNormalizer.class, "tileEntityTankFrame"); // TODO: Remove legacy support

		FancyFluidStorage.itemTitEgg = new ItemTitEgg();
		FancyFluidStorage.itemTit = new ItemTit();

		FluidRegistry.registerFluid(FancyFluidStorage.fluidMetaphasedFlux = new FluidMetaphasedFlux());
	}

	private static void registerBlockWithItemAndTile(Block block, Class<? extends TileEntity> tileClass, String oldTileName) {
		GameRegistry.register(block);
		GameRegistry.register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
		GameRegistry.registerTileEntityWithAlternatives(tileClass, block.getUnlocalizedName() + "TileEntity", oldTileName); // TODO: Remove legacy support
	}

}
