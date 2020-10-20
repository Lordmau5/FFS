package com.lordmau5.ffs.util;

import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ChunkManager;
import net.minecraftforge.common.util.LazyOptional;
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
        return "tile_" + Long.toHexString(valve.getPos().toLong());
    }

    public static boolean isBlockGlass(BlockState blockState) {
        if ( blockState == null || blockState.getMaterial() == Material.AIR ) {
            return false;
        }

        if ( blockState.getBlock() instanceof GlassBlock ) {
            return true;
        }

        ItemStack is = new ItemStack(blockState.getBlock(), 1);
        return blockState.getMaterial() == Material.GLASS && !is.getTranslationKey().contains("pane");
    }

    public static Direction getInsideForTankFrame(TreeMap<Integer, List<LayerBlockPos>> airBlocks, BlockPos frame) {
        for (Direction direction : Direction.values()) {
            for (int layer : airBlocks.keySet()) {
                if ( airBlocks.get(layer).contains(frame.offset(direction)) ) {
                    return direction;
                }
            }
        }
        return null;
    }

    public static boolean areTankBlocksValid(BlockState bottomBlock, World world, BlockPos bottomPos, Direction direction) {
        return isValidTankBlock(world, bottomPos, bottomBlock, direction);
    }

    public static boolean isValidTankBlock(World world, BlockPos pos, BlockState state, Direction direction) {
        if ( state == null ) {
            return false;
        }

        if ( world.isAirBlock(pos) ) {
            return false;
        }

        if ( state.getBlock() instanceof FallingBlock) {
            return false;
        }

//        if ( Compatibility.INSTANCE.isCNBLoaded ) {
//            if ( CNBAPIAccess.apiInstance.isBlockChiseled(world, pos) ) {
//                return direction != null && CNBCompatibility.INSTANCE.isValid(world, pos, direction);
//            }
//        }

        return isBlockGlass(state) || direction == null || state.isSolidSide(world, pos, direction);
    }

    public static boolean isFluidContainer(ItemStack playerItem) {
        return playerItem != ItemStack.EMPTY && FluidUtil.getFluidHandler(playerItem).isPresent();
    }

    public static boolean fluidContainerHandler(World world, AbstractTankValve valve, PlayerEntity player) {
        if ( world.isRemote ) {
            return true;
        }

        ItemStack current = player.getHeldItemMainhand();

        if ( current != ItemStack.EMPTY ) {
            if ( !isFluidContainer(current) ) {
                return false;
            }

            LazyOptional<FluidStack> stack = FluidUtil.getFluidContained(current);
            if (stack.isPresent() && !stack.orElseGet(() -> FluidStack.EMPTY).isEmpty() && valve.getTankConfig().isFluidLocked() && !valve.getTankConfig().getLockedFluid().isFluidEqual(stack.orElseGet(() -> FluidStack.EMPTY))) {
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

        player.sendStatusMessage(new TranslationTextComponent(key), actionBar);
    }

    public static void sendMessageToClient(PlayerEntity player, String key, boolean actionBar, Object... args) {
        if ( player == null ) {
            return;
        }

        player.sendStatusMessage(new TranslationTextComponent(key, args), actionBar);
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

}
