package br.veilchapter.client;

import br.veilchapter.entity.EntityTenant;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/**
 * Authored 64x64 humanoid rig: narrow shoulders, elongated arms, thin legs,
 * tilted neck. Uses vanilla model parts so no extra runtime animation library.
 */
public final class ModelTenant extends ModelBiped {
    public ModelTenant(){
        super(0.0F,0.0F,64,64);
        bipedHead=new ModelRenderer(this,0,0);
        bipedHead.addBox(-3.5F,-8.5F,-3.0F,7,9,6,0F);
        bipedHead.setRotationPoint(0F,-1F,0F);
        bipedHeadwear=new ModelRenderer(this,0,0);
        bipedBody=new ModelRenderer(this,20,16);
        bipedBody.addBox(-3F,0F,-1.5F,6,12,3,0F);
        bipedBody.setRotationPoint(0F,0F,0F);
        bipedRightArm=new ModelRenderer(this,44,16);
        bipedRightArm.addBox(-2F,-1F,-1.5F,3,18,3,0F);
        bipedRightArm.setRotationPoint(-3.5F,1F,0F);
        bipedLeftArm=new ModelRenderer(this,44,16);
        bipedLeftArm.mirror=true;
        bipedLeftArm.addBox(-1F,-1F,-1.5F,3,18,3,0F);
        bipedLeftArm.setRotationPoint(3.5F,1F,0F);
        bipedRightLeg=new ModelRenderer(this,0,28);
        bipedRightLeg.addBox(-1.6F,0F,-1.6F,3,12,3,0F);
        bipedRightLeg.setRotationPoint(-1.5F,12F,0F);
        bipedLeftLeg=new ModelRenderer(this,0,28);
        bipedLeftLeg.mirror=true;
        bipedLeftLeg.addBox(-1.4F,0F,-1.6F,3,12,3,0F);
        bipedLeftLeg.setRotationPoint(1.5F,12F,0F);
    }
    @Override public void setRotationAngles(float limbSwing,float limbSwingAmount,float ageInTicks,
            float netHeadYaw,float headPitch,float scaleFactor,Entity entityIn){
        super.setRotationAngles(0,0,ageInTicks,netHeadYaw,headPitch,scaleFactor,entityIn);
        // Slow, barely perceptible posture shifts. Never a full-body spin.
        float breathe=MathHelper.sin(ageInTicks*.068F)*.016F;
        bipedHead.rotateAngleZ=.11F+MathHelper.sin(ageInTicks*.13F)*.022F;
        bipedHead.rotateAngleX=bipedHead.rotateAngleX*.32F+.07F;
        bipedRightArm.rotateAngleZ=.09F+breathe;
        bipedLeftArm.rotateAngleZ=-.09F-breathe;
        bipedRightArm.rotateAngleX= -.04F+MathHelper.sin(ageInTicks*.075F)*.025F;
        bipedLeftArm.rotateAngleX= .02F-MathHelper.sin(ageInTicks*.075F)*.025F;
        bipedRightLeg.rotateAngleX=0F;
        bipedLeftLeg.rotateAngleX=0F;
        if(entityIn instanceof EntityTenant && ((EntityTenant)entityIn).getPhase()==2) {
            bipedHead.rotateAngleZ=.20F;
            bipedHead.rotateAngleX=.24F;
        }
    }
}
