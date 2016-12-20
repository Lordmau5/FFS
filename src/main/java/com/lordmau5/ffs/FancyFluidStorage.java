package com.lordmau5.ffs;

import com.lordmau5.ffs.client.FluidHelper;
import com.lordmau5.ffs.client.OverlayRenderHandler;
import com.lordmau5.ffs.compat.Compatibility;
import com.lordmau5.ffs.config.Config;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.proxy.GuiHandler;
import com.lordmau5.ffs.proxy.IProxy;
import com.lordmau5.ffs.util.GenericUtil;
import com.lordmau5.ffs.util.ModBlocksAndItems;
import com.lordmau5.ffs.util.TankManager;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by Dustin on 28.06.2015.
 */
@Mod(modid = FancyFluidStorage.MODID, name = "Fancy Fluid Storage", dependencies = "after:waila;after:OpenComputers;after:ComputerCraft;after:chisel", guiFactory = "com.lordmau5.ffs.config.GuiFactoryFFS")
public class FancyFluidStorage {

	public static final String MODID = "ffs";

	public static Block blockFluidValve;
	public static Block blockMetaphaser;
	public static Block blockTankComputer;

	public static Block blockFrameNormalizer;
	public static Block blockFrameNormalizerOpaque;

	public static Item itemTitEgg;
	public static Item itemTit;

	public static Fluid fluidMetaphasedFlux;

	public static final TankManager tankManager = new TankManager();

	@Mod.Instance(MODID)
	public static FancyFluidStorage INSTANCE;

	@SidedProxy(clientSide = "com.lordmau5.ffs.proxy.ClientProxy", serverSide = "com.lordmau5.ffs.proxy.CommonProxy")
	private static IProxy PROXY;

	@SuppressWarnings("deprecation")
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Compatibility.INSTANCE.init();

		new Config(new Configuration(event.getSuggestedConfigurationFile()));

		ModBlocksAndItems.preInit(event);

		NetworkRegistry.INSTANCE.registerGuiHandler(FancyFluidStorage.INSTANCE, new GuiHandler());
		NetworkHandler.registerChannels(event.getSide());

		PROXY.preInit();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {

		GameRegistry.addRecipe(new ItemStack(blockFluidValve), "IGI", "GBG", "IGI",
							   'I', Items.IRON_INGOT,
							   'G', Blocks.IRON_BARS,
							   'B', Items.BUCKET);

		GameRegistry.addRecipe(new ItemStack(blockTankComputer), "IGI", "GBG", "IGI",
							   'I', Items.IRON_INGOT,
							   'G', Blocks.IRON_BARS,
							   'B', Blocks.REDSTONE_BLOCK);

		if(Compatibility.INSTANCE.isEnergyModSupplied()) {
			GameRegistry.addRecipe(new ItemStack(blockMetaphaser), "IGI", "GBG", "IGI",
								   'I', Items.IRON_INGOT,
								   'G', Blocks.IRON_BARS,
								   'B', Items.COMPARATOR);
		}

		GameRegistry.addShapelessRecipe(new ItemStack(itemTitEgg), blockFluidValve, Items.EGG);
		GameRegistry.addSmelting(itemTitEgg, new ItemStack(itemTit), 0);

		PROXY.init(event);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		GenericUtil.init();

		ForgeChunkManager.setForcedChunkLoadingCallback(INSTANCE, (tickets, world) -> {
			if(tickets != null && tickets.size() > 0)
				GenericUtil.initChunkLoadTicket(world, tickets.get(0));
		});
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void loadTextures(TextureStitchEvent.Pre event) {
		FluidHelper.initTextures(event.getMap());

		OverlayRenderHandler.overlayTexture = event.getMap().registerSprite(new ResourceLocation("ffs", "blocks/overlay/tank_overlay_anim"));
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if(!event.getModID().equals(MODID))
			return;

		Config.syncConfig();
	}

	@SubscribeEvent
	public void onWorldUnload(WorldEvent.Unload event) {
		if(event.getWorld().isRemote) {
			System.out.println("Unloading dimension " + event.getWorld().provider.getDimension());
			FancyFluidStorage.tankManager.removeAllForDimension(event.getWorld().provider.getDimension());
		}
	}

	@Mod.EventHandler
	public void remapBlocks(FMLMissingMappingsEvent event) { // TODO: Remove legacy support
		for(FMLMissingMappingsEvent.MissingMapping mapping : event.getAll()) {
			if(mapping != null) {
				if(mapping.name.equals("ffs:blockFluidValve")) {
					if(mapping.type == GameRegistry.Type.BLOCK) {
						mapping.remap(blockFluidValve);
					}
					else if(mapping.type == GameRegistry.Type.ITEM) {
						mapping.remap(Item.getItemFromBlock(blockFluidValve));
					}
				}
				if(mapping.name.equals("ffs:blockMetaphaser")) {
					if(mapping.type == GameRegistry.Type.BLOCK) {
						mapping.remap(blockMetaphaser);
					}
					else if(mapping.type == GameRegistry.Type.ITEM) {
						mapping.remap(Item.getItemFromBlock(blockMetaphaser));
					}
				}
				if(mapping.name.equals("ffs:blockTankComputer")) {
					if(mapping.type == GameRegistry.Type.BLOCK) {
						mapping.remap(blockTankComputer);
					}
					else if(mapping.type == GameRegistry.Type.ITEM) {
						mapping.remap(Item.getItemFromBlock(blockTankComputer));
					}
				}
				if(mapping.name.equals("ffs:blockTankFrame")) {
					if(mapping.type == GameRegistry.Type.BLOCK) {
						mapping.remap(blockFrameNormalizer);
					}
//					else if(mapping.type == GameRegistry.Type.ITEM) {
//						mapping.remap(Item.getItemFromBlock(blockFrameNormalizer));
//					}
				}
				if(mapping.name.equals("ffs:blockTankFrameOpaque")) {
					if(mapping.type == GameRegistry.Type.BLOCK) {
						mapping.remap(blockFrameNormalizerOpaque);
					}
//					else if(mapping.type == GameRegistry.Type.ITEM) {
//						mapping.remap(Item.getItemFromBlock(blockFrameNormalizerOpaque));
//					}
				}
			}
		}
	}
}
