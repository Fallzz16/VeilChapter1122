package br.veilchapter.client;

import br.veilchapter.entity.EntityDragged;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * Uneven, hunched crawler: shoulder box, asymmetric long forearms, trailing legs.
 * Entirely geometric and procedural for dependable Forge 1.12.2 rendering.
 */
public final class ModelDragged extends ModelBiped {
    public ModelDragged() {
        super(0F,0F,64,64);
        bipedHead=new ModelRenderer(this,0,0);
        bipedHead.addBox(-4F,-8F,-3.7F,8,8,7,0);
        bipedHead.setRotationPoint(0F,1F,-1F);
        bipedHeadwear=new ModelRenderer(this,0,0);
        bipedBody=new ModelRenderer(this,22,16);
        bipedBody.addBox(-4F,-1F,-2F,8,12,4,0);
        bipedBody.setRotationPoint(0F,0F,0F);
        bipedRightArm=new ModelRenderer(this,44,16);
        bipedRightArm.addBox(-2F,-2F,-1.6F,4,18,3,0);
        bipedRightArm.setRotationPoint(-4.1F,2F,-1F);
        bipedLeftArm=new ModelRenderer(this,44,16);
        bipedLeftArm.mirror=true;
        bipedLeftArm.addBox(-1.3F,-1F,-1.6F,3,16,3,0);
        bipedLeftArm.setRotationPoint(4F,2F,-1F);
        bipedRightLeg=new ModelRenderer(this,0,30);
        bipedRightLeg.addBox(-1.7F,0F,-1.7F,4,13,4,0);
        bipedRightLeg.setRotationPoint(-2F,11F,2F);
        bipedLeftLeg=new ModelRenderer(this,0,30);
        bipedLeftLeg.mirror=true;
        bipedLeftLeg.addBox(-1.7F,0F,-1.7F,4,13,4,0);
        bipedLeftLeg.setRotationPoint(2F,11F,2F);
    }
    @Override public void setRotationAngles(float swing,float amount,float ticks,
            float headYaw,float headPitch,float scale,Entity entity){
        super.setRotationAngles(swing,amount,ticks,headYaw,headPitch,scale,entity);
        float crawl=MathHelper.sin(ticks*.39F)*(entity instanceof EntityDragged &&
                ((EntityDragged)entity).getMode()==1?.45F:.09F);
        bipedBody.rotateAngleX=.84F;
        bipedHead.rotateAngleX=-.30F;
        bipedHead.rotateAngleZ=.09F;
        bipedRightArm.rotateAngleX=-1.08F+crawl;
        bipedLeftArm.rotateAngleX=-1.05F-crawl;
        bipedRightArm.rotateAngleZ=-.13F;
        bipedLeftArm.rotateAngleZ=.19F;
        bipedRightLeg.rotateAngleX=.5F-crawl*.4F;
        bipedLeftLeg.rotateAngleX=.54F+crawl*.4F;
    }
}
