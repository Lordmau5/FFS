package com.lordmau5.ffs.tile.interfaces;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public interface IFacingTile {

    default Direction getTileFacing() {
        return null;
    }

    void setTileFacing(Direction facing);

    default void saveTileFacingToNBT(CompoundTag tag) {
        if ( getTileFacing() != null ) {
            tag.putInt("TileFacing", getTileFacing().get3DDataValue());
        }
    }

    default void readTileFacingFromNBT(CompoundTag tag) {
        if ( tag.contains("TileFacing") ) {
            setTileFacing(Direction.from3DDataValue(tag.getInt("TileFacing")));
        }
    }

}
