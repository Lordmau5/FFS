// package com.lordmau5.ffs.compat.top;

// import com.lordmau5.ffs.FancyFluidStorage;
// import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
// import mcjty.theoneprobe.api.IProbeHitData;
// import mcjty.theoneprobe.api.IProbeInfo;
// import mcjty.theoneprobe.api.IProbeInfoProvider;
// import mcjty.theoneprobe.api.ProbeMode;
// import mcjty.theoneprobe.apiimpl.ProbeConfig;
// import mcjty.theoneprobe.apiimpl.elements.ElementProgress;
// import mcjty.theoneprobe.config.Config;
// import net.minecraft.world.level.block.state.BlockState;
// import net.minecraft.world.entity.player.Player;
// import net.minecraft.network.chat.TextComponent;
// import net.minecraft.ChatFormatting;
// import net.minecraft.network.chat.TranslatableComponent;
// import net.minecraft.world.level.Level;
// import net.minecraftforge.fluids.FluidStack;

// public class TankInfoProvider implements IProbeInfoProvider {
//     @Override
//     public String getID() {
//         return FancyFluidStorage.MODID;
//     }

//     @Override
//     public void addProbeInfo(ProbeMode probeMode, IProbeInfo iProbeInfo, Player playerEntity, Level world, BlockState blockState, IProbeHitData iProbeHitData) {
//         AbstractTankValve valve = null;
//         if ( FancyFluidStorage.TANK_MANAGER.isPartOfTank(world, iProbeHitData.getPos()) ) {
//             valve = FancyFluidStorage.TANK_MANAGER.getValveForBlock(world, iProbeHitData.getPos());
//         }

//         if ( valve != null ) {
//             IProbeInfo vert = iProbeInfo.vertical();
//             vert.text(
//                     (new TranslatableComponent("top.ffs.part_of_tank"))
//                     .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
//             );
//             addFluidInfo(vert, Config.getDefaultConfig(), valve.getTankConfig().getFluidStack(), valve.getTankConfig().getFluidCapacity());
//         }
//     }

//     private void addFluidInfo(IProbeInfo probeInfo, ProbeConfig config, FluidStack fluidStack, int maxContents) {
//         int contents = fluidStack == FluidStack.EMPTY ? 0 : fluidStack.getAmount();
//         if ( fluidStack != FluidStack.EMPTY ) {
//             probeInfo.text(new TranslatableComponent("top.ffs.fluid", fluidStack.getDisplayName()));
//         }
//         if ( config.getTankMode() == 1 ) {
//             probeInfo.progress(contents, maxContents, probeInfo.defaultProgressStyle().suffix("mB").filledColor(Config.tankbarFilledColor).alternateFilledColor(Config.tankbarAlternateFilledColor)
//                     .borderColor(Config.tankbarBorderColor).numberFormat(Config.tankFormat.get()));
//         } else {
//             probeInfo.text(
//                 (new TextComponent(ElementProgress.format(contents, Config.tankFormat.get(), "mB")))
//                 .withStyle(ChatFormatting.GREEN)
//             );
//         }
//     }
// }
