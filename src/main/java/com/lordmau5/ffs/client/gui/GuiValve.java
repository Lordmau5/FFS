package com.lordmau5.ffs.client.gui;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.network.FFSPacket;
import com.lordmau5.ffs.network.NetworkHandler;
import com.lordmau5.ffs.tile.abstracts.AbstractTankTile;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import com.lordmau5.ffs.tile.interfaces.INameableTile;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import reborncore.client.gui.builder.GuiBase;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dustin on 05.07.2015.
 */
//TODO GuiBuilder all of this
public class GuiValve extends GuiBase
{
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

    public GuiValve(EntityPlayer player, AbstractTankTile tile, boolean isNonValve)
    {
        super(player, tile, new ContainerValue(tile));

        this.isNonValve = isNonValve;

        if (!isNonValve)
        {
            this.tile = tile;
            if (tile instanceof AbstractTankValve)
            {
                this.valve = (AbstractTankValve) tile;
            } else
            {
                this.valve = tile.getMasterValve();
            }
            this.masterValve = tile.getMasterValve();
        } else
        {
            this.valve = this.masterValve = tile.getMasterValve();
        }
    }

    private void initGuiValve()
    {
        this.left = (this.width - this.xSize_Valve) / 2;
        this.top = (this.height - this.ySize_Valve) / 2;
        if (this.tile instanceof INameableTile)
        {
            this.tileName = new GuiTextField(0, this.fontRenderer, this.left + 90, this.top + 102, 82, 10);
            this.tileName.setText(this.valve.getTileName());
            this.tileName.setMaxStringLength(32);
        }
        this.buttonList.add(this.lockFluidButton = new GuiButtonLockFluid(this.left + 62, this.top + 26, this.masterValve.getTankConfig().isFluidLocked()));
    }

    @Override
    public void initGui()
    {
        super.initGui();

        if (isNonValve)
        {
            this.left = (this.width - this.xSize_NoValve) / 2;
            this.top = (this.height - this.ySize_NoValve) / 2;
            this.buttonList.add(this.lockFluidButton = new GuiButtonLockFluid(this.left + 66, this.top + 26, this.masterValve.getTankConfig().isFluidLocked()));
        } else
        {
            initGuiValve();
        }
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        if (this.tile instanceof INameableTile)
        {
            if (!this.tileName.getText().isEmpty())
            {
                NetworkHandler.sendPacketToServer(new FFSPacket.Server.UpdateTileName(this.tile, this.tileName.getText()));
            }
        }
    }

    @Override
    protected void keyTyped(char keyChar, int keyCode)
    {
        if (this.tile instanceof INameableTile)
        {
            if (this.tileName.isFocused())
            {
                this.tileName.textboxKeyTyped(keyChar, keyCode);
                return;
            }
        }

        if (keyCode == 1 || keyCode == this.mc.gameSettings.keyBindInventory.getKeyCode())
        {
            this.mc.player.closeScreen();
            this.mc.setIngameFocus();
        }
    }

    @Override
    protected void mouseClicked(int p_73864_1_, int p_73864_2_, int p_73864_3_) throws IOException
    {
        super.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);

