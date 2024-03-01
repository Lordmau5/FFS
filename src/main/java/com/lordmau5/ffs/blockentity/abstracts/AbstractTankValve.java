package com.lordmau5.ffs.blockentity.abstracts;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.blockentity.interfaces.IFacingEntity;
import com.lordmau5.ffs.blockentity.interfaces.INameableEntity;
import com.lordmau5.ffs.blockentity.util.TankConfig;
import com.lordmau5.ffs.config.ServerConfig;
import com.lordmau5.ffs.network.FFSPacket;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.util.FFSStateProps;
import com.lordmau5.ffs.util.GenericUtil;
import com.lordmau5.ffs.util.TankManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.IBlockEntityRendererExtension;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.*;
import java.util.stream.Collectors;

import static net.neoforged.neoforge.client.extensions.IBlockEntityRendererExtension.INFINITE_EXTENT_AABB;

public abstract class AbstractTankValve extends AbstractTankEntity implements IFacingEntity, INameableEntity, IBlockEntityRendererExtension<AbstractTankValve>
{

    public final HashMap<Integer, TreeMap<Integer, HashSet<BlockPos>>> maps;
    private final HashSet<AbstractTankEntity> tankTiles;
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

        tankTiles = new HashSet<>();

        maps = new HashMap<>();
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

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T be) {
        AbstractTankEntity.tick(level, pos, state, be);

        AbstractTankValve valve = (AbstractTankValve) be;

        if (valve.needsUpdate > 0) {
            valve.markForUpdate();
        }

        if (level.isClientSide()) {
            return;
        }

        if (valve.initiated) {
            if (valve.isMain()) {
                if (valve.initialWaitTick-- <= 0) {
                    valve.initiated = false;
                    valve.buildTank(valve.getTileFacing());
                    return;
                }
            }
        }

        if (!valve.isValid())
            return;

        if (!valve.isMain() && valve.getMainValve() == null) {
            valve.setValid(false);
            valve.updateBlockAndNeighbors();
        }
    }

    public TreeMap<Integer, HashSet<BlockPos>> getFrameBlocks() {
        return maps.get(0);
    }

    public TreeMap<Integer, HashSet<BlockPos>> getAirBlocks() {
        return maps.get(1);
    }

    public TankConfig getTankConfig() {
        if (!isMain() && getMainValve() != null && getMainValve() != this) {
            return getMainValve().getTankConfig();
        }

        if (this.tankConfig == null) {
            this.tankConfig = new TankConfig(this);
        }

        return this.tankConfig;
    }

    private void setTankConfig(TankConfig tankConfig) {
        this.tankConfig = tankConfig;
    }

    public void setFluidLock(boolean state) {
        if (!state) {
            getTankConfig().unlockFluid();
        } else {
            if (getTankConfig().getFluidStack().isEmpty()) {
                return;
            }

            getTankConfig().lockFluid(getTankConfig().getFluidStack());
        }
        getMainValve().setNeedsUpdate();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getTankTiles(Class<T> type) {
        List<T> tiles = tankTiles.stream().filter(p -> type.isAssignableFrom(p.getClass())).map(p -> (T) p).collect(Collectors.toList());
        if (this.getClass().isAssignableFrom(type)) {
            tiles.add((T) this);
        }

        return tiles;
    }

    public List<AbstractTankEntity> getAllTankTiles(boolean include) {
        if (!isMain() && getMainValve() != null && getMainValve() != this) {
            return getMainValve().getAllTankTiles(include);
        }

        List<AbstractTankEntity> valves = getTankTiles(AbstractTankEntity.class);
        if (include) valves.add(this);
        return valves;
    }

    /**
     * Let a player build a tank!
     *
     * @param player - The player that tries to build the tank
     * @param inside - The direction of the inside of the tank
     */
    public void buildTank(Player player, Direction inside) {
        if (getLevel().isClientSide()) {
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
        if (getLevel().isClientSide()) {
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
        if (inside != null) {
            setTileFacing(inside);
        }

        /**
         * Actually setup the tank here
         */
        if (!setupTank()) {
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

    private void setTankTileFacing(TreeMap<Integer, HashSet<BlockPos>> airBlocks, BlockEntity tankTile) {
        HashSet<BlockPos> possibleAirBlocks = new HashSet<>();
        for (Direction dr : Direction.values()) {
            if (GenericUtil.isAirOrWaterLoggable(getLevel(), tankTile.getBlockPos().relative(dr))) {
                possibleAirBlocks.add(tankTile.getBlockPos().relative(dr));
            }
        }

        BlockPos insideAir = null;
        for (int layer : airBlocks.keySet()) {
            for (BlockPos pos : possibleAirBlocks) {
                if (airBlocks.get(layer).contains(pos)) {
                    insideAir = pos;
                    break;
                }
            }
        }

        if (insideAir == null) {
            return;
        }

        BlockPos dist = insideAir.subtract(tankTile.getBlockPos());
        for (Direction dr : Direction.values()) {
            if (dist.equals(new BlockPos(dr.getStepX(), dr.getStepY(), dr.getStepZ()))) {
                ((IFacingEntity) tankTile).setTileFacing(dr);
                break;
            }
        }
    }

    private boolean checkBlock(BlockPos pos) {
        if (GenericUtil.isAirOrWaterLoggable(getLevel(), pos)) return true;

        HashSet<BlockPos> blacklistedBlocks = new HashSet<>();
        HashSet<BlockPos> fallingBlocks = new HashSet<>();
        HashSet<BlockPos> invalidBlocks = new HashSet<>();

        BlockState state = getLevel().getBlockState(pos);

        if (isBlockBlacklisted(pos, state)) {
            blacklistedBlocks.add(pos);
        } else if (GenericUtil.isBlockFallingBlock(state)) {
            fallingBlocks.add(pos);
        } else if (getLevel().getFluidState(pos) != Fluids.EMPTY.defaultFluidState()) {
            invalidBlocks.add(pos);
        }

        return !checkInvalid(blacklistedBlocks, fallingBlocks, invalidBlocks, 0, 1);
    }

    private boolean searchAlgorithm() {
        getAirBlocks().clear();
        getFrameBlocks().clear();

        int currentAirBlocks = 1;
        int maxAirBlocks = ServerConfig.general.maxAirBlocks;
        BlockPos insidePos = getBlockPos().relative(getTileFacing());

        Deque<BlockPos> to_check = new ArrayDeque<>();
        HashSet<BlockPos> checked_blocks = new HashSet<>();
        TreeMap<Integer, HashSet<BlockPos>> air_blocks = new TreeMap<>();
        TreeMap<Integer, HashSet<BlockPos>> frame_blocks = new TreeMap<>();

        if (!checkBlock(insidePos)) return false;

        HashSet<BlockPos> zeroLayer = new HashSet<>();
        zeroLayer.add(insidePos);
        air_blocks.put(0, zeroLayer);

        to_check.add(insidePos);

        HashSet<BlockPos> blacklistedBlocks = new HashSet<>();
        HashSet<BlockPos> fallingBlocks = new HashSet<>();

        while (!to_check.isEmpty()) {
            BlockPos nextCheck = to_check.remove();

            for (Direction facing : Direction.values()) {
                BlockPos offsetPos = nextCheck.relative(facing);
                int layer = offsetPos.getY() - insidePos.getY();

                if (!air_blocks.containsKey(layer)) {
                    air_blocks.putIfAbsent(layer, new HashSet<>());
                }

                if (!frame_blocks.containsKey(layer)) {
                    frame_blocks.putIfAbsent(layer, new HashSet<>());
                }

                if (checked_blocks.contains(offsetPos)) {
                    continue;
                }
                checked_blocks.add(offsetPos);

                if (GenericUtil.isAirOrWaterLoggable(getLevel(), offsetPos)) {
                    if (!air_blocks.get(layer).contains(offsetPos)) {
                        air_blocks.get(layer).add(offsetPos);
                        to_check.add(offsetPos);
                        currentAirBlocks++;
                    }
                } else {
                    BlockState state = getLevel().getBlockState(offsetPos);

                    if (isBlockBlacklisted(offsetPos, state)) {
                        blacklistedBlocks.add(offsetPos);
                    } else if (GenericUtil.isBlockFallingBlock(state)) {
                        fallingBlocks.add(offsetPos);
                    } else {
                        frame_blocks.get(layer).add(offsetPos);
                    }
                }
            }

            if (currentAirBlocks > maxAirBlocks) {
                break;
            }
        }

        maps.put(0, frame_blocks);
        maps.put(1, air_blocks);

        if (checkInvalid(blacklistedBlocks, fallingBlocks, getInvalidFrameBlocks(), currentAirBlocks, maxAirBlocks)) {
            return false;
        }

        return currentAirBlocks > 0;
    }

    private boolean checkInvalid(HashSet<BlockPos> blacklistedBlocks, HashSet<BlockPos> fallingBlocks, HashSet<BlockPos> invalidBlocks, int currentAirBlocks, int maxAirBlocks) {
        boolean invalid = blacklistedBlocks.size() > 0 || fallingBlocks.size() > 0 || currentAirBlocks > maxAirBlocks;

        if (currentAirBlocks > maxAirBlocks) {
            GenericUtil.sendMessageToClient(buildPlayer, "chat.ffs.valve_too_much_air", false, maxAirBlocks);
            return invalid;
        }

        if (blacklistedBlocks.size() > 0) {
            BlockPos firstPos = blacklistedBlocks.stream().findFirst().get();
            BlockState state = getLevel().getBlockState(firstPos);

            GenericUtil.sendMessageToClient(
                    buildPlayer,
                    "chat.ffs.valve_blacklisted_block_found",
                    false,
                    state.getBlock().getName(),
                    firstPos.getX(),
                    firstPos.getY(),
                    firstPos.getZ(),
                    blacklistedBlocks.size() - 1
            );
        }

        if (fallingBlocks.size() > 0) {
            BlockPos firstPos = fallingBlocks.stream().findFirst().get();
            BlockState state = getLevel().getBlockState(firstPos);

            GenericUtil.sendMessageToClient(
                    buildPlayer,
                    "chat.ffs.valve_falling_block_found",
                    false,
                    state.getBlock().getName(),
                    firstPos.getX(),
                    firstPos.getY(),
                    firstPos.getZ(),
                    fallingBlocks.size() - 1
            );
        }

        if (invalidBlocks.size() > 0) {
            BlockPos firstPos = invalidBlocks.stream().findFirst().get();
            BlockState state = getLevel().getBlockState(firstPos);

            GenericUtil.sendMessageToClient(
                    buildPlayer,
                    "chat.ffs.valve_invalid_block_found",
                    false,
                    state.getBlock().getName(),
                    firstPos.getX(),
                    firstPos.getY(),
                    firstPos.getZ(),
                    invalidBlocks.size() - 1
            );

            invalid = true;
        }

        return invalid;
    }

    private HashSet<BlockPos> getInvalidFrameBlocks() {
        HashSet<BlockPos> invalidBlocks = new HashSet<>();

        for (int layer : getFrameBlocks().keySet()) {
            for (BlockPos pos : getFrameBlocks().get(layer)) {
                BlockState checkState = getLevel().getBlockState(pos);

                if (!GenericUtil.isValidTankBlock(getLevel(), pos, checkState, GenericUtil.getInsideForTankFrame(getAirBlocks(), pos))) {
                    invalidBlocks.add(pos);
                }
            }
        }

        return invalidBlocks;
    }

    private boolean isBlockBlacklisted(BlockPos pos, BlockState state) {
        if (!hasLevel() || getLevel().getBlockEntity(pos) instanceof AbstractTankEntity) return false;

        return state.is(FancyFluidStorage.TANK_BLACKLIST);
    }

    private boolean setupTank() {
        if (!searchAlgorithm()) {
            return false;
        }

        int size = 0;
        for (int layer : getAirBlocks().keySet()) {
            size += getAirBlocks().get(layer).size();
        }
        getTankConfig().setFluidCapacity(size * ServerConfig.general.mbPerTankBlock);

        for (int layer : getAirBlocks().keySet()) {
            for (BlockPos pos : getAirBlocks().get(layer)) {
                if (!GenericUtil.isAirOrWaterLoggable(getLevel(), pos)) {
                    return false;
                }
            }
        }

        FluidStack tempNewFluidStack = getTankConfig().getFluidStack();

        HashSet<BlockEntity> facingTiles = new HashSet<>();
        for (int layer : getFrameBlocks().keySet()) {
            for (BlockPos pos : getFrameBlocks().get(layer)) {
                if (TankManager.INSTANCE.isPartOfTank(getLevel(), pos)) {
                    AbstractTankValve valve = TankManager.INSTANCE.getValveForBlock(getLevel(), pos);
                    if (valve != null && valve != this) {
                        GenericUtil.sendMessageToClient(buildPlayer, "chat.ffs.valve_other_tank", false);
                        return false;
                    }
                    continue;
                }

                BlockEntity tile = getLevel().getBlockEntity(pos);
                if (tile != null) {
                    if (tile instanceof IFacingEntity) {
                        facingTiles.add(tile);
                    }

                    if (tile instanceof AbstractTankValve valve) {
                        if (valve == this) {
                            continue;
                        }

                        if (!valve.getTankConfig().getFluidStack().isEmpty()) {
                            if (getTankConfig() != null && !getTankConfig().getFluidStack().isEmpty()) {
                                FluidStack myFS = getTankConfig().getFluidStack();
                                FluidStack otherFS = valve.getTankConfig().getFluidStack();

                                if (!myFS.isFluidEqual(otherFS)) {
                                    GenericUtil.sendMessageToClient(buildPlayer, "chat.ffs.valve_different_fluids", false);
                                    return false;
                                }
                            } else {
                                tempNewFluidStack = valve.getTankConfig().getFluidStack();
                            }
                        }
                    }
                }
            }
        }

        getTankConfig().setFluidStack(tempNewFluidStack);
        // Make sure we don't overfill a tank. If the new tank is smaller than the old one, excess liquid disappear.
        if (!getTankConfig().getFluidStack().isEmpty() && getTankConfig().getFluidStack().getRawFluid() != Fluids.EMPTY) {
            getTankConfig().getFluidStack().setAmount(Math.min(getTankConfig().getFluidStack().getAmount(), getTankConfig().getFluidCapacity()));
        }

        for (BlockEntity facingTile : facingTiles) {
            setTankTileFacing(getAirBlocks(), facingTile);
        }
        setIsMain(true);

        for (int layer : getFrameBlocks().keySet()) {
            for (BlockPos pos : getFrameBlocks().get(layer)) {
                BlockEntity tile = getLevel().getBlockEntity(pos);
                if (tile == this) {
                    continue;
                }

                if (tile != null) {
                    if (tile instanceof AbstractTankValve valve) {
                        valve.setValid(true);
                        valve.setIsMain(false);
                        valve.setValvePos(getBlockPos());
                        valve.setTankConfig(getTankConfig());
                        tankTiles.add(valve);
                    } else if (tile instanceof AbstractTankEntity tankTile) {
                        tankTile.setValvePos(getBlockPos());
                        tankTiles.add((AbstractTankEntity) tile);
                    }
                }
            }
        }

        setValid(true);
        setValvePos(getBlockPos());

        TankManager.INSTANCE.add(getLevel(), getBlockPos(), getAirBlocks(), getFrameBlocks());

        return true;
    }

    public void breakTank() {
        if (hasLevel() && getLevel().isClientSide() || isRemoved()) {
            return;
        }

        if (!isMain() && getMainValve() != null && getMainValve() != this) {
            getMainValve().breakTank();
            return;
        }

        TankManager.INSTANCE.remove(getLevel(), getBlockPos());
        NetworkHandler.sendPacketToAllPlayers(new FFSPacket.Client.OnTankBreak(this));

        for (AbstractTankEntity tankEntity : getAllTankTiles(false)) {
            if (tankEntity == this || tankEntity.isRemoved()) {
                continue;
            }

            if (tankEntity instanceof AbstractTankValve valve) {
                valve.setTankConfig(null);
                valve.setValid(false);
                valve.updateBlockAndNeighbors(true);
            }

            tankEntity.setValvePos(null);
            tankEntity.markForUpdateNow(2);
        }

        setValid(false);

        tankTiles.clear();

        updateBlockAndNeighbors(true);
    }

    @Override
    public boolean isValid() {
        if (getMainValve() == null || getMainValve() == this)
            return this.isValid;

        return getMainValve().isValid;
    }

    private void setValid(boolean isValid) {
        this.isValid = isValid;

        if (getLevel() != null) {
            getLevel().setBlockAndUpdate(getBlockPos(), getBlockState().setValue(FFSStateProps.TILE_VALID, isValid));
        } else {
            markForUpdate();
        }
    }

    private void updateBlockAndNeighbors() {
        updateBlockAndNeighbors(false);
    }

    private void updateBlockAndNeighbors(boolean onlyThis) {
        if (getLevel().isClientSide())
            return;

        markForUpdateNow();

        if (!tankTiles.isEmpty() && !onlyThis) {
            for (AbstractTankEntity tile : tankTiles) {
                if (tile == this) {
                    continue;
                }

                tile.markForUpdateNow(2);
            }
        }
    }

    private void updateComparatorOutput() {
        if (this.lastComparatorOut != getComparatorOutput()) {
            this.lastComparatorOut = getComparatorOutput();
            if (isMain()) {
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
        } else {
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
        if (this.tile_name.isEmpty()) {
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
        if (isMain()) {
            isValid = nbt.getBoolean("IsValid");
            getTankConfig().readFromNBT(nbt);

            if (getLevel() != null && getLevel().isClientSide()) {
                if (isValid()) {
                    if (!TankManager.INSTANCE.isValveInHashSets(getLevel(), this)) {
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
        if (isMain()) {
            nbt.putBoolean("IsValid", isValid());
            getTankConfig().writeToNBT(nbt);
        }

        saveTileNameToNBT(nbt);
        saveTileFacingToNBT(nbt);
    }

    @Override
    public AABB getRenderBoundingBox(AbstractTankValve blockEntity)
    {
        return INFINITE_EXTENT_AABB;
    }


    public int getComparatorOutput() {
        if (!isValid()) {
            return 0;
        }

        return Mth.floor(((float) this.getTankConfig().getFluidAmount() / this.getTankConfig().getFluidCapacity()) * 14.0F);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AbstractTankValve && ((AbstractTankValve) obj).getBlockPos().equals(getBlockPos());
    }
}
