package com.lordmau5.ffs.config;

import com.lordmau5.ffs.FancyFluidStorage;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;

/**
 * Created by Lordmau5 on 10.11.2016.
 */
class GuiConfigFFS extends GuiConfig {
    public GuiConfigFFS(GuiScreen parentScreen) {
        super(parentScreen, getElements(), FancyFluidStorage.MODID, false, false, TextFormatting.WHITE + "FFS");
        titleLine2 = TextFormatting.GOLD + "" + TextFormatting.ITALIC + "The configuration screen ...";
    }

    private static List<IConfigElement> getElements() {
        return new ConfigElement(Config.config.getCategory(Configuration.CATEGORY_CLIENT)).getChildElements();
    }

}
