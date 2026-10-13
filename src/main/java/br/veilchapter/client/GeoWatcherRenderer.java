package br.veilchapter.client;

import br.veilchapter.entity.EntityWatcher;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

/** Original 3D bone animation renderer, preserving depth testing. */
public final class GeoWatcherRenderer extends GeoEntityRenderer<EntityWatcher> {
    public GeoWatcherRenderer(RenderManager manager){
        super(manager,new GeoWatcherModel());
        shadowSize=0F;
    }
}
