package com.lordmau5.ffs.network;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class NetworkHandler {
    private static int id = 0;

    private static final String PROTOCOL_VERSION = "1";
    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(FancyFluidStorage.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerChannels() {
        registerBiDirectionalHandlers();
        registerServerHandlers();
        registerClientHandlers();
    }

    private static void registerBiDirectionalHandlers() {
        // Update Fluid Lock
        INSTANCE.registerMessage(
                id++,
                FFSPacket.Server.UpdateFluidLock.class,
                FFSPacket.Server.UpdateFluidLock::encode,
                FFSPacket.Server.UpdateFluidLock::decode,
                FFSPacket.Server.UpdateFluidLock::onReceived
        );

        // Update Tile Name
        INSTANCE.registerMessage(
                id++,
                FFSPacket.Server.UpdateTileName.class,
                FFSPacket.Server.UpdateTileName::encode,
                FFSPacket.Server.UpdateTileName::decode,
                FFSPacket.Server.UpdateTileName::onReceived
        );
    }

    private static void registerServerHandlers() {
        // On Tank Request
        INSTANCE.registerMessage(
                id++,
                FFSPacket.Server.OnTankRequest.class,
                FFSPacket.Server.OnTankRequest::encode,
                FFSPacket.Server.OnTankRequest::decode,
                FFSPacket.Server.OnTankRequest::onReceived,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    private static void registerClientHandlers() {
        // Open GUI
        INSTANCE.registerMessage(
                id++,
                FFSPacket.Client.OpenGUI.class,
                FFSPacket.Client.OpenGUI::encode,
                FFSPacket.Client.OpenGUI::decode,
                FFSPacket.Client.OpenGUI::onReceived,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        // On Tank Build
        INSTANCE.registerMessage(
                id++,
                FFSPacket.Client.OnTankBuild.class,
                FFSPacket.Client.OnTankBuild::encode,
                FFSPacket.Client.OnTankBuild::decode,
                FFSPacket.Client.OnTankBuild::onReceived,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        // On Tank Break
        INSTANCE.registerMessage(
                id++,
                FFSPacket.Client.OnTankBreak.class,
                FFSPacket.Client.OnTankBreak::encode,
                FFSPacket.Client.OnTankBreak::decode,
                FFSPacket.Client.OnTankBreak::onReceived,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );

        // Clear Tanks
        INSTANCE.registerMessage(
                id++,
                FFSPacket.Client.ClearTanks.class,
                FFSPacket.Client.ClearTanks::encode,
                FFSPacket.Client.ClearTanks::decode,
                FFSPacket.Client.ClearTanks::onReceived,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static void sendPacketToPlayer(Object packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendPacketToAllPlayers(Object packet) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }

    public static void sendPacketToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }
}
