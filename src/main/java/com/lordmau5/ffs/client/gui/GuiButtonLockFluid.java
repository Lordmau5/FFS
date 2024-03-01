package com.lordmau5.ffs.client.gui;

import com.lordmau5.ffs.FancyFluidStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.resources.ResourceLocation;

public class GuiButtonLockFluid extends ImageButton {
    private static final ResourceLocation toggleTexture = new ResourceLocation(FancyFluidStorage.MOD_ID, "textures/gui/gui_tank_no_valve.png");
    private boolean state;

    GuiButtonLockFluid(int x, int y, boolean state, OnPress onPress) {
//        super(x, y, 8, 8, 0, 128, 0, null, onPress);
        //TODO
        super(x, y, 8, 8, null, onPress);
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
    public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick)
    {
        RenderSystem.disableDepthTest();

        int texStart = getState() ? 0 : 8;

        guiGraphics.blit(toggleTexture, this.getX(), this.getY(), texStart, 128, this.width, this.height, 256, 256);

        RenderSystem.enableDepthTest();
    }
}
