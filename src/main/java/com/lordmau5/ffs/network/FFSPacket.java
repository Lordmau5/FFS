package com.lordmau5.ffs.network;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.tile.abstracts.AbstractTankTile;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import com.lordmau5.ffs.tile.interfaces.INameableTile;
import com.lordmau5.ffs.util.LayerBlockPos;
import com.lordmau5.ffs.util.TankManager;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Supplier;

public abstract class FFSPacket {
    public static abstract class Client {
        public static class OpenGUI {
            public BlockPos pos;
            public boolean isValve;

            public OpenGUI() {
            }

            public OpenGUI(AbstractTankTile tile, boolean isValve) {
                this.pos = tile.getPos();
                this.isValve = isValve;
            }

            public void encode(PacketBuffer buffer) {
                buffer.writeBlockPos(this.pos);
                buffer.writeBoolean(this.isValve);
            }

            public static OpenGUI decode(PacketBuffer buffer) {
                OpenGUI packet = new OpenGUI();

                packet.pos = buffer.readBlockPos();
                packet.isValve = buffer.readBoolean();

                return packet;
            }

            public BlockPos getValvePos() {
                return pos;
            }

            public boolean getIsValve() {
                return isValve;
            }

            public static void onReceived(OpenGUI msg, Supplier<NetworkEvent.Context> ctxSupplier) {
                NetworkEvent.Context ctx = ctxSupplier.get();

                ctx.enqueueWork(() -> FFSPacketClientHandler.handleOnOpenGUI(msg));

                ctx.setPacketHandled(true);
            }
        }

        public static class OnTankBuild {
            private BlockPos valvePos;
            private TreeMap<Integer, List<LayerBlockPos>> airBlocks;
            private TreeMap<Integer, List<LayerBlockPos>> frameBlocks;

            public OnTankBuild() {
            }

            public OnTankBuild(AbstractTankValve valve) {
                this.valvePos = valve.getPos();
                this.airBlocks = valve.getAirBlocks();
                this.frameBlocks = valve.getFrameBlocks();
            }

            public void encode(PacketBuffer buffer) {
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
            }

            public static OnTankBuild decode(PacketBuffer buffer) {
                OnTankBuild packet = new OnTankBuild();

                packet.valvePos = BlockPos.fromLong(buffer.readLong());

                packet.airBlocks = new TreeMap<>();
                int layerSize = buffer.readInt();
                for (int i = 0; i < layerSize; i++) {
                    int layer = buffer.readInt();
                    int airBlockSize = buffer.readInt();
                    List<LayerBlockPos> layerBlocks = new ArrayList<>();
                    for (int j = 0; j < airBlockSize; j++) {
                        layerBlocks.add(new LayerBlockPos(BlockPos.fromLong(buffer.readLong()), buffer.readInt()));
                    }
                    packet.airBlocks.put(layer, layerBlocks);
                }

                packet.frameBlocks = new TreeMap<>();
                layerSize = buffer.readInt();
                for (int i = 0; i < layerSize; i++) {
                    int layer = buffer.readInt();
                    int frameBlockSize = buffer.readInt();
                    List<LayerBlockPos> layerBlocks = new ArrayList<>();
                    for (int j = 0; j < frameBlockSize; j++) {
                        layerBlocks.add(new LayerBlockPos(BlockPos.fromLong(buffer.readLong()), buffer.readInt()));
                    }
                    packet.frameBlocks.put(layer, layerBlocks);
                }

                return packet;
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

            public static void onReceived(OnTankBuild msg, Supplier<NetworkEvent.Context> ctxSupplier) {
                NetworkEvent.Context ctx = ctxSupplier.get();

                ctx.enqueueWork(() -> FFSPacketClientHandler.handleOnTankBuild(msg));

                ctx.setPacketHandled(true);
            }
        }

        public static class OnTankBreak {
            private BlockPos valvePos;

            public OnTankBreak() {
            }

            public OnTankBreak(AbstractTankValve valve) {
                this.valvePos = valve.getPos();
            }

            public void encode(PacketBuffer buffer) {
                buffer.writeBlockPos(this.valvePos);
            }

            public static OnTankBreak decode(PacketBuffer buffer) {
                OnTankBreak packet = new OnTankBreak();

                packet.valvePos = buffer.readBlockPos();

                return packet;
            }

            public BlockPos getValvePos() {
                return valvePos;
            }

            public static void onReceived(OnTankBreak msg, Supplier<NetworkEvent.Context> ctxSupplier) {
                NetworkEvent.Context ctx = ctxSupplier.get();

                ctx.enqueueWork(() -> FFSPacketClientHandler.handleOnTankBreak(msg));

                ctx.setPacketHandled(true);
            }
        }

        public static class ClearTanks {

            public ClearTanks() {
            }

            public void encode(PacketBuffer buffer) {}

