package com.lordmau5.ffs.client;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.config.Config;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import com.lordmau5.ffs.util.ClientRenderHelper;
import com.lordmau5.ffs.util.LayerBlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by Dustin on 29.06.2015.
 *
 * Shoutouts to Escapee for helping me with the new math behind the rendering! <3
 */
public class ValveRenderer extends FastTESR<AbstractTankValve> {

	private void preGL() {
		GlStateManager.pushMatrix();

		GlStateManager.disableCull();
	}

	private void postGL() {
		GlStateManager.enableCull();

		GlStateManager.popMatrix();
	}

	@Override
	public void renderTileEntityFast(AbstractTankValve valve, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vb) {
		if(valve == null || !valve.isValid() || !valve.isMaster()) {
			return;
		}

		if(valve.getTankConfig().getFluidCapacity() == 0 || valve.getTankConfig().getFluidAmount() == 0) {
			return;
		}

		BlockPos valvePos = valve.getPos();

		float fillPercentage = (float) valve.getTankConfig().getFluidAmount() / (float) valve.getTankConfig().getFluidCapacity();

		if(fillPercentage > 0 && valve.getTankConfig().getFluidStack() != null) {
			FluidStack fluid = valve.getTankConfig().getFluidStack();

			vb.setTranslation(x, y, z);

			TreeMap<Integer, List<LayerBlockPos>> airBlocks = FancyFluidStorage.tankManager.getAirBlocksForValve(valve);
			if(airBlocks == null || airBlocks.isEmpty()) {
				return;
			}

			TextureAtlasSprite still = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(fluid.getFluid().getStill(fluid).toString());
			TextureAtlasSprite flowing = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(fluid.getFluid().getFlowing(fluid).toString());

			preGL();
			if(fluid.getFluid().isGaseous()) {
				renderGasTank(still, flowing, airBlocks, valve, valvePos, vb, fluid, fillPercentage);
			}
			else {
				renderFluidTank(still, flowing, airBlocks, valve, valvePos, vb, fluid);
			}
			postGL();
		}
	}

	private void renderGasTank(TextureAtlasSprite still, TextureAtlasSprite flowing, TreeMap<Integer, List<LayerBlockPos>> airBlocks, AbstractTankValve valve, BlockPos valvePos, VertexBuffer vb, FluidStack fluid, float fillPercentage) {
		int color = ClientRenderHelper.changeAlpha(fluid.getFluid().getColor(), (int) (fillPercentage * 255));

		List<Integer> layers = new ArrayList<>(airBlocks.keySet());
		int topLayer = layers.get(layers.size() - 1) - 1;
		for(Integer layer : airBlocks.keySet()) {
			for(LayerBlockPos pos : airBlocks.get(layer)) {
				BlockPos fromPos = pos.subtract(valvePos);

				renderFluidBlock(still, flowing, vb, fluid, pos, fromPos, color, fromPos.getX() + 1d, fromPos.getY() + 1d, fromPos.getZ() + 1d, layer == topLayer);
			}
		}
	}

	private void renderFluidTank(TextureAtlasSprite still, TextureAtlasSprite flowing, TreeMap<Integer, List<LayerBlockPos>> airBlocks, AbstractTankValve valve, BlockPos valvePos, VertexBuffer vb, FluidStack fluid) {
		List<Integer> layers = new ArrayList<>(airBlocks.keySet());
		List<Double> fillLevels = new ArrayList<>();
		double fluidLeft = valve.getTankConfig().getFluidAmount();
		for(Integer layer : layers) {
			if(fluidLeft <= 0) {
				continue;
			}
			int layerBlockSize = airBlocks.get(layer).size();
			if(layerBlockSize == 0) {
				continue;
			}

			double layerCapacity = (double) Config.MB_PER_TANK_BLOCK * (double) layerBlockSize;
			fillLevels.add(Math.min(1, fluidLeft / layerCapacity));
			fluidLeft -= layerCapacity;
		}
		for(int i = 0; i < fillLevels.size(); i++) {
			int layer = layers.get(i) + 1;
			double currentLayerHeight = fillLevels.get(i);

			for(LayerBlockPos pos : airBlocks.get(layer)) {
				BlockPos fromPos = pos.subtract(valvePos);

				renderFluidBlock(still, flowing, vb, fluid, pos, fromPos, fluid.getFluid().getColor(fluid), fromPos.getX() + 1d, fromPos.getY() + currentLayerHeight, fromPos.getZ() + 1d, i == fillLevels.size() - 1);
			}
		}
	}

	private void renderFluidBlock(TextureAtlasSprite still, TextureAtlasSprite flowing, VertexBuffer vb, FluidStack fluid, BlockPos pos, BlockPos from, int color, double x2, double y2, double z2, boolean isTop) {
		int brightness = getWorld().getCombinedLight(pos, fluid.getFluid().getLuminosity());

		double x1 = from.getX(), y1 = from.getY(), z1 = from.getZ();

		BlockPos currentOffset;
		for(EnumFacing facing : EnumFacing.values()) {
			currentOffset = pos.offset(facing);
			if(facing == EnumFacing.UP && isTop || !getWorld().getBlockState(currentOffset).isOpaqueCube() && !getWorld().isAirBlock(currentOffset)) {
				ClientRenderHelper.putTexturedQuad(vb, (facing != EnumFacing.DOWN && facing != EnumFacing.UP) ? flowing : still, x1, y1, z1, x2 - x1, y2 - y1, z2 - z1, facing, color, brightness, facing != EnumFacing.DOWN && facing != EnumFacing.UP);
			}
		}
	}
}