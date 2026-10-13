package br.veilchapter;

import br.veilchapter.entity.EntityWatcher;
import br.veilchapter.entity.EntityPeeker;
import br.veilchapter.entity.EntityTenant;
import br.veilchapter.entity.EntityEcho;
import br.veilchapter.entity.EntityDragged;
import br.veilchapter.world.ClearingProvider; // used only for legacy save rescue
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.world.DimensionType;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = VeilMod.ID, name = VeilMod.NAME, version = VeilMod.VERSION,
     acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:forge@[14.23.5.2859,);required-after:geckolib3")
public final class VeilMod {
    public static final String ID = "veilchapter";
    public static final String NAME = "Veil Chapter - Psychological Horror";
    public static final String VERSION = "0.3.1-geckolib-art-dev";
    public static final int LEGACY_CLEARING_DIM = 713;
    public static Logger LOG;
    public static net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper NETWORK;
    @Mod.Instance(ID) public static VeilMod INSTANCE;
    @SidedProxy(clientSide="br.veilchapter.client.ClientProxy", serverSide="br.veilchapter.CommonProxy")
    public static CommonProxy proxy;
    @Mod.EventHandler public void preInit(FMLPreInitializationEvent event) {
        software.bernie.geckolib3.GeckoLib.initialize();
        LOG = event.getModLog();
        NETWORK=net.minecraftforge.fml.common.network.NetworkRegistry.INSTANCE.newSimpleChannel(ID);
        // Register the packet mapping on BOTH physical sides, not only in ClientProxy.
        NETWORK.registerMessage(br.veilchapter.network.DayStateHandler.class,
                br.veilchapter.network.DayStatePacket.class, 0,
                net.minecraftforge.fml.relauncher.Side.CLIENT);
        EntityRegistry.registerModEntity(new ResourceLocation(ID, "watcher"), EntityWatcher.class,
                "watcher", 0, this, 96, 3, false);
        EntityRegistry.registerModEntity(new ResourceLocation(ID, "peeker"), EntityPeeker.class,
                "peeker", 1, this, 80, 5, false);
        // ID=2 reserved for the new domestic entity. Existing worlds retain watcher=0, peeker=1.
        EntityRegistry.registerModEntity(new ResourceLocation(ID, "tenant"), EntityTenant.class,
                "tenant", 2, this, 80, 5, false);
        EntityRegistry.registerModEntity(new ResourceLocation(ID, "echo"), EntityEcho.class,
                "echo", 3, this, 72, 10, false);
        EntityRegistry.registerModEntity(new ResourceLocation(ID, "dragged"), EntityDragged.class,
                "dragged", 4, this, 72, 3, false);
        if (!DimensionManager.isDimensionRegistered(LEGACY_CLEARING_DIM)) {
            DimensionType clearing = DimensionType.register("Veil Clearing", "_veil", LEGACY_CLEARING_DIM,
                    ClearingProvider.class, false);
            DimensionManager.registerDimension(LEGACY_CLEARING_DIM, clearing);
        } else {
            LOG.warn("Dimensao legada 713 pertence a outro mod. Recuperacao de saves antigos inativa.");
            GameEvents.legacyDimensionUnavailable = true;
        }
        MinecraftForge.EVENT_BUS.register(new GameEvents());
        proxy.preInit();
    }
    @Mod.EventHandler public void serverStarting(FMLServerStartingEvent event) {
        LOG.info("VEIL_NO_TREE_CHAPTER1_READY: no tree trigger, no ritual, no forced ending.");
        event.registerServerCommand(new VeilCommands());
        event.registerServerCommand(new VeilDebugCommands());
    }
}
