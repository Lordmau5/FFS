package com.lordmau5.ffs.datagen;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.holder.FFSBlocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.ExistingFileHelper;

public class FFSBlockTags extends BlockTagsProvider {

    public FFSBlockTags(DataGenerator generator, ExistingFileHelper helper) {
        super(generator, FancyFluidStorage.MOD_ID, helper);
    }

    @Override
    public String getName() {
        return "Fancy Fluid Storage - Block Tags";
    }

    @Override
    protected void addTags() {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(FFSBlocks.fluidValve.get())
                .add(FFSBlocks.tankComputer.get());
    }
}