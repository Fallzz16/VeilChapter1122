package br.veilchapter.client;

import br.veilchapter.entity.EntityDragged;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/** Authored 128x128 atlas and multi-bone Blockbench-compatible resource rig. */
public final class GeoDraggedModel extends AnimatedGeoModel<EntityDragged> {
    private static final ResourceLocation GEO=new ResourceLocation("veilchapter","geo/dragged.geo.json");
    private static final ResourceLocation TEX=new ResourceLocation("veilchapter","textures/entity/dragged_geo.png");
    private static final ResourceLocation ANIM=new ResourceLocation("veilchapter","animations/dragged.animation.json");
    @Override public ResourceLocation getModelLocation(EntityDragged entity){return GEO;}
    @Override public ResourceLocation getTextureLocation(EntityDragged entity){return TEX;}
    @Override public ResourceLocation getAnimationFileLocation(EntityDragged entity){return ANIM;}
}
