package com.lordmau5.ffs.block.valves;

import com.lordmau5.ffs.block.abstracts.AbstractBlockValve;
import com.lordmau5.ffs.blockentity.valves.BlockEntityFluidValve;
import com.lordmau5.ffs.holder.BlockEntities;
import com.lordmau5.ffs.util.FFSStateProps;
import com.lordmau5.ffs.util.GenericUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class BlockFluidValve extends AbstractBlockValve {

    public BlockFluidValve() {
        super();

        registerDefaultState(getStateDefinition().any().setValue(FFSStateProps.TILE_VALID, false).setValue(FFSStateProps.TILE_MAIN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FFSStateProps.TILE_MAIN, FFSStateProps.TILE_VALID);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BlockEntities.tileEntityFluidValve.get().create(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return type == BlockEntities.tileEntityFluidValve.get() ? BlockEntityFluidValve::tick : null;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);

        if (player.isShiftKeyDown()) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof BlockEntityFluidValve) {
                ((BlockEntityFluidValve) tile).getTankConfig().writeToNBT(stack.getOrCreateTag());
            }
        }

        return stack;
    }

    private @Nonnull
    FluidStack loadFluidStackFromTankConfig(ItemStack stack) {
        if (!stack.hasTag()) {
            return FluidStack.EMPTY;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("TankConfig")) {
            return FluidStack.EMPTY;
        }

        CompoundTag tankConfig = tag.getCompound("TankConfig");

        return FluidStack.loadFluidStackFromNBT(tankConfig);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);

        FluidStack fluidStack = loadFluidStackFromTankConfig(stack);

        BlockEntity tile = worldIn.getBlockEntity(pos);
        if (tile instanceof BlockEntityFluidValve) {
            ((BlockEntityFluidValve) tile).getTankConfig().setFluidStack(fluidStack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        FluidStack fluidStack = loadFluidStackFromTankConfig(stack);

        tooltip.add(
                Component.translatable("description.ffs.fluid_valve.fluid", fluidStack.getDisplayName().getString())
                        .withStyle(ChatFormatting.GRAY)
        );
        tooltip.add(
                Component.translatable("description.ffs.fluid_valve.amount", GenericUtil.intToFancyNumber(fluidStack.getAmount()) + "mB")
                        .withStyle(ChatFormatting.GRAY)
        );
    }
}
