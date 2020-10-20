package com.lordmau5.ffs.tile.interfaces;

import net.minecraft.nbt.CompoundNBT;

public interface INameableTile {

    default String getTileName() {
        return "";
    }

    void setTileName(String name);

    default void saveTileNameToNBT(CompoundNBT tag) {
        if ( !getTileName().isEmpty() ) {
            tag.putString("TileName", getTileName());
        }
    }

    default void readTileNameFromNBT(CompoundNBT tag) {
        if ( tag.contains("TileName") ) {
            setTileName(tag.getString("TileName"));
        }
    }

}
