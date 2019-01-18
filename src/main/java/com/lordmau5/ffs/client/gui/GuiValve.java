package com.lordmau5.ffs.client.gui;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.network.FFSPacket;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.tile.abstracts.AbstractTankTile;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import com.lordmau5.ffs.tile.interfaces.INameableTile;
import com.lordmau5.ffs.util.ClientRenderHelper;
import com.lordmau5.ffs.util.GenericUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dustin on 05.07.2015.
 */
//TODO GuiBuilder all of this
public class GuiValve extends GuiScreen {
    private static final ResourceLocation tex_valve = new ResourceLocation(FancyFluidStorage.MODID + ":textures/gui/gui_tank_valve.png");
    private static final ResourceLocation tex_no_valve = new ResourceLocation(FancyFluidStorage.MODID + ":textures/gui/gui_tank_no_valve.png");
    private GuiButtonLockFluid lockFluidButton;
    private boolean isNonValve = false;
    private AbstractTankTile tile;
    private final AbstractTankValve valve;
    private final AbstractTankValve masterValve;
    private GuiTextField tileName;
    private final int xSize_Valve = 196;
    private final int ySize_Valve = 128;
    private final int xSize_NoValve = 96;
    private final int ySize_NoValve = 128;
    private int left = 0, top = 0;
    private int mouseX, mouseY;

    public GuiValve(AbstractTankTile tile, boolean isNonValve) {
        super();

        this.isNonValve = isNonValve;

        if ( !isNonValve ) {
            this.tile = tile;
            if ( tile instanceof AbstractTankValve ) {
                this.valve = (AbstractTankValve) tile;
            } else {
                this.valve = tile.getMasterValve();
            }
            this.masterValve = tile.getMasterValve();
        } else {
            this.valve = this.masterValve = tile.getMasterValve();
        }
    }

    private void initGuiValve() {
        this.left = (this.width - this.xSize_Valve) / 2;
        this.top = (this.height - this.ySize_Valve) / 2;
        if ( this.tile instanceof INameableTile ) {
            this.tileName = new GuiTextField(0, this.fontRenderer, this.left + 90, this.top + 102, 82, 10);
            this.tileName.setText(this.valve.getTileName());
            this.tileName.setMaxStringLength(32);
        }
        this.buttonList.add(this.lockFluidButton = new GuiButtonLockFluid(this.left + 62, this.top + 26, this.masterValve.getTankConfig().isFluidLocked()));
    }

