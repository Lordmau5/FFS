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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.*;

public class TankManager {

    private static class HashMapCache {
        private final WeakHashMap<RegistryKey<World>, WeakHashMap<BlockPos, TreeMap<Integer, List<LayerBlockPos>>>> valveToFrameBlocks = new WeakHashMap<>();
        private final WeakHashMap<RegistryKey<World>, WeakHashMap<BlockPos, TreeMap<Integer, List<LayerBlockPos>>>> valveToAirBlocks = new WeakHashMap<>();
        private final WeakHashMap<RegistryKey<World>, WeakHashMap<BlockPos, BlockPos>> frameBlockToValve = new WeakHashMap<>();
        private final WeakHashMap<RegistryKey<World>, WeakHashMap<BlockPos, BlockPos>> airBlockToValve = new WeakHashMap<>();

        private final WeakHashMap<RegistryKey<World>, List<BlockPos>> blocksToCheck = new WeakHashMap<>();

        private WeakHashMap<BlockPos, TreeMap<Integer, List<LayerBlockPos>>> getValveToFrameBlocks(World world) {
            RegistryKey<World> dimensionType = world.getDimensionKey();

            valveToFrameBlocks.putIfAbsent(dimensionType, new WeakHashMap<>());

            return valveToFrameBlocks.get(dimensionType);
        }

        private WeakHashMap<BlockPos, TreeMap<Integer, List<LayerBlockPos>>> getValveToAirBlocks(World world) {
            RegistryKey<World> dimensionType = world.getDimensionKey();

            valveToAirBlocks.putIfAbsent(dimensionType, new WeakHashMap<>());

            return valveToAirBlocks.get(dimensionType);
        }

        private WeakHashMap<BlockPos, BlockPos> getFrameBlockToValve(World world) {
            RegistryKey<World> dimensionType = world.getDimensionKey();

            frameBlockToValve.putIfAbsent(dimensionType, new WeakHashMap<>());

            return frameBlockToValve.get(dimensionType);
        }

        private WeakHashMap<BlockPos, BlockPos> getAirBlockToValve(World world) {
            RegistryKey<World> dimensionType = world.getDimensionKey();

            airBlockToValve.putIfAbsent(dimensionType, new WeakHashMap<>());

            return airBlockToValve.get(dimensionType);
        }

        private List<BlockPos> getBlocksToCheck(World world) {
            RegistryKey<World> dimensionType = world.getDimensionKey();

            blocksToCheck.putIfAbsent(dimensionType, new ArrayList<>());

            return blocksToCheck.get(dimensionType);
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
    }

    private static final HashMapCache CLIENT = new HashMapCache();
    private static final HashMapCache SERVER = new HashMapCache();

    public static HashMapCache get(@Nullable World world) {
        return (world != null && world.isRemote) ? CLIENT : SERVER;
    }

    @OnlyIn(Dist.CLIENT)
    public static void clear() {
        CLIENT.clear();
    }

    public void add(World world, BlockPos valvePos, TreeMap<Integer, List<LayerBlockPos>> airBlocks, TreeMap<Integer, List<LayerBlockPos>> frameBlocks) {
        if ( airBlocks.isEmpty() ) {
            return;
        }

        TileEntity tile = world.getTileEntity(valvePos);
        if ( !(tile instanceof AbstractTankValve) ) {
            return;
        }

        if ( !((AbstractTankValve) tile).isMain() ) {
            return;
        }

        addIgnore(world, valvePos, airBlocks, frameBlocks);
    }

    public void addIgnore(World world, BlockPos valvePos, TreeMap<Integer, List<LayerBlockPos>> airBlocks, TreeMap<Integer, List<LayerBlockPos>> frameBlocks) {
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
        if ( !isPartOfTank(world, pos) ) {
            return null;
        }

        TileEntity tile = null;
        if ( get(world).getFrameBlockToValve(world).containsKey(pos) ) {
            tile = world.getTileEntity(get(world).getFrameBlockToValve(world).get(pos));
        } else if ( get(world).getAirBlockToValve(world).containsKey(pos) ) {
            tile = world.getTileEntity(get(world).getAirBlockToValve(world).get(pos));
        }

        return tile instanceof AbstractTankValve ? (AbstractTankValve) tile : null;
    }

