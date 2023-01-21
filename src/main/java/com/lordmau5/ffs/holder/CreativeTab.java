package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FancyFluidStorage.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CreativeTab {

    public static CreativeModeTab ffsCreativeTab;

    @SubscribeEvent
    public static void registerTabs(CreativeModeTabEvent.Register event) {
        ffsCreativeTab = event.registerCreativeModeTab(new ResourceLocation(FancyFluidStorage.MOD_ID, "creative_tab"), builder -> builder
                .icon(() -> new ItemStack(Blocks.fluidValve.get()))
                .displayItems((featureFlags, output, hasOp) -> {
                    if (Blocks.fluidValve != null) {
                        output.accept(Blocks.fluidValve.get());
                    }
                    if (Blocks.tankComputer != null) {
                        output.accept(Blocks.tankComputer.get());
                    }
                    if (Items.titEgg != null) {
                        output.accept(Items.titEgg.get());
                    }
                    if (Items.tit != null) {
                        output.accept(Items.tit.get());
                    }
                })
        );
    }
}
