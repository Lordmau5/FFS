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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
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
    private Player buildPlayer;

    public AbstractTankValve(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);

        tankTiles = new ArrayList<>();

        maps = new WeakHashMap<>();
        maps.put(0, new TreeMap<>());
        maps.put(1, new TreeMap<>());

        setValid(false);
        setValvePos(null);
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        initiated = true;
        initialWaitTick = 20;
    }

//    @Override
//    public void setRemoved() {
//        super.setRemoved();
//
//        breakTank();
//    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T be) {
        AbstractTankTile.tick(level, pos, state, be);

        AbstractTankValve valve = (AbstractTankValve) be;

        if ( valve.needsUpdate > 0 ) {
            valve.markForUpdate();
        }

        if ( level.isClientSide ) {
            return;
        }

        if ( valve.initiated ) {
            if ( valve.isMain() ) {
                if ( valve.initialWaitTick-- <= 0 ) {
                    valve.initiated = false;
                    valve.buildTank(valve.getTileFacing());
                    return;
                }
            }
        }

        if ( !valve.isValid() )
            return;

        if ( !valve.isMain() && valve.getMainValve() == null ) {
            valve.setValid(false);
            valve.updateBlockAndNeighbors();
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
            if ( getTankConfig().getFluidStack().isEmpty() ) {
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
    public void buildTank(Player player, Direction inside) {
        if ( getLevel().isClientSide ) {
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
        if ( getLevel().isClientSide ) {
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

    private boolean isAirOrWaterloggable(Level world, BlockPos pos) {
        return GenericUtil.isAirOrWaterloggable(world, pos);
    }

    private void setTankTileFacing(TreeMap<Integer, List<LayerBlockPos>> airBlocks, BlockEntity tankTile) {
        List<BlockPos> possibleAirBlocks = new ArrayList<>();
        for (Direction dr : Direction.values()) {
            if (isAirOrWaterloggable(getLevel(), tankTile.getBlockPos().relative(dr))) {
                possibleAirBlocks.add(tankTile.getBlockPos().relative(dr));
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

        BlockPos dist = insideAir.subtract(tankTile.getBlockPos());
        for (Direction dr : Direction.values()) {
            if ( dist.equals(new BlockPos(dr.getStepX(), dr.getStepY(), dr.getStepZ())) ) {
                ((IFacingTile) tankTile).setTileFacing(dr);
                break;
            }
        }
    }

    private boolean searchAlgorithm() {
        int currentAirBlocks = 1;
        int maxAirBlocks = ServerConfig.general.maxAirBlocks;
        BlockPos insidePos = getBlockPos().relative(getTileFacing());

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
                BlockPos offsetPos = nextCheck.relative(facing);
                int layer = offsetPos.getY() - insidePos.getY();

                air_blocks.putIfAbsent(layer, Lists.newArrayList());
                frame_blocks.putIfAbsent(layer, Lists.newArrayList());

                if ( checked_blocks.contains(offsetPos) ) {
                    continue;
                }
                checked_blocks.add(offsetPos);

                LayerBlockPos _pos = new LayerBlockPos(offsetPos, offsetPos.getY() - insidePos.getY());
                if (isAirOrWaterloggable(getLevel(), offsetPos)) {
                    if ( !air_blocks.get(layer).contains(_pos) ) {
                        air_blocks.get(layer).add(_pos);
                        to_check.add(offsetPos);
                        currentAirBlocks++;
                    }
                } else {
                    if (isBlockBlacklisted(_pos)) {
                        if ( buildPlayer != null ) {
                            GenericUtil.sendMessageToClient(
                                    buildPlayer,
                                    "chat.ffs.valve_blacklisted_block_found",
                                    false,
                                    _pos.getX(),
                                    _pos.getY(),
                                    _pos.getZ()
                            );
                        }
                        return false;
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
        return false;

    // TODO: Figure out how to iterate over all block tags and find the ones we have blacklisted
//
//        if (getLevel().getBlockEntity(pos) instanceof AbstractTankTile) return false;
//
//        BlockState state = getLevel().getBlockState(pos);
//
//        ResourceLocation blacklist = new ResourceLocation(FancyFluidStorage.MODID, "blacklist");
//        Tag<Block> blockITag = BlockTags.getAllTags().getTag(blacklist);
//        if (blockITag == null) {
//            return ServerConfig.general.blockBlacklistInvert;
//        }
//
//        if (blockITag.contains(state.getBlock())) {
//            return !ServerConfig.general.blockBlacklistInvert;
//        }
//        return ServerConfig.general.blockBlacklistInvert;
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
                if (!isAirOrWaterloggable(getLevel(), pos)) {
                    return false;
                }
            }
        }

        FluidStack tempNewFluidStack = getTankConfig().getFluidStack();

        List<BlockEntity> facingTiles = new ArrayList<>();
        for (int layer : maps.get(0).keySet()) {
            for (BlockPos pos : maps.get(0).get(layer)) {
                BlockState check = getLevel().getBlockState(pos);
                if ( FancyFluidStorage.TANK_MANAGER.isPartOfTank(getLevel(), pos) ) {
                    AbstractTankValve valve = FancyFluidStorage.TANK_MANAGER.getValveForBlock(getLevel(), pos);
                    if ( valve != null && valve != this ) {
                        GenericUtil.sendMessageToClient(buildPlayer, "chat.ffs.valve_other_tank", false);
                        return false;
                    }
                    continue;
                }

                BlockEntity tile = getLevel().getBlockEntity(pos);
                if ( tile != null ) {
                    if ( tile instanceof IFacingTile ) {
                        facingTiles.add(tile);
                    }

                    if (tile instanceof AbstractTankValve valve) {
                        if ( valve == this ) {
                            continue;
                        }

                        if ( !valve.getTankConfig().getFluidStack().isEmpty() ) {
                            if ( getTankConfig() != null && !getTankConfig().getFluidStack().isEmpty() ) {
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

                if ( !GenericUtil.areTankBlocksValid(check, getLevel(), pos, GenericUtil.getInsideForTankFrame(getAirBlocks(), pos)) && !GenericUtil.isBlockGlass(check) ) {
                    return false;
                }
            }
        }

        getTankConfig().setFluidStack(tempNewFluidStack);
        // Make sure we don't overfill a tank. If the new tank is smaller than the old one, excess liquid disappear.
        if ( !getTankConfig().getFluidStack().isEmpty() && getTankConfig().getFluidStack().getRawFluid() != Fluids.EMPTY ) {
            getTankConfig().getFluidStack().setAmount(Math.min(getTankConfig().getFluidStack().getAmount(), getTankConfig().getFluidCapacity()));
        }

        for (BlockEntity facingTile : facingTiles) {
            setTankTileFacing(maps.get(1), facingTile);
        }
        setIsMain(true);

        for (int layer : maps.get(0).keySet()) {
            for (BlockPos pos : maps.get(0).get(layer)) {
                BlockEntity tile = getLevel().getBlockEntity(pos);
                if ( tile == this ) {
                    continue;
                }

                if ( tile != null ) {
                    if (tile instanceof AbstractTankValve valve) {

                        valve.setValid(true);
                        valve.setIsMain(false);
                        valve.setValvePos(getBlockPos());
                        valve.setTankConfig(getTankConfig());
                        tankTiles.add(valve);
                    } else if (tile instanceof AbstractTankTile tankTile) {
                        tankTile.setValvePos(getBlockPos());
                        tankTiles.add((AbstractTankTile) tile);
                    }
                }
            }
        }

        setValid(true);

        FancyFluidStorage.TANK_MANAGER.add(getLevel(), getBlockPos(), getAirBlocks(), getFrameBlocks());

        return true;
    }

    public void breakTank() {
        if ( hasLevel() && getLevel().isClientSide || isRemoved() ) {
            return;
        }

        if ( !isMain() && getMainValve() != null && getMainValve() != this ) {
            getMainValve().breakTank();
            return;
        }

        FancyFluidStorage.TANK_MANAGER.remove(getLevel(), getBlockPos());
        NetworkHandler.sendPacketToAllPlayers(new FFSPacket.Client.OnTankBreak(this));

        for (AbstractTankValve valve : getAllValves(false)) {
            if ( valve == this || valve.isRemoved() ) {
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

        if (getLevel() != null) {
            getLevel().setBlockAndUpdate(getBlockPos(), getBlockState().setValue(FFSStateProps.TILE_VALID, isValid));
        }
        else {
            markForUpdate();
        }
    }

    private void updateBlockAndNeighbors() {
        updateBlockAndNeighbors(false);
    }

    private void updateBlockAndNeighbors(boolean onlyThis) {
        if ( getLevel().isClientSide )
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
                    getLevel().updateNeighbourForOutputSignal(otherValve.getBlockPos(), otherValve.getBlockState().getBlock());
                }
            }
            getLevel().updateNeighbourForOutputSignal(getBlockPos(), getBlockState().getBlock());
        }
    }

    @Override
    public void markForUpdate() {
        updateComparatorOutput();

        super.markForUpdate();
    }

    public void setIsMain(boolean isMain) {
        this.isMain = isMain;

        if (getLevel() != null) {
            getLevel().setBlockAndUpdate(getBlockPos(), getBlockState().setValue(FFSStateProps.TILE_MAIN, isMain));
        }
        else {
            markForUpdate();
        }
    }

    @Override
    public void doUpdate() {
        super.doUpdate();

        if (getLevel() != null) {
            getLevel().setBlockAndUpdate(getBlockPos(), getBlockState()
                    .setValue(FFSStateProps.TILE_MAIN, isMain)
                    .setValue(FFSStateProps.TILE_VALID, isValid)
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
    public void load(CompoundTag nbt) {
        super.load(nbt);

        isMain = nbt.getBoolean("IsMain");
        if ( isMain() ) {
            isValid = nbt.getBoolean("IsValid");
            getTankConfig().readFromNBT(nbt);

            if ( getLevel() != null && getLevel().isClientSide ) {
                if ( isValid() ) {
                    if ( !FancyFluidStorage.TANK_MANAGER.isValveInLists(getLevel(), this) ) {
                        NetworkHandler.sendPacketToServer(new FFSPacket.Server.OnTankRequest(this));
                    }
                }
            }
        }

        readTileNameFromNBT(nbt);
        readTileFacingFromNBT(nbt);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        nbt.putBoolean("IsMain", isMain());
        if ( isMain() ) {
            nbt.putBoolean("IsValid", isValid());
            getTankConfig().writeToNBT(nbt);
        }

        saveTileNameToNBT(nbt);
        saveTileFacingToNBT(nbt);
    }

    @Override
    public AABB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    public int getComparatorOutput() {
        if ( !isValid() ) {
            return 0;
        }

        return Mth.floor(((float) this.getTankConfig().getFluidAmount() / this.getTankConfig().getFluidCapacity()) * 14.0F);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AbstractTankValve && ((AbstractTankValve) obj).getBlockPos().equals(getBlockPos());
    }
}
