package com.lordmau5.ffs.block.valves;

import com.lordmau5.ffs.block.abstracts.AbstractBlockValve;
import com.lordmau5.ffs.tile.valves.TileEntityFluidValve;
import com.lordmau5.ffs.util.FFSStateProps;
import com.lordmau5.ffs.util.GenericUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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

import javax.annotation.Nullable;
import java.util.List;

public class BlockFluidValve extends AbstractBlockValve {

    public BlockFluidValve() {
        super();

        setDefaultState(getDefaultState().with(FFSStateProps.TILE_VALID, false).with(FFSStateProps.TILE_MAIN, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);

        builder.add(FFSStateProps.TILE_MAIN, FFSStateProps.TILE_VALID);
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TileEntityFluidValve();
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player) {
        ItemStack stack = super.getPickBlock(state, target, world, pos, player);

        if (player.isSneaking()) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileEntityFluidValve) {
                ((TileEntityFluidValve) tile).getTankConfig().writeToNBT(stack.getOrCreateTag());
            }
        }

        return stack;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

        if (!stack.hasTag()) {
            return;
        }

        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains("TankConfig")) {
            return;
        }

        CompoundNBT tankConfig = tag.getCompound("TankConfig");

        FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tankConfig);

        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityFluidValve) {
            ((TileEntityFluidValve) tile).getTankConfig().setFluidStack(fluidStack);
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        if (!stack.hasTag()) {
            return;
        }

        CompoundNBT tag = stack.getOrCreateTag();
        if (!tag.contains("TankConfig")) {
            return;
        }

        CompoundNBT tankConfig = tag.getCompound("TankConfig");

        FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tankConfig);

        tooltip.add(
                new TranslationTextComponent("description.ffs.fluid_valve.fluid", fluidStack.getDisplayName().getString())
                .mergeStyle(TextFormatting.GRAY)
        );
        tooltip.add(
                new TranslationTextComponent("description.ffs.fluid_valve.amount", GenericUtil.intToFancyNumber(fluidStack.getAmount()) + "mB")
                .mergeStyle(TextFormatting.GRAY)
        );
    }
}
