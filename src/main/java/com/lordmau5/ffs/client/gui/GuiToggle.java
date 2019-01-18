package com.lordmau5.ffs.client.gui;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Created by Max on 7/6/2015.
 */
public class GuiToggle extends GuiButton {
    private static final ResourceLocation toggleTexture = new ResourceLocation(FancyFluidStorage.MODID + ":textures/gui/gui_tank_no_valve.png");
    private boolean state = false;
    private int textColor = 16777215;

    GuiToggle(int x, int y, String title, boolean state, int textColor) {
        super(0, x, y, 16, 8, title);
        this.state = state;
        this.textColor = textColor;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if ( this.visible ) {
            FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(toggleTexture);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect(this.x, this.y, getState() ? 32 : 16, 128, this.width, this.height);
            this.mouseDragged(mc, mouseX, mouseY);

            fontrenderer.drawString(this.displayString, this.x, this.y + 10, this.textColor);
        }
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean onMe = super.mousePressed(mc, mouseX, mouseY);
        if ( onMe ) {
            this.state = !this.state;
        }
        return onMe;
    }

    public boolean getState() {
        return this.state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

}
