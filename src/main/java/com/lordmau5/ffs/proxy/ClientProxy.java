package com.lordmau5.ffs.proxy;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.client.ValveRenderer;
import com.lordmau5.ffs.compat.waila.WailaPluginTank;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

/**
 * Created by Dustin on 29.06.2015.
 */
public class ClientProxy extends CommonProxy {
    public void preInit() {
        ClientRegistry.bindTileEntitySpecialRenderer(AbstractTankValve.class, new ValveRenderer());

        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(FancyFluidStorage.blockFluidValve), 0, new ModelResourceLocation("ffs:block_fluid_valve", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(FancyFluidStorage.blockTankComputer), 0, new ModelResourceLocation("ffs:block_tank_computer", "inventory"));

        ModelLoader.setCustomModelResourceLocation(FancyFluidStorage.itemTit, 0, new ModelResourceLocation(FancyFluidStorage.itemTit.getRegistryName().toString()));
        ModelLoader.setCustomModelResourceLocation(FancyFluidStorage.itemTitEgg, 0, new ModelResourceLocation(FancyFluidStorage.itemTitEgg.getRegistryName().toString()));
    }

    public void init(FMLInitializationEvent event) {
//        MinecraftForge.EVENT_BUS.register(new OverlayRenderHandler());

        if ( Loader.isModLoaded("waila") ) {
            WailaPluginTank.init();
        }

        super.init(event);
    }
}
