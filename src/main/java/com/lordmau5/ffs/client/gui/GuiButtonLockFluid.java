package com.lordmau5.ffs.client.gui;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Created by Max on 7/6/2015.
 */
class GuiButtonLockFluid extends GuiButton
{
    private static final ResourceLocation toggleTexture = new ResourceLocation(FancyFluidStorage.MODID + ":textures/gui/gui_tank_no_valve.png");
    private boolean state = false;

    GuiButtonLockFluid(int x, int y, boolean state)
    {
        super(0, x, y, 8, 8, "");
        this.state = state;
    }

    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            mc.getTextureManager().bindTexture(toggleTexture);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect(this.x - 1, this.y, 48, 128, 8, 8);
            this.drawTexturedModalRect(this.x, this.y - 1, getState() ? 1 : 9, 128, this.width, this.height);
            this.mouseDragged(mc, mouseX, mouseY);
        }
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY)
    {
        boolean onMe = super.mousePressed(mc, mouseX, mouseY);
        if (onMe)
        {
            this.state = !this.state;
        }
        return onMe;
    }

    public boolean getState()
    {
        return this.state;
    }

    public void setState(boolean state)
    {
        this.state = state;
    }

}
