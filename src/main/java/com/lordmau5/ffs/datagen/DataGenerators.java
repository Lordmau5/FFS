package com.lordmau5.ffs.datagen;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(modid = FancyFluidStorage.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        if (event.includeServer()) {
            generator.addProvider(new FFSRecipes(generator));
            generator.addProvider(new FFSBlockTags(generator, event.getExistingFileHelper()));
        }

        if (event.includeClient()) {
            generator.addProvider(new FFSBlockStates(generator, event.getExistingFileHelper()));
            generator.addProvider(new FFSItemModels(generator, event.getExistingFileHelper()));
            generator.addProvider(new FFSLanguageProvider(generator, "en_us"));
        }
    }
}
