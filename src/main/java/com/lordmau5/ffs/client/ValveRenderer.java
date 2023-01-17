package com.lordmau5.ffs.client;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.config.ServerConfig;
import com.lordmau5.ffs.tile.valves.TileEntityFluidValve;
import com.lordmau5.ffs.util.ClientRenderHelper;
import com.lordmau5.ffs.util.LayerBlockPos;
import com.lordmau5.ffs.util.TankManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ValveRenderer extends TileEntityRenderer<TileEntityFluidValve> {

    public ValveRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityFluidValve te) {
        return true;
    }

    @Override
    public void render(@Nonnull TileEntityFluidValve valve, float partialTicks, MatrixStack ms, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if ( !valve.isValid() || !valve.isMain() ) {
            return;
        }

        if ( valve.getTankConfig().getFluidCapacity() == 0 || valve.getTankConfig().getFluidAmount() == 0 ) {
            return;
        }

        BlockPos valvePos = valve.getBlockPos();

        float fillPercentage = (float) valve.getTankConfig().getFluidAmount() / (float) valve.getTankConfig().getFluidCapacity();

        if ( fillPercentage > 0 && !valve.getTankConfig().getFluidStack().isEmpty() ) {
            FluidStack fluid = valve.getTankConfig().getFluidStack();

            TreeMap<Integer, List<LayerBlockPos>> airBlocks = TankManager.INSTANCE.getAirBlocksForValve(valve);
            if ( airBlocks == null || airBlocks.isEmpty() ) {
                return;
            }

            TextureAtlasSprite still = Minecraft.getInstance().getTextureAtlas(PlayerContainer.BLOCK_ATLAS).apply(fluid.getFluid().getAttributes().getStillTexture(fluid));
            TextureAtlasSprite flowing = Minecraft.getInstance().getTextureAtlas(PlayerContainer.BLOCK_ATLAS).apply(fluid.getFluid().getAttributes().getFlowingTexture(fluid));

            ms.pushPose();

            Matrix4f matrix = ms.last().pose();

            if ( fluid.getFluid().getAttributes().isGaseous() ) {
                renderGasTank(still, flowing, airBlocks, valve, valvePos, bufferIn, matrix, fluid, fillPercentage);
            } else {
                renderFluidTank(still, flowing, airBlocks, valve, valvePos, bufferIn, matrix, fluid);
            }

            ms.popPose();
        }
    }

    private void renderGasTank(TextureAtlasSprite still, TextureAtlasSprite flowing, TreeMap<Integer, List<LayerBlockPos>> airBlocks, TileEntityFluidValve valve, BlockPos valvePos, IRenderTypeBuffer vb, Matrix4f matrix, FluidStack fluid, float fillPercentage) {
        int color = ClientRenderHelper.changeAlpha(fluid.getFluid().getAttributes().getColor(), (int) (fillPercentage * 255));

        List<Integer> layers = new ArrayList<>(airBlocks.keySet());
        int topLayer = layers.get(layers.size() - 1) - 1;
        for (Integer layer : airBlocks.keySet()) {
            for (LayerBlockPos pos : airBlocks.get(layer)) {
                BlockPos fromPos = pos.subtract(valvePos);

                renderFluidBlock(valve.getLevel(), still, flowing, vb, matrix, fluid, pos, fromPos, color, fromPos.getX() + 1f, fromPos.getY() + 1f, fromPos.getZ() + 1f, layer == topLayer);
            }
        }
    }

    private void renderFluidTank(TextureAtlasSprite still, TextureAtlasSprite flowing, TreeMap<Integer, List<LayerBlockPos>> airBlocks, TileEntityFluidValve valve, BlockPos valvePos, IRenderTypeBuffer vb, Matrix4f matrix, FluidStack fluid) {
        List<Integer> layers = new ArrayList<>(airBlocks.keySet());
        List<Float> fillLevels = new ArrayList<>();
        double fluidLeft = valve.getTankConfig().getFluidAmount();
        for (Integer layer : layers) {
            if ( fluidLeft <= 0 ) {
                continue;
            }
            int layerBlockSize = airBlocks.get(layer).size();
            if ( layerBlockSize == 0 ) {
                continue;
            }

            double layerCapacity = (double) ServerConfig.general.mbPerTankBlock * (double) layerBlockSize;
            fillLevels.add((float) Math.min(1, fluidLeft / layerCapacity));
            fluidLeft -= layerCapacity;
        }

        for (int i = 0; i < fillLevels.size(); i++) {
            int layer = layers.get(i) + 1;
            float currentLayerHeight = fillLevels.get(i);

            for (LayerBlockPos pos : airBlocks.get(layer)) {
                BlockPos fromPos = pos.subtract(valvePos);

                renderFluidBlock(valve.getLevel(), still, flowing, vb, matrix, fluid, pos, fromPos, fluid.getFluid().getAttributes().getColor(fluid), fromPos.getX() + 1f, fromPos.getY() + currentLayerHeight, fromPos.getZ() + 1f, i == fillLevels.size() - 1);
            }
        }
    }

    private void renderFluidBlock(World world, TextureAtlasSprite still, TextureAtlasSprite flowing, IRenderTypeBuffer vb, Matrix4f matrix, FluidStack fluid, BlockPos pos, BlockPos from, int color, float x2, float y2, float z2, boolean isTop) {
        int brightness = WorldRenderer.getLightColor(world, pos);

        float x1 = from.getX(), y1 = from.getY(), z1 = from.getZ();

        BlockPos currentOffset;
        for (Direction facing : Direction.values()) {
            currentOffset = pos.relative(facing);
            if ( facing == Direction.UP && isTop || !world.getBlockState(currentOffset).isSolidRender(world, currentOffset) && !world.isEmptyBlock(currentOffset) ) {
                ClientRenderHelper.putTexturedQuad(vb, (facing != Direction.DOWN && facing != Direction.UP) ? flowing : still, matrix, x1, y1, z1, x2 - x1, y2 - y1, z2 - z1, facing, color, brightness, facing != Direction.DOWN && facing != Direction.UP);
            }
        }
    }
}