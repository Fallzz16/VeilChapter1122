package br.veilchapter.entity;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
/** Passive supernatural observer: no attack AI or loot. */
public final class EntityWatcher extends EntityCreature {
    public EntityWatcher(World worldIn) { super(worldIn); setSize(.6F,1.8F); experienceValue=0; }
    @Override protected void initEntityAI() { }
    @Override protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.23);
    }
    @Override public boolean attackEntityFrom(DamageSource source, float amount) {
        if (!world.isRemote) setDead();
        return false;
    }
    @Override protected net.minecraft.util.SoundEvent getAmbientSound(){return null;}
    @Override protected net.minecraft.util.SoundEvent getHurtSound(DamageSource source){return null;}
    @Override protected net.minecraft.util.SoundEvent getDeathSound(){return null;}
    @Override protected void dropFewItems(boolean hit, int looting) { }
    @Override public boolean canBePushed(){return false;}
    @Override public void onLivingUpdate() {
        super.onLivingUpdate();
        setMotion(0,0,0);
        if(!world.isRemote && ticksExisted%8==0) {
            EntityPlayer p=world.getClosestPlayerToEntity(this,64);
            if(p!=null && !p.isDead) {
                double dx=p.posX-posX, dz=p.posZ-posZ;
                float target=(float)Math.toDegrees(Math.atan2(-dx,dz));
                float difference=net.minecraft.util.math.MathHelper.wrapDegrees(target-rotationYawHead);
                rotationYawHead+=net.minecraft.util.math.MathHelper.clamp(difference,-8F,8F);
                // Body stays eerily still while the head follows the observer.
            }
        }
    }
    private void setMotion(double x, double y, double z) { motionX=x;motionY=y;motionZ=z; }
}
