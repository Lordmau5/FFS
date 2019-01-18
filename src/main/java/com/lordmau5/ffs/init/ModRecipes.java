package com.lordmau5.ffs.init;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Created by Gigabit101 on 31/07/2017.
 */
public class ModRecipes {
    public static void init() {
        GameRegistry.addSmelting(FancyFluidStorage.itemTitEgg, new ItemStack(FancyFluidStorage.itemTit), 0);
    }
}
