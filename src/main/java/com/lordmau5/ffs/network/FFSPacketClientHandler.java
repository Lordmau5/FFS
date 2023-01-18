package com.lordmau5.ffs.network;

import com.lordmau5.ffs.client.gui.GuiValve;
import com.lordmau5.ffs.tile.abstracts.AbstractTankTile;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import com.lordmau5.ffs.util.TankManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public class FFSPacketClientHandler {

    public static void handleOnOpenGUI(FFSPacket.Client.OpenGUI msg) {
        ClientLevel world = Minecraft.getInstance().level;

        if (world != null) {
            BlockEntity tile = world.getBlockEntity(msg.pos);
            if (!(tile instanceof AbstractTankTile)) {
                return;
            }

            Minecraft.getInstance().setScreen(new GuiValve((AbstractTankTile) tile, msg.isValve));
        }
    }

    public static void handleOnTankBuild(FFSPacket.Client.OnTankBuild msg) {
        ClientLevel world = Minecraft.getInstance().level;

        if ( world != null ) {
            BlockEntity tile = world.getBlockEntity(msg.getValvePos());
            if ( tile instanceof AbstractTankValve) {
                TankManager.INSTANCE.add(world, msg.getValvePos(), msg.getAirBlocks(), msg.getFrameBlocks());
            }
        }
    }

    public static void handleOnTankBreak(FFSPacket.Client.OnTankBreak msg) {
        ClientLevel world = Minecraft.getInstance().level;

        if (world != null) {
            TankManager.INSTANCE.remove(world, msg.getValvePos());
        }
    }
}
