package br.veilchapter.client;

import br.veilchapter.entity.EntityWatcher;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/** Independent GeckoLib3 skeleton, sprite atlas and animation graph. */
public final class GeoWatcherModel extends AnimatedGeoModel<EntityWatcher> {
    private static final ResourceLocation GEO=new ResourceLocation("veilchapter","geo/watcher.geo.json");
    
    private static final ResourceLocation TEX=new ResourceLocation("veilchapter","textures/entity/watcher_geo.png");
    private static final ResourceLocation ANIM=new ResourceLocation("veilchapter","animations/watcher.animation.json");
    @Override public ResourceLocation getModelLocation(EntityWatcher entity){
        return GEO;
    }
    @Override public ResourceLocation getTextureLocation(EntityWatcher entity){return TEX;}
    @Override public ResourceLocation getAnimationFileLocation(EntityWatcher entity){return ANIM;}
}
