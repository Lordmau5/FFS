package com.lordmau5.ffs.client;

import com.lordmau5.ffs.config.ServerConfig;
import com.lordmau5.ffs.tile.valves.TileEntityFluidValve;
import com.lordmau5.ffs.util.ClientRenderHelper;
import com.lordmau5.ffs.util.GenericUtil;
import com.lordmau5.ffs.util.LayerBlockPos;
import com.lordmau5.ffs.util.TankManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

public class ValveRenderer extends TileEntityRenderer<TileEntityFluidValve> {

    private static class RenderBlock {
        final BlockPos pos;
        final int layer;
        final float height;
        final boolean isTopLayer;
        final HashSet<Direction> validFaces;

        private RenderBlock(BlockPos pos, int layer, float height, boolean isTopLayer) {
            this.pos = pos;
            this.layer = layer;
            this.height = height;
            this.isTopLayer = isTopLayer;

            this.validFaces = new HashSet<>();
        }

        private void addFace(Direction direction) {
            this.validFaces.add(direction);
        }

        @Override
        public int hashCode() {
            return pos.hashCode();
        }
    }

    private static class ValveCache {
        final TileEntityFluidValve valve;
        final HashSet<RenderBlock> validRenderBlocks;

        int cachedAmount = 0;
        int updateDelta = 0;

        private ValveCache(TileEntityFluidValve valve) {
            this.valve = valve;
            this.validRenderBlocks = new HashSet<>();

            this.cachedAmount = valve.getTankConfig().getFluidAmount();
        }

        private void updateCachedAmount() {
            int amount = valve.getTankConfig().getFluidAmount();

            updateDelta += Math.abs(amount - cachedAmount);

            cachedAmount = amount;

            if (updateDelta >= 1000) {
                this.validRenderBlocks.clear();

                updateDelta = 0;
            }
        }

        @Override
        public int hashCode() {
            return valve.hashCode();
        }
    }

    private HashMap<TileEntityFluidValve, ValveCache> cache;
    private Vector3d cameraPos;

