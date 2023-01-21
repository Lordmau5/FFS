package com.lordmau5.ffs.block.valves;

import com.lordmau5.ffs.block.abstracts.AbstractBlockValve;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import com.lordmau5.ffs.tile.util.TankConfig;
import com.lordmau5.ffs.tile.valves.TileEntityFluidValve;
import com.lordmau5.ffs.util.FFSStateProps;
import com.lordmau5.ffs.util.GenericUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockFluidValve extends AbstractBlockValve {

    public BlockFluidValve() {
        super(Block.Properties.of(Material.METAL).requiresCorrectToolForDrops().strength(5.0f, 6.0f));

        registerDefaultState(getStateDefinition().any().setValue(FFSStateProps.TILE_VALID, false).setValue(FFSStateProps.TILE_MAIN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);

        builder.add(FFSStateProps.TILE_MAIN, FFSStateProps.TILE_VALID);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileEntityFluidValve();
    }

    private void addTankConfigToStack(ItemStack stack, AbstractTankValve valve) {
        TankConfig tankConfig = valve.getTankConfig();

        if (tankConfig.isEmpty()) return;

        tankConfig.writeToNBT(stack.getOrCreateTag());
    }

    @Override
    public void playerWillDestroy(World level, BlockPos pos, BlockState state, PlayerEntity player) {
        TileEntity tile = level.getBlockEntity(pos);
        if (tile instanceof TileEntityFluidValve) {
            TileEntityFluidValve valve = (TileEntityFluidValve) tile;

            if (!level.isClientSide() && player.isCreative() && !valve.getTankConfig().isEmpty()) {
                ItemStack stack = new ItemStack(this);

                addTankConfigToStack(stack, valve);

                ItemEntity itementity = new ItemEntity(level, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, stack);
                itementity.setDefaultPickUpDelay();
                level.addFreshEntity(itementity);
            }
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        List<ItemStack> drops = new ArrayList<>();

        TileEntity tile = pBuilder.getOptionalParameter(LootParameters.BLOCK_ENTITY);
        if (tile instanceof TileEntityFluidValve) {
            ItemStack stack = new ItemStack(this);

            addTankConfigToStack(stack, (TileEntityFluidValve) tile);

            drops.add(stack);
        }

        return drops;
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);

        if (player.isShiftKeyDown()) {
            TileEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileEntityFluidValve) {
                addTankConfigToStack(stack, (TileEntityFluidValve) tile);
            }
        }

        return stack;
    }

    private @Nonnull
    FluidStack loadFluidStackFromTankConfig(ItemStack stack) {
        if (!stack.hasTag()) {
            return FluidStack.EMPTY;
        }

        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains("TankConfig")) {
            return FluidStack.EMPTY;
        }

        CompoundNBT tankConfig = tag.getCompound("TankConfig");

        return FluidStack.loadFluidStackFromNBT(tankConfig);
    }

    @Override
    public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(worldIn, pos, state, placer, stack);

        FluidStack fluidStack = loadFluidStackFromTankConfig(stack);

        TileEntity tile = worldIn.getBlockEntity(pos);
        if (tile instanceof TileEntityFluidValve) {
            ((TileEntityFluidValve) tile).getTankConfig().setFluidStack(fluidStack);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        FluidStack fluidStack = loadFluidStackFromTankConfig(stack);

        if (fluidStack.isEmpty()) return;

        tooltip.add(
                new TranslationTextComponent("description.ffs.fluid_valve.fluid", fluidStack.getDisplayName().getString())
                        .withStyle(TextFormatting.GRAY)
        );
        tooltip.add(
                new TranslationTextComponent("description.ffs.fluid_valve.amount", GenericUtil.intToFancyNumber(fluidStack.getAmount()) + "mB")
                        .withStyle(TextFormatting.GRAY)
        );
    }
}
