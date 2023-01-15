package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(FancyFluidStorage.MODID)
public class Sounds {

    public static SoundEvent birdSounds;

    public static void registerAll() {
        birdSounds = new SoundEvent(new ResourceLocation(FancyFluidStorage.MODID, "bird"));

        FancyFluidStorage.SOUND_EVENTS.register("bird", () -> birdSounds);
    }
}
