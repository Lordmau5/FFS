package com.lordmau5.ffs.tile.interfaces;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

public interface IFacingTile {

    default Direction getTileFacing() {
        return null;
    }

    void setTileFacing(Direction facing);

    default void saveTileFacingToNBT(CompoundNBT tag) {
        if ( getTileFacing() != null ) {
            tag.putInt("TileFacing", getTileFacing().getIndex());
        }
    }

    default void readTileFacingFromNBT(CompoundNBT tag) {
        if ( tag.contains("TileFacing") ) {
            setTileFacing(Direction.byIndex(tag.getInt("TileFacing")));
        }
    }

}
