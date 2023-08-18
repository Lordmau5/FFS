package com.lordmau5.ffs.datagen;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.holder.FFSBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class FFSBlockTags extends BlockTagsProvider
{
    public FFSBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, @Nullable ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Fancy Fluid Storage - Block Tags";
    }

    @Override
    protected void addTags(HolderLookup.Provider provider)
    {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(FFSBlocks.fluidValve.get(), FFSBlocks.tankComputer.get());

        tag(FancyFluidStorage.TANK_BLACKLIST).add(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.DIRT_PATH, Blocks.SPONGE, Blocks.WET_SPONGE, Blocks.PODZOL);
    }
}
