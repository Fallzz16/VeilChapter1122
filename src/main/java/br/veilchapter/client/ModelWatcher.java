package br.veilchapter.client;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/** Preserves the original Steve-derived skin, with a nonstandard motionless pose. */
public final class ModelWatcher extends ModelPlayer {
    public ModelWatcher(){super(0.0F,false);}
    @Override public void setRotationAngles(float swing,float amount,float time,
            float headYaw,float headPitch,float scale,Entity entity){
        super.setRotationAngles(0,0,time,headYaw,headPitch,scale,entity);
        bipedRightArm.rotateAngleX=-.032F;
        bipedLeftArm.rotateAngleX=.025F;
        bipedRightArm.rotateAngleZ=-.045F;
        bipedLeftArm.rotateAngleZ=.045F;
        bipedRightLeg.rotateAngleX=0;
        bipedLeftLeg.rotateAngleX=0;
        bipedHead.rotateAngleZ=MathHelper.sin(time*.053F)*.013F;
        bipedHeadwear.rotateAngleZ=bipedHead.rotateAngleZ;
    }
}
