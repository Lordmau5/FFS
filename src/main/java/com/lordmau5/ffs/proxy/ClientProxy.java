package com.lordmau5.ffs.proxy;

import com.lordmau5.ffs.client.ValveRenderer;
import com.lordmau5.ffs.compat.Compatibility;
import com.lordmau5.ffs.holder.Blocks;
import com.lordmau5.ffs.holder.TileEntities;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void registerEvents(IEventBus modBus) {
        super.registerEvents(modBus);

        modBus.addListener(this::clientSetup);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        Compatibility.initClient();

        RenderTypeLookup.setRenderLayer(Blocks.fluidValve, RenderType.getCutout());

        ClientRegistry.bindTileEntityRenderer(TileEntities.tileEntityFluidValve, ValveRenderer::new);
    }
}
