package br.veilchapter.client;

import br.veilchapter.entity.EntityDragged;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public final class RenderDragged extends RenderLiving<EntityDragged> {
    private static final ResourceLocation TEXTURE=
        new ResourceLocation("veilchapter","textures/entity/dragged.png");
    public RenderDragged(RenderManager manager){
        super(manager,new ModelDragged(),0.0F);
        shadowSize=.14F;
    }
    @Override protected ResourceLocation getEntityTexture(EntityDragged e){return TEXTURE;}
    @Override protected void preRenderCallback(EntityDragged e,float partial){
        GlStateManager.scale(.88F,.78F,1.1F);
    }
}
