package br.veilchapter.client;
import br.veilchapter.CommonProxy;
import br.veilchapter.entity.EntityWatcher;
import br.veilchapter.entity.EntityPeeker;
import br.veilchapter.entity.EntityTenant;
import br.veilchapter.entity.EntityEcho;
import br.veilchapter.entity.EntityDragged;
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
    @Override public void preInit() {
        // Packet registration happens during VeilMod.preInit on both sides.
        RenderingRegistry.registerEntityRenderingHandler(EntityWatcher.class, new IRenderFactory<EntityWatcher>() {
            @Override public net.minecraft.client.renderer.entity.Render<? super EntityWatcher> createRenderFor(RenderManager m) {
                return new RenderLiving<EntityWatcher>(m, new ModelWatcher(), 0.0F) {
                    @Override protected ResourceLocation getEntityTexture(EntityWatcher entity) {
                        return new ResourceLocation("veilchapter", "textures/entity/watcher.png");
                    }
                };
            }
        });
        RenderingRegistry.registerEntityRenderingHandler(EntityPeeker.class,
                new IRenderFactory<EntityPeeker>() {
                    @Override public net.minecraft.client.renderer.entity.Render<? super EntityPeeker>
                    createRenderFor(RenderManager manager) {
                        return new RenderPeeker(manager);
                    }
                });
        RenderingRegistry.registerEntityRenderingHandler(EntityTenant.class, new IRenderFactory<EntityTenant>(){
            @Override public net.minecraft.client.renderer.entity.Render<? super EntityTenant> createRenderFor(RenderManager manager){
                return new GeoTenantRenderer(manager);
            }
        });
        RenderingRegistry.registerEntityRenderingHandler(EntityEcho.class, new IRenderFactory<EntityEcho>(){
            @Override public net.minecraft.client.renderer.entity.Render<? super EntityEcho> createRenderFor(RenderManager manager){
                return new RenderEcho(manager);
            }
        });
        RenderingRegistry.registerEntityRenderingHandler(EntityDragged.class, new IRenderFactory<EntityDragged>(){
            @Override public net.minecraft.client.renderer.entity.Render<? super EntityDragged> createRenderFor(RenderManager manager){
                return new GeoDraggedRenderer(manager);
            }
        });
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(new ClientEvents());
    }
}
