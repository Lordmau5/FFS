package com.lordmau5.ffs.network;

import com.lordmau5.ffs.tile.abstracts.AbstractTankTile;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import com.lordmau5.ffs.util.LayerBlockPos;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by Dustin on 07.07.2015.
 */
public abstract class FFSPacket {
    public abstract void encode(ByteBuf buffer);

    public abstract void decode(ByteBuf buffer);

    public static abstract class Client {
        public static class OnTankBuild extends FFSPacket {
            private int dimensionId;
            private BlockPos valvePos;
            private TreeMap<Integer, List<LayerBlockPos>> airBlocks;
            private TreeMap<Integer, List<LayerBlockPos>> frameBlocks;

            public OnTankBuild() {
            }

            public OnTankBuild(AbstractTankValve valve) {
                this.dimensionId = valve.getWorld().provider.getDimension();
                this.valvePos = valve.getPos();
                this.airBlocks = valve.getAirBlocks();
                this.frameBlocks = valve.getFrameBlocks();
            }

            @Override
            public void encode(ByteBuf buffer) {
                buffer.writeInt(this.dimensionId);
                buffer.writeLong(this.valvePos.toLong());

                buffer.writeInt(this.airBlocks.size());
                for (int layer : this.airBlocks.keySet()) {
                    buffer.writeInt(layer);
                    buffer.writeInt(this.airBlocks.get(layer).size());
                    for (LayerBlockPos pos : this.airBlocks.get(layer)) {
                        buffer.writeLong(pos.toLong());
                        buffer.writeInt(pos.getLayer());
                    }
                }

                buffer.writeInt(this.frameBlocks.size());
                for (int layer : this.frameBlocks.keySet()) {
                    buffer.writeInt(layer);
                    buffer.writeInt(this.frameBlocks.get(layer).size());
                    for (LayerBlockPos pos : this.frameBlocks.get(layer)) {
                        buffer.writeLong(pos.toLong());
                        buffer.writeInt(pos.getLayer());
                    }
                }

                System.out.println(buffer.capacity());
            }

            @Override
            public void decode(ByteBuf buffer) {
                this.dimensionId = buffer.readInt();
                this.valvePos = BlockPos.fromLong(buffer.readLong());

                this.airBlocks = new TreeMap<>();
                int layerSize = buffer.readInt();
                for (int i = 0; i < layerSize; i++) {
                    int layer = buffer.readInt();
                    int airBlockSize = buffer.readInt();
                    List<LayerBlockPos> layerBlocks = new ArrayList<>();
                    for (int j = 0; j < airBlockSize; j++) {
                        layerBlocks.add(new LayerBlockPos(BlockPos.fromLong(buffer.readLong()), buffer.readInt()));
                    }
                    this.airBlocks.put(layer, layerBlocks);
                }

                this.frameBlocks = new TreeMap<>();
                layerSize = buffer.readInt();
                for (int i = 0; i < layerSize; i++) {
                    int layer = buffer.readInt();
                    int frameBlockSize = buffer.readInt();
                    List<LayerBlockPos> layerBlocks = new ArrayList<>();
                    for (int j = 0; j < frameBlockSize; j++) {
                        layerBlocks.add(new LayerBlockPos(BlockPos.fromLong(buffer.readLong()), buffer.readInt()));
                    }
                    this.frameBlocks.put(layer, layerBlocks);
                }
            }

            public int getDimension() {
                return dimensionId;
            }

            public BlockPos getValvePos() {
                return valvePos;
            }

            public TreeMap<Integer, List<LayerBlockPos>> getAirBlocks() {
                return airBlocks;
            }

            public TreeMap<Integer, List<LayerBlockPos>> getFrameBlocks() {
                return frameBlocks;
            }
        }

        public static class OnTankBreak extends FFSPacket {
            private int dimensionId;
            private BlockPos valvePos;

            public OnTankBreak() {
            }

            public OnTankBreak(AbstractTankValve valve) {
                this.dimensionId = valve.getWorld().provider.getDimension();
                this.valvePos = valve.getPos();
            }

            @Override
            public void encode(ByteBuf buffer) {
                buffer.writeInt(this.dimensionId);
                buffer.writeLong(this.valvePos.toLong());
            }

            @Override
            public void decode(ByteBuf buffer) {
                this.dimensionId = buffer.readInt();
                this.valvePos = BlockPos.fromLong(buffer.readLong());
            }

            public int getDimension() {
                return dimensionId;
            }

            public BlockPos getValvePos() {
                return valvePos;
            }
        }
    }

    public static class Server {
        public static class UpdateTileName extends FFSPacket {
            private BlockPos pos;
            private String name;

            public UpdateTileName() {
            }

            public UpdateTileName(AbstractTankTile tankTile, String name) {
                this.pos = tankTile.getPos();
                this.name = name;
            }

            @Override
            public void encode(ByteBuf buffer) {
                buffer.writeLong(this.pos.toLong());
                ByteBufUtils.writeUTF8String(buffer, this.name);
            }

            @Override
            public void decode(ByteBuf buffer) {
                this.pos = BlockPos.fromLong(buffer.readLong());
                this.name = ByteBufUtils.readUTF8String(buffer);
            }

            public BlockPos getPos() {
                return pos;
            }

            public String getName() {
                return name;
            }
        }

        public static class UpdateFluidLock extends FFSPacket {
            private BlockPos pos;
            private boolean fluidLock;

            public UpdateFluidLock() {
            }

            public UpdateFluidLock(AbstractTankValve valve) {
                this.pos = valve.getPos();
                this.fluidLock = valve.getTankConfig().isFluidLocked();
            }

            @Override
            public void encode(ByteBuf buffer) {
                buffer.writeLong(this.pos.toLong());
                buffer.writeBoolean(this.fluidLock);
            }

            @Override
            public void decode(ByteBuf buffer) {
                this.pos = BlockPos.fromLong(buffer.readLong());
                this.fluidLock = buffer.readBoolean();
            }

            public BlockPos getPos() {
                return pos;
            }

            public boolean isFluidLock() {
                return fluidLock;
            }
        }

        public static class OnTankRequest extends FFSPacket {
            private BlockPos pos;

            public OnTankRequest() {
            }

            public OnTankRequest(AbstractTankValve valve) {
                this.pos = valve.getPos();
            }

            @Override
            public void encode(ByteBuf buffer) {
                buffer.writeLong(this.pos.toLong());
            }

            @Override
            public void decode(ByteBuf buffer) {
                this.pos = BlockPos.fromLong(buffer.readLong());
            }

            public BlockPos getPos() {
                return pos;
            }
        }
    }
}
