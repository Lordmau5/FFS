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
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.test.TestExecutor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
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
    private static final ResourceLocation tex_valve = new ResourceLocation(FancyFluidStorage.MOD_ID + ":textures/gui/gui_tank_valve.png");
    private static final ResourceLocation tex_no_valve = new ResourceLocation(FancyFluidStorage.MOD_ID + ":textures/gui/gui_tank_no_valve.png");
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
            if ( !this.tileName.getValue().isEmpty() ) {
                NetworkHandler.sendPacketToServer(new FFSPacket.Server.UpdateTileName(this.tile, this.tileName.getValue()));
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.getMinecraft().options.keyInventory.matches(keyCode, scanCode)) {
            this.getMinecraft().player.closeContainer();
            this.getMinecraft().setWindowActive(true);
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
        this.getMinecraft().getTextureManager().bind(tex_valve);
        blit(matrixStack, this.left, this.top, 0, 0, this.xSize_Valve, this.ySize_Valve);

        ITextComponent fluid = new TranslationTextComponent("gui.ffs.fluid_valve.empty");
        if ( !this.valve.getTankConfig().getFluidStack().isEmpty() ) {
            fluid = this.valve.getTankConfig().getFluidStack().getDisplayName();
        }

        drawCenteredString(matrixStack, this.font, fluid, this.left + (this.xSize_Valve / 2), this.top + 6, 16777215);

        FluidStack stack = FluidStack.EMPTY;
        if ( this.valve.getTankConfig() != null && this.valve.getTankConfig().getFluidTank() != null ) {
            stack = this.valve.getTankConfig().getFluidTank().getFluid();
            if ( !stack.isEmpty() ) {
                int height = Math.min(89, (int) Math.ceil((float) this.valve.getTankConfig().getFluidAmount() / (float) this.valve.getTankConfig().getFluidCapacity() * 89));
                this.drawFluid(matrixStack, this.left + 20, this.top + 27 + (89 - height), stack, 48, height);
            }
        }

        // call to super to draw buttons and other such fancy things
        super.render(matrixStack, x, y, partialTicks);

        if ( this.tile instanceof INameableTile ) {
            drawTileName(matrixStack, this.left, this.top, partialTicks);
        }

        if ( this.mouseX >= this.left + 62 && this.mouseX < this.left + 62 + 8 && this.mouseY >= this.top + 26 && this.mouseY < this.top + 26 + 8 ) {
            lockedFluidHoveringText(matrixStack);
        } else {
            if ( !stack.isEmpty() ) {
                fluidHoveringText(matrixStack, fluid, 20, 27, 89);
            }
        }
    }

    private void drawGUINoValve(MatrixStack matrixStack, int x, int y, float partialTicks) {
        this.getMinecraft().getTextureManager().bind(tex_no_valve);
        blit(matrixStack, this.left, this.top, 0, 0, this.xSize_NoValve, this.ySize_NoValve);

        ITextComponent fluid = new TranslationTextComponent("gui.ffs.fluid_valve.empty");
        if ( !this.valve.getTankConfig().isEmpty() ) {
            fluid = this.valve.getTankConfig().getFluidStack().getDisplayName();
        }

        drawCenteredString(matrixStack, this.font, fluid, this.left + (this.xSize_NoValve / 2), this.top + 6, 16777215);

        FluidStack stack = FluidStack.EMPTY;
        if ( this.valve.getTankConfig() != null && this.valve.getTankConfig().getFluidTank() != null ) {
            stack = this.valve.getTankConfig().getFluidTank().getFluid();
            if ( !stack.isEmpty() ) {
                int height = Math.min(89, (int) Math.ceil((float) this.valve.getTankConfig().getFluidAmount() / (float) this.valve.getTankConfig().getFluidCapacity() * 89));
                this.drawFluid(matrixStack, this.left + 24, this.top + 27 + (89 - height), stack, 48, height);
            }
        }

        // call to super to draw buttons and other such fancy things
        super.render(matrixStack, x, y, partialTicks);

        if ( this.mouseX >= this.left + 66 && this.mouseX < this.left + 66 + 8 && this.mouseY >= this.top + 26 && this.mouseY < this.top + 26 + 8 ) {
            lockedFluidHoveringText(matrixStack);
        } else {
            if ( !stack.isEmpty() ) {
                fluidHoveringText(matrixStack, fluid, 24, 27, 89);
            }
        }
    }

    @Override
    public void render(MatrixStack matrixStack, int x, int y, float partialTicks) {
        this.mouseX = x;
        this.mouseY = y;

        if ( isValve ) {
            drawGUIValve(matrixStack, mouseX, mouseY, partialTicks);
        } else {
            drawGUINoValve(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    private void drawTileName(MatrixStack matrixStack, int x, int y, float partialTicks) {
        int length = this.font.width("Tile Name");
        this.font.draw(matrixStack, TextFormatting.BLACK + "Tile Name", x + 86 + (length / 2), y + 90, Color.white.getRGB());
        this.tileName.render(matrixStack, x, y, partialTicks);
    }

    private void lockedFluidHoveringText(MatrixStack ms) {
        List<ITextComponent> texts = new ArrayList<>();
        if (this.valve.getTankConfig().isFluidLocked()) {
            texts.add(
                    (new TranslationTextComponent("gui.ffs.fluid_valve.fluid_base"))
                    .append(" ")
                    .append(new TranslationTextComponent("gui.ffs.fluid_valve.fluid_locked").withStyle(TextFormatting.RED))
            );
            texts.add(
                    (new TranslationTextComponent("description.ffs.fluid_valve.fluid", this.valve.getTankConfig().getLockedFluid().getDisplayName()))
                    .withStyle(TextFormatting.GRAY)
            );
        }
        else {
            texts.add(
                    (new TranslationTextComponent("gui.ffs.fluid_valve.fluid_base"))
                    .append(" ")
                    .append(new TranslationTextComponent("gui.ffs.fluid_valve.fluid_unlocked").withStyle(TextFormatting.GREEN))
            );
        }

        this.renderWrappedToolTip(ms, texts, this.mouseX, this.mouseY, this.font);
    }

    private void fluidHoveringText(MatrixStack ms, ITextComponent fluid, int tank_x, int tank_y, int height) {
        if ( this.mouseX >= this.left + tank_x && this.mouseX < this.left + tank_x + 48 &&
                this.mouseY >= this.top + tank_y && this.mouseY < this.top + tank_y + height ) {
            List<ITextComponent> texts = new ArrayList<>();
            texts.add(fluid);
            texts.add(
                    new StringTextComponent(
                            TextFormatting.GRAY
                                    + (GenericUtil.intToFancyNumber(this.valve.getTankConfig().getFluidAmount()) + " / " + GenericUtil.intToFancyNumber(this.valve.getTankConfig().getFluidCapacity()))
                                    + " mB"
                    )
            );

            this.renderWrappedToolTip(ms, texts, this.mouseX, this.mouseY, this.font);
        }
    }

    public void drawFluid(MatrixStack ms, int x, int y, FluidStack fluid, int width, int height) {
        ms.pushPose();

        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        ClientRenderHelper.setBlockTextureSheet();
        int color = fluid.getFluid().getAttributes().getColor(fluid);
        ClientRenderHelper.setGLColorFromInt(color);
        drawTiledTexture(ms, x, y, ClientRenderHelper.getTexture(fluid.getFluid().getAttributes().getStillTexture(fluid)), width, height);

        ms.popPose();
    }

    public void drawTiledTexture(MatrixStack ms, int x, int y, TextureAtlasSprite icon, int width, int height) {
        int i;
        int j;

        int drawHeight;
        int drawWidth;

        for (i = 0; i < width; i += 16) {
            for (j = 0; j < height; j += 16) {
                drawWidth = Math.min(width - i, 16);
                drawHeight = Math.min(height - j, 16);
                drawScaledTexturedModelRectFromIcon(ms, x + i, y + j, icon, drawWidth, drawHeight);
            }
        }
        GlStateManager._color4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void drawScaledTexturedModelRectFromIcon(MatrixStack ms, int x, int y, TextureAtlasSprite icon, int width, int height) {
        if ( icon == null ) {
            return;
        }

//        RenderSystem.shad(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderTexture(0, icon.atlas().location());



        BufferBuilder builder = Tessellator.getInstance().getBuilder();
        builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        float minU = icon.getU0();
        float maxU = icon.getU1();
        float minV = icon.getV0();
        float maxV = icon.getV1();

        float actualWidth = minU + (maxU - minU) * width / icon.getWidth();
        float actualHeight = minV + (maxV - minV) * height / icon.getHeight();

        float zLevel = 1.0f;

        Matrix4f matrix = ms.last().pose();

        buildSquare(matrix, builder, x, x + width, y, y + height, zLevel, minU, actualWidth, minV, actualHeight);

        RenderSystem.enableDepthTest();
        builder.end();

        WorldVertexBufferUploader.end(builder);

//        Tessellator.getInstance().end();
//        BufferUploader.drawWithShader(builder.end());

//        buffer.pos(x, y + height, zLevel).tex(minU, minV + (maxV - minV) * height / 16F).endVertex();
//        buffer.pos(x + width, y + height, zLevel).tex(minU + (maxU - minU) * width / 16F, minV + (maxV - minV) * height / 16F).endVertex();
//        buffer.pos(x + width, y, zLevel).tex(minU + (maxU - minU) * width / 16F, minV).endVertex();
//        buffer.pos(x, y, zLevel).tex(minU, minV).endVertex();
//        Tessellator.getInstance().draw();
    }

    private static void buildSquare(Matrix4f matrix, BufferBuilder builder, float x1, float x2, float y1, float y2, float z, float u1, float u2, float v1, float v2) {
        builder.vertex(matrix, x1, y2, z).uv(u1, v2).endVertex();
        builder.vertex(matrix, x2, y2, z).uv(u2, v2).endVertex();
        builder.vertex(matrix, x2, y1, z).uv(u2, v1).endVertex();
        builder.vertex(matrix, x1, y1, z).uv(u1, v1).endVertex();
    }
}
