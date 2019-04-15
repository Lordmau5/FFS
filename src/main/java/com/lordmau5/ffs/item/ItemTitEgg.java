package com.lordmau5.ffs.item;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.client.CreativeTabFFS;
import net.minecraft.item.Item;

/**
 * Created by Lordmau5 on 08.12.2016.
 */
public class ItemTitEgg extends Item {
    public ItemTitEgg() {
        setRegistryName("item_tit_egg");
        setTranslationKey(FancyFluidStorage.MODID + ".item_tit_egg");
        setCreativeTab(CreativeTabFFS.INSTANCE);
    }

}
