package com.lordmau5.ffs.item;

import com.lordmau5.ffs.holder.FFSSounds;
import com.lordmau5.ffs.holder.ModCreativeTab;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.Random;

public class ItemTit extends Item {
    public ItemTit(final Item.Properties properties) {
        super(properties.tab(ModCreativeTab.instance));
    }

    private void playSound(Level level, Player player) {
        float randomPitch = 0.75f + (new Random().nextFloat() * 0.5f);
        level.playSound(player, player.getX(), player.getY(), player.getZ(), FFSSounds.birdSounds.get(), SoundSource.AMBIENT, 1.0f, randomPitch);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().getBlockEntity(context.getClickedPos()) != null)
            playSound(context.getLevel(), context.getPlayer());

        return super.onItemUseFirst(stack, context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        playSound(level, player);

        return super.use(level, player, hand);
    }
}
