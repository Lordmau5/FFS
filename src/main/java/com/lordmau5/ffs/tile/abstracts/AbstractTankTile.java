package com.lordmau5.ffs.tile.abstracts;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public abstract class AbstractTankTile extends TileEntity implements ITickableTileEntity {

    /**
     * Necessary stuff for the interfaces.
     * Current interface list:
     * INameableTile, IFacingTile
     */
    protected Direction tile_facing = null;
    String tile_name = "";

    private int needsUpdate = 0;
    private BlockPos mainValvePos;

    public AbstractTankTile(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public void setNeedsUpdate() {
        if ( this.needsUpdate == 0 ) {
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
        if ( getWorld() != null && this.mainValvePos != null ) {
            TileEntity tile = getWorld().getTileEntity(this.mainValvePos);
            return tile instanceof AbstractTankValve ? (AbstractTankValve) tile : null;
        }

        return null;
    }

    @Override
    public void read(CompoundNBT nbt) {
        super.read(nbt);

        setValvePos(nbt.contains("valvePos") ? BlockPos.fromLong(nbt.getLong("valvePos")) : null);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);

        if ( getMainValve() != null ) {
            nbt.putLong("valvePos", getMainValve().getPos().toLong());
        }

        return nbt;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        super.onDataPacket(net, pkt);

        boolean oldIsValid = isValid();

        read(pkt.getNbtCompound());

        if ( getWorld() != null && getWorld().isRemote && oldIsValid != isValid() ) {
            markForUpdateNow();
        }
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT tag = new CompoundNBT();
        write(tag);
        return new SUpdateTileEntityPacket(getPos(), 42, tag);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT tag = new CompoundNBT();
        write(tag);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundNBT tag) {
        read(tag);
    }

    public void doUpdate() {}

    public void markForUpdate() {
        if ( getWorld() == null ) {
            setNeedsUpdate();
            return;
        }

        if ( --this.needsUpdate == 0 ) {
            BlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
            doUpdate();
            markDirty();
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

    @Override
    public void tick() {
        if ( this.needsUpdate > 0 ) {
            markForUpdate();
        }
    }

    @Override
    public int hashCode() {
        return getPos().hashCode();
    }

}
