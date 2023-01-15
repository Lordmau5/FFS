package com.lordmau5.ffs.item;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.world.item.Item;

public class ItemTitEgg extends Item {
    public ItemTitEgg(final Item.Properties properties) {
        super(properties.tab(FancyFluidStorage.ITEM_GROUP));
    }

}
