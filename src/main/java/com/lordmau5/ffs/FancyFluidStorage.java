package com.lordmau5.ffs;

import com.lordmau5.ffs.compat.Compatibility;
import com.lordmau5.ffs.compat.top.TOPCompatibility;
import com.lordmau5.ffs.init.ModBlocksAndItems;
import com.lordmau5.ffs.init.ModRecipes;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.proxy.GuiHandler;
import com.lordmau5.ffs.proxy.IProxy;
import com.lordmau5.ffs.util.GenericUtil;
import com.lordmau5.ffs.util.TankManager;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * Created by Dustin on 28.06.2015.
 */
@Mod(modid = FancyFluidStorage.MODID, name = "Fancy Fluid Storage", dependencies = "after:waila")
public class FancyFluidStorage {
    public static final String MODID = "ffs";
    public static final TankManager tankManager = new TankManager();
    public static Block blockFluidValve;
    public static Block blockTankComputer;
    public static Item itemTitEgg;
    public static Item itemTit;
    @Mod.Instance(MODID)
    public static FancyFluidStorage INSTANCE;

    @SidedProxy(clientSide = "com.lordmau5.ffs.proxy.ClientProxy", serverSide = "com.lordmau5.ffs.proxy.CommonProxy")
    private static IProxy PROXY;

    @SuppressWarnings("deprecation")
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Compatibility.INSTANCE.init();

        ModBlocksAndItems.preInit(event);

        NetworkRegistry.INSTANCE.registerGuiHandler(FancyFluidStorage.INSTANCE, new GuiHandler());
        NetworkHandler.registerChannels(event.getSide());

        TOPCompatibility.register();

        PROXY.preInit();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ModRecipes.init();

        PROXY.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        GenericUtil.init();

        ForgeChunkManager.setForcedChunkLoadingCallback(INSTANCE, (tickets, world) -> {
            if ( tickets != null && tickets.size() > 0 )
                GenericUtil.initChunkLoadTicket(world, tickets.get(0));
        });
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if ( event.getWorld().isRemote ) {
            FancyFluidStorage.tankManager.removeAllForDimension(event.getWorld().provider.getDimension());
        }
    }
}
