package com.lordmau5.ffs.blockentity.interfaces;

import net.minecraft.nbt.CompoundTag;

public interface INameableEntity {

    default String getTileName() {
        return "";
    }

    void setTileName(String name);

    default void saveTileNameToNBT(CompoundTag tag) {
        if (!getTileName().isEmpty()) {
            tag.putString("TileName", getTileName());
        }
    }

    default void readTileNameFromNBT(CompoundTag tag) {
        if (tag.contains("TileName")) {
            setTileName(tag.getString("TileName"));
        }
    }

}
