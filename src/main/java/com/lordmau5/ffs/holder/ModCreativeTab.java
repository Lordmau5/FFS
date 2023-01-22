package com.lordmau5.ffs.holder;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeTab extends CreativeModeTab {
    public static final ModCreativeTab instance = new ModCreativeTab(CreativeModeTab.TABS.length, FancyFluidStorage.MOD_ID);

    private ModCreativeTab(int index, String label) {
        super(index, label);
    }

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(FFSBlocks.fluidValve.get());
    }
}
