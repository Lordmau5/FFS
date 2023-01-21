package com.lordmau5.ffs.block.abstracts;

import com.lordmau5.ffs.blockentity.abstracts.AbstractTankValve;
import com.lordmau5.ffs.network.FFSPacket;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.util.GenericUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public abstract class AbstractBlockValve extends Block implements EntityBlock {

    protected AbstractBlockValve(Properties properties) {
        super(properties);
    }

    @Override
    @Nullable
    public abstract <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                           BlockEntityType<T> eb);

    @Override
    @Nullable
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        BlockEntity tile = level.getBlockEntity(pos);
        if (tile instanceof AbstractTankValve) {
            AbstractTankValve valve = (AbstractTankValve) level.getBlockEntity(pos);
            if (valve != null && valve.isValid()) {
                valve.breakTank();
            }
        }

        super.wasExploded(level, pos, explosion);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!worldIn.isClientSide() && newState.isAir()) {
            AbstractTankValve valve = (AbstractTankValve) worldIn.getBlockEntity(pos);
            if (valve != null && valve.isValid()) {
                valve.breakTank();
            }
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide()) {
            AbstractTankValve valve = (AbstractTankValve) level.getBlockEntity(pos);
            if (valve != null && valve.isValid()) {
                valve.breakTank();
            }
        }

        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide()) return InteractionResult.SUCCESS;

        if (player.isShiftKeyDown()) return InteractionResult.PASS;

        AbstractTankValve valve = (AbstractTankValve) worldIn.getBlockEntity(pos);
        if (valve == null) {
            return InteractionResult.PASS;
        }

        if (valve.isValid()) {
            if (GenericUtil.isFluidContainer(player.getMainHandItem())) {
                if (GenericUtil.fluidContainerHandler(worldIn, valve, player)) {
                    valve.markForUpdateNow();
                    return InteractionResult.CONSUME;
                }
            }

            NetworkHandler.sendPacketToPlayer(new FFSPacket.Client.OpenGUI(valve, false), (ServerPlayer) player);
        } else {
            valve.buildTank(player, hit.getDirection().getOpposite());
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos) {
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof AbstractTankValve) {
            AbstractTankValve valve = (AbstractTankValve) te;
            return valve.getComparatorOutput();
        }
        return 0;
    }

    @Override
    public boolean isValidSpawn(BlockState state, BlockGetter level, BlockPos pos, SpawnPlacements.Type type, EntityType<?> entityType) {
        return false;
    }

}
