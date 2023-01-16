package com.lordmau5.ffs.util;

import com.lordmau5.ffs.block.abstracts.AbstractBlockValve;
import com.lordmau5.ffs.network.FFSPacket;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
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

import javax.annotation.Nullable;
import java.util.*;

public class TankManager {

    public static TankManager INSTANCE = new TankManager();

    private static class HashMapCache {
        private final HashMap<ResourceKey<Level>, HashMap<BlockPos, TreeMap<Integer, List<LayerBlockPos>>>> valveToFrameBlocks = new HashMap<>();
        private final HashMap<ResourceKey<Level>, HashMap<BlockPos, TreeMap<Integer, List<LayerBlockPos>>>> valveToAirBlocks = new HashMap<>();
        private final HashMap<ResourceKey<Level>, HashMap<BlockPos, BlockPos>> frameBlockToValve = new HashMap<>();
        private final HashMap<ResourceKey<Level>, HashMap<BlockPos, BlockPos>> airBlockToValve = new HashMap<>();

        private final HashMap<ResourceKey<Level>, List<BlockPos>> blocksToCheck = new HashMap<>();

        private HashMap<BlockPos, TreeMap<Integer, List<LayerBlockPos>>> getValveToFrameBlocks(Level world) {
            ResourceKey<Level> dimension = world.dimension();

            valveToFrameBlocks.putIfAbsent(dimension, new HashMap<>());

            return valveToFrameBlocks.get(dimension);
        }

