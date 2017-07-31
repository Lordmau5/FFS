package com.lordmau5.ffs.item;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.item.Item;
import reborncore.RebornRegistry;

/**
 * Created by Lordmau5 on 08.12.2016.
 * <p>
 * Big thanks to InsomniaKitten for the amazing textures!
 */
public class ItemTit extends Item
{

    public ItemTit()
    {
        setRegistryName("item_tit");
        setUnlocalizedName(FancyFluidStorage.MODID + ".item_tit");

        RebornRegistry.registerItem(this);
    }

}
