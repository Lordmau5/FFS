package com.lordmau5.ffs.network;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;


public class NetworkHandler {
    private static int id = 0;

    private static final String PROTOCOL_VERSION = "1";

    public static void init(IEventBus bus) {
        bus.addListener(NetworkHandler::registerEvent);
    }

    public static void registerEvent(RegisterPayloadHandlerEvent event)
    {
        IPayloadRegistrar registrar = event.registrar(FancyFluidStorage.MOD_ID).versioned(PROTOCOL_VERSION);
        registerChannels(registrar);
    }

    public static void registerChannels(IPayloadRegistrar registrar) {
        registerBiDirectionalHandlers(registrar);
        registerServerHandlers(registrar);
        registerClientHandlers(registrar);
    }

    private static void registerBiDirectionalHandlers(IPayloadRegistrar INSTANCE) {
        // Update Fluid Lock
        INSTANCE.play(
                FFSPacket.Server.UpdateFluidLock.ID,
                FFSPacket.Server.UpdateFluidLock::decode,
                FFSPacket.Server.UpdateFluidLock::onReceived
        );

        // Update Tile Name
        INSTANCE.play(
                FFSPacket.Server.UpdateTileName.ID,
                FFSPacket.Server.UpdateTileName::decode,
                FFSPacket.Server.UpdateTileName::onReceived
        );
    }

    private static void registerServerHandlers(IPayloadRegistrar INSTANCE) {
        // On Tank Request
        INSTANCE.play(
                FFSPacket.Server.OnTankRequest.ID,
                FFSPacket.Server.OnTankRequest::decode,
                FFSPacket.Server.OnTankRequest::onReceived
        );
    }

    private static void registerClientHandlers(IPayloadRegistrar INSTANCE) {
        // Open GUI
        INSTANCE.play(
                FFSPacket.Client.OpenGUI.ID,
                FFSPacket.Client.OpenGUI::decode,
                FFSPacket.Client.OpenGUI::onReceived
        );

        // On Tank Build
        INSTANCE.play(
                FFSPacket.Client.OnTankBuild.ID,
                FFSPacket.Client.OnTankBuild::decode,
                FFSPacket.Client.OnTankBuild::onReceived
        );

        // On Tank Break
        INSTANCE.play(
                FFSPacket.Client.OnTankBreak.ID,
                FFSPacket.Client.OnTankBreak::decode,
                FFSPacket.Client.OnTankBreak::onReceived
        );

        // Clear Tanks
        INSTANCE.play(
                FFSPacket.Client.ClearTanks.ID,
                FFSPacket.Client.ClearTanks::decode,
                FFSPacket.Client.ClearTanks::onReceived
        );
    }

    public static void sendPacketToPlayer(CustomPacketPayload msg, ServerPlayer player) {
        PacketDistributor.PLAYER.with(player).send(msg);
    }

    public static void sendPacketToAllPlayers(CustomPacketPayload msg) {
        PacketDistributor.ALL.noArg().send(msg);
    }

    public static void sendPacketToServer(CustomPacketPayload msg) {
        PacketDistributor.SERVER.noArg().send(msg);
    }
}
