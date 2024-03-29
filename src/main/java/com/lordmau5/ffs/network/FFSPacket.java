package com.lordmau5.ffs.network;

import com.google.common.collect.Sets;
import com.lordmau5.ffs.blockentity.abstracts.AbstractTankEntity;
import com.lordmau5.ffs.blockentity.abstracts.AbstractTankValve;
import com.lordmau5.ffs.blockentity.interfaces.INameableEntity;
import com.lordmau5.ffs.util.TankManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.TreeMap;
import java.util.function.Supplier;

public abstract class FFSPacket {
    public static abstract class Client {
        public static class OpenGUI {
            public BlockPos pos;
            public boolean isValve;

            public OpenGUI() {
            }

            public OpenGUI(AbstractTankEntity tile, boolean isValve) {
                this.pos = tile.getBlockPos();
                this.isValve = isValve;
            }

            public void encode(FriendlyByteBuf buffer) {
                buffer.writeBlockPos(this.pos);
                buffer.writeBoolean(this.isValve);
            }

            public static OpenGUI decode(FriendlyByteBuf buffer) {
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
            private TreeMap<Integer, HashSet<BlockPos>> airBlocks;
            private TreeMap<Integer, HashSet<BlockPos>> frameBlocks;

            public OnTankBuild() {
            }

            public OnTankBuild(AbstractTankValve valve) {
                this.valvePos = valve.getBlockPos();
                this.airBlocks = valve.getAirBlocks();
                this.frameBlocks = valve.getFrameBlocks();
            }

            public void encode(FriendlyByteBuf buffer) {
                buffer.writeLong(this.valvePos.asLong());

                buffer.writeInt(this.airBlocks.size());
                for (int layer : this.airBlocks.keySet()) {
                    buffer.writeInt(layer);

                    var layerAirBlocks = this.airBlocks.get(layer);
                    buffer.writeCollection(layerAirBlocks, FriendlyByteBuf::writeBlockPos);
                }

                buffer.writeInt(this.frameBlocks.size());
                for (int layer : this.frameBlocks.keySet()) {
                    buffer.writeInt(layer);

                    var layerFrameBlocks = this.frameBlocks.get(layer);
                    buffer.writeCollection(layerFrameBlocks, FriendlyByteBuf::writeBlockPos);
                }
            }

            public static OnTankBuild decode(FriendlyByteBuf buffer) {
                OnTankBuild packet = new OnTankBuild();

                packet.valvePos = BlockPos.of(buffer.readLong());

                packet.airBlocks = new TreeMap<>();
                int layerSize = buffer.readInt();
                for (int i = 0; i < layerSize; i++) {
                    int layer = buffer.readInt();

                    HashSet<BlockPos> layerBlocks = buffer.readCollection(Sets::newHashSetWithExpectedSize, reader -> BlockPos.of(reader.readLong()));

                    packet.airBlocks.put(layer, layerBlocks);
                }

                packet.frameBlocks = new TreeMap<>();
                layerSize = buffer.readInt();
                for (int i = 0; i < layerSize; i++) {
                    int layer = buffer.readInt();

                    HashSet<BlockPos> layerBlocks = buffer.readCollection(Sets::newHashSetWithExpectedSize, reader -> BlockPos.of(reader.readLong()));

                    packet.frameBlocks.put(layer, layerBlocks);
                }

                return packet;
            }

            public BlockPos getValvePos() {
                return valvePos;
            }

            public TreeMap<Integer, HashSet<BlockPos>> getAirBlocks() {
                return airBlocks;
            }

            public TreeMap<Integer, HashSet<BlockPos>> getFrameBlocks() {
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
                this.valvePos = valve.getBlockPos();
            }

            public void encode(FriendlyByteBuf buffer) {
                buffer.writeBlockPos(this.valvePos);
            }

            public static OnTankBreak decode(FriendlyByteBuf buffer) {
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

            public void encode(FriendlyByteBuf buffer) {
            }

            public static ClearTanks decode(FriendlyByteBuf buffer) {
                return new ClearTanks();
            }

            public static void onReceived(ClearTanks msg, Supplier<NetworkEvent.Context> ctxSupplier) {
                NetworkEvent.Context ctx = ctxSupplier.get();

                ctx.enqueueWork(() -> TankManager.INSTANCE.clear());

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

            public UpdateTileName(AbstractTankEntity tankTile, String name) {
                this.pos = tankTile.getBlockPos();
                this.name = name;
            }

            public void encode(FriendlyByteBuf buffer) {
                buffer.writeBlockPos(this.pos);
                buffer.writeUtf(this.name);
            }

            public static UpdateTileName decode(FriendlyByteBuf buffer) {
                UpdateTileName packet = new UpdateTileName();

                packet.pos = buffer.readBlockPos();
                packet.name = buffer.readUtf();

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
                    ServerPlayer playerEntity = ctx.getSender();
                    Level world = playerEntity != null ? playerEntity.level() : null;

                    if (world != null) {
                        BlockEntity tile = world.getBlockEntity(msg.getPos());
                        if (tile instanceof AbstractTankEntity && tile instanceof INameableEntity) {
                            AbstractTankEntity abstractTankTile = (AbstractTankEntity) tile;
                            ((INameableEntity) abstractTankTile).setTileName(msg.getName());
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
                this.pos = valve.getBlockPos();
                this.fluidLock = valve.getTankConfig().isFluidLocked();
            }

            public void encode(FriendlyByteBuf buffer) {
                buffer.writeBlockPos(this.pos);
                buffer.writeBoolean(this.fluidLock);
            }

            public static UpdateFluidLock decode(FriendlyByteBuf buffer) {
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
                    ServerPlayer playerEntity = ctx.getSender();
                    Level world = playerEntity != null ? playerEntity.level() : null;

                    if (world != null) {
                        BlockEntity tile = world.getBlockEntity(msg.getPos());
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
                this.pos = valve.getBlockPos();
            }

            public void encode(FriendlyByteBuf buffer) {
                buffer.writeBlockPos(this.pos);
            }

            public static OnTankRequest decode(FriendlyByteBuf buffer) {
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
                    ServerPlayer playerEntity = ctx.getSender();
                    Level world = playerEntity != null ? playerEntity.level() : null;

                    if (world != null) {
                        BlockEntity tile = world.getBlockEntity(msg.getPos());
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
