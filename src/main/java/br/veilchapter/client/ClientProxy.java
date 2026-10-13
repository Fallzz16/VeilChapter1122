package br.veilchapter.client;
import br.veilchapter.CommonProxy;
import br.veilchapter.entity.EntityWatcher;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
public final class ClientProxy extends CommonProxy {
    @Override public void acceptDayState(final br.veilchapter.network.DayStatePacket packet) {
        net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
            ClientDayState.day=packet.day;
            ClientDayState.active=packet.active;
            ClientDayState.completed=packet.completed;
        });
    }
    @Override public void acceptCinematicState(final br.veilchapter.network.CinematicStatePacket packet) {
        net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
            ClientCinematicState.locked=packet.locked;
        });
    }
    @Override public void preInit() {
        // Packet registration happens during VeilMod.preInit on both sides.
        RenderingRegistry.registerEntityRenderingHandler(EntityWatcher.class, new IRenderFactory<EntityWatcher>() {
            @Override public net.minecraft.client.renderer.entity.Render<? super EntityWatcher> createRenderFor(RenderManager m) {
                return new RenderLiving<EntityWatcher>(m, new ModelPlayer(0.0F, false), 0.0F) {
                    @Override protected ResourceLocation getEntityTexture(EntityWatcher entity) {
                        return new ResourceLocation("veilchapter", "textures/entity/watcher.png");
                    }
                };
            }
        });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }
}
