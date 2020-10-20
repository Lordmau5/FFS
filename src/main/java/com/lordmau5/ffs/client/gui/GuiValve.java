package com.lordmau5.ffs.client.gui;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.network.FFSPacket;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.tile.abstracts.AbstractTankTile;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import com.lordmau5.ffs.tile.interfaces.INameableTile;
import com.lordmau5.ffs.util.ClientRenderHelper;
import com.lordmau5.ffs.util.GenericUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiValve extends Screen {
    private static final ResourceLocation tex_valve = new ResourceLocation(FancyFluidStorage.MODID + ":textures/gui/gui_tank_valve.png");
    private static final ResourceLocation tex_no_valve = new ResourceLocation(FancyFluidStorage.MODID + ":textures/gui/gui_tank_no_valve.png");
    private final AbstractTankValve valve;
    private final AbstractTankValve mainValve;
    private final int xSize_Valve = 196;
    private final int ySize_Valve = 128;
    private final int xSize_NoValve = 96;
    private final int ySize_NoValve = 128;
    private GuiButtonLockFluid lockFluidButton;
    private boolean isValve = false;
    private AbstractTankTile tile;
    private TextFieldWidget tileName;
    private int left = 0, top = 0;
    private int mouseX, mouseY;

    public GuiValve(AbstractTankTile tile, boolean isValve) {
        super(new TranslationTextComponent("ffs.gui.valve"));

        this.isValve = isValve;

        if ( isValve ) {
            this.tile = tile;
            if ( tile instanceof AbstractTankValve ) {
                this.valve = (AbstractTankValve) tile;
            } else {
                this.valve = tile.getMainValve();
            }
            this.mainValve = tile.getMainValve();
        } else {
            this.valve = this.mainValve = tile.getMainValve();
        }
    }

    private void initGuiValve() {
        this.left = (this.width - this.xSize_Valve) / 2;
        this.top = (this.height - this.ySize_Valve) / 2;
        if ( this.tile instanceof INameableTile ) {
//            this.tileName = new TextFieldWidget(this.font, this.left + 90, this.top + 102, 82, 10, new TranslationTextComponent("tileName"));
//            this.tileName.setText(this.valve.getTileName());
//            this.tileName.setMaxStringLength(32);
        }
        this.addButton(this.lockFluidButton = new GuiButtonLockFluid(this.left + 62, this.top + 26, this.mainValve.getTankConfig().isFluidLocked(), (button) -> {
            this.lockFluidButton.toggleState();

            this.mainValve.setFluidLock(this.lockFluidButton.getState());
            NetworkHandler.sendPacketToServer(new FFSPacket.Server.UpdateFluidLock(this.mainValve));
        }));
    }

    @Override
    protected void init() {
        super.init();

        if ( isValve ) {
            initGuiValve();
        } else {
            this.left = (this.width - this.xSize_NoValve) / 2;
            this.top = (this.height - this.ySize_NoValve) / 2;
            this.addButton(this.lockFluidButton = new GuiButtonLockFluid(this.left + 65, this.top + 26, this.mainValve.getTankConfig().isFluidLocked(), (button) -> {
                this.lockFluidButton.toggleState();

                this.mainValve.setFluidLock(this.lockFluidButton.getState());
                this.lockFluidButton.setState(this.mainValve.getTankConfig().isFluidLocked());
                NetworkHandler.sendPacketToServer(new FFSPacket.Server.UpdateFluidLock(this.mainValve));
            }));
        }
    }

    @Override
    public void onClose() {
        super.onClose();

        if ( this.tile instanceof INameableTile ) {
            if ( !this.tileName.getText().isEmpty() ) {
                NetworkHandler.sendPacketToServer(new FFSPacket.Server.UpdateTileName(this.tile, this.tileName.getText()));
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.getMinecraft().gameSettings.keyBindInventory.matchesKey(keyCode, scanCode)) {
            this.getMinecraft().player.closeScreen();
            this.getMinecraft().setGameFocused(true);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        if ( this.tile instanceof INameableTile ) {
            this.tileName.mouseClicked(mouseX, mouseY, button);
        }

        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void drawGUIValve(MatrixStack matrixStack, int x, int y, float partialTicks) {
        this.getMinecraft().getTextureManager().bindTexture(tex_valve);
        blit(this.left, this.top, 0, 0, this.xSize_Valve, this.ySize_Valve);

        ITextComponent fluid = new TranslationTextComponent("gui.ffs.fluid_valve.empty");
        if ( this.valve.getTankConfig().getFluidStack() != FluidStack.EMPTY ) {
            fluid = this.valve.getTankConfig().getFluidStack().getDisplayName();
        }

        drawCenteredString(this.font, fluid.getFormattedText(), this.left + (this.xSize_Valve / 2), this.top + 6, 16777215);

        FluidStack stack = null;
        if ( this.valve.getTankConfig() != null && this.valve.getTankConfig().getFluidTank() != null ) {
            stack = this.valve.getTankConfig().getFluidTank().getFluid();
            if ( stack != null ) {
                int height = Math.min(89, (int) Math.ceil((float) this.valve.getTankConfig().getFluidAmount() / (float) this.valve.getTankConfig().getFluidCapacity() * 89));
                this.drawFluid(matrixStack, this.left + 20, this.top + 27 + (89 - height), stack, 48, height);
            }
        }

        // call to super to draw buttons and other such fancy things
        super.render(x, y, partialTicks);

        if ( this.tile instanceof INameableTile ) {
            drawTileName(matrixStack, this.left, this.top, partialTicks);
        }

        if ( this.mouseX >= this.left + 62 && this.mouseX < this.left + 62 + 8 && this.mouseY >= this.top + 26 && this.mouseY < this.top + 26 + 8 ) {
            lockedFluidHoveringText(matrixStack);
        } else {
            if ( stack != null ) {
                fluidHoveringText(matrixStack, fluid, 20, 27, 89);
            }
        }
    }

    private void drawGUINoValve(MatrixStack matrixStack, int x, int y, float partialTicks) {
        this.getMinecraft().getTextureManager().bindTexture(tex_no_valve);
        blit(this.left, this.top, 0, 0, this.xSize_NoValve, this.ySize_NoValve);

        ITextComponent fluid = new TranslationTextComponent("gui.ffs.fluid_valve.empty");
        if ( !this.valve.getTankConfig().isEmpty() ) {
            fluid = this.valve.getTankConfig().getFluidStack().getDisplayName();
        }

        drawCenteredString(this.font, fluid.getFormattedText(), this.left + (this.xSize_NoValve / 2), this.top + 6, 16777215);

        FluidStack stack = null;
        if ( this.valve.getTankConfig() != null && this.valve.getTankConfig().getFluidTank() != null ) {
            stack = this.valve.getTankConfig().getFluidTank().getFluid();
            if ( stack != null ) {
                int height = Math.min(89, (int) Math.ceil((float) this.valve.getTankConfig().getFluidAmount() / (float) this.valve.getTankConfig().getFluidCapacity() * 89));
                this.drawFluid(matrixStack, this.left + 24, this.top + 27 + (89 - height), stack, 48, height);
            }
        }

        // call to super to draw buttons and other such fancy things
        super.render(x, y, partialTicks);

        if ( this.mouseX >= this.left + 66 && this.mouseX < this.left + 66 + 8 && this.mouseY >= this.top + 26 && this.mouseY < this.top + 26 + 8 ) {
            lockedFluidHoveringText(matrixStack);
        } else {
            if ( stack != null ) {
                fluidHoveringText(matrixStack, fluid, 24, 27, 89);
            }
        }
    }

    @Override
    public void render(int x, int y, float partialTicks) {
        this.mouseX = x;
        this.mouseY = y;

        if ( isValve ) {
            drawGUIValve(null, mouseX, mouseY, partialTicks);
        } else {
            drawGUINoValve(null, mouseX, mouseY, partialTicks);
        }
    }

    private void drawTileName(MatrixStack matrixStack, int x, int y, float partialTicks) {
        int length = this.font.getStringWidth("Tile Name");
        this.font.drawString(TextFormatting.BLACK + "Tile Name", x + 86 + (length / 2), y + 90, Color.white.getRGB());
        this.tileName.render(x, y, partialTicks);
    }

    private void lockedFluidHoveringText(MatrixStack ms) {
        List<String> texts = new ArrayList<>();
        if (this.valve.getTankConfig().isFluidLocked()) {
            texts.add(
                    (new TranslationTextComponent("gui.ffs.fluid_valve.fluid_base"))
                    .appendText(" ")
                    .appendSibling(new TranslationTextComponent("gui.ffs.fluid_valve.fluid_locked").applyTextStyle(TextFormatting.RED))
                    .getFormattedText()
            );
            texts.add(
                    (new TranslationTextComponent("description.ffs.fluid_valve.fluid", this.valve.getTankConfig().getLockedFluid().getDisplayName()))
                    .applyTextStyle(TextFormatting.GRAY)
                    .getFormattedText()
            );
        }
        else {
            texts.add(
                    (new TranslationTextComponent("gui.ffs.fluid_valve.fluid_base"))
                    .appendText(" ")
                    .appendSibling(new TranslationTextComponent("gui.ffs.fluid_valve.fluid_unlocked").applyTextStyle(TextFormatting.GREEN))
                    .getFormattedText()
            );
        }

        this.renderTooltip(texts, this.mouseX, this.mouseY, this.font);
    }

    private void fluidHoveringText(MatrixStack ms, ITextComponent fluid, int tank_x, int tank_y, int height) {
        if ( this.mouseX >= this.left + tank_x && this.mouseX < this.left + tank_x + 48 &&
                this.mouseY >= this.top + tank_y && this.mouseY < this.top + tank_y + height ) {
            List<String> texts = new ArrayList<>();
            texts.add(fluid.getFormattedText());
            texts.add(
                    new StringTextComponent(
                            TextFormatting.GRAY
                                    + (GenericUtil.intToFancyNumber(this.valve.getTankConfig().getFluidAmount()) + " / " + GenericUtil.intToFancyNumber(this.valve.getTankConfig().getFluidCapacity()))
                                    + " mB"
                    )
                    .getFormattedText()
            );

            this.renderTooltip(texts, this.mouseX, this.mouseY, this.font);
        }
    }

    public void drawFluid(MatrixStack ms, int x, int y, FluidStack fluid, int width, int height) {
        if ( fluid == null ) {
            return;
        }

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        ClientRenderHelper.setBlockTextureSheet();
        int color = fluid.getFluid().getAttributes().getColor(fluid);
        ClientRenderHelper.setGLColorFromInt(color);
        drawTiledTexture(x, y, ClientRenderHelper.getTexture(fluid.getFluid().getAttributes().getStillTexture(fluid)), width, height);
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
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void drawScaledTexturedModelRectFromIcon(int x, int y, TextureAtlasSprite icon, int width, int height) {
        if ( icon == null ) {
            return;
        }
        float minU = icon.getMinU();
        float maxU = icon.getMaxU();
        float minV = icon.getMinV();
        float maxV = icon.getMaxV();

        float zLevel = 1.0f;

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        buffer.pos(x, y + height, zLevel).tex(minU, minV + (maxV - minV) * height / 16F).endVertex();
        buffer.pos(x + width, y + height, zLevel).tex(minU + (maxU - minU) * width / 16F, minV + (maxV - minV) * height / 16F).endVertex();
        buffer.pos(x + width, y, zLevel).tex(minU + (maxU - minU) * width / 16F, minV).endVertex();
        buffer.pos(x, y, zLevel).tex(minU, minV).endVertex();
        Tessellator.getInstance().draw();
    }
}