    public List<BlockPos> getFrameBlocksForValve(AbstractTankValve valve) {
        World world = valve.getWorld();

        List<BlockPos> blocks = new ArrayList<>();
        if ( get(world).getValveToFrameBlocks(world).containsKey(valve.getPos()) ) {
            for (int layer : get(world).getValveToFrameBlocks(world).get(valve.getPos()).keySet()) {
                blocks.addAll(get(world).getValveToFrameBlocks(world).get(valve.getPos()).get(layer));
            }
        }

        return blocks;
    }

    public TreeMap<Integer, List<LayerBlockPos>> getAirBlocksForValve(AbstractTankValve valve) {
        World world = valve.getWorld();

        if ( get(world).getValveToAirBlocks(world).containsKey(valve.getPos()) ) {
            return get(world).getValveToAirBlocks(world).get(valve.getPos());
        }

        return null;
    }

    public boolean isValveInLists(World world, AbstractTankValve valve) {
        return get(world).getValveToAirBlocks(world).containsKey(valve.getPos());
    }

    public boolean isPartOfTank(World world, BlockPos pos) {
        boolean ret = get(world).getFrameBlockToValve(world).containsKey(pos)
                || get(world).getAirBlockToValve(world).containsKey(pos);

        return ret;
    }

    @SubscribeEvent
    public void entityJoinWorld(EntityJoinWorldEvent event) {
        if ( event.getWorld() == null || event.getEntity() == null ) {
            return;
        }

        if ( !(event.getEntity() instanceof CreatureEntity) ) {
            return;
        }

        if ( isPartOfTank(event.getWorld(), event.getEntity().getPosition()) ) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.WorldTickEvent event) {
        if ( event.world.isRemote ) {
            return;
        }

        if ( event.phase != TickEvent.Phase.END ) {
            return;
        }

        World world = event.world;

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

    private void addBlockForCheck(World world, BlockPos pos) {
        get(world).getBlocksToCheck(world).add(pos);
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        IWorld world = event.getWorld();
        BlockPos pos = event.getPos();

        if ( world.isRemote() || !(world instanceof World) ) {
            return;
        }

        World wWorld = (World) world;

        if ( !isPartOfTank(wWorld, pos) ) {
            return;
        }

        addBlockForCheck(wWorld, pos);
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        IWorld world = event.getWorld();
        BlockPos pos = event.getPos();

        if ( world.isRemote() || !(world instanceof World) ) {
            return;
        }

        World wWorld = (World) world;

        if ( !isPartOfTank(wWorld, pos) ) {
            return;
        }

        addBlockForCheck(wWorld, pos);
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        BlockPos pos = event.getPos();
        World world = event.getWorld();
        PlayerEntity player = event.getPlayer();

        if ( player == null ) {
            return;
        }

        if ( !isPartOfTank(world, pos) ) {
            return;
        }

        if ( event.getHand() == Hand.OFF_HAND ) {
            event.setCanceled(true);
            return;
        }

        if ( world.isRemote ) {
            player.swingArm(Hand.MAIN_HAND);
        }

        if ( world.getBlockState(pos).getBlock() instanceof AbstractBlockValve ) {
            return;
        }

        if ( player.isSneaking() ) {
            ItemStack mainHand = player.getHeldItemMainhand();
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
                    if (!world.isRemote) {
                        NetworkHandler.sendPacketToPlayer(new FFSPacket.Client.OpenGUI(tile, false), (ServerPlayerEntity) player);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onFillBucket(FillBucketEvent event) {
        if ( event.getEntity().isSneaking() ) {
            return;
        }

        if ( event.getTarget() == null ) {
            return;
        }

        if (event.getTarget().getType() != RayTraceResult.Type.BLOCK) {
            return;
        }

        BlockRayTraceResult rayTraceResult = (BlockRayTraceResult) event.getTarget();

        BlockPos pos = rayTraceResult.getPos();

        if ( !isPartOfTank(event.getWorld(), pos) ) {
            return;
        }

        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayerEntity)) {
            return;
        }

        NetworkHandler.sendPacketToPlayer(new FFSPacket.Client.ClearTanks(), (ServerPlayerEntity) event.getPlayer());
    }

}
