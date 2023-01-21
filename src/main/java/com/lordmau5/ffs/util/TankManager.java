package com.lordmau5.ffs.util;

import com.lordmau5.ffs.block.abstracts.AbstractBlockValve;
import com.lordmau5.ffs.holder.Items;
import com.lordmau5.ffs.network.FFSPacket;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
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
        private final HashMap<RegistryKey<World>, HashMap<BlockPos, TreeMap<Integer, HashSet<LayerBlockPos>>>> valveToFrameBlocks = new HashMap<>();
        private final HashMap<RegistryKey<World>, HashMap<BlockPos, TreeMap<Integer, HashSet<LayerBlockPos>>>> valveToAirBlocks = new HashMap<>();
        private final HashMap<RegistryKey<World>, HashMap<BlockPos, BlockPos>> frameBlockToValve = new HashMap<>();
        private final HashMap<RegistryKey<World>, HashMap<BlockPos, BlockPos>> airBlockToValve = new HashMap<>();

        private final HashMap<RegistryKey<World>, HashSet<BlockPos>> blocksToCheck = new HashMap<>();

        private HashMap<BlockPos, TreeMap<Integer, HashSet<LayerBlockPos>>> getValveToFrameBlocks(World world) {
            RegistryKey<World> dimension = world.dimension();

            valveToFrameBlocks.putIfAbsent(dimension, new HashMap<>());

            return valveToFrameBlocks.get(dimension);
        }

        private HashMap<BlockPos, TreeMap<Integer, HashSet<LayerBlockPos>>> getValveToAirBlocks(World world) {
            RegistryKey<World> dimension = world.dimension();

            valveToAirBlocks.putIfAbsent(dimension, new HashMap<>());

            return valveToAirBlocks.get(dimension);
        }

        private HashMap<BlockPos, BlockPos> getFrameBlockToValve(World world) {
            RegistryKey<World> dimension = world.dimension();

            frameBlockToValve.putIfAbsent(dimension, new HashMap<>());

            return frameBlockToValve.get(dimension);
        }

        private HashMap<BlockPos, BlockPos> getAirBlockToValve(World world) {
            RegistryKey<World> dimension = world.dimension();

            airBlockToValve.putIfAbsent(dimension, new HashMap<>());

            return airBlockToValve.get(dimension);
        }

        private HashSet<BlockPos> getBlocksToCheck(World world) {
            RegistryKey<World> dimension = world.dimension();

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

    public HashMapCache get(@Nullable World world) {
        return (world != null && world.isClientSide()) ? CLIENT : SERVER;
    }

    @OnlyIn(Dist.CLIENT)
    public void clear() {
        CLIENT.clear();
    }

    public void add(World world, BlockPos valvePos, TreeMap<Integer, HashSet<LayerBlockPos>> airBlocks, TreeMap<Integer, HashSet<LayerBlockPos>> frameBlocks) {
        if (airBlocks.isEmpty() || frameBlocks.isEmpty()) {
            return;
        }

        TileEntity tile = world.getBlockEntity(valvePos);
        if (!(tile instanceof AbstractTankValve)) {
            return;
        }

        if (!((AbstractTankValve) tile).isMain()) {
            return;
        }

        addIgnore(world, valvePos, airBlocks, frameBlocks);
    }

    public void addIgnore(World world, BlockPos valvePos, TreeMap<Integer, HashSet<LayerBlockPos>> airBlocks, TreeMap<Integer, HashSet<LayerBlockPos>> frameBlocks) {
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

    public void remove(World world, BlockPos valve) {
        get(world).getAirBlockToValve(world).values().removeAll(Collections.singleton(valve));
        get(world).getValveToAirBlocks(world).remove(valve);

        get(world).getFrameBlockToValve(world).values().removeAll(Collections.singleton(valve));
        get(world).getValveToFrameBlocks(world).remove(valve);
    }

    public void removeAllForDimension(World world) {
        get(world).getValveToAirBlocks(world).clear();
        get(world).getValveToFrameBlocks(world).clear();
        get(world).getAirBlockToValve(world).clear();
        get(world).getFrameBlockToValve(world).clear();
        get(world).getBlocksToCheck(world).clear();
    }

    public AbstractTankValve getValveForBlock(World world, BlockPos pos) {
        if (!isPartOfTank(world, pos)) {
            return null;
        }

        TileEntity tile = null;
        if (get(world).getFrameBlockToValve(world).containsKey(pos)) {
            tile = world.getBlockEntity(get(world).getFrameBlockToValve(world).get(pos));
        } else if (get(world).getAirBlockToValve(world).containsKey(pos)) {
            tile = world.getBlockEntity(get(world).getAirBlockToValve(world).get(pos));
        }

        return tile instanceof AbstractTankValve ? (AbstractTankValve) tile : null;
    }

    public @Nullable
    TreeMap<Integer, HashSet<LayerBlockPos>> getAirBlocksForValve(AbstractTankValve valve) {
        World world = valve.getLevel();

        if (world == null) {
            return null;
        }

        HashMap<BlockPos, TreeMap<Integer, HashSet<LayerBlockPos>>> valveToAirBlocks = get(world).getValveToAirBlocks(world);

        if (valveToAirBlocks.containsKey(valve.getBlockPos())) {
            return valveToAirBlocks.get(valve.getBlockPos());
        }

        return null;
    }

    public @Nonnull
    TreeMap<Integer, HashSet<LayerBlockPos>> getFrameBlocksForValve(AbstractTankValve valve) {
        World world = valve.getLevel();

        if (world == null) {
            return new TreeMap<>();
        }

        HashMap<BlockPos, TreeMap<Integer, HashSet<LayerBlockPos>>> valveToAirBlocks = get(world).getValveToFrameBlocks(world);

        if (valveToAirBlocks.containsKey(valve.getBlockPos())) {
            return valveToAirBlocks.get(valve.getBlockPos());
        }

        return new TreeMap<>();
    }

    public @Nonnull
    HashSet<BlockPos> getAllFrameBlocksForValve(AbstractTankValve valve) {
        HashSet<BlockPos> allFrameBlocks = new HashSet<>();

        TreeMap<Integer, HashSet<LayerBlockPos>> layerFrameBlocks = getFrameBlocksForValve(valve);
        for (int layer : layerFrameBlocks.keySet()) {
            allFrameBlocks.addAll(layerFrameBlocks.get(layer));
        }

        return allFrameBlocks;
    }

    public boolean isValveInHashSets(World world, AbstractTankValve valve) {
        return get(world).getValveToAirBlocks(world).containsKey(valve.getBlockPos());
    }

    public boolean isPartOfTank(World world, BlockPos pos) {
        return get(world).getFrameBlockToValve(world).containsKey(pos)
                || get(world).getAirBlockToValve(world).containsKey(pos);
    }

    private void addBlockForCheck(IWorld accessor, BlockPos pos) {
        if (accessor.isClientSide() || !(accessor instanceof World)) {
            return;
        }

        World world = (World) accessor;

        if (!isPartOfTank(world, pos)) {
            return;
        }

        get(world).getBlocksToCheck(world).add(pos);
    }

    // --- Events ---

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld() == null || event.getEntity() == null) {
            return;
        }

        if (!(event.getEntity() instanceof CreatureEntity)) {
            return;
        }

        if (isPartOfTank(event.getWorld(), event.getEntity().blockPosition())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onServerTick(final TickEvent.WorldTickEvent event) {
        if (event.world.isClientSide()) {
            return;
        }

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        World level = event.world;

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
        addBlockForCheck(event.getWorld(), event.getPos());
    }

    @SubscribeEvent
    public void onBlockPlace(final BlockEvent.EntityPlaceEvent event) {
        addBlockForCheck(event.getWorld(), event.getPos());
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        BlockPos pos = event.getPos();
        World world = event.getWorld();
        PlayerEntity player = event.getPlayer();

        if (world.isClientSide()) {
            return;
        }

        if (player == null) {
            return;
        }

        if (!isPartOfTank(world, pos)) {
            return;
        }

        if (event.getHand() == Hand.OFF_HAND) {
            event.setCanceled(true);
            return;
        }

        if (world.getBlockState(pos).getBlock() instanceof AbstractBlockValve) {
            return;
        }

        TileEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity != null) {
            ItemStack heldItem = player.getItemInHand(Hand.MAIN_HAND);

            if (heldItem.isEmpty() && heldItem.getItem() != Items.tit.getItem()) {
                heldItem = player.getItemInHand(Hand.OFF_HAND);

                if (heldItem.isEmpty() && heldItem.getItem() != Items.tit.getItem()) {
                    return;
                }
            }
        }

        if (player.isShiftKeyDown()) {
            return;
        }

        player.swing(Hand.MAIN_HAND, true);

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
                    NetworkHandler.sendPacketToPlayer(new FFSPacket.Client.OpenGUI(tile, false), (ServerPlayerEntity) player);
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

        if (event.getTarget().getType() != RayTraceResult.Type.BLOCK) {
            return;
        }

        BlockRayTraceResult rayTraceResult = (BlockRayTraceResult) event.getTarget();

        BlockPos pos = rayTraceResult.getBlockPos();

        if (!isPartOfTank(event.getWorld(), pos)) {
            return;
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayerEntity)) {
            return;
        }

        NetworkHandler.sendPacketToPlayer(new FFSPacket.Client.ClearTanks(), (ServerPlayerEntity) event.getEntity());
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        IWorld iLevel = event.getWorld();

        if (iLevel.isClientSide() || !(iLevel instanceof World)) {
            return;
        }

        INSTANCE.removeAllForDimension((World) iLevel);
    }

}