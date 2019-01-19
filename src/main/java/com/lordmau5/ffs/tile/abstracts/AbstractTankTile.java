package com.lordmau5.ffs.tile.abstracts;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by Dustin on 20.01.2016.
 */
public abstract class AbstractTankTile extends TileEntity implements ITickable {

    /**
     * Necessary stuff for the interfaces.
     * Current interface list:
     * INameableTile, IFacingTile
     */
    protected EnumFacing tile_facing = null;
    String tile_name = "";

    private int needsUpdate = 0;
    private BlockPos masterValvePos;

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
        return getMasterValve() != null && getMasterValve().isValid();
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    void setValvePos(BlockPos masterValvePos) {
        this.masterValvePos = masterValvePos;
    }

    public AbstractTankValve getMasterValve() {
        if ( getWorld() != null && this.masterValvePos != null ) {
            TileEntity tile = getWorld().getTileEntity(this.masterValvePos);
            return tile instanceof AbstractTankValve ? (AbstractTankValve) tile : null;
        }

        return null;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);

        setValvePos(tag.hasKey("valvePos") ? BlockPos.fromLong(tag.getLong("valvePos")) : null);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        if ( getMasterValve() != null ) {
            tag.setLong("valvePos", getMasterValve().getPos().toLong());
        }

        super.writeToNBT(tag);
        return tag;
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);

        boolean oldIsValid = isValid();

        readFromNBT(pkt.getNbtCompound());

        if ( getWorld() != null && getWorld().isRemote && oldIsValid != isValid() ) {
            markForUpdateNow();
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new SPacketUpdateTileEntity(getPos(), 0, tag);
    }

    public void markForUpdate() {
        if ( getWorld() == null ) {
            setNeedsUpdate();
            return;
        }

        if ( this.needsUpdate-- == 0 ) {
            IBlockState state = getWorld().getBlockState(getPos());
            getWorld().notifyBlockUpdate(getPos(), state, state, 3);
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
    public void update() {
        if ( this.needsUpdate > 0 ) {
            markForUpdate();
        }
    }

    @Override
    public int hashCode() {
        return getPos().hashCode();
    }

}
