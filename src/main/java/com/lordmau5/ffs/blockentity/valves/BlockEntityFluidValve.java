package com.lordmau5.ffs.blockentity.valves;


import com.lordmau5.ffs.blockentity.abstracts.AbstractTankValve;
import com.lordmau5.ffs.holder.BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockEntityFluidValve extends AbstractTankValve {

    private final LazyOptional<IFluidHandler> holder = LazyOptional.of(() -> getTankConfig().getFluidTank());

    public BlockEntityFluidValve(BlockPos pos, BlockState state) {
        super(BlockEntities.tileEntityFluidValve.get(), pos, state);
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T be) {
        AbstractTankValve.tick(level, pos, state, be);

        BlockEntityFluidValve valve = (BlockEntityFluidValve) be;

        if (level.isClientSide()) {
            return;
        }

        if (!valve.isValid()) {
            return;
        }

        FluidStack fluidStack = valve.getTankConfig().getFluidStack();
        if (fluidStack.isEmpty()) {
            return;
        }

        Fluid fluid = fluidStack.getFluid();
        if (fluid == null) {
            return;
        }

        if (fluid == Fluids.WATER) {
            if (level.isRaining()) {
                int rate = (int) Math.floor(level.rainLevel * 5 * level.getBiome(pos).value().getDownfall());
                if (pos.getY() == level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, pos).getY() - 1) {
                    FluidStack waterStack = fluidStack.copy();
                    waterStack.setAmount(rate * 10);
                    valve.getTankConfig().getFluidTank().fill(waterStack, IFluidHandler.FluidAction.EXECUTE);
                }
            }
        }
    }

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return holder.cast();
        return super.getCapability(cap, side);
    }
}
