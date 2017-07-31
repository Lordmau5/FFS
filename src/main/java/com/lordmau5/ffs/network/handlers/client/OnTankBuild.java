package com.lordmau5.ffs.network.handlers.client;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.network.FFSPacket;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by Lordmau5 on 12.11.2016.
 */
public class OnTankBuild extends SimpleChannelInboundHandler<FFSPacket.Client.OnTankBuild>
{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FFSPacket.Client.OnTankBuild msg) throws Exception
    {
        if (Minecraft.getMinecraft().world != null)
        {
            TileEntity tile = Minecraft.getMinecraft().world.getTileEntity(msg.getValvePos());
            if (tile != null && tile instanceof AbstractTankValve)
            {
                FancyFluidStorage.tankManager.add(Minecraft.getMinecraft().world, msg.getValvePos(), msg.getAirBlocks(), msg.getFrameBlocks());
            }
        }
    }
}
