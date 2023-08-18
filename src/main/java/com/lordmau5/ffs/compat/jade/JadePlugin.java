package com.lordmau5.ffs.compat.jade;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.block.abstracts.AbstractBlock;
import com.lordmau5.ffs.blockentity.abstracts.AbstractTankValve;
import com.lordmau5.ffs.blockentity.util.TankConfig;
import com.lordmau5.ffs.holder.FFSItems;
import com.lordmau5.ffs.util.TankManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.*;
import snownee.jade.api.view.FluidView;
import snownee.jade.util.FluidTextHelper;

@WailaPlugin(FancyFluidStorage.MOD_ID)
public class JadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addTooltipCollectedCallback((iTooltip, accessor) -> {
            HitResult result = accessor.getHitResult();
            if (!(result instanceof BlockHitResult blockHitResult)) return;

            BlockPos pos = blockHitResult.getBlockPos();
            Level level = accessor.getLevel();

            Block block = level.getBlockState(pos).getBlock();
            if (block instanceof AbstractBlock) return;

            AbstractTankValve valve = null;
            if (TankManager.INSTANCE.isPartOfTank(level, pos)) {
                valve = TankManager.INSTANCE.getValveForBlock(level, pos);
            }

            if (valve == null) return;

            TankConfig config = valve.getTankConfig();

            IElementHelper helper = iTooltip.getElementHelper();

            float progress = config.getFilledPercentage();

            boolean isFluidEmpty = config.getFluidStack().isEmpty();

            FluidView view = new FluidView(helper.fluid(JadeFluidObject.of(config.getFluidStack().getFluid())));
            view.fluidName = isFluidEmpty ? Component.literal("Empty") : config.getFluidStack().getDisplayName();
            view.current = FluidTextHelper.getUnicodeMillibuckets(isFluidEmpty ? config.getFluidCapacity() : config.getFluidAmount(), true);

            IProgressStyle progressStyle = helper.progressStyle().overlay(view.overlay);

            Component fluidAmount = Component.literal(view.current);
            if (isFluidEmpty) {
                fluidAmount = fluidAmount.copy().withStyle(ChatFormatting.GRAY);
            }

            Component textComponent = Component.translatable("jade.fluid", IDisplayHelper.get().stripColor(view.fluidName), fluidAmount);

            iTooltip.add(1, helper.progress(progress, textComponent, progressStyle, BoxStyle.DEFAULT, true));

            IElement icon = helper.item(new ItemStack(FFSItems.tit.get()), 1.0f).size(new Vec2(16, 16)).translate(new Vec2(-4, -1));

            iTooltip.add(1, icon);

            IElement text = helper.text(Component.translatable("top.ffs.part_of_tank")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)).translate(new Vec2(-2, 4));

            iTooltip.append(1, text);
        });
    }

}