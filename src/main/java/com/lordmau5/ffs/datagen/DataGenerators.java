package com.lordmau5.ffs.datagen;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FancyFluidStorage.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(event.includeServer(), new FFSRecipes(generator));
        generator.addProvider(event.includeServer(), new FFSBlockTags(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new FFSBlockStates(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new FFSItemModels(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new FFSLanguageProvider(generator, "en_us"));
    }
}
