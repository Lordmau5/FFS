package com.lordmau5.ffs.item;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.holder.Sounds;
import net.minecraft.client.audio.SoundSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

import java.util.Random;

public class ItemTit extends Item {
    public ItemTit(final Item.Properties properties) {
        super(properties.tab(FancyFluidStorage.ITEM_GROUP));
    }

    private void playSound(World level, PlayerEntity player) {
        float randomPitch = 0.75f + (new Random().nextFloat() * 0.5f);
        level.playSound(player, player.getX(), player.getY(), player.getZ(), Sounds.birdSounds, SoundCategory.AMBIENT, 1.0f, randomPitch);
    }

    @Override
    public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
        if (context.getLevel().getBlockEntity(context.getClickedPos()) != null)
            playSound(context.getLevel(), context.getPlayer());

        return super.onItemUseFirst(stack, context);
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        playSound(level, player);

        return super.use(level, player, hand);
    }
}
