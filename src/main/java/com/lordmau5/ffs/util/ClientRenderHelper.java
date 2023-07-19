package com.lordmau5.ffs.util;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClientRenderHelper {

    public static void setBlockTextureSheet() {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
    }

    public static void setGLColorFromInt(int color) {
        float alpha = (float) (color >> 24 & 0xFF) / 255.0F;
        float red = (float) (color >> 16 & 0xFF) / 255.0F;
        float green = (float) (color >> 8 & 0xFF) / 255.0F;
        float blue = (float) (color & 0xFF) / 255.0F;

        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    public static TextureAtlasSprite getTexture(ResourceLocation location) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(location);
    }

    private static BakedQuad createQuad(List<Vec3> vectors, float[] cols, TextureAtlasSprite sprite, Direction face, float u1, float u2, float v1, float v2) {
        QuadBakingVertexConsumer.Buffered quadBaker = new QuadBakingVertexConsumer.Buffered();
        Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());

        putVertex(quadBaker, normal, vectors.get(0).x, vectors.get(0).y, vectors.get(0).z, u1, v1, sprite, cols, face);
        putVertex(quadBaker, normal, vectors.get(1).x, vectors.get(1).y, vectors.get(1).z, u1, v2, sprite, cols, face);
        putVertex(quadBaker, normal, vectors.get(2).x, vectors.get(2).y, vectors.get(2).z, u2, v2, sprite, cols, face);
        putVertex(quadBaker, normal, vectors.get(3).x, vectors.get(3).y, vectors.get(3).z, u2, v1, sprite, cols, face);

        return quadBaker.getQuad();
    }

    private static void putVertex(QuadBakingVertexConsumer quadBaker, Vec3 normal,
                                  double x, double y, double z, float u, float v, TextureAtlasSprite sprite, float[] cols, Direction face) {
        quadBaker.vertex(x, y, z);
        quadBaker.normal((float) normal.x, (float) normal.y, (float) normal.z);
        quadBaker.color(cols[0], cols[1], cols[2], cols[3]);
        quadBaker.uv(sprite.getU(u), sprite.getV(v));
        quadBaker.setSprite(sprite);
        quadBaker.setDirection(face);
        quadBaker.endVertex();
    }

    public static void putTexturedQuad(PoseStack ps, MultiBufferSource renderer, TextureAtlasSprite sprite, BlockPos offset, float height, Direction direction, int color, int brightness, boolean flowing) {
        float a = (color >> 24 & 0xFF) / 255.0f;
        float r = (color >> 16 & 0xFF) / 255.0f;
        float g = (color >> 8 & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        putTexturedQuad(ps, renderer, sprite, offset, height, direction, r, g, b, a, brightness, flowing);
    }

    private static void putTexturedQuad(PoseStack ps, MultiBufferSource renderer, TextureAtlasSprite sprite, BlockPos offset, float height, Direction direction, float r, float g, float b, float a, int brightness, boolean flowing) {
        if (sprite == null) {
            return;
        }

        float size = flowing ? 8.0f : 16.0f;

        float minU = sprite.getU0();
        float minV = flowing ? (size * (1.0f - height)) : sprite.getV0();

        float x = 0.0f;
        float y = 0.0f;
        float z = 0.0f;

        float x2 = 1.0f;
        float z2 = 1.0f;

        VertexConsumer consumer = renderer.getBuffer(FFSRenderTypes.fluidRenderType);

        ps.pushPose();

        ps.translate(offset.getX(), offset.getY(), offset.getZ());

        float[] cols = new float[]{r, g, b, a};

        switch (direction) {
            case DOWN -> {
                BakedQuad quad = createQuad(ImmutableList.of(new Vec3(x, y, z2), new Vec3(x, y, z), new Vec3(x2, y, z), new Vec3(x2, y, z2)), cols, sprite, Direction.DOWN, minU, size, minV, size);

                consumer.putBulkData(ps.last(), quad, r, g, b, a, brightness, 0, false);
            }
            case UP -> {
                BakedQuad quad = createQuad(ImmutableList.of(new Vec3(x, height, z), new Vec3(x, height, z2), new Vec3(x2, height, z2), new Vec3(x2, height, z)), cols, sprite, Direction.UP, minU, size, minV, size);

                consumer.putBulkData(ps.last(), quad, r, g, b, a, brightness, 0, false);
            }
            case NORTH -> {
                BakedQuad quad = createQuad(ImmutableList.of(new Vec3(x2, height, z), new Vec3(x2, y, z), new Vec3(x, y, z), new Vec3(x, height, z)), cols, sprite, Direction.NORTH, minU, size, minV, size);

                consumer.putBulkData(ps.last(), quad, r, g, b, a, brightness, 0, false);
            }
            case SOUTH -> {
                BakedQuad quad = createQuad(ImmutableList.of(new Vec3(x, height, z2), new Vec3(x, y, z2), new Vec3(x2, y, z2), new Vec3(x2, height, z2)), cols, sprite, Direction.SOUTH, minU, size, minV, size);

                consumer.putBulkData(ps.last(), quad, r, g, b, a, brightness, 0, false);
            }
            case WEST -> {
                BakedQuad quad = createQuad(ImmutableList.of(new Vec3(x, height, z), new Vec3(x, y, z), new Vec3(x, y, z2), new Vec3(x, height, z2)), cols, sprite, Direction.WEST, minU, size, minV, size);

                consumer.putBulkData(ps.last(), quad, r, g, b, a, brightness, 0, false);
            }
            case EAST -> {
                BakedQuad quad = createQuad(ImmutableList.of(new Vec3(x2, height, z2), new Vec3(x2, y, z2), new Vec3(x2, y, z), new Vec3(x2, height, z)), cols, sprite, Direction.EAST, minU, size, minV, size);

                consumer.putBulkData(ps.last(), quad, r, g, b, a, brightness, 0, false);
            }
        }

        ps.popPose();
    }

    public static int changeAlpha(int origColor, int userInputAlpha) {
        origColor = origColor & 0x00ffffff; //drop the previous alpha value
        return (userInputAlpha << 24) | origColor; //add the one the user provided
    }

}