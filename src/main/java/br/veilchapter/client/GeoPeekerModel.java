package br.veilchapter.client;

import br.veilchapter.entity.EntityPeeker;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/** Independent GeckoLib3 skeleton, sprite atlas and animation graph. */
public final class GeoPeekerModel extends AnimatedGeoModel<EntityPeeker> {
    private static final ResourceLocation GEO=new ResourceLocation("veilchapter","geo/peeker.geo.json");
    private static final ResourceLocation EYES_GEO=new ResourceLocation("veilchapter","geo/peeker_eyes.geo.json");
    private static final ResourceLocation TEX=new ResourceLocation("veilchapter","textures/entity/peeker_geo.png");
    private static final ResourceLocation ANIM=new ResourceLocation("veilchapter","animations/peeker.animation.json");
    @Override public ResourceLocation getModelLocation(EntityPeeker entity){
        return entity.isEyesOnly()?EYES_GEO:GEO;
    }
    @Override public ResourceLocation getTextureLocation(EntityPeeker entity){return TEX;}
    @Override public ResourceLocation getAnimationFileLocation(EntityPeeker entity){return ANIM;}
}
