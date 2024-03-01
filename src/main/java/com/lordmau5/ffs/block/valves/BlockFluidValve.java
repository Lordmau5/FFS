package com.lordmau5.ffs.block.valves;

import com.lordmau5.ffs.block.abstracts.AbstractBlock;
import com.lordmau5.ffs.blockentity.abstracts.AbstractTankValve;
import com.lordmau5.ffs.blockentity.util.TankConfig;
import com.lordmau5.ffs.blockentity.valves.BlockEntityFluidValve;
import com.lordmau5.ffs.holder.FFSBlockEntities;
import com.lordmau5.ffs.util.FFSStateProps;
import com.lordmau5.ffs.util.GenericUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockFluidValve extends AbstractBlock {

    public BlockFluidValve() {
        super(Properties.of().requiresCorrectToolForDrops().strength(5.0f, 6.0f));

        registerDefaultState(getStateDefinition().any().setValue(FFSStateProps.TILE_VALID, false).setValue(FFSStateProps.TILE_MAIN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FFSStateProps.TILE_MAIN, FFSStateProps.TILE_VALID);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return FFSBlockEntities.tileEntityFluidValve.get().create(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return type == FFSBlockEntities.tileEntityFluidValve.get() ? BlockEntityFluidValve::tick : null;
    }

    private void addTankConfigToStack(ItemStack stack, AbstractTankValve valve) {
        TankConfig tankConfig = valve.getTankConfig();

        if (tankConfig.isEmpty()) return;

        tankConfig.writeToNBT(stack.getOrCreateTag());
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof BlockEntityFluidValve valve) {
            if (!level.isClientSide() && player.isCreative() && !valve.getTankConfig().isEmpty()) {
                ItemStack stack = new ItemStack(this);

                addTankConfigToStack(stack, valve);

                ItemEntity itementity = new ItemEntity(level, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, stack);
                itementity.setDefaultPickUpDelay();
                level.addFreshEntity(itementity);
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams)
    {
        List<ItemStack> drops = new ArrayList<>();

        BlockEntity tile = pParams.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (tile instanceof BlockEntityFluidValve valve) {
            ItemStack stack = new ItemStack(this);

            addTankConfigToStack(stack, valve);

            drops.add(stack);
        }

        return drops;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        ItemStack stack = super.getCloneItemStack(state, target, level, pos, player);

        if (player.isShiftKeyDown()) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof BlockEntityFluidValve valve) {
                addTankConfigToStack(stack, valve);
            }
        }

        return stack;
    }

    private @Nonnull FluidStack loadFluidStackFromTankConfig(ItemStack stack) {
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
        if (tile instanceof BlockEntityFluidValve valve) {
            valve.getTankConfig().setFluidStack(fluidStack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        FluidStack fluidStack = loadFluidStackFromTankConfig(stack);

        if (fluidStack.isEmpty()) return;

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
