package com.lordmau5.ffs.util;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.block.abstracts.AbstractBlockValve;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.*;

/**
 * Created by Lordmau5 on 06.10.2016.
 */
public class TankManager
{
    private final WeakHashMap<Integer, WeakHashMap<BlockPos, TreeMap<Integer, List<LayerBlockPos>>>> valveToFrameBlocks = new WeakHashMap<>();
    private final WeakHashMap<Integer, WeakHashMap<BlockPos, TreeMap<Integer, List<LayerBlockPos>>>> valveToAirBlocks = new WeakHashMap<>();
    private final WeakHashMap<Integer, WeakHashMap<BlockPos, BlockPos>> frameBlockToValve = new WeakHashMap<>();
    private final WeakHashMap<Integer, WeakHashMap<BlockPos, BlockPos>> airBlockToValve = new WeakHashMap<>();

    private final WeakHashMap<Integer, List<BlockPos>> blocksToCheck = new WeakHashMap<>();

    public TankManager() {}

    private int getDimensionSafely(World world)
    {
        return getDimensionSafely(world.provider.getDimension());
    }

    private int getDimensionSafely(int dimensionId)
    {
        valveToFrameBlocks.putIfAbsent(dimensionId, new WeakHashMap<>());
        valveToAirBlocks.putIfAbsent(dimensionId, new WeakHashMap<>());
        frameBlockToValve.putIfAbsent(dimensionId, new WeakHashMap<>());
        airBlockToValve.putIfAbsent(dimensionId, new WeakHashMap<>());
        blocksToCheck.putIfAbsent(dimensionId, new ArrayList<>());
        return dimensionId;
    }

    public void add(World world, BlockPos valvePos, TreeMap<Integer, List<LayerBlockPos>> airBlocks, TreeMap<Integer, List<LayerBlockPos>> frameBlocks)
    {
        if (airBlocks.isEmpty())
        {
            return;
        }

        TileEntity tile = world.getTileEntity(valvePos);
        if (tile == null || !(tile instanceof AbstractTankValve))
        {
            return;
        }

        if (!((AbstractTankValve) tile).isMaster())
        {
            return;
        }

        addIgnore(world.provider.getDimension(), valvePos, airBlocks, frameBlocks);
    }

    public void addIgnore(int dimensionId, BlockPos valvePos, TreeMap<Integer, List<LayerBlockPos>> airBlocks, TreeMap<Integer, List<LayerBlockPos>> frameBlocks)
    {
        dimensionId = getDimensionSafely(dimensionId);

        valveToAirBlocks.get(dimensionId).put(valvePos, airBlocks);
        for (int layer : airBlocks.keySet())
        {
            for (LayerBlockPos pos : airBlocks.get(layer))
            {
                airBlockToValve.get(dimensionId).put(pos, valvePos);
            }
        }

        valveToFrameBlocks.get(dimensionId).put(valvePos, frameBlocks);
        for (int layer : frameBlocks.keySet())
        {
            for (LayerBlockPos pos : frameBlocks.get(layer))
            {
                frameBlockToValve.get(dimensionId).put(pos, valvePos);
            }
        }
    }

    public void remove(int dimensionId, BlockPos valve)
    {
        dimensionId = getDimensionSafely(dimensionId);

        airBlockToValve.get(dimensionId).values().removeAll(Collections.singleton(valve));
        valveToAirBlocks.get(dimensionId).remove(valve);

        frameBlockToValve.get(dimensionId).values().removeAll(Collections.singleton(valve));
        valveToFrameBlocks.get(dimensionId).remove(valve);
    }

    public void removeAllForDimension(int dimensionId)
    {
        dimensionId = getDimensionSafely(dimensionId);

        valveToAirBlocks.get(dimensionId).clear();
        valveToFrameBlocks.get(dimensionId).clear();
        airBlockToValve.get(dimensionId).clear();
        frameBlockToValve.get(dimensionId).clear();
        blocksToCheck.get(dimensionId).clear();
    }

    public AbstractTankValve getValveForBlock(World world, BlockPos pos)
    {
        if (!isPartOfTank(world, pos))
        {
            return null;
        }

        int dimensionId = getDimensionSafely(world);

        TileEntity tile = null;
        if (frameBlockToValve.get(dimensionId).containsKey(pos))
        {
            tile = world.getTileEntity(frameBlockToValve.get(dimensionId).get(pos));
        } else if (airBlockToValve.get(dimensionId).containsKey(pos))
        {
            tile = world.getTileEntity(airBlockToValve.get(dimensionId).get(pos));
        }

        return tile instanceof AbstractTankValve ? (AbstractTankValve) tile : null;
    }

    public List<BlockPos> getFrameBlocksForValve(AbstractTankValve valve)
    {
        int dimensionId = getDimensionSafely(valve.getWorld());

        List<BlockPos> blocks = new ArrayList<>();
        if (valveToFrameBlocks.get(dimensionId).containsKey(valve.getPos()))
        {
            for (int layer : valveToFrameBlocks.get(dimensionId).get(valve.getPos()).keySet())
            {
                blocks.addAll(valveToFrameBlocks.get(dimensionId).get(valve.getPos()).get(layer));
            }
        }

        return blocks;
    }

