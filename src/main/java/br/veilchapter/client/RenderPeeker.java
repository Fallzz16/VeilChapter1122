package br.veilchapter.client;

import br.veilchapter.entity.EntityPeeker;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Depth-tested Minecraft-sized shadow/head and two pale eyes over terrain edges. */
public final class RenderPeeker extends Render<EntityPeeker> {
    private static final ResourceLocation TEXTURE =
        new ResourceLocation("veilchapter","textures/entity/watcher.png");
    public RenderPeeker(RenderManager manager) { super(manager); shadowSize=0F; }
    @Override protected ResourceLocation getEntityTexture(EntityPeeker entity) {return TEXTURE;}
    @Override public void doRender(EntityPeeker entity,double x,double y,double z,
                                   float entityYaw,float partialTicks) {
        GlStateManager.pushMatrix();
        // A short physical emergence, with a subtle retreat near the end.
        float age=entity.ticksExisted+partialTicks;
        float emerge=Math.min(1F,age/9F);
        float retreat=Math.min(1F,Math.max(0F,age-48F)/12F);
        GlStateManager.translate(x,y-.22F*(1F-emerge)-.22F*retreat,z);
        // Keep the face toward the observer; terrain depth test hides the lower half.
        GlStateManager.rotate(180F-renderManager.playerViewY,0F,1F,0F);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        try {
            Tessellator tess=Tessellator.getInstance();
            BufferBuilder buf=tess.getBuffer();
            buf.begin(GL11.GL_QUADS,DefaultVertexFormats.POSITION_COLOR);
            if (!entity.isEyesOnly()) {
                quad(buf,-.32,-.24,.32,.48,0,2,2,4);
                quad(buf,-.31,.16,.31,.46,.007,3,3,6);
            }
            // Dull eyes, not shiny particles: unsettling when briefly noticed.
            quad(buf,-.23,.12,-.10,.24,.035,169,173,162);
            quad(buf,.10,.12,.23,.24,.035,169,173,162);
            quad(buf,-.17,.14,-.13,.20,.043,29,31,30);
            quad(buf,.16,.14,.20,.20,.043,29,31,30);
            tess.draw();
        } finally {
            GlStateManager.enableCull();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
        }
    }
    private static void quad(BufferBuilder buf,double x0,double y0,double x1,double y1,
                             double z,int red,int green,int blue) {
        buf.pos(x0,y0,z).color(red,green,blue,255).endVertex();
        buf.pos(x1,y0,z).color(red,green,blue,255).endVertex();
        buf.pos(x1,y1,z).color(red,green,blue,255).endVertex();
        buf.pos(x0,y1,z).color(red,green,blue,255).endVertex();
    }
}
