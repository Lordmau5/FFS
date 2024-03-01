package com.lordmau5.ffs.datagen;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.holder.FFSBlocks;
import com.lordmau5.ffs.holder.FFSItems;
import net.minecraft.data.DataGenerator;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class FFSLanguageProvider extends LanguageProvider
{

    public FFSLanguageProvider(DataGenerator generator, String locale) {
        super(generator.getPackOutput(), FancyFluidStorage.MOD_ID, locale);
    }

    @Override
    public String getName() {
        return "Fancy Fluid Storage - Languages";
    }

    @Override
    protected void addTranslations() {
        String mod_id = FancyFluidStorage.MOD_ID;

        add("itemGroup." + mod_id, "Fancy Fluid Storage");

        add(FFSBlocks.fluidValve.get(), "Tank Valve");
        add(FFSBlocks.tankComputer.get(), "Tank Computer");

        add(FFSItems.tit.get(), "T.I.T. (Tank Interface Technician)");
        add(FFSItems.titEgg.get(), "T.I.T. Egg");

        add("chat." + mod_id + ".valve_other_tank", "One or more blocks already belong to another tank.");
        add("chat." + mod_id + ".valve_too_much_air", "Too many air blocks. Limit: %s");
        add("chat." + mod_id + ".valve_different_fluids", "One or more valves contain different fluids. Could not build the tank.");
        add("chat." + mod_id + ".valve_blacklisted_block_found", "Blacklisted block (%s) found at %s, %s, %s. (And %s more)");
        add("chat." + mod_id + ".valve_falling_block_found", "Falling block (%s) found at %s, %s, %s. (And %s more)");
        add("chat." + mod_id + ".valve_invalid_block_found", "Invalid block (%s) found at %s, %s, %s. (And %s more)");

        add("description." + mod_id + ".fluid_valve.fluid", "Fluid: %s");
        add("description." + mod_id + ".fluid_valve.amount", "Amount: %s");

        add("gui." + mod_id + ".fluid_valve", "Tank Valve");
        add("gui." + mod_id + ".fluid_valve.empty", "Empty");
        add("gui." + mod_id + ".fluid_valve.fluid_base", "Fluid");
        add("gui." + mod_id + ".fluid_valve.fluid_locked", "Locked");
        add("gui." + mod_id + ".fluid_valve.fluid_unlocked", "Unlocked");

        add("top." + mod_id + ".part_of_tank", "Part of a tank");
        add("top." + mod_id + ".fluid", "Fluid: %s");
    }
}
