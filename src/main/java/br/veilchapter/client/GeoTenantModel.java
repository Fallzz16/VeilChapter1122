package br.veilchapter.client;

import br.veilchapter.entity.EntityTenant;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/** Authored 128x128 atlas and multi-bone Blockbench-compatible resource rig. */
public final class GeoTenantModel extends AnimatedGeoModel<EntityTenant> {
    private static final ResourceLocation GEO=new ResourceLocation("veilchapter","geo/tenant.geo.json");
    private static final ResourceLocation TEX=new ResourceLocation("veilchapter","textures/entity/tenant_geo.png");
    private static final ResourceLocation ANIM=new ResourceLocation("veilchapter","animations/tenant.animation.json");
    @Override public ResourceLocation getModelLocation(EntityTenant entity){return GEO;}
    @Override public ResourceLocation getTextureLocation(EntityTenant entity){return TEX;}
    @Override public ResourceLocation getAnimationFileLocation(EntityTenant entity){return ANIM;}
}
