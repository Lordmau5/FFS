package com.lordmau5.ffs;

import com.lordmau5.ffs.config.ServerConfig;
import com.lordmau5.ffs.holder.Blocks;
import com.lordmau5.ffs.holder.Items;
import com.lordmau5.ffs.holder.Sounds;
import com.lordmau5.ffs.holder.TileEntities;
import com.lordmau5.ffs.proxy.ClientProxy;
import com.lordmau5.ffs.proxy.CommonProxy;
import com.lordmau5.ffs.util.Config;
import com.lordmau5.ffs.util.GenericUtil;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

@Mod(FancyFluidStorage.MOD_ID)
public class FancyFluidStorage {
    public static final String MOD_ID = "ffs";

    // Registers
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);

    public static FancyFluidStorage INSTANCE;

    public static final CommonProxy proxy = DistExecutor.safeRunForDist(
        () -> ClientProxy::new,
        () -> CommonProxy::new
    );

    public static final ItemGroup ITEM_GROUP = new ItemGroup(-1, MOD_ID) {
        @Override
        @Nonnull
        @OnlyIn(Dist.CLIENT)
        public ItemStack makeIcon() {
            return new ItemStack(Blocks.fluidValve);
        }
    };

    public FancyFluidStorage() {
        INSTANCE = this;

        final ModLoadingContext context = ModLoadingContext.get();
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        Blocks.registerAll();
        BLOCKS.register(bus);

        Items.registerAll();
        ITEMS.register(bus);

        TileEntities.registerAll();
        TILE_ENTITIES.register(bus);

        Sounds.registerAll();
        SOUND_EVENTS.register(bus);

        proxy.registerEvents(bus);

        GenericUtil.init();

        context.registerConfig(ModConfig.Type.SERVER, Config.walkClass(ServerConfig.class, bus));
    }
}
