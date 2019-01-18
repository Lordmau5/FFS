package com.lordmau5.ffs.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;

/**
 * Created by Dustin on 07.07.2015.
 */
class PacketCodec extends FMLIndexedMessageToMessageCodec<FFSPacket> {

    private int lastDiscriminator = 0;

    public PacketCodec() {
        addPacket(FFSPacket.Server.UpdateTileName.class);
        addPacket(FFSPacket.Server.UpdateFluidLock.class);

        addPacket(FFSPacket.Server.OnTankRequest.class);

        addPacket(FFSPacket.Client.OnTankBuild.class);
        addPacket(FFSPacket.Client.OnTankBreak.class);
    }

    private void addPacket(Class<? extends FFSPacket> type) {
        this.addDiscriminator(lastDiscriminator, type);
        lastDiscriminator++;
    }

    @Override
    public void encodeInto(ChannelHandlerContext ctx, FFSPacket msg, ByteBuf target) throws Exception {
        msg.encode(target);
    }

    @Override
    public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, FFSPacket msg) {
        msg.decode(source);
    }
}
