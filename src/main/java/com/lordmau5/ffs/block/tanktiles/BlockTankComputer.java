package com.lordmau5.ffs.block.tanktiles;

import com.lordmau5.ffs.FancyFluidStorage;
import com.lordmau5.ffs.tile.abstracts.AbstractTankTile;
import com.lordmau5.ffs.tile.abstracts.AbstractTankValve;
import com.lordmau5.ffs.tile.tanktiles.TileEntityTankComputer;
import com.lordmau5.ffs.util.FFSStateProps;
import com.lordmau5.ffs.util.GenericUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Created by Dustin on 28.06.2015.
 */
public class BlockTankComputer extends Block
{

    public BlockTankComputer()
    {
        super(Material.IRON);
        setUnlocalizedName(FancyFluidStorage.MODID + ".block_tank_computer");
        setRegistryName("block_tank_computer");
        setCreativeTab(CreativeTabs.REDSTONE);
        setHardness(5.0F); // Same hardness as an iron block
        setResistance(10.0F); // Same as hardness

        setDefaultState(blockState.getBaseState()
                .withProperty(FFSStateProps.TILE_VALID, false));
    }

    @Override
    public boolean hasTileEntity(IBlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state)
    {
        return new TileEntityTankComputer();
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile != null && tile instanceof AbstractTankTile && ((AbstractTankTile) tile).getMasterValve() != null)
        {
            ((AbstractTankTile) tile).getMasterValve().breakTank();
        }
        super.onBlockDestroyedByExplosion(world, pos, explosion);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (!world.isRemote && tile != null && tile instanceof AbstractTankTile && ((AbstractTankTile) tile).getMasterValve() != null)
        {
            ((AbstractTankTile) tile).getMasterValve().breakTank();
        }

        super.breakBlock(world, pos, state);
    }


    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (player.isSneaking()) return false;

        AbstractTankTile tile = (AbstractTankTile) world.getTileEntity(pos);
        if (tile != null && tile.getMasterValve() != null)
        {
            AbstractTankValve valve = tile.getMasterValve();
            if (GenericUtil.isFluidContainer(player.getHeldItemMainhand()))
            {
                return GenericUtil.fluidContainerHandler(world, valve, player);
            }

            player.openGui(FancyFluidStorage.INSTANCE, 0, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FFSStateProps.TILE_VALID);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
    {
        TileEntity tile = world.getTileEntity(pos);
        if (tile != null && tile instanceof TileEntityTankComputer)
        {
            TileEntityTankComputer valve = (TileEntityTankComputer) tile;

            state = state.withProperty(FFSStateProps.TILE_VALID, valve.isValid());
        }
        return state;
    }


    @Override
    public int getMetaFromState(IBlockState state)
    {
        return 0;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side)
    {
        IBlockState otherState = worldIn.getBlockState(pos.offset(side));
        return otherState != getBlockState();
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type)
    {
        return false;
    }
}
