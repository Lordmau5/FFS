package com.lordmau5.ffs.client;

import com.lordmau5.ffs.config.ServerConfig;
import com.lordmau5.ffs.tile.valves.TileEntityFluidValve;
import com.lordmau5.ffs.util.ClientRenderHelper;
import com.lordmau5.ffs.util.LayerBlockPos;
import com.lordmau5.ffs.util.TankManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

public class ValveRenderer implements BlockEntityRenderer<TileEntityFluidValve> {

    BlockEntityRendererProvider.Context context;

    public ValveRenderer(BlockEntityRendererProvider.Context context) {
        this.context = context;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityFluidValve te) {
        return true;
    }

    @Override
    public void render(@Nonnull TileEntityFluidValve valve, float partialTicks, PoseStack ms, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (!valve.isValid() || !valve.isMain()) {
            return;
        }

        if (valve.getTankConfig().getFluidCapacity() == 0 || valve.getTankConfig().getFluidAmount() == 0) {
            return;
        }

        BlockPos valvePos = valve.getBlockPos();

        float fillPercentage = (float) valve.getTankConfig().getFluidAmount() / (float) valve.getTankConfig().getFluidCapacity();

        if (fillPercentage > 0 && !valve.getTankConfig().getFluidStack().isEmpty()) {
            FluidStack fluid = valve.getTankConfig().getFluidStack();

            TreeMap<Integer, HashSet<LayerBlockPos>> airBlocks = TankManager.INSTANCE.getAirBlocksForValve(valve);
            if (airBlocks == null || airBlocks.isEmpty()) {
                return;
            }

            IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());

            TextureAtlasSprite still = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extensions.getStillTexture(fluid));
            TextureAtlasSprite flowing = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(extensions.getFlowingTexture(fluid));

            ms.pushPose();

            Matrix4f matrix = ms.last().pose();

//            if ( extensions.isGaseous() ) {
//                renderGasTank(still, flowing, airBlocks, valve, valvePos, bufferIn, matrix, fluid, fillPercentage);
//            } else {
            renderFluidTank(still, flowing, airBlocks, valve, valvePos, bufferIn, matrix, fluid);
//            }

            ms.popPose();
        }
    }

    private void renderGasTank(TextureAtlasSprite still, TextureAtlasSprite flowing, TreeMap<Integer, HashSet<LayerBlockPos>> airBlocks, TileEntityFluidValve valve, BlockPos valvePos, MultiBufferSource vb, Matrix4f matrix, FluidStack fluid, float fillPercentage) {
//        int color = ClientRenderHelper.changeAlpha(fluid.getFluid().getAttributes().getColor(), (int) (fillPercentage * 255));
        int color = 0;

        List<Integer> layers = new ArrayList<>(airBlocks.keySet());
        int topLayer = layers.get(layers.size() - 1) - 1;
        for (Integer layer : airBlocks.keySet()) {
            for (LayerBlockPos pos : airBlocks.get(layer)) {
                BlockPos fromPos = pos.subtract(valvePos);

                renderFluidBlock(valve.getLevel(), still, flowing, vb, matrix, fluid, pos, fromPos, color, fromPos.getX() + 1f, fromPos.getY() + 1f, fromPos.getZ() + 1f, layer == topLayer);
            }
        }
    }

    private void renderFluidTank(TextureAtlasSprite still, TextureAtlasSprite flowing, TreeMap<Integer, HashSet<LayerBlockPos>> airBlocks, TileEntityFluidValve valve, BlockPos valvePos, MultiBufferSource vb, Matrix4f matrix, FluidStack fluid) {
        List<Integer> layers = new ArrayList<>(airBlocks.keySet());
        List<Float> fillLevels = new ArrayList<>();
        double fluidLeft = valve.getTankConfig().getFluidAmount();
        for (Integer layer : layers) {
            if (fluidLeft <= 0) {
                continue;
            }
            int layerBlockSize = airBlocks.get(layer).size();
            if (layerBlockSize == 0) {
                continue;
            }

            double layerCapacity = (double) ServerConfig.general.mbPerTankBlock * (double) layerBlockSize;
            fillLevels.add((float) Math.min(1, fluidLeft / layerCapacity));
            fluidLeft -= layerCapacity;
        }

        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());

        for (int i = 0; i < fillLevels.size(); i++) {
            int layer = layers.get(i) + 1;
            float currentLayerHeight = fillLevels.get(i);

            for (LayerBlockPos pos : airBlocks.get(layer)) {
                BlockPos fromPos = pos.subtract(valvePos);


                renderFluidBlock(valve.getLevel(), still, flowing, vb, matrix, fluid, pos, fromPos, extensions.getTintColor(fluid), fromPos.getX() + 1f, fromPos.getY() + currentLayerHeight, fromPos.getZ() + 1f, i == fillLevels.size() - 1);
            }
        }
    }

    private void renderFluidBlock(Level world, TextureAtlasSprite still, TextureAtlasSprite flowing, MultiBufferSource vb, Matrix4f matrix, FluidStack fluid, BlockPos pos, BlockPos from, int color, float x2, float y2, float z2, boolean isTop) {
        int brightness = LevelRenderer.getLightColor(world, pos);

        float x1 = from.getX(), y1 = from.getY(), z1 = from.getZ();

        BlockPos currentOffset;
        for (Direction facing : Direction.values()) {
            currentOffset = pos.relative(facing);
            if (facing == Direction.UP && isTop || !world.getBlockState(currentOffset).isSolidRender(world, currentOffset) && !world.isEmptyBlock(currentOffset)) {
                ClientRenderHelper.putTexturedQuad(vb, (facing != Direction.DOWN && facing != Direction.UP) ? flowing : still, matrix, x1, y1, z1, x2 - x1, y2 - y1, z2 - z1, facing, color, brightness, facing != Direction.DOWN && facing != Direction.UP);
            }
        }
    }
}