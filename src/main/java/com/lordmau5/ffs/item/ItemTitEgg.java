package com.lordmau5.ffs.item;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.item.Item;
import reborncore.RebornRegistry;

/**
 * Created by Lordmau5 on 08.12.2016.
 * <p>
 * Big thanks to InsomniaKitten for the amazing textures!
 */
public class ItemTitEgg extends Item
{

    public ItemTitEgg()
    {
        setRegistryName("item_tit_egg");
        setUnlocalizedName(FancyFluidStorage.MODID + ".item_tit_egg");

        RebornRegistry.registerItem(this);
    }

}
