package com.lordmau5.ffs.datagen;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.holder.FFSBlocks;
import com.lordmau5.ffs.holder.FFSItems;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class FFSItemModels extends ItemModelProvider {

    public FFSItemModels(DataGenerator packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, FancyFluidStorage.MOD_ID, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Fancy Fluid Storage - Item Models";
    }

    @Override
    protected void registerModels() {
        registerItems();

        registerFluidValve();
        registerTankComputer();
    }

    private void registerItems() {
        singleTexture(FFSItems.titEgg.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/item_tit_egg"));

        singleTexture(FFSItems.tit.getId().getPath(),
                mcLoc("item/generated"),
                "layer0", modLoc("item/item_tit"));
    }

    private void registerFluidValve() {
        ItemModelBuilder builder = withExistingParent(FFSBlocks.fluidValve.getId().getPath(), "block/block");

        builder.texture("base", modLoc("block/base"));
        builder.texture("valid", modLoc("block/fluid_valve/valid"));

        builder.element().textureAll("#base").end();
        builder.element().textureAll("#valid").end();

        fixTransforms(builder);
    }

    private void registerTankComputer() {
        ItemModelBuilder builder = withExistingParent(FFSBlocks.tankComputer.getId().getPath(), "block/block");

        builder.texture("base", modLoc("block/base"));
        builder.texture("valid", modLoc("block/tank_computer/valid"));

        builder.element().textureAll("#base").end();
        builder.element().face(Direction.NORTH).texture("#valid").end();

        fixTransforms(builder);
    }

    private void fixTransforms(ItemModelBuilder builder) {
        builder.transforms().transform(ItemTransforms.TransformType.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, 135, 0)
                .translation(0, 0, 0)
                .scale(0.4f, 0.4f, 0.4f)
                .end();
    }
}
