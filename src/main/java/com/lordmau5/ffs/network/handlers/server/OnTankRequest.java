package com.lordmau5.ffs.network.handlers.server;

import com.lordmau5.ffs.network.FFSPacket;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Created by Lordmau5 on 12.11.2016.
 */
public class OnTankRequest extends SimpleChannelInboundHandler<FFSPacket.Server.OnTankRequest> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FFSPacket.Server.OnTankRequest msg) throws Exception {
		World world = NetworkHandler.getPlayer(ctx).getEntityWorld();
		if(world != null) {
			TileEntity tile = world.getTileEntity(msg.getPos());
			if(tile != null && tile instanceof AbstractTankValve) {
				NetworkHandler.sendPacketToPlayer(new FFSPacket.Client.OnTankBuild((AbstractTankValve) tile), NetworkHandler.getPlayer(ctx));
			}
		}
	}
}
