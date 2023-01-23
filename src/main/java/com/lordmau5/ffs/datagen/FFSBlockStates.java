package com.lordmau5.ffs.datagen;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.holder.FFSBlocks;
import com.lordmau5.ffs.util.FFSStateProps;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.MultiPartBlockStateBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

public class FFSBlockStates extends BlockStateProvider {

    public FFSBlockStates(DataGenerator gen, ExistingFileHelper helper) {
        super(gen, FancyFluidStorage.MOD_ID, helper);
    }

    @Override
    public String getName() {
        return "Fancy Fluid Storage - Block States";
    }

    private int getRotationX(Direction dir) {
        return switch (dir) {
            case UP -> -90;
            case DOWN -> 90;
            default -> 0;
        };
    }

    @Override
    protected void registerStatesAndModels() {
        registerFluidValve();
        registerTankComputer();
    }

    private void registerFluidValve() {
        BlockModelBuilder base = models().cubeAll("block/fluid_valve", modLoc("block/base"));

        BlockModelBuilder valid = models().cubeAll("block/fluid_valve/valid", modLoc("block/fluid_valve/valid"));

        BlockModelBuilder invalid = models().cubeAll("block/fluid_valve/invalid", modLoc("block/fluid_valve/invalid"));

        BlockModelBuilder main = models().cubeAll("block/fluid_valve/main", modLoc("block/fluid_valve/main"));

        MultiPartBlockStateBuilder builder = getMultipartBuilder(FFSBlocks.fluidValve.get());
        builder.part().modelFile(base).addModel();

        BlockModelBuilder[] validModels = new BlockModelBuilder[]{valid, invalid};
        for (int i = 0; i < 2; i++) {
            boolean isValid = i == 0;

            builder.part().modelFile(validModels[i])
                    .addModel()
                    .condition(FFSStateProps.TILE_VALID, isValid);
        }

        builder.part().modelFile(main).addModel().condition(FFSStateProps.TILE_MAIN, true);
    }

    private void registerTankComputer() {
        BlockModelBuilder valid = models().getBuilder("block/tank_computer/valid")
                .element().face(Direction.NORTH).texture("#single").end().end()
                .texture("single", modLoc("block/tank_computer/valid"));

        BlockModelBuilder invalid = models().getBuilder("block/tank_computer/invalid")
                .element().face(Direction.NORTH).texture("#single").end().end()
                .texture("single", modLoc("block/tank_computer/invalid"));

        BlockModelBuilder base = models().cubeAll("block/tank_computer", modLoc("block/base"));

        MultiPartBlockStateBuilder builder = getMultipartBuilder(FFSBlocks.tankComputer.get());
        builder.part().modelFile(base).addModel();

        BlockModelBuilder[] models = new BlockModelBuilder[]{valid, invalid};
        for (int i = 0; i < 2; i++) {
            boolean isValid = i == 0;

            for (Direction dir : Direction.values()) {
                builder.part().modelFile(models[i])
                        .rotationX(getRotationX(dir))
                        .rotationY((int) dir.getOpposite().toYRot())
                        .addModel()
                        .condition(FFSStateProps.FACING, dir)
                        .condition(FFSStateProps.TILE_VALID, isValid);
            }
        }
    }
}