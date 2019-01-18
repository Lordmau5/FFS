package com.lordmau5.ffs.tile.abstracts;

import com.google.common.collect.Lists;
import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.config.Config;
import com.lordmau5.ffs.network.FFSPacket;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.tile.interfaces.IFacingTile;
import com.lordmau5.ffs.tile.interfaces.INameableTile;
import com.lordmau5.ffs.tile.util.TankConfig;
import com.lordmau5.ffs.util.GenericUtil;
import com.lordmau5.ffs.util.LayerBlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Dustin on 21.01.2016.
 */
public abstract class AbstractTankValve extends AbstractTankTile implements IFacingTile, INameableTile {

    private final List<AbstractTankTile> tankTiles;
    private int initialWaitTick = 20;
    public final WeakHashMap<Integer, TreeMap<Integer, List<LayerBlockPos>>> maps;
    private TankConfig tankConfig;
    private boolean isValid;
    private boolean isMaster;
    private boolean initiated;

    // TANK LOGIC
    private int lastComparatorOut = 0;
    // ---------------
    private EntityPlayer buildPlayer;

    public AbstractTankValve() {
        tankTiles = new ArrayList<>();

        maps = new WeakHashMap<>();
        maps.put(0, new TreeMap<>());
        maps.put(1, new TreeMap<>());

        setValid(false);
        setValvePos(null);
    }

    @Override
    public void validate() {
        super.validate();
        initiated = true;
        initialWaitTick = 20;
    }

    @Override
    public void invalidate() {
        super.invalidate();

        breakTank();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();

        breakTank();
    }

    @Override
    public void update() {
        super.update();

        if ( getWorld().isRemote ) {
            return;
        }

        if ( initiated ) {
            if ( isMaster() ) {
//				if(bottomDiagFrame != null && topDiagFrame != null) { // Potential fix for huge-ass tanks not loading properly on master-valve chunk load.
//					Chunk chunkBottom = getWorld().getChunkFromBlockCoords(bottomDiagFrame);
//					Chunk chunkTop = getWorld().getChunkFromBlockCoords(topDiagFrame);
//
//					BlockPos pos_chunkBottom = new BlockPos(chunkBottom.xPosition, 0, chunkBottom.zPosition);
//					BlockPos pos_chunkTop = new BlockPos(chunkTop.xPosition, 0, chunkTop.zPosition);
//
//					BlockPos diff = pos_chunkTop.subtract(pos_chunkBottom);
//					for(int x = 0; x <= diff.getX(); x++) {
//						for(int z = 0; z <= diff.getZ(); z++) {
//							ForgeChunkManager.forceChunk(GenericUtil.getChunkLoadTicket(getWorld()), new ChunkPos(pos_chunkTop.getX() + x, pos_chunkTop.getZ() + z));
//						}
//					}
//
//					updateBlockAndNeighbors();
//				}
                if ( initialWaitTick-- <= 0 ) {
                    initiated = false;
                    buildTank(getTileFacing());
                    return;
                }
            }
        }

        if ( !isValid() )
            return;

        if ( !isMaster() && getMasterValve() == null ) {
            setValid(false);
            updateBlockAndNeighbors();
        }
    }

    public TreeMap<Integer, List<LayerBlockPos>> getFrameBlocks() {
        return maps.get(0);
    }

    public TreeMap<Integer, List<LayerBlockPos>> getAirBlocks() {
        return maps.get(1);
    }

    public TankConfig getTankConfig() {
        if ( !isMaster() && getMasterValve() != null && getMasterValve() != this ) {
            return getMasterValve().getTankConfig();
        }

        if ( this.tankConfig == null ) {
            this.tankConfig = new TankConfig(this);
        }

        return this.tankConfig;
    }

    private void setTankConfig(TankConfig tankConfig) {
        this.tankConfig = tankConfig;
    }

