package br.veilchapter.client;

import br.veilchapter.entity.EntityDragged;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public final class GeoDraggedRenderer extends GeoEntityRenderer<EntityDragged> {
    public GeoDraggedRenderer(RenderManager manager){
        super(manager,new GeoDraggedModel());
        shadowSize=.08F;
    }
}
