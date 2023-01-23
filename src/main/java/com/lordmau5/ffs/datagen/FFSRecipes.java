package com.lordmau5.ffs.datagen;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.holder.FFSBlocks;
import com.lordmau5.ffs.holder.FFSItems;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.ConditionalRecipe;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class FFSRecipes extends RecipeProvider {

    public FFSRecipes(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    public String getName() {
        return "Fancy Fluid Storage - Recipes";
    }

    @Override
    protected void buildCraftingRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(FFSBlocks.fluidValve.get())
                .pattern("igi")
                .pattern("gbg")
                .pattern("igi")
                .define('i', Tags.Items.INGOTS_IRON)
                .define('g', Blocks.IRON_BARS)
                .define('b', Items.BUCKET)
                .group(FancyFluidStorage.MOD_ID)
                .unlockedBy("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(Items.BUCKET))
                .save(consumer);

        var cc_computer = ForgeRegistries.ITEMS.getHolder(new ResourceLocation("computercraft", "computer_normal"));
        cc_computer.ifPresent(itemHolder -> whenModLoaded(ShapelessRecipeBuilder.shapeless(FFSBlocks.tankComputer.get())
                        .requires(FFSBlocks.fluidValve.get())
                        .requires(itemHolder.value())
                        .group(FancyFluidStorage.MOD_ID)
                        .unlockedBy("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(FFSBlocks.fluidValve.get(), itemHolder.value()))
                , "computercraft").build(consumer, "ffs", "tank_computer"));

        ShapelessRecipeBuilder.shapeless(FFSItems.titEgg.get())
                .requires(FFSBlocks.fluidValve.get())
                .requires(Items.EGG)
                .group(FancyFluidStorage.MOD_ID)
                .unlockedBy("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(FFSBlocks.fluidValve.get()))
                .save(consumer);

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(FFSItems.titEgg.get()),
                        FFSItems.tit.get(), 1.0f, 100)
                .unlockedBy("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(FFSItems.titEgg.get()))
                .save(consumer);
    }

    private ConditionalRecipe.Builder whenModLoaded(ShapelessRecipeBuilder recipe, String modid) {
        return ConditionalRecipe.builder().addCondition(new ModLoadedCondition(modid)).addRecipe(recipe::save);
    }
}