    @Override
    public void initGui() {
        super.initGui();

        if ( isNonValve ) {
            this.left = (this.width - this.xSize_NoValve) / 2;
            this.top = (this.height - this.ySize_NoValve) / 2;
            this.buttonList.add(this.lockFluidButton = new GuiButtonLockFluid(this.left + 66, this.top + 26, this.masterValve.getTankConfig().isFluidLocked()));
        } else {
            initGuiValve();
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        if ( this.tile instanceof INameableTile ) {
            if ( !this.tileName.getText().isEmpty() ) {
                NetworkHandler.sendPacketToServer(new FFSPacket.Server.UpdateTileName(this.tile, this.tileName.getText()));
            }
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode) {
        if ( this.tile instanceof INameableTile ) {
            if ( this.tileName.isFocused() ) {
                this.tileName.textboxKeyTyped(keyChar, keyCode);
                return;
            }
        }

        if ( keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode() ) {
            this.mc.player.closeScreen();
            this.mc.setIngameFocus();
        }
    }

    @Override
    protected void mouseClicked(int p_73864_1_, int p_73864_2_, int p_73864_3_) throws IOException {
        super.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);

        if ( this.tile instanceof INameableTile ) {
            this.tileName.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private void drawGUIValve(int x, int y, float partialTicks) {
        GlStateManager.pushMatrix();

        this.mc.renderEngine.bindTexture(tex_valve);
        this.drawTexturedModalRect(this.left, this.top, 0, 0, this.xSize_Valve, this.ySize_Valve);

        String fluid = "Empty";
        if ( this.valve.getTankConfig().getFluidStack() != null ) {
            fluid = this.valve.getTankConfig().getFluidStack().getLocalizedName();
        }

        this.drawCenteredString(this.fontRenderer, fluid, this.left + (this.xSize_Valve / 2), this.top + 6, 16777215);

        FluidStack stack = null;
        if ( this.valve.getTankConfig() != null && this.valve.getTankConfig().getFluidTank() != null ) {
            stack = this.valve.getTankConfig().getFluidTank().getFluid();
            if ( stack != null ) {
                int height = Math.min(89, (int) Math.ceil((float) this.valve.getTankConfig().getFluidAmount() / (float) this.valve.getTankConfig().getFluidCapacity() * 89));
                this.drawFluid(this.left + 20, this.top + 27 + (89 - height), stack, 48, height);
            }
        }

        // call to super to draw buttons and other such fancy things
        super.drawScreen(x, y, partialTicks);

        if ( this.tile instanceof INameableTile ) {
            drawTileName(this.left, this.top);
        }

        if ( this.mouseX >= this.left + 62 && this.mouseX < this.left + 62 + 8 && this.mouseY >= this.top + 26 && this.mouseY < this.top + 26 + 8 ) {
            lockedFluidHoveringText();
        } else {
            if ( stack != null ) {
                fluidHoveringText(fluid, 20, 27, 89);
            }
        }

        GlStateManager.popMatrix();
    }

    private void drawGUINoValve(int x, int y, float partialTicks) {
        GlStateManager.pushMatrix();

        this.mc.renderEngine.bindTexture(tex_no_valve);
        this.drawTexturedModalRect(this.left, this.top, 0, 0, this.xSize_NoValve, this.ySize_NoValve);

        String fluid = "Empty";
        if ( this.valve.getTankConfig().getFluidStack() != null ) {
            fluid = this.valve.getTankConfig().getFluidStack().getLocalizedName();
        }

        this.drawCenteredString(this.fontRenderer, fluid, this.left + (this.xSize_NoValve / 2), this.top + 6, 16777215);

        FluidStack stack = null;
        if ( this.valve.getTankConfig() != null && this.valve.getTankConfig().getFluidTank() != null ) {
            stack = this.valve.getTankConfig().getFluidTank().getFluid();
            if ( stack != null ) {
                int height = Math.min(89, (int) Math.ceil((float) this.valve.getTankConfig().getFluidAmount() / (float) this.valve.getTankConfig().getFluidCapacity() * 89));
                this.drawFluid(this.left + 24, this.top + 27 + (89 - height), stack, 48, height);
            }
        }

        // call to super to draw buttons and other such fancy things
        super.drawScreen(x, y, partialTicks);

        if ( this.mouseX >= this.left + 66 && this.mouseX < this.left + 66 + 8 && this.mouseY >= this.top + 26 && this.mouseY < this.top + 26 + 8 ) {
            lockedFluidHoveringText();
        } else {
            if ( stack != null ) {
                fluidHoveringText(fluid, 24, 27, 89);
            }
        }
        GlStateManager.popMatrix();
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks) {
        this.mouseX = x;
        this.mouseY = y;

        if ( isNonValve ) {
            drawGUINoValve(x, y, partialTicks);
        } else {
            drawGUIValve(x, y, partialTicks);
        }
    }

    private void drawTileName(int x, int y) {
        int length = this.fontRenderer.getStringWidth("Tile Name");
        this.fontRenderer.drawString(ChatFormatting.BLACK + "Tile Name", x + 86 + (length / 2), y + 90, Color.white.getRGB());
        this.tileName.drawTextBox();
    }

    private void lockedFluidHoveringText() {
        List<String> texts = new ArrayList<>();
        texts.add("Fluid " + (this.valve.getTankConfig().isFluidLocked() ? (ChatFormatting.RED + "Locked") : (ChatFormatting.GREEN + "Unlocked")));

        if ( this.valve.getTankConfig().isFluidLocked() ) {
            texts.add(ChatFormatting.GRAY + "Locked to: " + this.valve.getTankConfig().getLockedFluid().getLocalizedName());
        }

        GlStateManager.pushMatrix();
        drawHoveringText(texts, this.mouseX, this.mouseY, this.fontRenderer);
        GlStateManager.popMatrix();
    }

    private void fluidHoveringText(String fluid, int tank_x, int tank_y, int height) {
        if ( this.mouseX >= this.left + tank_x && this.mouseX < this.left + tank_x + 48 &&
                this.mouseY >= this.top + tank_y && this.mouseY < this.top + tank_y + height ) {
            List<String> texts = new ArrayList<>();
            texts.add(fluid);
            texts.add(ChatFormatting.GRAY + (GenericUtil.intToFancyNumber(this.valve.getTankConfig().getFluidAmount()) + " / " + GenericUtil.intToFancyNumber(this.valve.getTankConfig().getFluidCapacity())) + " mB");

            GlStateManager.pushMatrix();
            drawHoveringText(texts, this.mouseX, this.mouseY, this.fontRenderer);
            GlStateManager.popMatrix();
        }
    }

    public void actionPerformed(GuiButton btn) {
        if ( btn == this.lockFluidButton ) {
            GuiButtonLockFluid toggle = (GuiButtonLockFluid) btn;

            this.masterValve.toggleFluidLock(toggle.getState());
            toggle.setState(this.masterValve.getTankConfig().isFluidLocked());
            NetworkHandler.sendPacketToServer(new FFSPacket.Server.UpdateFluidLock(this.masterValve));
        }
    }

    /* Thanks to CoFH Core for the Fluid rendering code */
    public void drawFluid(int x, int y, FluidStack fluid, int width, int height) {

        if ( fluid == null ) {
            return;
        }
        GL11.glPushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        ClientRenderHelper.setBlockTextureSheet();
        int color = fluid.getFluid().getColor(fluid);
        ClientRenderHelper.setGLColorFromInt(color);
        drawTiledTexture(x, y, ClientRenderHelper.getTexture(fluid.getFluid().getStill(fluid)), width, height);
        GL11.glPopMatrix();
    }

    public void drawTiledTexture(int x, int y, TextureAtlasSprite icon, int width, int height) {

        int i;
        int j;

        int drawHeight;
        int drawWidth;

        for (i = 0; i < width; i += 16) {
            for (j = 0; j < height; j += 16) {
                drawWidth = Math.min(width - i, 16);
                drawHeight = Math.min(height - j, 16);
                drawScaledTexturedModelRectFromIcon(x + i, y + j, icon, drawWidth, drawHeight);
            }
        }
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void drawScaledTexturedModelRectFromIcon(int x, int y, TextureAtlasSprite icon, int width, int height) {

        if ( icon == null ) {
            return;
        }
        double minU = icon.getMinU();
        double maxU = icon.getMaxU();
        double minV = icon.getMinV();
        double maxV = icon.getMaxV();

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, this.zLevel).tex(minU, minV + (maxV - minV) * height / 16F).endVertex();
        buffer.pos(x + width, y + height, this.zLevel).tex(minU + (maxU - minU) * width / 16F, minV + (maxV - minV) * height / 16F).endVertex();
        buffer.pos(x + width, y, this.zLevel).tex(minU + (maxU - minU) * width / 16F, minV).endVertex();
        buffer.pos(x, y, this.zLevel).tex(minU, minV).endVertex();
        Tessellator.getInstance().draw();
    }
}