        if (this.tile instanceof INameableTile)
        {
            this.tileName.mouseClicked(p_73864_1_, p_73864_2_, p_73864_3_);
        }
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    private void drawGUIValve(int x, int y, float partialTicks)
    {
        GlStateManager.pushMatrix();

        this.mc.renderEngine.bindTexture(tex_valve);
        this.drawTexturedModalRect(this.left, this.top, 0, 0, this.xSize_Valve, this.ySize_Valve);

        String fluid = "Empty";
        if (this.valve.getTankConfig().getFluidStack() != null)
        {
            fluid = this.valve.getTankConfig().getFluidStack().getLocalizedName();
        }

        this.drawCenteredString(this.fontRenderer, fluid, this.left + (this.xSize_Valve / 2), this.top + 6, 16777215);

	    builder.drawFluid(this, this.valve.getTankConfig().getFluidTank().getFluid(), this.left + 20, this.top + 27, 48, 89, this.valve.getTankConfig().getFluidCapacity());
        //builder.drawTank(this, this.left + 20, this.top + 27, mouseX, mouseY, this.valve.getTankConfig().getFluidTank().getFluid(), 89, this.valve.getTankConfig().getFluidTank().getFluid() == null, Layer.BACKGROUND);

        // call to super to draw buttons and other such fancy things
        super.basicDrawScreen(x, y, partialTicks);

        if (this.tile instanceof INameableTile)
        {
            drawTileName(this.left, this.top);
        }

	    if(isPointInRegion(10, 45, 50, 89, x, y))
	    {
		    lockedFluidHoveringText();
	    }

	    GlStateManager.popMatrix();
    }

    private void drawGUINoValve(int x, int y, float partialTicks)
    {
	    GlStateManager.pushMatrix();

        this.mc.renderEngine.bindTexture(tex_no_valve);
        this.drawTexturedModalRect(this.left, this.top, 0, 0, this.xSize_NoValve, this.ySize_NoValve);

        String fluid = "Empty";
        if (this.valve.getTankConfig().getFluidStack() != null)
        {
            fluid = this.valve.getTankConfig().getFluidStack().getLocalizedName();
        }

        this.drawCenteredString(this.fontRenderer, fluid, this.left + (this.xSize_NoValve / 2), this.top + 6, 16777215);

	    builder.drawFluid(this, this.valve.getTankConfig().getFluidTank().getFluid(), this.left + 24, this.top + 27, 48, 89, this.valve.getTankConfig().getFluidCapacity());

        // call to super to draw buttons and other such fancy things
        super.basicDrawScreen(x, y, partialTicks);

	    if(isPointInRegion(62, 45, 50, 89, x, y))
	    {
		    lockedFluidHoveringText();
	    }
	    GlStateManager.popMatrix();
    }

    @Override
    public void drawScreen(int x, int y, float partialTicks)
    {
        this.mouseX = x;
        this.mouseY = y;

        if (isNonValve)
        {
            drawGUINoValve(x, y, partialTicks);
        }
        else
        {
            drawGUIValve(x, y, partialTicks);
        }

	    this.renderHoveredToolTip(x, y);
    }

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{

	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{

	}

	private void drawTileName(int x, int y)
    {
        int length = this.fontRenderer.getStringWidth("Tile Name");
        this.fontRenderer.drawString(ChatFormatting.BLACK + "Tile Name", x + 86 + (length / 2), y + 90, Color.white.getRGB());
        this.tileName.drawTextBox();
    }

    private void lockedFluidHoveringText()
    {
        List<String> texts = new ArrayList<>();
        texts.add("Fluid " + (this.valve.getTankConfig().isFluidLocked() ? (ChatFormatting.RED + "Locked") : (ChatFormatting.GREEN + "Unlocked")));

        if (this.valve.getTankConfig().isFluidLocked())
        {
            texts.add(ChatFormatting.GRAY + "Locked to: " + this.valve.getTankConfig().getLockedFluid().getLocalizedName());
        }

        GlStateManager.pushMatrix();
        drawHoveringText(texts, this.mouseX, this.mouseY, this.fontRenderer);
        GlStateManager.popMatrix();
    }

    public void actionPerformed(GuiButton btn)
    {
        if (btn == this.lockFluidButton)
        {
            GuiButtonLockFluid toggle = (GuiButtonLockFluid) btn;

            this.masterValve.toggleFluidLock(toggle.getState());
            toggle.setState(this.masterValve.getTankConfig().isFluidLocked());
            NetworkHandler.sendPacketToServer(new FFSPacket.Server.UpdateFluidLock(this.masterValve));
        }
    }
}