        private HashMap<BlockPos, TreeMap<Integer, List<LayerBlockPos>>> getValveToAirBlocks(Level world) {
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

        private List<BlockPos> getBlocksToCheck(Level world) {
            ResourceKey<Level> dimension = world.dimension();

            blocksToCheck.putIfAbsent(dimension, new ArrayList<>());

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

//    public static void initialize() {
//        MinecraftForge.EVENT_BUS.addListener(TankManager::entityJoinWorld);
//        MinecraftForge.EVENT_BUS.addListener(TankManager::onServerTick);
//        MinecraftForge.EVENT_BUS.addListener(TankManager::onBlockBreak);
//        MinecraftForge.EVENT_BUS.addListener(TankManager::onBlockPlace);
//        MinecraftForge.EVENT_BUS.addListener(TankManager::onRightClick);
//        MinecraftForge.EVENT_BUS.addListener(TankManager::onFillBucket);
//        MinecraftForge.EVENT_BUS.addListener(TankManager::onDimensionChange);
//        MinecraftForge.EVENT_BUS.addListener(TankManager::onWorldUnload);
//    }

    private final HashMapCache CLIENT = new HashMapCache();
    private final HashMapCache SERVER = new HashMapCache();

    public HashMapCache get(@Nullable Level world) {
        return (world != null && world.isClientSide) ? CLIENT : SERVER;
    }

    @OnlyIn(Dist.CLIENT)
    public void clear() {
        CLIENT.clear();
    }

    public void add(Level world, BlockPos valvePos, TreeMap<Integer, List<LayerBlockPos>> airBlocks, TreeMap<Integer, List<LayerBlockPos>> frameBlocks) {
        if ( airBlocks.isEmpty() ) {
            return;
        }

        BlockEntity tile = world.getBlockEntity(valvePos);
        if ( !(tile instanceof AbstractTankValve) ) {
            return;
        }

        if ( !((AbstractTankValve) tile).isMain() ) {
            return;
        }

        addIgnore(world, valvePos, airBlocks, frameBlocks);
    }

    public void addIgnore(Level world, BlockPos valvePos, TreeMap<Integer, List<LayerBlockPos>> airBlocks, TreeMap<Integer, List<LayerBlockPos>> frameBlocks) {
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
        if ( !isPartOfTank(world, pos) ) {
            return null;
        }

        BlockEntity tile = null;
        if ( get(world).getFrameBlockToValve(world).containsKey(pos) ) {
            tile = world.getBlockEntity(get(world).getFrameBlockToValve(world).get(pos));
        } else if ( get(world).getAirBlockToValve(world).containsKey(pos) ) {
            tile = world.getBlockEntity(get(world).getAirBlockToValve(world).get(pos));
        }

        return tile instanceof AbstractTankValve ? (AbstractTankValve) tile : null;
    }

    public List<BlockPos> getFrameBlocksForValve(AbstractTankValve valve) {
        Level world = valve.getLevel();

        List<BlockPos> blocks = new ArrayList<>();
        if ( get(world).getValveToFrameBlocks(world).containsKey(valve.getBlockPos()) ) {
            for (int layer : get(world).getValveToFrameBlocks(world).get(valve.getBlockPos()).keySet()) {
                blocks.addAll(get(world).getValveToFrameBlocks(world).get(valve.getBlockPos()).get(layer));
            }
        }

        return blocks;
    }

    public TreeMap<Integer, List<LayerBlockPos>> getAirBlocksForValve(AbstractTankValve valve) {
        Level world = valve.getLevel();

        var valveToAirBlocks = get(world).getValveToAirBlocks(world);

        if ( valveToAirBlocks.containsKey(valve.getBlockPos()) ) {
            return valveToAirBlocks.get(valve.getBlockPos());
        }

        return null;
    }

    public boolean isValveInLists(Level world, AbstractTankValve valve) {
        return get(world).getValveToAirBlocks(world).containsKey(valve.getBlockPos());
    }

    public boolean isPartOfTank(Level world, BlockPos pos) {
        return get(world).getFrameBlockToValve(world).containsKey(pos)
                || get(world).getAirBlockToValve(world).containsKey(pos);
    }

    private void addBlockForCheck(LevelAccessor accessor, BlockPos pos) {
        if ( accessor.isClientSide() || !(accessor instanceof Level world) ) {
            return;
        }

        if ( !isPartOfTank(world, pos) ) {
            return;
        }

        get(world).getBlocksToCheck(world).add(pos);
    }

    // --- Events ---

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent event) {
        if ( event.getWorld() == null || event.getEntity() == null ) {
            return;
        }

        if ( !(event.getEntity() instanceof PathfinderMob) ) {
            return;
        }

        if ( isPartOfTank(event.getWorld(), event.getEntity().blockPosition()) ) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onServerTick(final TickEvent.WorldTickEvent event) {
        if ( event.world.isClientSide ) {
            return;
        }

        if ( event.phase != TickEvent.Phase.END ) {
            return;
        }

        Level world = event.world;

        if (get(world).getBlocksToCheck(world).isEmpty() ) {
            return;
        }

        AbstractTankValve valve;
        for (BlockPos pos : get(world).getBlocksToCheck(world)) {
            if ( isPartOfTank(event.world, pos) ) {
                valve = getValveForBlock(event.world, pos);
                if ( valve != null ) {
                    if ( !GenericUtil.isValidTankBlock(event.world, pos, event.world.getBlockState(pos), GenericUtil.getInsideForTankFrame(valve.getAirBlocks(), pos)) ) {
                        valve.breakTank();
                        break;
                    }
                }
            }
        }
        get(world).getBlocksToCheck(world).clear();
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
        Level world = event.getWorld();
        Player player = event.getPlayer();

        if ( player == null ) {
            return;
        }

        if ( !isPartOfTank(world, pos) ) {
            return;
        }

        if ( event.getHand() == InteractionHand.OFF_HAND ) {
            event.setCanceled(true);
            return;
        }

        if ( world.isClientSide ) {
            player.swing(InteractionHand.MAIN_HAND);
        }

        if ( world.getBlockState(pos).getBlock() instanceof AbstractBlockValve ) {
            return;
        }

        if ( player.isShiftKeyDown() ) {
            ItemStack mainHand = player.getMainHandItem();
            if ( mainHand != ItemStack.EMPTY ) {
                if ( player.isCreative() ) {
                    mainHand = mainHand.copy();
                }
//                mainHand.onItemUse(player, world, pos, Hand.MAIN_HAND, event.getFace(), (float) event.getHitVec().x, (float) event.getHitVec().y, (float) event.getHitVec().z);
            }
            return;
        }

        event.setCanceled(true);

        AbstractTankValve tile = getValveForBlock(world, pos);
        if ( tile != null && tile.getMainValve() != null ) {
            AbstractTankValve valve = tile.getMainValve();
            if ( valve.isValid() ) {
                if ( GenericUtil.isFluidContainer(event.getItemStack()) ) {
                    if ( GenericUtil.fluidContainerHandler(world, valve, player) ) {
                        valve.markForUpdateNow();
                    }
                } else {
                    if (!world.isClientSide) {
                        NetworkHandler.sendPacketToPlayer(new FFSPacket.Client.OpenGUI(tile, false), (ServerPlayer) player);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onFillBucket(FillBucketEvent event) {
        if ( event.getEntity().isShiftKeyDown() ) {
            return;
        }

        if ( event.getTarget() == null ) {
            return;
        }

        if (event.getTarget().getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult rayTraceResult = (BlockHitResult) event.getTarget();

        BlockPos pos = rayTraceResult.getBlockPos();

        if ( !isPartOfTank(event.getWorld(), pos) ) {
            return;
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer)) {
            return;
        }

        NetworkHandler.sendPacketToPlayer(new FFSPacket.Client.ClearTanks(), (ServerPlayer) event.getPlayer());
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        LevelAccessor iWorld = event.getWorld();

        if (iWorld.isClientSide() || !(iWorld instanceof Level world) ) {
            return;
        }

        INSTANCE.removeAllForDimension(world);
    }

}
