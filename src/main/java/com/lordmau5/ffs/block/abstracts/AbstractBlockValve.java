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
import net.minecraft.fluid.IFluidState;
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

    protected AbstractBlockValve() {
        super(Block.Properties.create(Material.IRON).hardnessAndResistance(5.0f, 10.0f));
    }

    @Override
    public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader worldIn, BlockPos pos) {
        float delta = super.getPlayerRelativeBlockHardness(state, player, worldIn, pos);

        TileEntity tile = worldIn.getTileEntity(pos);
        if ( tile instanceof AbstractTankValve ) {
            AbstractTankValve valve = (AbstractTankValve) tile;
            if ( valve.isValid() && !valve.getTankConfig().isEmpty() ) {
                return delta / 4;
            }
        }
        return delta;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public abstract TileEntity createTileEntity(BlockState state, IBlockReader world);

    @Override
    public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
        TileEntity tile = world.getTileEntity(pos);
        if ( tile instanceof AbstractTankValve ) {
            AbstractTankValve valve = (AbstractTankValve) world.getTileEntity(pos);
            if ( valve != null && valve.isValid() ) {
                valve.breakTank();
            }
        }

        super.onExplosionDestroy(world, pos, explosion);
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if ( !worldIn.isRemote && newState.isAir(worldIn, pos) ) {
            AbstractTankValve valve = (AbstractTankValve) worldIn.getTileEntity(pos);
            if ( valve != null && valve.isValid() ) {
                valve.breakTank();
            }
        }

        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, IFluidState fluid) {
        if ( !world.isRemote ) {
            AbstractTankValve valve = (AbstractTankValve) world.getTileEntity(pos);
            if ( valve != null && valve.isValid() ) {
                valve.breakTank();
            }
        }

        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) return ActionResultType.SUCCESS;

        if ( player.isSneaking() ) return ActionResultType.PASS;

        AbstractTankValve valve = (AbstractTankValve) worldIn.getTileEntity(pos);
        if ( valve == null ) {
            return ActionResultType.PASS;
        }

        if ( valve.isValid() ) {
            if ( GenericUtil.isFluidContainer(player.getHeldItemMainhand()) ) {
                if ( GenericUtil.fluidContainerHandler(worldIn, valve, player) ) {
                    valve.markForUpdateNow();
                    return ActionResultType.CONSUME;
                }
            }

            NetworkHandler.sendPacketToPlayer(new FFSPacket.Client.OpenGUI(valve, false), (ServerPlayerEntity) player);
        } else {
            valve.buildTank_player(player, hit.getFace().getOpposite());
        }
        return ActionResultType.CONSUME;
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(BlockState state, World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
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
