package br.veilchapter.client;

import br.veilchapter.entity.EntityPeeker;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

/** Original 3D bone animation renderer, preserving depth testing. */
public final class GeoPeekerRenderer extends GeoEntityRenderer<EntityPeeker> {
    public GeoPeekerRenderer(RenderManager manager){
        super(manager,new GeoPeekerModel());
        shadowSize=0F;
    }
}
