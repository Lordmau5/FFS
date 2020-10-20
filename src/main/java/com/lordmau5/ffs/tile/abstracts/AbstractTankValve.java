package com.lordmau5.ffs.tile.abstracts;

import com.google.common.collect.Lists;
import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.config.ServerConfig;
import com.lordmau5.ffs.network.FFSPacket;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.tile.interfaces.IFacingTile;
import com.lordmau5.ffs.tile.interfaces.INameableTile;
import com.lordmau5.ffs.tile.util.TankConfig;
import com.lordmau5.ffs.util.FFSStateProps;
import com.lordmau5.ffs.util.GenericUtil;
import com.lordmau5.ffs.util.LayerBlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractTankValve extends AbstractTankTile implements IFacingTile, INameableTile {

    public final WeakHashMap<Integer, TreeMap<Integer, List<LayerBlockPos>>> maps;
    private final List<AbstractTankTile> tankTiles;
    private int initialWaitTick = 20;
    private TankConfig tankConfig;
    private boolean isValid;
    private boolean isMain;
    private boolean initiated;

    // TANK LOGIC
    private int lastComparatorOut = 0;
    // ---------------
    private PlayerEntity buildPlayer;

    public AbstractTankValve(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);

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

//    @Override
//    public void invalidate() {
//        super.invalidate();
//
//        breakTank();
//    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();

        breakTank();
    }

    @Override
    public void tick() {
        super.tick();

        if ( getWorld().isRemote ) {
            return;
        }

        if ( initiated ) {
            if ( isMain() ) {
                if ( initialWaitTick-- <= 0 ) {
                    initiated = false;
                    buildTank(getTileFacing());
                    return;
                }
            }
        }

        if ( !isValid() )
            return;

        if ( !isMain() && getMainValve() == null ) {
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
        if ( !isMain() && getMainValve() != null && getMainValve() != this ) {
            return getMainValve().getTankConfig();
        }

        if ( this.tankConfig == null ) {
            this.tankConfig = new TankConfig(this);
        }

        return this.tankConfig;
    }

    private void setTankConfig(TankConfig tankConfig) {
        this.tankConfig = tankConfig;
    }

    public void setFluidLock(boolean state) {
        if ( !state ) {
            getTankConfig().unlockFluid();
        } else {
            if ( getTankConfig().getFluidStack() == FluidStack.EMPTY ) {
                return;
            }

            getTankConfig().lockFluid(getTankConfig().getFluidStack());
        }
        getMainValve().setNeedsUpdate();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getTankTiles(Class<T> type) {
        List<T> tiles = tankTiles.stream().filter(p -> type.isAssignableFrom(p.getClass())).map(p -> (T) p).collect(Collectors.toList());
        if ( this.getClass().isAssignableFrom(type) ) {
            tiles.add((T) this);
        }

        return tiles;
    }

    public List<AbstractTankValve> getAllValves(boolean include) {
        if ( !isMain() && getMainValve() != null && getMainValve() != this ) {
            return getMainValve().getAllValves(include);
        }

        List<AbstractTankValve> valves = getTankTiles(AbstractTankValve.class);
        if ( include ) valves.add(this);
        return valves;
    }

    /**
     * Let a player build a tank!
     *
     * @param player - The player that tries to build the tank
     * @param inside - The direction of the inside of the tank
     */
    public void buildTank_player(PlayerEntity player, Direction inside) {
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
    private void buildTank(Direction inside) {
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
        for (Direction dr : Direction.values()) {
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
        for (Direction dr : Direction.values()) {
            if ( dist.equals(new BlockPos(dr.getXOffset(), dr.getYOffset(), dr.getZOffset())) ) {
                ((IFacingTile) tankTile).setTileFacing(dr);
                break;
            }
        }
    }

    private boolean searchAlgorithm() {
        int currentAirBlocks = 1;
        int maxAirBlocks = ServerConfig.general.maxAirBlocks;
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
            for (Direction facing : Direction.values()) {
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
                    if (isBlockBlacklisted(_pos)) {
                        continue;
                    }
                    frame_blocks.get(layer).add(_pos);
                }
            }

            if ( currentAirBlocks > maxAirBlocks ) {
                if ( buildPlayer != null ) {
                    GenericUtil.sendMessageToClient(buildPlayer, "chat.ffs.valve_too_much_air", false, maxAirBlocks);
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

    private boolean isBlockBlacklisted(BlockPos pos) {
        if (world.getTileEntity(pos) instanceof AbstractTankTile) return false;

        BlockState state = world.getBlockState(pos);

        ResourceLocation blacklist = new ResourceLocation(FancyFluidStorage.MODID, "blacklist");
        Tag<Block> blockITag = BlockTags.getCollection().get(blacklist);
        if (blockITag == null) {
            return ServerConfig.general.blockBlacklistInvert;
        }

        if (blockITag.contains(state.getBlock())) {
            return !ServerConfig.general.blockBlacklistInvert;
        }
        return ServerConfig.general.blockBlacklistInvert;
    }

    private boolean setupTank() {
        if ( !searchAlgorithm() ) {
            return false;
        }

        int size = 0;
        for (int layer : maps.get(1).keySet()) {
            size += maps.get(1).get(layer).size();
        }
        getTankConfig().setFluidCapacity(size * ServerConfig.general.mbPerTankBlock);

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
                BlockState check = getWorld().getBlockState(pos);
                if ( FancyFluidStorage.TANK_MANAGER.isPartOfTank(getWorld(), pos) ) {
                    AbstractTankValve valve = FancyFluidStorage.TANK_MANAGER.getValveForBlock(getWorld(), pos);
                    if ( valve != null && valve != this ) {
                        GenericUtil.sendMessageToClient(buildPlayer, "chat.ffs.valve_other_tank", false);
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

                        if ( valve.getTankConfig().getFluidStack() != FluidStack.EMPTY ) {
                            if ( getTankConfig() != null && getTankConfig().getFluidStack() != FluidStack.EMPTY ) {
                                FluidStack myFS = getTankConfig().getFluidStack();
                                FluidStack otherFS = valve.getTankConfig().getFluidStack();

                                if ( !myFS.isFluidEqual(otherFS) ) {
                                    GenericUtil.sendMessageToClient(buildPlayer, "chat.ffs.valve_different_fluids", false);
                                    return false;
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
        if ( getTankConfig().getFluidStack() != FluidStack.EMPTY && getTankConfig().getFluidStack().getRawFluid() != Fluids.EMPTY ) {
            getTankConfig().getFluidStack().setAmount(Math.min(getTankConfig().getFluidStack().getAmount(), getTankConfig().getFluidCapacity()));
        }

        for (TileEntity facingTile : facingTiles) {
            setTankTileFacing(maps.get(1), facingTile);
        }
        setIsMain(true);

        for (int layer : maps.get(0).keySet()) {
            for (BlockPos pos : maps.get(0).get(layer)) {
                TileEntity tile = getWorld().getTileEntity(pos);
                if ( tile == this ) {
                    continue;
                }

                if ( tile != null ) {
                    if ( tile instanceof AbstractTankValve ) {
                        AbstractTankValve valve = (AbstractTankValve) tile;

                        valve.setValid(true);
                        valve.setIsMain(false);
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

        FancyFluidStorage.TANK_MANAGER.add(getWorld(), getPos(), getAirBlocks(), getFrameBlocks());

        return true;
    }

    public void breakTank() {
        if ( getWorld().isRemote ) {
            return;
        }

        if ( !isMain() && getMainValve() != null && getMainValve() != this ) {
            getMainValve().breakTank();
            return;
        }

        FancyFluidStorage.TANK_MANAGER.remove(getWorld(), getPos());
        NetworkHandler.sendPacketToAllPlayers(new FFSPacket.Client.OnTankBreak(this));

        for (AbstractTankValve valve : getAllValves(false)) {
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
        if ( getMainValve() == null || getMainValve() == this )
            return this.isValid;

        return getMainValve().isValid;
    }

    private void setValid(boolean isValid) {
        this.isValid = isValid;

        if (getWorld() != null) {
            getWorld().setBlockState(getPos(), getBlockState().with(FFSStateProps.TILE_VALID, isValid));
        }
        else {
            markForUpdate();
        }
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
            if ( isMain() ) {
                for (AbstractTankValve otherValve : getTankTiles(AbstractTankValve.class)) {
                    getWorld().updateComparatorOutputLevel(otherValve.getPos(), otherValve.getBlockState().getBlock());
                }
            }
            getWorld().updateComparatorOutputLevel(getPos(), getBlockState().getBlock());
        }
    }

    @Override
    public void markForUpdate() {
        updateComparatorOutput();

        super.markForUpdate();
    }

    public void setIsMain(boolean isMain) {
        this.isMain = isMain;

        if (getWorld() != null) {
            getWorld().setBlockState(getPos(), getBlockState().with(FFSStateProps.TILE_MAIN, isMain));
        }
        else {
            markForUpdate();
        }
    }

    @Override
    public void doUpdate() {
        super.doUpdate();

        if (getWorld() != null) {
            getWorld().setBlockState(getPos(), getBlockState()
                    .with(FFSStateProps.TILE_MAIN, isMain)
                    .with(FFSStateProps.TILE_VALID, isValid)
            );
        }
    }

    public boolean isMain() {
        return isMain;
    }

    @Override
    public AbstractTankValve getMainValve() {
        return isMain() ? this : super.getMainValve();
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
    public Direction getTileFacing() {
        return this.tile_facing;
    }

    @Override
    public void setTileFacing(Direction facing) {
        this.tile_facing = facing;
    }

    @Override
    public void read(CompoundNBT nbt) {
        super.read(nbt);

        isMain = nbt.getBoolean("IsMain");
        if ( isMain() ) {
            isValid = nbt.getBoolean("IsValid");
            getTankConfig().readFromNBT(nbt);

            if ( getWorld() != null && getWorld().isRemote ) {
                if ( isValid() ) {
                    if ( !FancyFluidStorage.TANK_MANAGER.isValveInLists(getWorld(), this) ) {
                        NetworkHandler.sendPacketToServer(new FFSPacket.Server.OnTankRequest(this));
                    }
                }
            }
        }

        readTileNameFromNBT(nbt);
        readTileFacingFromNBT(nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);

        nbt.putBoolean("IsMain", isMain());
        if ( isMain() ) {
            nbt.putBoolean("IsValid", isValid());
            getTankConfig().writeToNBT(nbt);
        }

        saveTileNameToNBT(nbt);
        saveTileFacingToNBT(nbt);

        return nbt;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
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
