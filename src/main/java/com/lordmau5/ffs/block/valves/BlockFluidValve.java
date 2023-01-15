package com.lordmau5.ffs.block.valves;

import com.lordmau5.ffs.block.abstracts.AbstractBlockValve;
import com.lordmau5.ffs.tile.valves.TileEntityFluidValve;
import com.lordmau5.ffs.util.FFSStateProps;
import com.lordmau5.ffs.util.GenericUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

public class BlockFluidValve extends AbstractBlockValve {

    public BlockFluidValve() {
        super();

        registerDefaultState(defaultBlockState().setValue(FFSStateProps.TILE_VALID, false).setValue(FFSStateProps.TILE_MAIN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(FFSStateProps.TILE_MAIN, FFSStateProps.TILE_VALID);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileEntityFluidValve(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> eb) {
        return TileEntityFluidValve::tick;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);

        if (player.isShiftKeyDown()) {
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileEntityFluidValve) {
                ((TileEntityFluidValve) tile).getTankConfig().writeToNBT(stack.getOrCreateTag());
            }
        }

        return stack;
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);

        if (!stack.hasTag()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("TankConfig")) {
            return;
        }

        CompoundTag tankConfig = tag.getCompound("TankConfig");

        FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tankConfig);

        BlockEntity tile = worldIn.getBlockEntity(pos);
        if (tile instanceof TileEntityFluidValve) {
            ((TileEntityFluidValve) tile).getTankConfig().setFluidStack(fluidStack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        if (!stack.hasTag()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("TankConfig")) {
            return;
        }

        CompoundTag tankConfig = tag.getCompound("TankConfig");

        FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tankConfig);

        tooltip.add(
                new TranslatableComponent("description.ffs.fluid_valve.fluid", fluidStack.getDisplayName().getString())
                .withStyle(ChatFormatting.GRAY)
        );
        tooltip.add(
                new TranslatableComponent("description.ffs.fluid_valve.amount", GenericUtil.intToFancyNumber(fluidStack.getAmount()) + "mB")
                .withStyle(ChatFormatting.GRAY)
        );
    }
}
