package com.lordmau5.ffs.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientRenderHelper {
    public static final ResourceLocation MC_BLOCK_SHEET = new ResourceLocation("textures/atlas/blocks.png");

    public static void setBlockTextureSheet() {
        Minecraft.getInstance().getTextureManager().bind(MC_BLOCK_SHEET);
    }

    public static void setGLColorFromInt(int color) {
        float alpha = (float) (color >> 24 & 0xFF) / 255.0F;
        float red = (float) (color >> 16 & 0xFF) / 255.0F;
        float green = (float) (color >> 8 & 0xFF) / 255.0F;
        float blue = (float) (color & 0xFF) / 255.0F;

        GlStateManager._color4f(red, green, blue, alpha);
    }

    public static TextureAtlasSprite getTexture(ResourceLocation location) {
        return Minecraft.getInstance().getTextureAtlas(PlayerContainer.BLOCK_ATLAS).apply(location);
    }

    public static void putTexturedQuad(IRenderTypeBuffer renderer, TextureAtlasSprite sprite, Matrix4f matrix, float x, float y, float z, float w, float h, float d, Direction direction, int color, int brightness, boolean flowing) {
        int l1 = brightness >> 0x10 & 0xFFFF;
        int l2 = brightness & 0xFFFF;
        int a = color >> 24 & 0xFF;
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        putTexturedQuad(renderer, sprite, matrix, x, y, z, w, h, d, direction, r, g, b, a, l1, l2, flowing);
    }

    private static void putTexturedQuad(IRenderTypeBuffer renderTypeBuffer, TextureAtlasSprite sprite, Matrix4f matrix, float x, float y, float z, float w, float h, float d, Direction direction, int r, int g, int b, int a, int light1, int light2, boolean flowing) {
        if ( sprite == null ) {
            return;
        }
        float minU;
        float maxU;
        float minV;
        float maxV;
        double size = 16f;
        if ( flowing ) {
            size = 8f;
        }
        float x2 = x + w;
        float y2 = y + h;
        float z2 = z + d;
        double xt1 = x % 1d;
        double xt2 = xt1 + w;
        while ( xt2 > 1f ) xt2 -= 1f;
        double yt1 = y % 1d;
        double yt2 = yt1 + h;
        while ( yt2 > 1f ) yt2 -= 1f;
        double zt1 = z % 1d;
        double zt2 = zt1 + d;
        while ( zt2 > 1f ) zt2 -= 1f;

        if ( flowing ) {
            double tmp = 1d - yt1;
            yt1 = 1d - yt2;
            yt2 = tmp;
        }

        switch (direction) {
            case DOWN:
            case UP:
                minU = sprite.getU(xt1 * size);
                maxU = sprite.getU(xt2 * size);
                minV = sprite.getV(zt1 * size);
                maxV = sprite.getV(zt2 * size);
                break;
            case NORTH:
            case SOUTH:
                minU = sprite.getU(xt2 * size);
                maxU = sprite.getU(xt1 * size);
                minV = sprite.getV(yt1 * size);
                maxV = sprite.getV(yt2 * size);
                break;
            case WEST:
            case EAST:
                minU = sprite.getU(zt2 * size);
                maxU = sprite.getU(zt1 * size);
                minV = sprite.getV(yt1 * size);
                maxV = sprite.getV(yt2 * size);
                break;
            default:
                minU = sprite.getU0();
                maxU = sprite.getU1();
                minV = sprite.getV0();
                maxV = sprite.getV1();
        }

        IVertexBuilder renderer = renderTypeBuffer.getBuffer(RenderType.text(sprite.atlas().location()));
        switch (direction) {
            case DOWN:
                renderer.vertex(matrix, x, y, z).color(r, g, b, a).uv(minU, minV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y, z).color(r, g, b, a).uv(maxU, minV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y, z2).color(r, g, b, a).uv(maxU, maxV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x, y, z2).color(r, g, b, a).uv(minU, maxV).uv2(light1, light2).endVertex();
                break;
            case UP:
                renderer.vertex(matrix, x, y2, z).color(r, g, b, a).uv(minU, minV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x, y2, z2).color(r, g, b, a).uv(minU, maxV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv(maxU, maxV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y2, z).color(r, g, b, a).uv(maxU, minV).uv2(light1, light2).endVertex();
                break;
            case NORTH:
                renderer.vertex(matrix, x, y, z).color(r, g, b, a).uv(minU, maxV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x, y2, z).color(r, g, b, a).uv(minU, minV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y2, z).color(r, g, b, a).uv(maxU, minV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y, z).color(r, g, b, a).uv(maxU, maxV).uv2(light1, light2).endVertex();
                break;
            case SOUTH:
                renderer.vertex(matrix, x, y, z2).color(r, g, b, a).uv(maxU, maxV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y, z2).color(r, g, b, a).uv(minU, maxV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv(minU, minV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x, y2, z2).color(r, g, b, a).uv(maxU, minV).uv2(light1, light2).endVertex();
                break;
            case WEST:
                renderer.vertex(matrix, x, y, z).color(r, g, b, a).uv(maxU, maxV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x, y, z2).color(r, g, b, a).uv(minU, maxV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x, y2, z2).color(r, g, b, a).uv(minU, minV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x, y2, z).color(r, g, b, a).uv(maxU, minV).uv2(light1, light2).endVertex();
                break;
            case EAST:
                renderer.vertex(matrix, x2, y, z).color(r, g, b, a).uv(minU, maxV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y2, z).color(r, g, b, a).uv(minU, minV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y2, z2).color(r, g, b, a).uv(maxU, minV).uv2(light1, light2).endVertex();
                renderer.vertex(matrix, x2, y, z2).color(r, g, b, a).uv(maxU, maxV).uv2(light1, light2).endVertex();
                break;
        }
    }

    public static int changeAlpha(int origColor, int userInputAlpha) {
        origColor = origColor & 0x00ffffff; //drop the previous alpha value
        return (userInputAlpha << 24) | origColor; //add the one the user inputted
    }

}