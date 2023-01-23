package com.lordmau5.ffs.client.gui;

import com.lordmau5.ffs.FancyFluidStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;

public class GuiButtonLockFluid extends ImageButton {
    private static final ResourceLocation toggleTexture = new ResourceLocation(FancyFluidStorage.MOD_ID, "textures/gui/gui_tank_no_valve.png");
    private boolean state;

    GuiButtonLockFluid(int x, int y, boolean state, OnPress onPress) {
        super(x, y, 8, 8, 0, 128, 0, toggleTexture, onPress);
        this.state = state;
    }

    public boolean getState() {
        return this.state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public void toggleState() {
        this.state = !this.state;
    }

    @Override
    public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShaderTexture(0, toggleTexture);

        RenderSystem.disableDepthTest();

        int texStart = getState() ? 0 : 8;

        blit(matrixStack, this.x, this.y, (float) texStart, (float) 128, this.width, this.height, 256, 256);

        RenderSystem.enableDepthTest();
    }
}
