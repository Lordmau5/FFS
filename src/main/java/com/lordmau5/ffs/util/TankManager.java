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
        CLIENT.valveToFrameBlocks.clear();
        CLIENT.valveToAirBlocks.clear();
        CLIENT.frameBlockToValve.clear();
        CLIENT.airBlockToValve.clear();
        CLIENT.blocksToCheck.clear();

        System.out.println("Cleared tanks!");
    }

    private IWorld getDimensionSafely(World world) {
        get(world).valveToFrameBlocks.putIfAbsent(world.getDimensionKey(), new WeakHashMap<>());
        get(world).valveToAirBlocks.putIfAbsent(world.getDimensionKey(), new WeakHashMap<>());
        get(world).frameBlockToValve.putIfAbsent(world.getDimensionKey(), new WeakHashMap<>());
        get(world).airBlockToValve.putIfAbsent(world.getDimensionKey(), new WeakHashMap<>());
        get(world).blocksToCheck.putIfAbsent(world.getDimensionKey(), new ArrayList<>());

        return world;
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
        getDimensionSafely(world);

        get(world).valveToAirBlocks.get(world.getDimensionKey()).put(valvePos, airBlocks);
        for (int layer : airBlocks.keySet()) {
            for (LayerBlockPos pos : airBlocks.get(layer)) {
                get(world).airBlockToValve.get(world.getDimensionKey()).put(pos, valvePos);
            }
        }

        get(world).valveToFrameBlocks.get(world.getDimensionKey()).put(valvePos, frameBlocks);
        for (int layer : frameBlocks.keySet()) {
            for (LayerBlockPos pos : frameBlocks.get(layer)) {
                get(world).frameBlockToValve.get(world.getDimensionKey()).put(pos, valvePos);
            }
        }
    }

    public void remove(World world, BlockPos valve) {
        getDimensionSafely(world);

        get(world).airBlockToValve.get(world.getDimensionKey()).values().removeAll(Collections.singleton(valve));
        get(world).valveToAirBlocks.get(world.getDimensionKey()).remove(valve);

        get(world).frameBlockToValve.get(world.getDimensionKey()).values().removeAll(Collections.singleton(valve));
        get(world).valveToFrameBlocks.get(world.getDimensionKey()).remove(valve);
    }

    public void removeAllForDimension(World world) {
        getDimensionSafely(world);

        get(world).valveToAirBlocks.get(world.getDimensionKey()).clear();
        get(world).valveToFrameBlocks.get(world.getDimensionKey()).clear();
        get(world).airBlockToValve.get(world.getDimensionKey()).clear();
        get(world).frameBlockToValve.get(world.getDimensionKey()).clear();
        get(world).blocksToCheck.get(world.getDimensionKey()).clear();
    }

    public AbstractTankValve getValveForBlock(World world, BlockPos pos) {
        if ( !isPartOfTank(world, pos) ) {
            return null;
        }

        getDimensionSafely(world);

        TileEntity tile = null;
        if ( get(world).frameBlockToValve.get(world.getDimensionKey()).containsKey(pos) ) {
            tile = world.getTileEntity(get(world).frameBlockToValve.get(world.getDimensionKey()).get(pos));
        } else if ( get(world).airBlockToValve.get(world.getDimensionKey()).containsKey(pos) ) {
            tile = world.getTileEntity(get(world).airBlockToValve.get(world.getDimensionKey()).get(pos));
        }

        return tile instanceof AbstractTankValve ? (AbstractTankValve) tile : null;
    }

    public List<BlockPos> getFrameBlocksForValve(AbstractTankValve valve) {
        World world = valve.getWorld();

        getDimensionSafely(world);

        List<BlockPos> blocks = new ArrayList<>();
        if ( get(world).valveToFrameBlocks.get(world.getDimensionKey()).containsKey(valve.getPos()) ) {
            for (int layer : get(world).valveToFrameBlocks.get(world.getDimensionKey()).get(valve.getPos()).keySet()) {
                blocks.addAll(get(world).valveToFrameBlocks.get(world.getDimensionKey()).get(valve.getPos()).get(layer));
            }
        }

        return blocks;
    }

    public TreeMap<Integer, List<LayerBlockPos>> getAirBlocksForValve(AbstractTankValve valve) {
        World world = valve.getWorld();

        getDimensionSafely(world);

        if ( get(world).valveToAirBlocks.get(world.getDimensionKey()).containsKey(valve.getPos()) ) {
            return get(world).valveToAirBlocks.get(world.getDimensionKey()).get(valve.getPos());
        }

        return null;
    }

    public boolean isValveInLists(World world, AbstractTankValve valve) {
        getDimensionSafely(world);

        return get(world).valveToAirBlocks.get(world.getDimensionKey()).containsKey(valve.getPos());
    }

    public boolean isPartOfTank(World world, BlockPos pos) {
        getDimensionSafely(world);

        boolean ret = get(world).frameBlockToValve.getOrDefault(world.getDimensionKey(), new WeakHashMap<>()).containsKey(pos)
                || get(world).airBlockToValve.getOrDefault(world.getDimensionKey(), new WeakHashMap<>()).containsKey(pos);

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
        if ( get(world).blocksToCheck.isEmpty() || get(world).blocksToCheck.get(world.getDimensionKey()) == null || get(world).blocksToCheck.get(world.getDimensionKey()).isEmpty() ) {
            return;
        }

        AbstractTankValve valve;
        for (BlockPos pos : get(world).blocksToCheck.get(world.getDimensionKey())) {
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
        get(world).blocksToCheck.get(world.getDimensionKey()).clear();
    }

    private void addBlockForCheck(World world, BlockPos pos) {
        getDimensionSafely(world);

        List<BlockPos> blocks = get(world).blocksToCheck.get(world.getDimensionKey());
        if ( blocks == null ) {
            blocks = new ArrayList<>();
        }

        blocks.add(pos);
        get(world).blocksToCheck.put(world.getDimensionKey(), blocks);
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
//                    player.openGui(FancyFluidStorage.INSTANCE, 1, tile.getWorld(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
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
