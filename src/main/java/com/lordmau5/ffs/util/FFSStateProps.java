package com.lordmau5.ffs.util;

import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class FFSStateProps {
    public static final BooleanProperty TILE_VALID = BooleanProperty.create("tile_valid");
    public static final BooleanProperty TILE_MAIN = BooleanProperty.create("tile_main");
    // TODO: Facing maybe? So we only render the overlay on the inside and outside?
    // Maybe not really required since... idk, why?
}
