package com.lordmau5.ffs.util;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.compat.Compatibility;
import com.lordmau5.ffs.compat.cnb.CNBAPIAccess;
import com.lordmau5.ffs.compat.cnb.CNBCompatibility;
import com.lordmau5.ffs.config.Config;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import java.text.NumberFormat;
import java.util.*;

/**
 * Created by Dustin on 28.06.2015.
 */
public class GenericUtil {

	private static List<Block> blacklistedBlocks;

	private static Map<World, ForgeChunkManager.Ticket> chunkloadTicketMap;

	public static void init() {
		blacklistedBlocks = new ArrayList<>();

		blacklistedBlocks.add(Blocks.GRASS);
		blacklistedBlocks.add(Blocks.DIRT);
		blacklistedBlocks.add(Blocks.BEDROCK);
		blacklistedBlocks.add(Blocks.SPONGE);

		chunkloadTicketMap = new HashMap<>();
	}

	public static String getUniquePositionName(AbstractTankValve valve) {
		return "tile_" + Long.toHexString(valve.getPos().toLong());
	}

	public static boolean isBlockGlass(IBlockState blockState) {
		if(blockState == null || blockState.getMaterial() == Material.AIR) {
			return false;
		}

		if(blockState.getBlock() instanceof BlockGlass) {
			return true;
		}

		ItemStack is = new ItemStack(blockState.getBlock(), 1);
		return blockState.getMaterial() == Material.GLASS && !is.getUnlocalizedName().contains("pane");

	}

	public static EnumFacing getInsideForTankFrame(TreeMap<Integer, List<LayerBlockPos>> airBlocks, BlockPos frame) {
		for(EnumFacing facing : EnumFacing.VALUES) {
			for(int layer : airBlocks.keySet()) {
				if(airBlocks.get(layer).contains(frame.offset(facing))) {
					return facing;
				}
			}
		}
		return null;
	}

	public static boolean areTankBlocksValid(IBlockState bottomBlock, World world, BlockPos bottomPos, EnumFacing facing) {
		return isValidTankBlock(world, bottomPos, bottomBlock, facing);
	}

	public static boolean isValidTankBlock(World world, BlockPos pos, IBlockState state, EnumFacing facing) {
		if(state == null) {
			return false;
		}

		if(world.isAirBlock(pos)) {
			return false;
		}

		if(state.getBlock() instanceof BlockFalling) {
			return false;
		}

		if(Compatibility.INSTANCE.isCNBLoaded) {
			if(CNBAPIAccess.apiInstance.isBlockChiseled(world, pos)) {
				return facing != null && CNBCompatibility.INSTANCE.isValid(world, pos, facing);
			}
		}

		return isBlockGlass(state) || facing == null || world.isSideSolid(pos, facing);
	}

	public static boolean isFluidContainer(ItemStack playerItem) {
		return FluidUtil.getFluidHandler(playerItem) != null;
	}

	public static boolean fluidContainerHandler(World world, AbstractTankValve valve, EntityPlayer player) {
		if(world.isRemote) {
			return true;
		}

		ItemStack current = player.getHeldItemMainhand();

		if(current != null) {
			if(!isFluidContainer(current)) {
				return false;
			}

			IFluidHandler fluidHandler = FluidUtil.getFluidHandler(current);
			for(IFluidTankProperties prop : fluidHandler.getTankProperties()) {
				if(prop.getContents() != null) { // Got something
					FluidStack content = prop.getContents().copy();
					content.amount = Math.max(content.amount, 1000);

					int qty = valve.fillFromContainer(prop.getContents(), false);
					if(qty != 0) {
						if(valve.getTankConfig().getFluidStack() != null) {
							if(prop.canDrainFluidType(valve.getTankConfig().getFluidStack()) && prop.getContents().amount >= 1000) {
								fluidHandler.drain(1000, !player.isCreative());
								valve.fillFromContainer(content, true);
								valve.markForUpdateNow();
							}
						}
						else {
							if(prop.canDrain() && prop.getContents().amount >= 1000) {
								fluidHandler.drain(1000, !player.isCreative());
								valve.fillFromContainer(content, true);
								valve.markForUpdateNow();
							}
						}
					}
				}
				else { // Got nothing
					if(valve.getTankConfig().getFluidStack() != null) {
						if(prop.canFillFluidType(valve.getTankConfig().getFluidStack()) && valve.getTankConfig().getFluidAmount() >= 1000) {
							FluidStack content = valve.getTankConfig().getFluidStack().copy();
							content.amount = 1000;

							fluidHandler.fill(content, !player.isCreative());
							valve.getTankConfig().getFluidTank().drain(content.amount, true);
							valve.markForUpdateNow();
						}
					}
				}
			}
			return false;
		}
		return false;
	}

	public static String intToFancyNumber(int number) {
		return NumberFormat.getIntegerInstance(Locale.ENGLISH).format(number);
	}

	public static void sendMessageToClient(EntityPlayer player, String message) {
		if(player == null) {
			return;
		}

		player.sendMessage(new TextComponentString(message));
	}

	public static void initChunkLoadTicket(World world, ForgeChunkManager.Ticket ticket) {
		chunkloadTicketMap.put(world, ticket);
	}

	public static ForgeChunkManager.Ticket getChunkLoadTicket(World world) {
		if(chunkloadTicketMap.containsKey(world)) {
			return chunkloadTicketMap.get(world);
		}

		ForgeChunkManager.Ticket chunkloadTicket = ForgeChunkManager.requestTicket(FancyFluidStorage.INSTANCE, world, ForgeChunkManager.Type.NORMAL);
		chunkloadTicketMap.put(world, chunkloadTicket);
		return chunkloadTicket;
	}

	public static double calculateEnergyLoss() {
		return (100 - Config.METAPHASED_FLUX_ENERGY_LOSS) / 100d;
	}

}