    public ValveRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);

        this.cache = new HashMap<>();
    }

    @Override
    public boolean shouldRenderOffScreen(TileEntityFluidValve te) {
        return true;
    }

    @Override
    public void render(@Nonnull TileEntityFluidValve valve, float partialTicks, MatrixStack ms, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if (!valve.isValid()) {
            cache.remove(valve);
            return;
        }

        if (!valve.isMain()) {
            return;
        }

        if (valve.getTankConfig().getFluidCapacity() == 0 || valve.getTankConfig().getFluidAmount() == 0) {
            cache.remove(valve);
            return;
        }

        cameraPos = Minecraft.getInstance().getCameraEntity().getEyePosition(partialTicks);

        BlockPos valvePos = valve.getBlockPos();

        float fillPercentage = (float) valve.getTankConfig().getFluidAmount() / (float) valve.getTankConfig().getFluidCapacity();

        if (fillPercentage > 0 && !valve.getTankConfig().getFluidStack().isEmpty()) {

            FluidStack fluid = valve.getTankConfig().getFluidStack();

            TreeMap<Integer, HashSet<LayerBlockPos>> airBlocks = TankManager.INSTANCE.getAirBlocksForValve(valve);
            if (airBlocks == null || airBlocks.isEmpty()) {
                return;
            }

            FluidAttributes attributes = fluid.getFluid().getAttributes();

            TextureAtlasSprite still = Minecraft.getInstance().getTextureAtlas(PlayerContainer.BLOCK_ATLAS).apply(attributes.getStillTexture(fluid));
            TextureAtlasSprite flowing = Minecraft.getInstance().getTextureAtlas(PlayerContainer.BLOCK_ATLAS).apply(attributes.getFlowingTexture(fluid));

            ms.pushPose();

            boolean isGaseous = attributes.isGaseous();

            if (isGaseous) {
                ValveCache cache = ensureRenderBlocksGas(valve, airBlocks);
                renderGasTank(still, airBlocks, valve.getLevel(), cache, valvePos, ms, bufferIn, fluid, fillPercentage);
            } else {
                ValveCache cache = ensureRenderBlocksFluid(valve, airBlocks);
                renderFluidTank(still, flowing, airBlocks, valve.getLevel(), cache, valvePos, ms, bufferIn, fluid);
            }

            ms.popPose();
        }
    }

    private ValveCache getCache(TileEntityFluidValve valve) {
        if (this.cache.containsKey(valve)) return this.cache.get(valve);

        ValveCache cache = new ValveCache(valve);
        this.cache.put(valve, cache);

        return cache;
    }

    private boolean isValidFace(Direction facing, World level, BlockPos pos, boolean isTopLayer) {
        if (facing == Direction.UP && isTopLayer) {
            return true;
        }

        BlockState state = getBlockState(level, pos);
        return !state.isSolidRender(level, pos) && !GenericUtil.isAirOrWaterLoggable(level, pos, state);
    }

    private ValveCache ensureRenderBlocksGas(TileEntityFluidValve valve, TreeMap<Integer, HashSet<LayerBlockPos>> airBlocks) {
        ValveCache cache = getCache(valve);
        World level = valve.getLevel();

        if (cache.validRenderBlocks.size() > 0) return cache;

        int topLayer = airBlocks.keySet().size() - 1;
        for (Integer layer : airBlocks.keySet()) {
            for (BlockPos pos : airBlocks.get(layer)) {
                RenderBlock rb = new RenderBlock(pos, layer, 1.0f, layer == topLayer);

                for (Direction facing : Direction.values()) {
                    BlockPos currentOffset = pos.relative(facing);

                    if (isValidFace(facing, level, currentOffset, rb.isTopLayer))
                        rb.addFace(facing);
                }

                if (!rb.validFaces.isEmpty())
                    cache.validRenderBlocks.add(rb);
            }
        }

        return cache;
    }

    private ValveCache ensureRenderBlocksFluid(TileEntityFluidValve valve, TreeMap<Integer, HashSet<LayerBlockPos>> airBlocks) {
        ValveCache cache = getCache(valve);
        World level = valve.getLevel();

        cache.updateCachedAmount();

        if (cache.validRenderBlocks.size() > 0) return cache;

        float fluidLeft = valve.getTankConfig().getFluidAmount();
        for (Integer layer : airBlocks.keySet()) {
            if (fluidLeft <= 0) {
                continue;
            }

            int layerBlockSize = airBlocks.get(layer).size();
            if (layerBlockSize == 0) {
                continue;
            }

            float layerCapacity = ServerConfig.general.mbPerTankBlock * layerBlockSize;
            float height = Math.min(1, fluidLeft / layerCapacity);

            fluidLeft -= layerCapacity;

            for (BlockPos pos : airBlocks.get(layer)) {
                RenderBlock rb = new RenderBlock(pos, layer, height, fluidLeft <= 0);

                for (Direction facing : Direction.values()) {
                    BlockPos currentOffset = pos.relative(facing);

                    if (isValidFace(facing, level, currentOffset, rb.isTopLayer))
                        rb.addFace(facing);
                }

                if (!rb.validFaces.isEmpty())
                    cache.validRenderBlocks.add(rb);
            }
        }

        return cache;
    }

    private void renderGasTank(TextureAtlasSprite still, TreeMap<Integer, HashSet<LayerBlockPos>> airBlocks, World level, ValveCache cache, BlockPos valvePos, MatrixStack ps, IRenderTypeBuffer vb, FluidStack fluid, float fillPercentage) {
        FluidAttributes attributes = fluid.getFluid().getAttributes();

        int color = ClientRenderHelper.changeAlpha(attributes.getColor(fluid), (int) (fillPercentage * 255));

        BlockPos playerPos = Minecraft.getInstance().player.blockPosition();

        int topLayer = airBlocks.keySet().size() - 1;
        for (RenderBlock rb : cache.validRenderBlocks) {
            if (!playerPos.closerThan(rb.pos, 150)) continue;

            BlockPos offset = rb.pos.subtract(valvePos);

            renderGasBlock(level, still, ps, vb, rb, offset, color, rb.layer == topLayer);
        }
    }

    private BlockState getBlockState(World level, BlockPos pos) {
        return level.getBlockState(pos);
    }

    private void renderFluidTank(TextureAtlasSprite still, TextureAtlasSprite flowing, TreeMap<Integer, HashSet<LayerBlockPos>> airBlocks, World level, ValveCache cache, BlockPos valvePos, MatrixStack ps, IRenderTypeBuffer vb, FluidStack fluid) {
        FluidAttributes attributes = fluid.getFluid().getAttributes();

        int color = attributes.getColor(fluid);

        BlockPos playerPos = Minecraft.getInstance().player.blockPosition();

        int topLayer = airBlocks.keySet().size() - 1;
        for (RenderBlock rb : cache.validRenderBlocks) {
            if (!playerPos.closerThan(rb.pos, 150)) continue;

            BlockPos offset = rb.pos.subtract(valvePos);

            renderFluidBlock(level, still, flowing, ps, vb, rb, offset, color, rb.layer == topLayer);
        }
    }

    private int getLightColor(World level, BlockPos pos) {
        return WorldRenderer.getLightColor(level, getBlockState(level, pos), pos);
    }

    private int renderGasBlock(World level, TextureAtlasSprite still, MatrixStack ps, IRenderTypeBuffer vb, RenderBlock rb, BlockPos offset, int color, boolean isTop) {
        BlockPos pos = rb.pos;
        int brightness = getLightColor(level, pos);

        Vector3d difference = cameraPos.subtract(pos.getX(), pos.getY(), pos.getZ());

        int renderedFaces = 0;

        for (Direction facing : rb.validFaces) {
            switch (facing) {
                case UP: {
                    if (difference.y() < 1) continue;
                }
                case DOWN: {
                    if (difference.y() > 0) continue;
                }
                case SOUTH: {
                    if (difference.z() < 1) continue;
                }
                case NORTH: {
                    if (difference.z() > 0) continue;
                }
                case EAST: {
                    if (difference.x() < 1) continue;
                }
                case WEST: {
                    if (difference.x() > 0) continue;
                }
            }

            renderedFaces++;

            if (facing == Direction.UP && isTop) {
                ClientRenderHelper.putTexturedQuad(ps, vb, still, offset, rb.height, facing, color, brightness, false);
                continue;
            }

            ClientRenderHelper.putTexturedQuad(ps, vb, still, offset, rb.height, facing, color, brightness, false);
        }

        return renderedFaces;
    }

    private int renderFluidBlock(World level, TextureAtlasSprite still, TextureAtlasSprite flowing, MatrixStack ps, IRenderTypeBuffer vb, RenderBlock rb, BlockPos offset, int color, boolean isTop) {
        BlockPos pos = rb.pos;
        int brightness = getLightColor(level, pos);

        Vector3d difference = cameraPos.subtract(pos.getX(), (pos.getY() - 1), pos.getZ()).subtract(0.0f, rb.height, 0.0f);

        int renderedFaces = 0;

        for (Direction facing : rb.validFaces) {
            boolean isFlowing = true;

            switch (facing) {
                case UP: {
                    isFlowing = false;

                    if (difference.y() < 1) continue;

                    break;
                }
                case DOWN: {
                    isFlowing = false;

                    if (difference.y() > 0) continue;

                    break;
                }
                case SOUTH: {
                    if (difference.z() < 1) continue;

                    break;
                }
                case NORTH: {
                    if (difference.z() > 0) continue;

                    break;
                }
                case EAST: {
                    if (difference.x() < 1) continue;

                    break;
                }
                case WEST: {
                    if (difference.x() > 0) continue;

                    break;
                }
            }

            renderedFaces++;

            if (facing == Direction.UP && isTop) {
                ClientRenderHelper.putTexturedQuad(ps, vb, still, offset, rb.height, facing, color, brightness, false);
                continue;
            }

            ClientRenderHelper.putTexturedQuad(ps, vb, isFlowing ? flowing : still, offset, rb.height, facing, color, brightness, isFlowing);
        }

        return renderedFaces;
    }
}