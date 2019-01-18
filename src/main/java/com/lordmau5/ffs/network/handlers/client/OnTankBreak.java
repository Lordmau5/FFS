package com.lordmau5.ffs.network.handlers.client;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.network.FFSPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by Lordmau5 on 12.11.2016.
 */
public class OnTankBreak extends SimpleChannelInboundHandler<FFSPacket.Client.OnTankBreak> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FFSPacket.Client.OnTankBreak msg) throws Exception {
        FancyFluidStorage.tankManager.remove(msg.getDimension(), msg.getValvePos());
    }
}