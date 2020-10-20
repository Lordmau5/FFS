package com.lordmau5.ffs.network;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.client.gui.GuiValve;
import com.lordmau5.ffs.tile.abstracts.AbstractTankTile;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.TileEntity;

public class FFSPacketClientHandler {

    public static void handleOnOpenGUI(FFSPacket.Client.OpenGUI msg) {
        ClientWorld world = Minecraft.getInstance().world;

        if (world != null) {
            TileEntity tile = world.getTileEntity(msg.pos);
            if (!(tile instanceof AbstractTankTile)) {
                return;
            }

            Minecraft.getInstance().displayGuiScreen(new GuiValve((AbstractTankTile) tile, msg.isValve));
        }
    }

    public static void handleOnTankBuild(FFSPacket.Client.OnTankBuild msg) {
        ClientWorld world = Minecraft.getInstance().world;

        if ( world != null ) {
            TileEntity tile = world.getTileEntity(msg.getValvePos());
            if ( tile instanceof AbstractTankValve) {
                FancyFluidStorage.TANK_MANAGER.add(world, msg.getValvePos(), msg.getAirBlocks(), msg.getFrameBlocks());
            }
        }
    }

    public static void handleOnTankBreak(FFSPacket.Client.OnTankBreak msg) {
        ClientWorld world = Minecraft.getInstance().world;

        if (world != null) {
            FancyFluidStorage.TANK_MANAGER.remove(world, msg.getValvePos());
        }
    }
}
