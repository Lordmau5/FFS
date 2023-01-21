package com.lordmau5.ffs.util;

import com.lordmau5.ffs.block.abstracts.AbstractBlockValve;
import com.lordmau5.ffs.blockentity.abstracts.AbstractTankValve;
import com.lordmau5.ffs.holder.Items;
import com.lordmau5.ffs.network.FFSPacket;
import com.lordmau5.ffs.network.NetworkHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class TankManager {

    public static TankManager INSTANCE = new TankManager();

    private static class HashMapCache {
        private final HashMap<ResourceKey<Level>, HashMap<BlockPos, TreeMap<Integer, HashSet<LayerBlockPos>>>> valveToFrameBlocks = new HashMap<>();
        private final HashMap<ResourceKey<Level>, HashMap<BlockPos, TreeMap<Integer, HashSet<LayerBlockPos>>>> valveToAirBlocks = new HashMap<>();
        private final HashMap<ResourceKey<Level>, HashMap<BlockPos, BlockPos>> frameBlockToValve = new HashMap<>();
        private final HashMap<ResourceKey<Level>, HashMap<BlockPos, BlockPos>> airBlockToValve = new HashMap<>();

        private final HashMap<ResourceKey<Level>, HashSet<BlockPos>> blocksToCheck = new HashMap<>();

        private HashMap<BlockPos, TreeMap<Integer, HashSet<LayerBlockPos>>> getValveToFrameBlocks(Level world) {
            ResourceKey<Level> dimension = world.dimension();

            valveToFrameBlocks.putIfAbsent(dimension, new HashMap<>());

            return valveToFrameBlocks.get(dimension);
        }

        private HashMap<BlockPos, TreeMap<Integer, HashSet<LayerBlockPos>>> getValveToAirBlocks(Level world) {
            ResourceKey<Level> dimension = world.dimension();

            valveToAirBlocks.putIfAbsent(dimension, new HashMap<>());

            return valveToAirBlocks.get(dimension);
        }

        private HashMap<BlockPos, BlockPos> getFrameBlockToValve(Level world) {
            ResourceKey<Level> dimension = world.dimension();

            frameBlockToValve.putIfAbsent(dimension, new HashMap<>());

            return frameBlockToValve.get(dimension);
        }

        private HashMap<BlockPos, BlockPos> getAirBlockToValve(Level world) {
            ResourceKey<Level> dimension = world.dimension();

            airBlockToValve.putIfAbsent(dimension, new HashMap<>());

            return airBlockToValve.get(dimension);
        }

        private HashSet<BlockPos> getBlocksToCheck(Level world) {
            ResourceKey<Level> dimension = world.dimension();

            blocksToCheck.putIfAbsent(dimension, new HashSet<>());

            return blocksToCheck.get(dimension);
        }

        private void clear() {
            valveToFrameBlocks.clear();
            valveToAirBlocks.clear();
            frameBlockToValve.clear();
            airBlockToValve.clear();
            blocksToCheck.clear();
        }
    }

    public TankManager() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private final HashMapCache CLIENT = new HashMapCache();
    private final HashMapCache SERVER = new HashMapCache();

    public HashMapCache get(@Nullable Level world) {
        return (world != null && world.isClientSide()) ? CLIENT : SERVER;
    }

    @OnlyIn(Dist.CLIENT)
    public void clear() {
        CLIENT.clear();
    }

    public void add(Level world, BlockPos valvePos, TreeMap<Integer, HashSet<LayerBlockPos>> airBlocks, TreeMap<Integer, HashSet<LayerBlockPos>> frameBlocks) {
        if (airBlocks.isEmpty() || frameBlocks.isEmpty()) {
            return;
        }

        BlockEntity tile = world.getBlockEntity(valvePos);
        if (!(tile instanceof AbstractTankValve)) {
            return;
        }

        if (!((AbstractTankValve) tile).isMain()) {
            return;
        }

        addIgnore(world, valvePos, airBlocks, frameBlocks);
    }

    public void addIgnore(Level world, BlockPos valvePos, TreeMap<Integer, HashSet<LayerBlockPos>> airBlocks, TreeMap<Integer, HashSet<LayerBlockPos>> frameBlocks) {
        get(world).getValveToAirBlocks(world).put(valvePos, airBlocks);
        for (int layer : airBlocks.keySet()) {
            for (LayerBlockPos pos : airBlocks.get(layer)) {
                get(world).getAirBlockToValve(world).put(pos, valvePos);
            }
        }

        get(world).getValveToFrameBlocks(world).put(valvePos, frameBlocks);
        for (int layer : frameBlocks.keySet()) {
            for (LayerBlockPos pos : frameBlocks.get(layer)) {
                get(world).getFrameBlockToValve(world).put(pos, valvePos);
            }
        }
    }

    public void remove(Level world, BlockPos valve) {
        get(world).getAirBlockToValve(world).values().removeAll(Collections.singleton(valve));
        get(world).getValveToAirBlocks(world).remove(valve);

        get(world).getFrameBlockToValve(world).values().removeAll(Collections.singleton(valve));
        get(world).getValveToFrameBlocks(world).remove(valve);
    }

    public void removeAllForDimension(Level world) {
        get(world).getValveToAirBlocks(world).clear();
        get(world).getValveToFrameBlocks(world).clear();
        get(world).getAirBlockToValve(world).clear();
        get(world).getFrameBlockToValve(world).clear();
        get(world).getBlocksToCheck(world).clear();
    }

    public AbstractTankValve getValveForBlock(Level world, BlockPos pos) {
        if (!isPartOfTank(world, pos)) {
            return null;
        }

        BlockEntity tile = null;
        if (get(world).getFrameBlockToValve(world).containsKey(pos)) {
            tile = world.getBlockEntity(get(world).getFrameBlockToValve(world).get(pos));
        } else if (get(world).getAirBlockToValve(world).containsKey(pos)) {
            tile = world.getBlockEntity(get(world).getAirBlockToValve(world).get(pos));
        }

        return tile instanceof AbstractTankValve ? (AbstractTankValve) tile : null;
    }

    public @Nullable
    TreeMap<Integer, HashSet<LayerBlockPos>> getAirBlocksForValve(AbstractTankValve valve) {
        Level world = valve.getLevel();

        if (world == null) {
            return null;
        }

        var valveToAirBlocks = get(world).getValveToAirBlocks(world);

        if (valveToAirBlocks.containsKey(valve.getBlockPos())) {
            return valveToAirBlocks.get(valve.getBlockPos());
        }

        return null;
    }

    public @Nonnull
    TreeMap<Integer, HashSet<LayerBlockPos>> getFrameBlocksForValve(AbstractTankValve valve) {
        Level world = valve.getLevel();

        if (world == null) {
            return new TreeMap<>();
        }

        var valveToAirBlocks = get(world).getValveToFrameBlocks(world);

        if (valveToAirBlocks.containsKey(valve.getBlockPos())) {
            return valveToAirBlocks.get(valve.getBlockPos());
        }

        return new TreeMap<>();
    }

    public @Nonnull
    HashSet<BlockPos> getAllFrameBlocksForValve(AbstractTankValve valve) {
        HashSet<BlockPos> allFrameBlocks = new HashSet<>();

        var layerFrameBlocks = getFrameBlocksForValve(valve);
        for (int layer : layerFrameBlocks.keySet()) {
            allFrameBlocks.addAll(layerFrameBlocks.get(layer));
        }

        return allFrameBlocks;
    }

    public boolean isValveInHashSets(Level world, AbstractTankValve valve) {
        return get(world).getValveToAirBlocks(world).containsKey(valve.getBlockPos());
    }

    public boolean isPartOfTank(Level world, BlockPos pos) {
        return get(world).getFrameBlockToValve(world).containsKey(pos)
                || get(world).getAirBlockToValve(world).containsKey(pos);
    }

    private void addBlockForCheck(LevelAccessor accessor, BlockPos pos) {
        if (accessor.isClientSide() || !(accessor instanceof Level world)) {
            return;
        }

        if (!isPartOfTank(world, pos)) {
            return;
        }

        get(world).getBlocksToCheck(world).add(pos);
    }

    // --- Events ---

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getLevel() == null || event.getEntity() == null) {
            return;
        }

        if (!(event.getEntity() instanceof PathfinderMob)) {
            return;
        }

        if (isPartOfTank(event.getLevel(), event.getEntity().blockPosition())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onServerTick(final TickEvent.LevelTickEvent event) {
        if (event.level.isClientSide()) {
            return;
        }

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Level level = event.level;

        if (get(level).getBlocksToCheck(level).isEmpty()) {
            return;
        }

        AbstractTankValve valve;
        for (BlockPos pos : get(level).getBlocksToCheck(level)) {
            if (isPartOfTank(level, pos)) {
                valve = getValveForBlock(level, pos);
                if (valve != null) {
                    if (!GenericUtil.isValidTankBlock(level, pos, level.getBlockState(pos), GenericUtil.getInsideForTankFrame(valve.getAirBlocks(), pos))) {
                        valve.breakTank();
                        break;
                    }
                }
            }
        }
        get(level).getBlocksToCheck(level).clear();
    }

    @SubscribeEvent
    public void onBlockBreak(final BlockEvent.BreakEvent event) {
        addBlockForCheck(event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public void onBlockPlace(final BlockEvent.EntityPlaceEvent event) {
        addBlockForCheck(event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        BlockPos pos = event.getPos();
        Level world = event.getLevel();
        Player player = event.getEntity();

        if (world.isClientSide()) {
            return;
        }

        if (player == null) {
            return;
        }

        if (!isPartOfTank(world, pos)) {
            return;
        }

        if (event.getHand() == InteractionHand.OFF_HAND) {
            event.setCanceled(true);
            return;
        }

        if (world.getBlockState(pos).getBlock() instanceof AbstractBlockValve) {
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity != null) {
            ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

            if (heldItem.isEmpty() && heldItem.getItem() != Items.tit.get()) {
                heldItem = player.getItemInHand(InteractionHand.OFF_HAND);

                if (heldItem.isEmpty() && heldItem.getItem() != Items.tit.get()) {
                    return;
                }
            }
        }

        player.swing(InteractionHand.MAIN_HAND, true);

        event.setCanceled(true);

        AbstractTankValve tile = getValveForBlock(world, pos);
        if (tile != null && tile.getMainValve() != null) {
            AbstractTankValve valve = tile.getMainValve();
            if (valve.isValid()) {
                if (GenericUtil.isFluidContainer(event.getItemStack())) {
                    if (GenericUtil.fluidContainerHandler(world, valve, player)) {
                        valve.markForUpdateNow();
                    }
                } else {
                    NetworkHandler.sendPacketToPlayer(new FFSPacket.Client.OpenGUI(tile, false), (ServerPlayer) player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onFillBucket(FillBucketEvent event) {
        if (event.getEntity().isShiftKeyDown()) {
            return;
        }

        if (event.getTarget() == null) {
            return;
        }

        if (event.getTarget().getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult rayTraceResult = (BlockHitResult) event.getTarget();

        BlockPos pos = rayTraceResult.getBlockPos();

        if (!isPartOfTank(event.getLevel(), pos)) {
            return;
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }

        NetworkHandler.sendPacketToPlayer(new FFSPacket.Client.ClearTanks(), (ServerPlayer) event.getEntity());
    }

    @SubscribeEvent
    public void onWorldUnload(LevelEvent.Unload event) {
        LevelAccessor iLevel = event.getLevel();

        if (iLevel.isClientSide() || !(iLevel instanceof Level world)) {
            return;
        }

        INSTANCE.removeAllForDimension(world);
    }

}
