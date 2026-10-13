package br.veilchapter.client;

import br.veilchapter.entity.EntityTenant;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/** A hand-authored humanoid silhouette with subtle procedural animation. */
public final class RenderTenant extends RenderLiving<EntityTenant> {
    private static final ResourceLocation TEXTURE=
        new ResourceLocation("veilchapter","textures/entity/tenant.png");
    public RenderTenant(RenderManager manager){
        super(manager,new ModelTenant(),0.0F);
        shadowSize=0;
    }
    @Override protected ResourceLocation getEntityTexture(EntityTenant entity){return TEXTURE;}
    @Override protected void preRenderCallback(EntityTenant entity,float partialTickTime){
        GlStateManager.scale(.92F,1.28F,.92F);
    }
}