    public void toggleFluidLock(boolean state) {
        if ( !state ) {
            getTankConfig().unlockFluid();
        } else {
            if ( getTankConfig().getFluidStack() == null ) {
                return;
            }

            getTankConfig().lockFluid(getTankConfig().getFluidStack());
        }
        getMasterValve().setNeedsUpdate();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getTankTiles(Class<T> type) {
        List<T> tiles = tankTiles.stream().filter(p -> type.isAssignableFrom(p.getClass())).map(p -> (T) p).collect(Collectors.toList());
        if ( this.getClass().isAssignableFrom(type) ) {
            tiles.add((T) this);
        }

        return tiles;
    }

    public List<AbstractTankValve> getAllValves() {
        if ( !isMaster() && getMasterValve() != null && getMasterValve() != this ) {
            return getMasterValve().getAllValves();
        }

        List<AbstractTankValve> valves = getTankTiles(AbstractTankValve.class);
        valves.add(this);
        return valves;
    }

    /**
     * Let a player build a tank!
     *
     * @param player - The player that tries to build the tank
     * @param inside - The direction of the inside of the tank
     */
    public void buildTank_player(EntityPlayer player, EnumFacing inside) {
        if ( getWorld().isRemote ) {
            return;
        }

        buildPlayer = player;
        buildTank(inside);
    }

    /**
     * Let's build a tank!
     *
     * @param inside - The direction of the inside of the tank
     */
    private void buildTank(EnumFacing inside) {
        /**
         * Don't build if it's on the client!
         */
        if ( getWorld().isRemote ) {
            return;
        }

        /**
         * Let's first set the tank to be invalid,
         * since it should stay like that if the building fails.
         * Also, let's reset variables.
         */
        setValid(false);

        getTankConfig().setFluidCapacity(0);

        tankTiles.clear();

        /**
         * Now, set the inside direction according to the variable.
         */
        if ( inside != null ) {
            setTileFacing(inside);
        }

        /**
         * Actually setup the tank here
         */
        if ( !setupTank() ) {
            return;
        }

        /**
         * Just in case, set *initiated* to false again.
         * Also, update our neighbor blocks, e.g. pipes or similar.
         */
        initiated = false;
        buildPlayer = null;
        updateBlockAndNeighbors();
    }

    private void setTankTileFacing(TreeMap<Integer, List<LayerBlockPos>> airBlocks, TileEntity tankTile) {
        List<BlockPos> possibleAirBlocks = new ArrayList<>();
        for (EnumFacing dr : EnumFacing.VALUES) {
            if ( getWorld().isAirBlock(tankTile.getPos().offset(dr)) ) {
                possibleAirBlocks.add(tankTile.getPos().offset(dr));
            }
        }

        BlockPos insideAir = null;
        for (int layer : airBlocks.keySet()) {
            for (BlockPos pos : possibleAirBlocks) {
                if ( airBlocks.get(layer).contains(pos) ) {
                    insideAir = pos;
                    break;
                }
            }
        }

        if ( insideAir == null ) {
            return;
        }

        BlockPos dist = insideAir.subtract(tankTile.getPos());
        for (EnumFacing dr : EnumFacing.VALUES) {
            if ( dist.equals(new BlockPos(dr.getFrontOffsetX(), dr.getFrontOffsetY(), dr.getFrontOffsetZ())) ) {
                ((IFacingTile) tankTile).setTileFacing(dr);
                break;
            }
        }
    }

    private boolean searchAlgorithm() {
        int currentAirBlocks = 1, currentFrameBlocks = 0;
        int maxAirBlocks = Config.MAX_AIR_BLOCKS;
        BlockPos insidePos = getPos().offset(getTileFacing());

        Queue<BlockPos> to_check = new LinkedList<>();
        List<BlockPos> checked_blocks = new ArrayList<>();
        TreeMap<Integer, List<LayerBlockPos>> air_blocks = new TreeMap<>();
        TreeMap<Integer, List<LayerBlockPos>> frame_blocks = new TreeMap<>();

        LayerBlockPos pos = new LayerBlockPos(insidePos, 0);
        air_blocks.put(0, Lists.newArrayList(pos));

        to_check.add(insidePos);

        while ( !to_check.isEmpty() ) {
            BlockPos nextCheck = to_check.remove();
            for (EnumFacing facing : EnumFacing.VALUES) {
                BlockPos offsetPos = nextCheck.offset(facing);
                int layer = offsetPos.getY() - insidePos.getY();

                air_blocks.putIfAbsent(layer, Lists.newArrayList());
                frame_blocks.putIfAbsent(layer, Lists.newArrayList());

                if ( checked_blocks.contains(offsetPos) ) {
                    continue;
                }
                checked_blocks.add(offsetPos);

                LayerBlockPos _pos = new LayerBlockPos(offsetPos, offsetPos.getY() - insidePos.getY());
                if ( getWorld().isAirBlock(offsetPos) ) {
                    if ( !air_blocks.get(layer).contains(_pos) ) {
                        air_blocks.get(layer).add(_pos);
                        to_check.add(offsetPos);
                        currentAirBlocks++;
                    }
                } else {
                    frame_blocks.get(layer).add(_pos);
                    currentFrameBlocks++;
                }
            }
            if ( currentAirBlocks > maxAirBlocks ) {
                if ( buildPlayer != null ) {
                    GenericUtil.sendMessageToClient(buildPlayer, "Too many air blocks! Limit: " + maxAirBlocks);
                }
                return false;
            }
        }

        if ( currentAirBlocks == 0 ) {
            return false;
        }

        maps.put(0, frame_blocks);
        maps.put(1, air_blocks);
        return true;
    }

    private boolean setupTank() {
        if ( !searchAlgorithm() ) {
            return false;
        }

        int size = 0;
        for (int layer : maps.get(1).keySet()) {
            size += maps.get(1).get(layer).size();
        }
        getTankConfig().setFluidCapacity(size * Config.MB_PER_TANK_BLOCK);

        for (int layer : maps.get(1).keySet()) {
            for (BlockPos pos : maps.get(1).get(layer)) {
                if ( !getWorld().isAirBlock(pos) ) {
                    return false;
                }
            }
        }

        FluidStack tempNewFluidStack = getTankConfig().getFluidStack();

        List<TileEntity> facingTiles = new ArrayList<>();
        for (int layer : maps.get(0).keySet()) {
            for (BlockPos pos : maps.get(0).get(layer)) {
                IBlockState check = getWorld().getBlockState(pos);
                if ( FancyFluidStorage.tankManager.isPartOfTank(getWorld(), pos) ) {
                    AbstractTankValve valve = FancyFluidStorage.tankManager.getValveForBlock(getWorld(), pos);
                    if ( valve != null && valve != this ) {
                        GenericUtil.sendMessageToClient(buildPlayer, "One or more blocks already belong to another tank!");
                        return false;
                    }
                    continue;
                }

                TileEntity tile = getWorld().getTileEntity(pos);
                if ( tile != null ) {
                    if ( tile instanceof IFacingTile ) {
                        facingTiles.add(tile);
                    }

                    if ( tile instanceof AbstractTankValve ) {
                        AbstractTankValve valve = (AbstractTankValve) tile;
                        if ( valve == this ) {
                            continue;
                        }

                        if ( valve.getTankConfig().getFluidStack() != null ) {
                            if ( getTankConfig() != null && getTankConfig().getFluidStack() != null ) {
                                FluidStack myFS = getTankConfig().getFluidStack();
                                FluidStack otherFS = valve.getTankConfig().getFluidStack();

                                if ( !myFS.isFluidEqual(otherFS) ) {
                                    GenericUtil.sendMessageToClient(buildPlayer, "One or more valves contain different fluids! Could not build the tank!");
                                    return false;
                                } else {
//                                    tempNewFluidStack.amount += otherFS.amount;
//                                    break;
                                }
                            } else {
                                tempNewFluidStack = valve.getTankConfig().getFluidStack();
                            }
                        }
                    }
                }

                if ( !GenericUtil.areTankBlocksValid(check, getWorld(), pos, GenericUtil.getInsideForTankFrame(getAirBlocks(), pos)) && !GenericUtil.isBlockGlass(check) ) {
                    return false;
                }
            }
        }

        getTankConfig().setFluidStack(tempNewFluidStack);
        // Make sure we don't overfill a tank. If the new tank is smaller than the old one, excess liquid disappear.
        if ( getTankConfig().getFluidStack() != null ) {
            getTankConfig().getFluidStack().amount = Math.min(getTankConfig().getFluidStack().amount, getTankConfig().getFluidCapacity());
        }

        for (TileEntity facingTile : facingTiles) {
            setTankTileFacing(maps.get(1), facingTile);
        }
        isMaster = true;

        for (int layer : maps.get(0).keySet()) {
            for (BlockPos pos : maps.get(0).get(layer)) {
                TileEntity tile = getWorld().getTileEntity(pos);
                if ( tile == this ) {
                    continue;
                }

                if ( tile != null ) {
                    if ( tile instanceof AbstractTankValve ) {
                        AbstractTankValve valve = (AbstractTankValve) tile;

                        valve.isMaster = false;
                        valve.setValvePos(getPos());
                        valve.setTankConfig(getTankConfig());
                        tankTiles.add(valve);
                    } else if ( tile instanceof AbstractTankTile ) {
                        AbstractTankTile tankTile = (AbstractTankTile) tile;
                        tankTile.setValvePos(getPos());
                        tankTiles.add((AbstractTankTile) tile);
                    }
                }
            }
        }

        setValid(true);

        FancyFluidStorage.tankManager.add(getWorld(), getPos(), getAirBlocks(), getFrameBlocks());

        return true;
    }

    public void breakTank() {
        if ( getWorld().isRemote ) {
            return;
        }

        if ( !isMaster() && getMasterValve() != null && getMasterValve() != this ) {
            getMasterValve().breakTank();
            return;
        }

        FancyFluidStorage.tankManager.remove(getWorld().provider.getDimension(), getPos());
        NetworkHandler.sendPacketToAllPlayers(new FFSPacket.Client.OnTankBreak(this));

        for (AbstractTankValve valve : getAllValves()) {
            if ( valve == this ) {
                continue;
            }

            valve.setTankConfig(null);
            valve.setValvePos(null);
            valve.setValid(false);
            valve.updateBlockAndNeighbors(true);
        }
        setValid(false);

        tankTiles.removeAll(getTankTiles(AbstractTankValve.class));
        for (AbstractTankTile tankTile : tankTiles) {
            tankTile.setValvePos(null);
        }

        tankTiles.clear();

        updateBlockAndNeighbors(true);
    }

    @Override
    public boolean isValid() {
        if ( getMasterValve() == null || getMasterValve() == this )
            return this.isValid;

        return getMasterValve().isValid;
    }

    private void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    private void updateBlockAndNeighbors() {
        updateBlockAndNeighbors(false);
    }

    private void updateBlockAndNeighbors(boolean onlyThis) {
        if ( getWorld().isRemote )
            return;

        markForUpdateNow();

        if ( !tankTiles.isEmpty() && !onlyThis ) {
            for (AbstractTankTile tile : tankTiles) {
                if ( tile == this ) {
                    continue;
                }

                tile.markForUpdateNow(2);
            }
        }
    }

    private void updateComparatorOutput() {
        if ( this.lastComparatorOut != getComparatorOutput() ) {
            this.lastComparatorOut = getComparatorOutput();
            if ( isMaster() ) {
                for (AbstractTankValve otherValve : getTankTiles(AbstractTankValve.class)) {
                    getWorld().updateComparatorOutputLevel(otherValve.getPos(), otherValve.getBlockType());
                }
            }
            getWorld().updateComparatorOutputLevel(getPos(), getBlockType());
        }
    }

    @Override
    public void markForUpdate() {
        updateComparatorOutput();

        super.markForUpdate();
    }

    public boolean isMaster() {
        return isMaster;
    }

    @Override
    public AbstractTankValve getMasterValve() {
        return isMaster() ? this : super.getMasterValve();
    }

    @Override
    public String getTileName() {
        if ( this.tile_name.isEmpty() ) {
            setTileName(GenericUtil.getUniquePositionName(this));
        }

        return this.tile_name;
    }

    @Override
    public void setTileName(String name) {
        this.tile_name = name;
    }

    @Override
    public EnumFacing getTileFacing() {
        return this.tile_facing;
    }

    @Override
    public void setTileFacing(EnumFacing facing) {
        this.tile_facing = facing;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        isMaster = tag.getBoolean("master");
        if ( isMaster() ) {
            isValid = tag.getBoolean("isValid");
            getTankConfig().readFromNBT(tag);

            if ( getWorld() != null && getWorld().isRemote ) {
                if ( isValid() ) {
                    if ( !FancyFluidStorage.tankManager.isValveInLists(getWorld(), this) ) {
                        NetworkHandler.sendPacketToServer(new FFSPacket.Server.OnTankRequest(this));
                    }
                }
            }
        }

//		if(tag.hasKey("bottomDiagF") && tag.hasKey("topDiagF")) {
//			int[] bottomDiagF = tag.getIntArray("bottomDiagF");
//			int[] topDiagF = tag.getIntArray("topDiagF");
//			bottomDiagFrame = new BlockPos(bottomDiagF[0], bottomDiagF[1], bottomDiagF[2]);
//			topDiagFrame = new BlockPos(topDiagF[0], topDiagF[1], topDiagF[2]);
//		}

        readTileNameFromNBT(tag);
        readTileFacingFromNBT(tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setBoolean("master", isMaster());
        if ( isMaster() ) {
            tag.setBoolean("isValid", isValid());
            getTankConfig().writeToNBT(tag);
        }

//		if(bottomDiagFrame != null && topDiagFrame != null) {
//			tag.setIntArray("bottomDiagF", new int[]{bottomDiagFrame.getX(), bottomDiagFrame.getY(), bottomDiagFrame.getZ()});
//			tag.setIntArray("topDiagF", new int[]{topDiagFrame.getX(), topDiagFrame.getY(), topDiagFrame.getZ()});
//		}

        saveTileNameToNBT(tag);
        saveTileFacingToNBT(tag);

        super.writeToNBT(tag);
        return tag;
    }


    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    public int fillFromContainer(FluidStack resource, boolean doFill) {
        if ( !canFillIncludingContainers(resource) ) {
            return 0;
        }

        return getTankConfig().getFluidTank().fill(resource, doFill);
    }

    private boolean canFillIncludingContainers(FluidStack fluid) {
        if ( getTankConfig().getFluidStack() != null && !getTankConfig().getFluidStack().isFluidEqual(fluid) ) {
            return false;
        }

        return !(getTankConfig().isFluidLocked() && !getTankConfig().getLockedFluid().isFluidEqual(fluid));
    }

    public int getComparatorOutput() {
        if ( !isValid() ) {
            return 0;
        }

        return MathHelper.floor(((float) this.getTankConfig().getFluidAmount() / this.getTankConfig().getFluidCapacity()) * 14.0F);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AbstractTankValve && ((AbstractTankValve) obj).getPos().equals(getPos());

    }
}
