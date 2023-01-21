package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class Sounds {

    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, FancyFluidStorage.MOD_ID);

    public static final RegistryObject<SoundEvent> birdSounds = register("bird", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FancyFluidStorage.MOD_ID, "bird")));

    private static <T extends SoundEvent> RegistryObject<T> register(final String name, final Supplier<T> sound) {
        return SOUND_EVENTS.register(name, sound);
    }

    public static void register() {
        SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
