package br.veilchapter.client;

import br.veilchapter.entity.EntityTenant;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public final class GeoTenantRenderer extends GeoEntityRenderer<EntityTenant> {
    public GeoTenantRenderer(RenderManager manager){
        super(manager,new GeoTenantModel());
        shadowSize=0F;
    }
}
