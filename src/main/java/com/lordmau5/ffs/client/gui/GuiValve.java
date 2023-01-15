package com.lordmau5.ffs.client.gui;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.network.FFSPacket;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.tile.abstracts.AbstractTankTile;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import com.lordmau5.ffs.tile.interfaces.INameableTile;
import com.lordmau5.ffs.util.ClientRenderHelper;
import com.lordmau5.ffs.util.GenericUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private EditBox tileName;
    private int left = 0, top = 0;
    private int mouseX, mouseY;

    public GuiValve(AbstractTankTile tile, boolean isValve) {
        super(new TranslatableComponent("ffs.gui.valve"));

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
        this.addWidget(this.lockFluidButton = new GuiButtonLockFluid(this.left + 62, this.top + 26, this.mainValve.getTankConfig().isFluidLocked(), (button) -> {
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
            this.addWidget(this.lockFluidButton = new GuiButtonLockFluid(this.left + 65, this.top + 26, this.mainValve.getTankConfig().isFluidLocked(), (button) -> {
                this.lockFluidButton.toggleState();

                this.mainValve.setFluidLock(this.lockFluidButton.getState());
                this.lockFluidButton.setState(this.mainValve.getTankConfig().isFluidLocked());
                NetworkHandler.sendPacketToServer(new FFSPacket.Server.UpdateFluidLock(this.mainValve));
            }));
        }
    }

    @Override
    public void removed() {
        super.removed();

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

    private void drawGUIValve(PoseStack matrixStack, int x, int y, float partialTicks) {
        RenderSystem.setShaderTexture(0, tex_valve);
        blit(matrixStack, this.left, this.top, 0, 0, this.xSize_Valve, this.ySize_Valve);

        Component fluid = new TranslatableComponent("gui.ffs.fluid_valve.empty");
        if ( this.valve.getTankConfig().getFluidStack() != FluidStack.EMPTY ) {
            fluid = this.valve.getTankConfig().getFluidStack().getDisplayName();
        }

        drawCenteredString(matrixStack, this.font, fluid, this.left + (this.xSize_Valve / 2), this.top + 6, 16777215);

        FluidStack stack = null;
        if ( this.valve.getTankConfig() != null && this.valve.getTankConfig().getFluidTank() != null ) {
            stack = this.valve.getTankConfig().getFluidTank().getFluid();
            if ( stack != null ) {
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
            if ( stack != null ) {
                fluidHoveringText(matrixStack, fluid, 20, 27, 89);
            }
        }
    }

    private void drawGUINoValve(PoseStack matrixStack, int x, int y, float partialTicks) {
        RenderSystem.setShaderTexture(0, tex_no_valve);
        blit(matrixStack, this.left, this.top, 0, 0, this.xSize_NoValve, this.ySize_NoValve);

        Component fluid = new TranslatableComponent("gui.ffs.fluid_valve.empty");
        if ( !this.valve.getTankConfig().isEmpty() ) {
            fluid = this.valve.getTankConfig().getFluidStack().getDisplayName();
        }

        drawCenteredString(matrixStack, this.font, fluid, this.left + (this.xSize_NoValve / 2), this.top + 6, 16777215);

        FluidStack stack = null;
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
            if ( stack != null ) {
                fluidHoveringText(matrixStack, fluid, 24, 27, 89);
            }
        }
    }

    @Override
    public void render(PoseStack matrixStack, int x, int y, float partialTicks) {
        this.mouseX = x;
        this.mouseY = y;

        if ( isValve ) {
            drawGUIValve(matrixStack, mouseX, mouseY, partialTicks);
        } else {
            drawGUINoValve(matrixStack, mouseX, mouseY, partialTicks);
        }
    }

    private void drawTileName(PoseStack matrixStack, int x, int y, float partialTicks) {
        int length = this.font.width("Tile Name");
        this.font.draw(matrixStack, ChatFormatting.BLACK + "Tile Name", x + 86 + (length / 2), y + 90, Color.white.getRGB());
        this.tileName.render(matrixStack, x, y, partialTicks);
    }

    private void lockedFluidHoveringText(PoseStack ms) {
        List<Component> texts = new ArrayList<>();
        if (this.valve.getTankConfig().isFluidLocked()) {
            texts.add(
                    (new TranslatableComponent("gui.ffs.fluid_valve.fluid_base"))
                    .append(" ")
                    .append(new TranslatableComponent("gui.ffs.fluid_valve.fluid_locked").withStyle(ChatFormatting.RED))
            );
            texts.add(
                    (new TranslatableComponent("description.ffs.fluid_valve.fluid", this.valve.getTankConfig().getLockedFluid().getDisplayName()))
                    .withStyle(ChatFormatting.GRAY)
            );
        }
        else {
            texts.add(
                    (new TranslatableComponent("gui.ffs.fluid_valve.fluid_base"))
                    .append(" ")
                    .append(new TranslatableComponent("gui.ffs.fluid_valve.fluid_unlocked").withStyle(ChatFormatting.GREEN))
            );
        }

        this.renderTooltip(ms, texts, Optional.empty(), this.mouseX, this.mouseY, this.font);
    }

    private void fluidHoveringText(PoseStack ms, Component fluid, int tank_x, int tank_y, int height) {
        if ( this.mouseX >= this.left + tank_x && this.mouseX < this.left + tank_x + 48 &&
                this.mouseY >= this.top + tank_y && this.mouseY < this.top + tank_y + height ) {
            List<Component> texts = new ArrayList<>();
            texts.add(fluid);
            texts.add(
                    new TextComponent(
                            ChatFormatting.GRAY
                                    + (GenericUtil.intToFancyNumber(this.valve.getTankConfig().getFluidAmount()) + " / " + GenericUtil.intToFancyNumber(this.valve.getTankConfig().getFluidCapacity()))
                                    + " mB"
                    )
            );

            this.renderTooltip(ms, texts, Optional.empty(), this.mouseX, this.mouseY, this.font);
        }
    }

    public void drawFluid(PoseStack ms, int x, int y, @Nonnull FluidStack fluid, int width, int height) {
        ms.pushPose();

        GlStateManager._enableBlend();
        GlStateManager._blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        ClientRenderHelper.setBlockTextureSheet();
        int color = fluid.getFluid().getAttributes().getColor(fluid);
        ClientRenderHelper.setGLColorFromInt(color);
        drawTiledTexture(ms, x, y, ClientRenderHelper.getTexture(fluid.getFluid().getAttributes().getStillTexture(fluid)), width, height);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        ms.popPose();
    }

    public void drawTiledTexture(PoseStack ms, int x, int y, TextureAtlasSprite icon, int width, int height) {
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
    }

    public void drawScaledTexturedModelRectFromIcon(PoseStack ms, int x, int y, TextureAtlasSprite icon, int width, int height) {
        if ( icon == null ) {
            return;
        }

        RenderSystem.setShaderTexture(0, icon.atlas().location());

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        float u1 = icon.getU0();
        float v1 = icon.getV0();
        int spriteHeight = icon.getHeight();
        int spriteWidth = icon.getWidth();
        int startX = x;
        int startY = y;
        do {
            int renderHeight = Math.min(spriteHeight, height);
            height -= renderHeight;
            float v2 = icon.getV((16f * renderHeight) / spriteHeight);

            // we need to draw the quads per width too
            int x2 = startX;
            int widthLeft = width;
            Matrix4f matrix = ms.last().pose();
            // tile horizontally
            do {
                int renderWidth = Math.min(spriteWidth, widthLeft);
                widthLeft -= renderWidth;

                float u2 = icon.getU((16f * renderWidth) / spriteWidth);
                buildSquare(matrix, builder, x2, x2 + renderWidth, startY, startY + renderHeight, 100, u1, u2, v1, v2);
                x2 += renderWidth;
            } while(widthLeft > 0);

            startY += renderHeight;
        } while(height > 0);

        // finish drawing sprites
        builder.end();

        RenderSystem.enableDepthTest();
        BufferUploader.end(builder);
    }

    private static void buildSquare(Matrix4f matrix, BufferBuilder builder, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2) {
        builder.vertex(matrix, x1, y2, z).uv(u1, v2).endVertex();
        builder.vertex(matrix, x2, y2, z).uv(u2, v2).endVertex();
        builder.vertex(matrix, x2, y1, z).uv(u2, v1).endVertex();
        builder.vertex(matrix, x1, y1, z).uv(u1, v1).endVertex();
    }
}
