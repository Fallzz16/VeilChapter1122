package br.veilchapter.client;

import br.veilchapter.entity.EntityEcho;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/** Intentionally no visual model: the spatial sound source is the manifestation. */
public final class RenderEcho extends Render<EntityEcho> {
    public RenderEcho(RenderManager manager){super(manager);shadowSize=0;}
    @Override public void doRender(EntityEcho e,double x,double y,double z,float yaw,float partial){}
    @Override protected ResourceLocation getEntityTexture(EntityEcho e){return null;}
}
