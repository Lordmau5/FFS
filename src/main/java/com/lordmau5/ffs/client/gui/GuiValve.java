package com.lordmau5.ffs.client.gui;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.network.FFSPacket;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.tile.abstracts.AbstractTankTile;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import com.lordmau5.ffs.tile.interfaces.INameableTile;
import com.lordmau5.ffs.util.ClientRenderHelper;
import com.lordmau5.ffs.util.GenericUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private final boolean isValve;
    private AbstractTankTile tile;
    private EditBox tileName;
    private int left = 0, top = 0;
    private int mouseX, mouseY;

    public GuiValve(AbstractTankTile tile, boolean isValve) {
        super(Component.translatable("gui.ffs.fluid_valve"));

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
        this.addRenderableWidget(this.lockFluidButton = new GuiButtonLockFluid(this.left + 62, this.top + 26, this.mainValve.getTankConfig().isFluidLocked(), (button) -> {
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
            this.addRenderableWidget(this.lockFluidButton = new GuiButtonLockFluid(this.left + 65, this.top + 26, this.mainValve.getTankConfig().isFluidLocked(), (button) -> {
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
            Objects.requireNonNull(this.getMinecraft().player).closeContainer();
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

        Component fluid = Component.translatable("gui.ffs.fluid_valve.empty");
        if ( !this.valve.getTankConfig().getFluidStack().isEmpty() ) {
            fluid = this.valve.getTankConfig().getFluidStack().getDisplayName();
        }

        drawCenteredString(matrixStack, this.font, fluid, this.left + (this.xSize_Valve / 2), this.top + 6, 16777215);

        FluidStack stack = null;
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
            if ( stack != null ) {
                fluidHoveringText(matrixStack, fluid, 20, 27, 89);
            }
        }
    }

    private void drawGUINoValve(PoseStack matrixStack, int x, int y, float partialTicks) {
        RenderSystem.setShaderTexture(0, tex_no_valve);
        blit(matrixStack, this.left, this.top, 0, 0, this.xSize_NoValve, this.ySize_NoValve);

        Component fluid = Component.translatable("gui.ffs.fluid_valve.empty");
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
    public void render(@NotNull PoseStack matrixStack, int x, int y, float partialTicks) {
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
        this.font.draw(matrixStack, ChatFormatting.BLACK + "Tile Name", x + 86 + (length / 2.0f), y + 90, Color.white.getRGB());
        this.tileName.render(matrixStack, x, y, partialTicks);
    }

    private void lockedFluidHoveringText(PoseStack ms) {
        List<Component> texts = new ArrayList<>();
        if (this.valve.getTankConfig().isFluidLocked()) {
            texts.add(
                    (Component.translatable("gui.ffs.fluid_valve.fluid_base"))
                    .append(" ")
                    .append(Component.translatable("gui.ffs.fluid_valve.fluid_locked").withStyle(ChatFormatting.RED))
            );
            texts.add(
                    (Component.translatable("description.ffs.fluid_valve.fluid", this.valve.getTankConfig().getLockedFluid().getDisplayName()))
                    .withStyle(ChatFormatting.GRAY)
            );
        }
        else {
            texts.add(
                    (Component.translatable("gui.ffs.fluid_valve.fluid_base"))
                    .append(" ")
                    .append(Component.translatable("gui.ffs.fluid_valve.fluid_unlocked").withStyle(ChatFormatting.GREEN))
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
                    Component.literal(
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

        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());

        ClientRenderHelper.setBlockTextureSheet();
        int color = extensions.getTintColor(fluid);
        ClientRenderHelper.setGLColorFromInt(color);
        drawTiledTexture(ms, x, y, ClientRenderHelper.getTexture(extensions.getStillTexture(fluid)), width, height);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        ms.popPose();
    }

    public void drawTiledTexture(PoseStack ms, int x, int y, TextureAtlasSprite icon, int width, int height) {
        int i;
        int j;

        int drawHeight;
        int drawWidth;

        int iconWidth = icon.getWidth();
        int iconHeight = icon.getHeight();

        for (i = 0; i < width; i += iconWidth) {
            for (j = 0; j < height; j += iconHeight) {
                drawWidth = Math.min(width - i, iconWidth);
                drawHeight = Math.min(height - j, iconHeight);
//                blit(ms, x + i, y + j, 0, drawWidth, drawHeight, icon);
                drawScaledTexturedModelRectFromIcon(ms, x + i, y + j, icon, drawWidth, drawHeight);
            }
        }
    }

    public void drawScaledTexturedModelRectFromIcon(PoseStack ms, int x, int y, TextureAtlasSprite icon, int width, int height) {
        if ( icon == null ) {
            return;
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
//        RenderSystem.setShaderTexture(0, icon.atlas().location());

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

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
        BufferUploader.drawWithShader(builder.end());
    }

    private static void buildSquare(Matrix4f matrix, BufferBuilder builder, float x1, float x2, float y1, float y2, float z, float u1, float u2, float v1, float v2) {
        builder.vertex(matrix, x1, y2, z).uv(u1, v2).endVertex();
        builder.vertex(matrix, x2, y2, z).uv(u2, v2).endVertex();
        builder.vertex(matrix, x2, y1, z).uv(u2, v1).endVertex();
        builder.vertex(matrix, x1, y1, z).uv(u1, v1).endVertex();
    }
}