    public TreeMap<Integer, List<LayerBlockPos>> getAirBlocksForValve(AbstractTankValve valve)
    {
        int dimensionId = getDimensionSafely(valve.getWorld());

        if (valveToAirBlocks.get(dimensionId).containsKey(valve.getPos()))
        {
            return valveToAirBlocks.get(dimensionId).get(valve.getPos());
        }

        return null;
    }

    public boolean isValveInLists(World world, AbstractTankValve valve)
    {
        int dimensionId = getDimensionSafely(world);

        return valveToAirBlocks.get(dimensionId).containsKey(valve.getPos());
    }

    public boolean isPartOfTank(World world, BlockPos pos)
    {
        int dimensionId = getDimensionSafely(world);

        return frameBlockToValve.get(dimensionId).containsKey(pos) || airBlockToValve.get(dimensionId).containsKey(pos);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.WorldTickEvent event)
    {
        if (event.world.isRemote)
        {
            return;
        }

        if (event.phase != TickEvent.Phase.END)
        {
            return;
        }

        int dimensionId = event.world.provider.getDimension();
        if (blocksToCheck.isEmpty() || blocksToCheck.get(dimensionId) == null || blocksToCheck.get(dimensionId).isEmpty())
        {
            return;
        }

        AbstractTankValve valve;
        for (BlockPos pos : blocksToCheck.get(dimensionId))
        {
            if (isPartOfTank(event.world, pos))
            {
                valve = getValveForBlock(event.world, pos);
                if (valve != null)
                {
                    if (!GenericUtil.isValidTankBlock(event.world, pos, event.world.getBlockState(pos), GenericUtil.getInsideForTankFrame(valve.getAirBlocks(), pos)))
                    {
                        valve.breakTank();
                        break;
                    }
                }
            }
        }
        blocksToCheck.get(dimensionId).clear();
    }

    private void addBlockForCheck(World world, BlockPos pos)
    {
        int dimensionId = world.provider.getDimension();
        List<BlockPos> blocks = blocksToCheck.get(dimensionId);
        if (blocks == null)
        {
            blocks = new ArrayList<>();
        }

        blocks.add(pos);
        blocksToCheck.put(dimensionId, blocks);
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event)
    {
        if (event.getWorld().isRemote)
        {
            return;
        }

        BlockPos pos = event.getPos();

        if (!isPartOfTank(event.getWorld(), pos))
        {
            return;
        }

        addBlockForCheck(event.getWorld(), pos);
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.PlaceEvent event)
    {
        if (event.getWorld().isRemote)
        {
            return;
        }

        BlockPos pos = event.getPos();

        if (!isPartOfTank(event.getWorld(), pos))
        {
            return;
        }

        addBlockForCheck(event.getWorld(), pos);
    }

    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickBlock event)
    {
        BlockPos pos = event.getPos();

        if (event.getEntityPlayer() == null)
        {
            return;
        }

        if (!isPartOfTank(event.getWorld(), pos))
        {
            return;
        }

        if (event.getHand() == EnumHand.OFF_HAND)
        {
            event.setCanceled(true);
            return;
        }

        if (event.getWorld().isRemote)
        {
            event.getEntityPlayer().swingArm(EnumHand.MAIN_HAND);
        }

        if (event.getEntityPlayer().getHeldItemOffhand() != null)
        {
            if (event.getEntityPlayer().getHeldItemOffhand().getItem() == FancyFluidStorage.itemTit)
            {
                TileEntity tile = event.getWorld().getTileEntity(event.getPos());
                if (tile != null)
                {
                    addBlockForCheck(event.getWorld(), pos);
                    return;
                }
            }
        }

        if (event.getWorld().getBlockState(event.getPos()).getBlock() instanceof AbstractBlockValve)
        {
            return;
        }

        event.setCanceled(true);

        if (event.getEntityPlayer().isSneaking())
        {
            ItemStack mainHand = event.getEntityPlayer().getHeldItemMainhand();
            if (mainHand != null)
            {
                if (event.getEntityPlayer().isCreative())
                {
                    mainHand = mainHand.copy();
                }
                mainHand.onItemUse(event.getEntityPlayer(), event.getWorld(), event.getPos(), EnumHand.MAIN_HAND, event.getFace(), (float) event.getHitVec().x, (float) event.getHitVec().y, (float) event.getHitVec().z);
                event.getEntityPlayer().swingArm(EnumHand.MAIN_HAND);
            }
            return;
        }

        AbstractTankValve tile = getValveForBlock(event.getWorld(), pos);
        if (tile != null && tile.getMasterValve() != null)
        {
            AbstractTankValve valve = tile.getMasterValve();
            if (valve.isValid())
            {
                event.getEntityPlayer().swingArm(EnumHand.MAIN_HAND);
                if (GenericUtil.isFluidContainer(event.getItemStack()))
                {
                    GenericUtil.fluidContainerHandler(event.getWorld(), valve, event.getEntityPlayer());
                } else
                {
                    event.getEntityPlayer().openGui(FancyFluidStorage.INSTANCE, 1, tile.getWorld(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());
                }
            }
        }
    }

    @SubscribeEvent
    public void onFillBucket(FillBucketEvent event)
    {
        if (event.getEntityPlayer().isSneaking())
        {
            return;
        }

        if (event.getTarget() == null)
        {
            return;
        }

        BlockPos pos = event.getTarget().getBlockPos();

        if (!isPartOfTank(event.getWorld(), pos))
        {
            return;
        }

        event.setCanceled(true);
    }

}
