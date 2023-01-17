package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.registries.ObjectHolder;

@ObjectHolder(FancyFluidStorage.MOD_ID)
public class Sounds {

    public static SoundEvent birdSounds;

    public static void registerAll() {
        birdSounds = new SoundEvent(new ResourceLocation(FancyFluidStorage.MOD_ID, "bird"));

        FancyFluidStorage.SOUND_EVENTS.register("bird", () -> birdSounds);
    }
}
