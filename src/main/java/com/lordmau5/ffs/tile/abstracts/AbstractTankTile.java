package com.lordmau5.ffs.tile.abstracts;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public abstract class AbstractTankTile extends BlockEntity {

    /**
     * Necessary stuff for the interfaces.
     * Current interface list:
     * INameableTile, IFacingTile
     */
    protected Direction tile_facing = null;
    String tile_name = "";

    protected int needsUpdate = 0;
    private BlockPos mainValvePos;

    public AbstractTankTile(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    public void setNeedsUpdate() {
        if (this.needsUpdate == 0) {
            this.needsUpdate = 20;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        setNeedsUpdate();
    }

    public boolean isValid() {
        return getMainValve() != null && getMainValve().isValid();
    }

    void setValvePos(BlockPos mainValvePos) {
        this.mainValvePos = mainValvePos;
    }

    public AbstractTankValve getMainValve() {
        if (getLevel() != null && this.mainValvePos != null) {
            BlockEntity tile = getLevel().getBlockEntity(this.mainValvePos);
            return tile instanceof AbstractTankValve ? (AbstractTankValve) tile : null;
        }

        return null;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        setValvePos(nbt.contains("valvePos") ? BlockPos.of(nbt.getLong("valvePos")) : null);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        if (getMainValve() != null) {
            nbt.putLong("valvePos", getMainValve().getBlockPos().asLong());
        }
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);

        boolean oldIsValid = isValid();

        if (pkt.getTag() != null) {
            load(pkt.getTag());
        }

        if (getLevel() != null && getLevel().isClientSide() && oldIsValid != isValid()) {
            markForUpdateNow();
        }
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        ClientboundBlockEntityDataPacket packet = ClientboundBlockEntityDataPacket.create(this);
        if (packet.getTag() != null) {
            saveAdditional(packet.getTag());
        }
        return packet;
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        load(tag);
    }

    public void doUpdate() {
    }

    public void markForUpdate() {
        if (getLevel() == null) {
            setNeedsUpdate();
            return;
        }

        if (--this.needsUpdate == 0) {
            BlockState state = getLevel().getBlockState(getBlockPos());
            getLevel().sendBlockUpdated(getBlockPos(), state, state, 3);
            doUpdate();
            setChanged();
        }
    }

    public void markForUpdateNow() {
        this.needsUpdate = 1;
        markForUpdate();
    }

    public void markForUpdateNow(int when) {
        this.needsUpdate = Math.min(when, 20);
        markForUpdate();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T be) {
        AbstractTankTile tile = (AbstractTankTile) be;

        if (tile.needsUpdate > 0) {
            tile.markForUpdate();
        }
    }

    @Override
    public int hashCode() {
        return getBlockPos().hashCode();
    }

}
