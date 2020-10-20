package com.lordmau5.ffs.util;

import net.minecraft.util.math.BlockPos;

public class LayerBlockPos extends BlockPos {
    private int layer;

    public LayerBlockPos(BlockPos pos, int layer) {
        super(pos);

        this.layer = layer;
    }

    public int getLayer() {
        return this.layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object otherLayeredBlockPos) {
        return super.equals(otherLayeredBlockPos);
    }
}
