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
import com.lordmau5.ffs.util.TankManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

@Mod(FancyFluidStorage.MODID)
public class FancyFluidStorage {
    public static final String MODID = "ffs";
    public static final TankManager TANK_MANAGER = new TankManager();

    // Registers
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static FancyFluidStorage INSTANCE;

    public static final CommonProxy proxy = DistExecutor.safeRunForDist(
        () -> ClientProxy::new,
        () -> CommonProxy::new
    );

    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab(-1, MODID) {
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
        BLOCK_ENTITIES.register(bus);

        Sounds.registerAll();
        SOUND_EVENTS.register(bus);

        proxy.registerEvents(bus);

        GenericUtil.init();

        context.registerConfig(ModConfig.Type.SERVER, Config.walkClass(ServerConfig.class, bus));
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        LevelAccessor iWorld = event.getWorld();

        if (iWorld.isClientSide() || !(iWorld instanceof Level) ) {
            return;
        }

        Level world = (Level) iWorld;

        FancyFluidStorage.TANK_MANAGER.removeAllForDimension(world);
    }
}
