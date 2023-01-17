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
            tag.putInt("TileFacing", getTileFacing().get3DDataValue());
        }
    }

    default void readTileFacingFromNBT(CompoundNBT tag) {
        if ( tag.contains("TileFacing") ) {
            setTileFacing(Direction.from3DDataValue(tag.getInt("TileFacing")));
        }
    }

}
