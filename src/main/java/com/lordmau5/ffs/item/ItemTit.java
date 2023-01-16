package com.lordmau5.ffs.item;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.holder.ModCreativeTab;
import com.lordmau5.ffs.holder.Sounds;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import java.util.Random;

public class ItemTit extends Item {
    public ItemTit(final Item.Properties properties) {
        super(properties.tab(ModCreativeTab.instance));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        float randomPitch = 0.75f + (new Random().nextFloat() * 0.5f);
        worldIn.playSound(playerIn, playerIn.getX(), playerIn.getY(), playerIn.getZ(), Sounds.birdSounds.get(), SoundSource.AMBIENT, 1.0f, randomPitch);

        return super.use(worldIn, playerIn, handIn);
    }
}
