package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.client.ValveRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class FFSBlockRendererManager {
    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(FFSBlockEntities.tileEntityFluidValve.get(), ValveRenderer::new);
    }
}
