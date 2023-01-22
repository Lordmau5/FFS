package com.lordmau5.ffs.util;

import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class FFSStateProps {
    public static final BooleanProperty TILE_VALID = BooleanProperty.create("tile_valid");
    public static final BooleanProperty TILE_MAIN = BooleanProperty.create("tile_main");

    public static final DirectionProperty FACING = DirectionalBlock.FACING;
}