            public static ClearTanks decode(PacketBuffer buffer) {
                return new ClearTanks();
            }

            public static void onReceived(ClearTanks msg, Supplier<NetworkEvent.Context> ctxSupplier) {
                NetworkEvent.Context ctx = ctxSupplier.get();

                ctx.enqueueWork(() -> FancyFluidStorage.TANK_MANAGER.clear());

                ctx.setPacketHandled(true);
            }
        }
    }

    public static class Server {
        public static class UpdateTileName {
            private BlockPos pos;
            private String name;

            public UpdateTileName() {
            }

            public UpdateTileName(AbstractTankTile tankTile, String name) {
                this.pos = tankTile.getPos();
                this.name = name;
            }

            public void encode(PacketBuffer buffer) {
                buffer.writeBlockPos(this.pos);
                buffer.writeString(this.name);
            }

            public static UpdateTileName decode(PacketBuffer buffer) {
                UpdateTileName packet = new UpdateTileName();

                packet.pos = buffer.readBlockPos();
                packet.name = buffer.readString();

                return packet;
            }

            public BlockPos getPos() {
                return pos;
            }

            public String getName() {
                return name;
            }

            public static void onReceived(UpdateTileName msg, Supplier<NetworkEvent.Context> ctxSupplier) {
                NetworkEvent.Context ctx = ctxSupplier.get();

                ctx.enqueueWork(() -> {
                    ServerPlayerEntity playerEntity = ctx.getSender();
                    World world = playerEntity != null ? playerEntity.world : null;

                    if (world != null) {
                        TileEntity tile = world.getTileEntity(msg.getPos());
                        if (tile instanceof AbstractTankTile && tile instanceof INameableTile) {
                            AbstractTankTile abstractTankTile = (AbstractTankTile) tile;
                            ((INameableTile) abstractTankTile).setTileName(msg.getName());
                            abstractTankTile.markForUpdateNow();
                        }
                    }
                });

                ctx.setPacketHandled(true);
            }
        }

        public static class UpdateFluidLock {
            private BlockPos pos;
            private boolean fluidLock;

            public UpdateFluidLock() {
            }

            public UpdateFluidLock(AbstractTankValve valve) {
                this.pos = valve.getPos();
                this.fluidLock = valve.getTankConfig().isFluidLocked();
            }

            public void encode(PacketBuffer buffer) {
                buffer.writeBlockPos(this.pos);
                buffer.writeBoolean(this.fluidLock);
            }

            public static UpdateFluidLock decode(PacketBuffer buffer) {
                UpdateFluidLock packet = new UpdateFluidLock();

                packet.pos = buffer.readBlockPos();
                packet.fluidLock = buffer.readBoolean();

                return packet;
            }

            public BlockPos getPos() {
                return pos;
            }

            public boolean isFluidLock() {
                return fluidLock;
            }

            public static void onReceived(UpdateFluidLock msg, Supplier<NetworkEvent.Context> ctxSupplier) {
                NetworkEvent.Context ctx = ctxSupplier.get();

                ctx.enqueueWork(() -> {
                    ServerPlayerEntity playerEntity = ctx.getSender();
                    World world = playerEntity != null ? playerEntity.world : null;

                    if (world != null) {
                        TileEntity tile = world.getTileEntity(msg.getPos());
                        if (tile instanceof AbstractTankValve) {
                            AbstractTankValve valve = (AbstractTankValve) tile;
                            valve.setFluidLock(msg.isFluidLock());
                        }
                    }
                });

                ctx.setPacketHandled(true);
            }
        }

        public static class OnTankRequest {
            private BlockPos pos;

            public OnTankRequest() {
            }

            public OnTankRequest(AbstractTankValve valve) {
                this.pos = valve.getPos();
            }

            public void encode(PacketBuffer buffer) {
                buffer.writeBlockPos(this.pos);
            }

            public static OnTankRequest decode(PacketBuffer buffer) {
                OnTankRequest packet = new OnTankRequest();

                packet.pos = buffer.readBlockPos();

                return packet;
            }

            public BlockPos getPos() {
                return pos;
            }

            public static void onReceived(OnTankRequest msg, Supplier<NetworkEvent.Context> ctxSupplier) {
                NetworkEvent.Context ctx = ctxSupplier.get();

                ctx.enqueueWork(() -> {
                    ServerPlayerEntity playerEntity = ctx.getSender();
                    World world = playerEntity != null ? playerEntity.world : null;

                    if (world != null) {
                        TileEntity tile = world.getTileEntity(msg.getPos());
                        if (tile instanceof AbstractTankValve) {
                            NetworkHandler.sendPacketToPlayer(new FFSPacket.Client.OnTankBuild((AbstractTankValve) tile), playerEntity);
                        }
                    }
                });

                ctx.setPacketHandled(true);
            }
        }
    }
}
