package com.lordmau5.ffs.proxy;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.client.ValveRenderer;
import com.lordmau5.ffs.compat.Compatibility;
import com.lordmau5.ffs.holder.Blocks;
import com.lordmau5.ffs.holder.TileEntities;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fmlclient.registry.ClientRegistry;

@OnlyIn(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

    @Override
    public void registerEvents(IEventBus modBus) {
        super.registerEvents(modBus);

        modBus.addListener(this::clientSetup);
    }

    private void clientSetup(final EntityRenderersEvent.RegisterRenderers event) {
        Compatibility.initClient();

        ItemBlockRenderTypes.setRenderLayer(Blocks.fluidValve, RenderType.cutout());

        event.registerBlockEntityRenderer(TileEntities.tileEntityFluidValve.get(), ValveRenderer::new);
    }
}
