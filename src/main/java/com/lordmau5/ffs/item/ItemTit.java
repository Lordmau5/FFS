package com.lordmau5.ffs.item;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.holder.Sounds;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import java.util.Random;

public class ItemTit extends Item {
    public ItemTit(final Item.Properties properties) {
        super(properties.group(FancyFluidStorage.ITEM_GROUP));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        float randomPitch = 0.75f + (new Random().nextFloat() * 0.5f);
        worldIn.playSound(playerIn, playerIn.getPosX(), playerIn.getPosY(), playerIn.getPosZ(), Sounds.birdSounds, SoundCategory.AMBIENT, 1.0f, randomPitch);

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
