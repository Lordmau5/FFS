package com.lordmau5.ffs.compat.oc;

import com.lordmau5.ffs.tile.tanktiles.TileEntityTankComputer;
import li.cil.oc.api.Driver;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import li.cil.oc.api.prefab.ManagedEnvironment;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Created by Vexatos
 * Adjusted to suit FFS.
 */
public class OCCompatibility {

	public void init() {
		Driver.add(new OpenComputersDriver());
	}

	public static class OpenComputersDriver extends DriverSidedTileEntity {

		@Override
		public Class<?> getTileEntityClass() {
			return TileEntityTankComputer.class;
		}

		@Override
		public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing facing) {
			return new InternalManagedEnvironment(((TileEntityTankComputer) world.getTileEntity(pos)));
		}

		public static class InternalManagedEnvironment extends ManagedEnvironmentOCTile<TileEntityTankComputer> {
			public InternalManagedEnvironment(TileEntityTankComputer tile) {
				super(tile, "ffs_valve");
			}

			@Override
			public int priority() {
				return 1;
			}

			@Callback(doc = "function():string;  Returns the fluid name, if the tank contains a fluid.")
			public Object[] getFluidName(Context c, Arguments a) {
				if(tile.getMasterValve().getTankConfig().getFluidStack() == null) {
					return null;
				}
				return new Object[]{tile.getMasterValve().getTankConfig().getFluidStack().getLocalizedName()};
			}

			@Callback(doc = "function():number;  Returns the fluid amount. If there is no fluid, returns 0.")
			public Object[] getFluidAmount(Context c, Arguments a) {
				return new Object[]{tile.getMasterValve().getTankConfig().getFluidAmount()};
			}

			@Callback(doc = "function():number;  Returns the tank capacity.")
			public Object[] getFluidCapacity(Context c, Arguments a) {
				return new Object[]{tile.getMasterValve().getTankConfig().getFluidCapacity()};
			}

			@Callback(doc = "function():boolean;  Returns if the tank is locked to a certain fluid.")
			public Object[] isFluidLocked(Context c, Arguments a) {
				return new Object[]{tile.getMasterValve().getTankConfig().isFluidLocked()};
			}

			@Callback(doc = "function():string;  Returns the locked fluid name, if the tank is locked, otherwise null.")
			public Object[] getLockedFluid(Context c, Arguments a) {
				return new Object[]{tile.getMasterValve().getTankConfig().isFluidLocked() ? tile.getMasterValve().getTankConfig().getLockedFluid().getLocalizedName() : null};
			}

			@Callback(doc = "function([boolean:state]):boolean;  (Un-)locks the fluid in the tank. Returns the new state.")
			public Object[] toggleFluidLock(Context c, Arguments a) throws Exception {
				if(a.count() == 0) {
					if(tile.getMasterValve().getTankConfig().getFluidStack() == null) {
						throw new Exception("can't lock tank to fluid, no fluid in tank");
					}

					tile.getMasterValve().toggleFluidLock(!tile.getMasterValve().getTankConfig().isFluidLocked());

					return new Object[]{tile.getMasterValve().getTankConfig().isFluidLocked()};
				}
				else if(a.count() == 1) {
					if(!a.isBoolean(1)) {
						throw new Exception("expected argument 1 to be of type \"Boolean\", found \"" + a.checkAny(1).getClass().getSimpleName() + "\"");
					}

					boolean state = a.checkBoolean(1);
					if(state && tile.getMasterValve().getTankConfig().getFluidStack() == null) {
						throw new Exception("can't lock tank to fluid, no fluid in tank");
					}

					tile.getMasterValve().toggleFluidLock(state);

					return new Object[]{tile.getMasterValve().getTankConfig().isFluidLocked()};
				}
				else {
					throw new Exception("insufficient number of arguments found - expected 1, got " + a.count());
				}
			}
		}
	}

}
