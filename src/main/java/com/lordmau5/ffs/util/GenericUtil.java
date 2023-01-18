package com.lordmau5.ffs.util;

import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.text.NumberFormat;
import java.util.*;

public class GenericUtil {
    private static Map<Level, ChunkMap> chunkloadTicketMap;

    public static void init() {
        chunkloadTicketMap = new HashMap<>();
    }

    public static String getUniquePositionName(AbstractTankValve valve) {
        return "tile_" + Long.toHexString(valve.getBlockPos().asLong());
    }

    public static boolean isBlockGlass(BlockState blockState) {
        if (blockState == null || blockState.getMaterial() == Material.AIR) {
            return false;
        }

        if (blockState.getBlock() instanceof GlassBlock) {
            return true;
        }

        ItemStack is = new ItemStack(blockState.getBlock(), 1);
        return blockState.getMaterial() == Material.GLASS && !is.getDescriptionId().contains("pane");
    }

    public static Direction getInsideForTankFrame(TreeMap<Integer, HashSet<LayerBlockPos>> airBlocks, BlockPos frame) {
        for (Direction direction : Direction.values()) {
            for (int layer : airBlocks.keySet()) {
                if (airBlocks.get(layer).contains(frame.relative(direction))) {
                    return direction;
                }
            }
        }
        return null;
    }

    public static boolean isBlockFallingBlock(BlockState state) {
        return state != null && state.getBlock() instanceof FallingBlock;
    }

    public static boolean isValidTankBlock(Level world, BlockPos pos, BlockState state, Direction direction) {
        if (state == null) {
            return false;
        }

        if (world.isEmptyBlock(pos)) {
            return false;
        }

        if (isBlockFallingBlock(state)) {
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

    public static boolean fluidContainerHandler(Level world, AbstractTankValve valve, Player player) {
        if (world.isClientSide) {
            return true;
        }

        ItemStack current = player.getMainHandItem();

        if (current != ItemStack.EMPTY) {
            if (!isFluidContainer(current)) {
                return false;
            }

            Optional<FluidStack> stack = FluidUtil.getFluidContained(current);
            if (stack.isPresent() && !stack.get().isEmpty() && valve.getTankConfig().isFluidLocked() && !valve.getTankConfig().getLockedFluid().isFluidEqual(stack.get())) {
                return false;
            }

            return FluidUtil.interactWithFluidHandler(player, InteractionHand.MAIN_HAND, valve.getTankConfig().getFluidTank());
        }
        return false;
    }

    public static String intToFancyNumber(int number) {
        return NumberFormat.getIntegerInstance(Locale.ENGLISH).format(number);
    }

    public static void sendMessageToClient(Player player, String key, boolean actionBar) {
        if (player == null) {
            return;
        }

        player.displayClientMessage(new TranslatableComponent(key), actionBar);
    }

    public static void sendMessageToClient(Player player, String key, boolean actionBar, Object... args) {
        if (player == null) {
            return;
        }

        player.displayClientMessage(new TranslatableComponent(key, args), actionBar);
    }

    public static void initChunkLoadTicket(Level world, ChunkMap ticket) {
        chunkloadTicketMap.put(world, ticket);
    }

    public static ChunkMap getChunkLoadTicket(Level world) {
        if (chunkloadTicketMap.containsKey(world)) {
            return chunkloadTicketMap.get(world);
        }

//        ForgeChunkManager.Ticket chunkloadTicket = ChunkTicketManager.requestTicket(FancyFluidStorage.INSTANCE, world, ForgeChunkManager.Type.NORMAL);
//        chunkloadTicketMap.put(world, chunkloadTicket);
//        return chunkloadTicket;
        return null;
    }

    // Check if a block is either air or water-loggable
    public static boolean isAirOrWaterloggable(Level world, BlockPos pos) {
        if (world.isEmptyBlock(pos)) {
            return true;
        }

        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // Comparing against ILiquidContainer instead of IWaterLoggable for better compatibility
        if (block instanceof LiquidBlockContainer) {
            return ((LiquidBlockContainer) block).canPlaceLiquid(world, pos, state, Fluids.WATER);
        }

        return false;
    }

}
