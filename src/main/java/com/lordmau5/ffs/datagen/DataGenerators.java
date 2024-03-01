package com.lordmau5.ffs.datagen;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod.EventBusSubscriber(modid = FancyFluidStorage.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(event.includeServer(), new FFSRecipes(generator, event.getLookupProvider()));
        generator.addProvider(event.includeServer(), new FFSBlockTags(generator.getPackOutput(), event.getLookupProvider(), FancyFluidStorage.MOD_ID, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new FFSBlockStates(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new FFSItemModels(generator, event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new FFSLanguageProvider(generator, "en_us"));
    }
}
