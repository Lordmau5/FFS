package com.lordmau5.ffs.blockentity.tanktiles;

import com.lordmau5.ffs.blockentity.abstracts.AbstractTankEntity;
import com.lordmau5.ffs.compat.computercraft.TankComputerPeripheral;
import com.lordmau5.ffs.holder.BlockEntities;
import com.lordmau5.ffs.holder.Capabilities;
import com.lordmau5.ffs.util.FFSStateProps;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockEntityTankComputer extends AbstractTankEntity {

    private final TankComputerPeripheral peripheral = new TankComputerPeripheral(this);
    private LazyOptional<IPeripheral> peripheralCap;

    public BlockEntityTankComputer(BlockPos pos, BlockState state) {
        super(BlockEntities.tileEntityTankComputer.get(), pos, state);
    }

    @Override
    public void doUpdate() {
        super.doUpdate();

        if (getLevel() != null) {
            getLevel().setBlockAndUpdate(getBlockPos(), getBlockState()
                    .setValue(FFSStateProps.TILE_VALID, isValid())
            );
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == Capabilities.CAPABILITY_PERIPHERAL) {
            if (peripheralCap == null) {
                peripheralCap = LazyOptional.of(() -> peripheral);
            }

            return peripheralCap.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();

        if (peripheralCap != null) peripheralCap.invalidate();
    }
}
