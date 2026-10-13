package br.veilchapter;

import br.veilchapter.entity.EntityWatcher;
import br.veilchapter.world.ClearingProvider;
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
     acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:forge@[14.23.5.2859,)")
public final class VeilMod {
    public static final String ID = "veilchapter";
    public static final String NAME = "Veil Chapter - Psychological Horror";
    public static final String VERSION = "0.2.8-tree30-atmos";
    public static final int CLEARING_DIM = 713;
    public static Logger LOG;
    public static net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper NETWORK;
    @Mod.Instance(ID) public static VeilMod INSTANCE;
    @SidedProxy(clientSide="br.veilchapter.client.ClientProxy", serverSide="br.veilchapter.CommonProxy")
    public static CommonProxy proxy;
    @Mod.EventHandler public void preInit(FMLPreInitializationEvent event) {
        LOG = event.getModLog();
        NETWORK=net.minecraftforge.fml.common.network.NetworkRegistry.INSTANCE.newSimpleChannel(ID);
        // Register the packet mapping on BOTH physical sides, not only in ClientProxy.
        NETWORK.registerMessage(br.veilchapter.network.DayStateHandler.class,
                br.veilchapter.network.DayStatePacket.class, 0,
                net.minecraftforge.fml.relauncher.Side.CLIENT);
        EntityRegistry.registerModEntity(new ResourceLocation(ID, "watcher"), EntityWatcher.class,
                "watcher", 0, this, 96, 3, false);
        if (!DimensionManager.isDimensionRegistered(CLEARING_DIM)) {
            DimensionType clearing = DimensionType.register("Veil Clearing", "_veil", CLEARING_DIM,
                    ClearingProvider.class, false);
            DimensionManager.registerDimension(CLEARING_DIM, clearing);
        } else {
            LOG.error("Dimensao {} ja pertence a outro mod. Evento da arvore desabilitado.", CLEARING_DIM);
            GameEvents.dimensionUnavailable = true;
        }
        MinecraftForge.EVENT_BUS.register(new GameEvents());
        proxy.preInit();
    }
    @Mod.EventHandler public void serverStarting(FMLServerStartingEvent event) {
        if(Boolean.getBoolean("veilchapter.ciTreePlan"))
            br.veilchapter.world.TreeScene.verifyPlanForCI();
        event.registerServerCommand(new VeilCommands());
        event.registerServerCommand(new VeilDebugCommands());
    }
}
