package com.lordmau5.ffs.block.abstracts;

import com.lordmau5.ffs.network.FFSPacket;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import com.lordmau5.ffs.util.GenericUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public abstract class AbstractBlockValve extends Block {

    protected AbstractBlockValve(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public abstract TileEntity createTileEntity(BlockState state, IBlockReader world);

    @Override
    public void wasExploded(World world, BlockPos pos, Explosion explosion) {
        TileEntity tile = world.getBlockEntity(pos);
        if ( tile instanceof AbstractTankValve ) {
            AbstractTankValve valve = (AbstractTankValve) world.getBlockEntity(pos);
            if ( valve != null && valve.isValid() ) {
                valve.breakTank();
            }
        }

        super.wasExploded(world, pos, explosion);
    }

    @Override
    public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if ( !worldIn.isClientSide && newState.isAir(worldIn, pos) ) {
            AbstractTankValve valve = (AbstractTankValve) worldIn.getBlockEntity(pos);
            if ( valve != null && valve.isValid() ) {
                valve.breakTank();
            }
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        if ( !world.isClientSide ) {
            AbstractTankValve valve = (AbstractTankValve) world.getBlockEntity(pos);
            if ( valve != null && valve.isValid() ) {
                valve.breakTank();
            }
        }

        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isClientSide) return ActionResultType.SUCCESS;

        if ( player.isCrouching() ) return ActionResultType.PASS;

        AbstractTankValve valve = (AbstractTankValve) worldIn.getBlockEntity(pos);
        if ( valve == null ) {
            return ActionResultType.PASS;
        }

        if ( valve.isValid() ) {
            if ( GenericUtil.isFluidContainer(player.getMainHandItem()) ) {
                if ( GenericUtil.fluidContainerHandler(worldIn, valve, player) ) {
                    valve.markForUpdateNow();
                    return ActionResultType.CONSUME;
                }
            }

            NetworkHandler.sendPacketToPlayer(new FFSPacket.Client.OpenGUI(valve, false), (ServerPlayerEntity) player);
        } else {
            valve.buildTank(player, hit.getDirection().getOpposite());
        }
        return ActionResultType.CONSUME;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, World world, BlockPos pos) {
        TileEntity te = world.getBlockEntity(pos);
        if ( te instanceof AbstractTankValve ) {
            AbstractTankValve valve = (AbstractTankValve) te;
            return valve.getComparatorOutput();
        }
        return 0;
    }

    @Override
    public boolean canCreatureSpawn(BlockState state, IBlockReader world, BlockPos pos, EntitySpawnPlacementRegistry.PlacementType type, @Nullable EntityType<?> entityType) {
        return false;
    }

}
