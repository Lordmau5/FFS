package com.lordmau5.ffs.client;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.config.Config;
import com.lordmau5.ffs.tile.abstracts.AbstractTankTile;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Created by Dustin on 14.02.2016.
 */
public class OverlayRenderHandler {

	public static TextureAtlasSprite overlayTexture;
	private final Minecraft mc = Minecraft.getMinecraft();
	private double playerX;
	private double playerY;
	private double playerZ;
	private BlockPos lastPos;
	private int ticksRemaining;
	private BlockRenderLayer[] renderLayers = null;

	@SubscribeEvent
	public void clientEndTick(TickEvent.ClientTickEvent event) {
		if(event.phase != TickEvent.Phase.END) {
			return;
		}

		if(ticksRemaining > 0) {
			ticksRemaining--;
		}

		if(mc.objectMouseOver != null) {
			BlockPos pos = mc.objectMouseOver.getBlockPos();
			if(pos != null && lastPos != null && !pos.equals(lastPos)) {
				int maxTicks = 20 * 5;
				ticksRemaining = maxTicks;
			}
			lastPos = pos;
		}
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event) {
		if(!Config.TANK_OVERLAY_RENDER) {
			return;
		}

		EntityPlayer player = Minecraft.getMinecraft().player;
		if(lastPos == null) {
			return;
		}

		World world = player.getEntityWorld();
		if(world == null) {
			return;
		}

		AbstractTankValve valve = null;

		TileEntity tile = world.getTileEntity(lastPos);
		if(tile != null && tile instanceof AbstractTankTile) {
			valve = ((AbstractTankTile) tile).getMasterValve();
		}
		else {
			if(FancyFluidStorage.tankManager.isPartOfTank(world, lastPos)) {
				valve = FancyFluidStorage.tankManager.getValveForBlock(world, lastPos);
			}
		}

		if(valve == null || !valve.isValid()) {
			return;
		}

		playerX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) event.getPartialTicks();
		playerY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) event.getPartialTicks();
		playerZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) event.getPartialTicks();

		if(renderLayers == null) {
			renderLayers = BlockRenderLayer.values();
		}

		drawAll(player.getPosition(), valve, world);
	}

	private void drawAll(BlockPos playerPos, AbstractTankValve valve, World world) {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.enableCull();
		GlStateManager.doPolygonOffset(-3.0F, -3.0F);
		GlStateManager.enablePolygonOffset();

		BlockPos valvePos = valve.getPos();

		List<BlockPos> tankBlocks = FancyFluidStorage.tankManager.getFrameBlocksForValve(valve);
		tankBlocks.add(valvePos);

		Tessellator tess = Tessellator.getInstance();
		VertexBuffer vb = tess.getBuffer();
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		vb.setTranslation(-playerX, -playerY, -playerZ);

		for(BlockPos pos : tankBlocks) {
			if(playerPos.distanceSq(pos) > 20)
				continue;

			IBlockState state = world.getBlockState(pos);
			for(BlockRenderLayer layer : renderLayers) {
				if(state.getBlock().canRenderInLayer(state, layer)) {
					ForgeHooksClient.setRenderLayer(layer);
					mc.getBlockRendererDispatcher().renderBlockDamage(state, pos, overlayTexture, world);
				}
			}
		}

		tess.draw();
		vb.setTranslation(0, 0, 0);

		GlStateManager.doPolygonOffset(0.0F, 0.0F);
		GlStateManager.disablePolygonOffset();
		GlStateManager.disableCull();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

}
