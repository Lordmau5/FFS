package com.lordmau5.ffs.client;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

/**
 * Created by Gigabit101 on 01/08/2017.
 */
public class CreativeTabFFS extends CreativeTabs {
    public static CreativeTabFFS INSTANCE = new CreativeTabFFS();

    public CreativeTabFFS() {
        super(FancyFluidStorage.MODID);
    }

    @Override
    public ItemStack getTabIconItem() {
        return new ItemStack(FancyFluidStorage.blockFluidValve);
    }
}
