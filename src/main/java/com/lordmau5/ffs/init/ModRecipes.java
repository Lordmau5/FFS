package com.lordmau5.ffs.init;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.compat.Compatibility;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import reborncore.common.util.RebornCraftingHelper;

/**
 * Created by Gigabit101 on 31/07/2017.
 */
public class ModRecipes
{
    public static void init()
    {
        RebornCraftingHelper.addShapedOreRecipe(new ItemStack(FancyFluidStorage.blockFluidValve),
                "IGI", "GBG", "IGI",
                'I', Items.IRON_INGOT,
                'G', Blocks.IRON_BARS,
                'B', Items.BUCKET);

		RebornCraftingHelper.addShapedOreRecipe(new ItemStack(FancyFluidStorage.blockTankComputer),
                "IGI", "GBG", "IGI",
                'I', Items.IRON_INGOT,
                'G', Blocks.IRON_BARS,
                'B', Blocks.REDSTONE_BLOCK);

		if(Compatibility.INSTANCE.isEnergyModSupplied())
        {
			RebornCraftingHelper.addShapedOreRecipe(new ItemStack(FancyFluidStorage.blockMetaphaser),
                    "IGI", "GBG", "IGI",
                    'I', Items.IRON_INGOT,
                    'G', Blocks.IRON_BARS,
                    'B', Items.COMPARATOR);
		}

		RebornCraftingHelper.addShapelessRecipe(new ItemStack(FancyFluidStorage.itemTitEgg), FancyFluidStorage.blockFluidValve, Items.EGG);
        RebornCraftingHelper.addSmelting(FancyFluidStorage.itemTitEgg, new ItemStack(FancyFluidStorage.itemTit), 0);
    }
}
