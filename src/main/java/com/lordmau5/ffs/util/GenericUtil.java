package com.lordmau5.ffs.util;

import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ChunkManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.text.NumberFormat;
import java.util.*;

public class GenericUtil {
    private static Map<World, ChunkManager> chunkloadTicketMap;

    public static void init() {
        chunkloadTicketMap = new HashMap<>();
    }

    public static String getUniquePositionName(AbstractTankValve valve) {
        return "tile_" + Long.toHexString(valve.getBlockPos().asLong());
    }

    public static boolean isBlockGlass(BlockState blockState) {
        if ( blockState == null || blockState.getMaterial() == Material.AIR ) {
            return false;
        }

        if ( blockState.getBlock() instanceof GlassBlock ) {
            return true;
        }

        ItemStack is = new ItemStack(blockState.getBlock(), 1);
        return blockState.getMaterial() == Material.GLASS && !is.getDescriptionId().contains("pane");
    }

    public static Direction getInsideForTankFrame(TreeMap<Integer, HashSet<LayerBlockPos>> airBlocks, BlockPos frame) {
        for (Direction direction : Direction.values()) {
            for (int layer : airBlocks.keySet()) {
                if ( airBlocks.get(layer).contains(frame.relative(direction)) ) {
                    return direction;
                }
            }
        }
        return null;
    }

    public static boolean isBlockFallingBlock(BlockState state) {
        return state != null && state.getBlock() instanceof FallingBlock;
    }

    public static boolean isValidTankBlock(World world, BlockPos pos, BlockState state, Direction direction) {
        if ( state == null ) {
            return false;
        }

        if ( world.isEmptyBlock(pos) ) {
            return false;
        }

        if ( isBlockFallingBlock(state)) {
            return false;
        }

//        if ( Compatibility.INSTANCE.isCNBLoaded ) {
//            if ( CNBAPIAccess.apiInstance.isBlockChiseled(world, pos) ) {
//                return direction != null && CNBCompatibility.INSTANCE.isValid(world, pos, direction);
//            }
//        }

        return isBlockGlass(state) || direction == null || state.isFaceSturdy(world, pos, direction);
    }

    public static boolean isFluidContainer(ItemStack playerItem) {
        return playerItem != ItemStack.EMPTY && FluidUtil.getFluidHandler(playerItem).isPresent();
    }

    public static boolean fluidContainerHandler(World world, AbstractTankValve valve, PlayerEntity player) {
        if ( world.isClientSide ) {
            return true;
        }

        ItemStack current = player.getMainHandItem();

        if ( current != ItemStack.EMPTY ) {
            if ( !isFluidContainer(current) ) {
                return false;
            }

            Optional<FluidStack> stack = FluidUtil.getFluidContained(current);
            if (stack.isPresent() && !stack.get().isEmpty() && valve.getTankConfig().isFluidLocked() && !valve.getTankConfig().getLockedFluid().isFluidEqual(stack.get())) {
                return false;
            }

            return FluidUtil.interactWithFluidHandler(player, Hand.MAIN_HAND, valve.getTankConfig().getFluidTank());
        }
        return false;
    }

    public static String intToFancyNumber(int number) {
        return NumberFormat.getIntegerInstance(Locale.ENGLISH).format(number);
    }

    public static void sendMessageToClient(PlayerEntity player, String key, boolean actionBar) {
        if ( player == null ) {
            return;
        }

        player.displayClientMessage(new TranslationTextComponent(key), actionBar);
    }

    public static void sendMessageToClient(PlayerEntity player, String key, boolean actionBar, Object... args) {
        if ( player == null ) {
            return;
        }

        player.displayClientMessage(new TranslationTextComponent(key, args), actionBar);
    }

    public static void initChunkLoadTicket(World world, ChunkManager ticket) {
        chunkloadTicketMap.put(world, ticket);
    }

    public static ChunkManager getChunkLoadTicket(World world) {
        if ( chunkloadTicketMap.containsKey(world) ) {
            return chunkloadTicketMap.get(world);
        }

//        ForgeChunkManager.Ticket chunkloadTicket = ChunkTicketManager.requestTicket(FancyFluidStorage.INSTANCE, world, ForgeChunkManager.Type.NORMAL);
//        chunkloadTicketMap.put(world, chunkloadTicket);
//        return chunkloadTicket;
        return null;
    }

    // Check if a block is either air or water-loggable
    public static boolean isAirOrWaterloggable(World world, BlockPos pos) {
        if (world.isEmptyBlock(pos)) {
            return true;
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // Comparing against ILiquidContainer instead of IWaterLoggable for better compatibility
        if (block instanceof ILiquidContainer) {
            return ((ILiquidContainer) block).canPlaceLiquid(world, pos, state, Fluids.WATER);
        }

        return false;
    }

}
