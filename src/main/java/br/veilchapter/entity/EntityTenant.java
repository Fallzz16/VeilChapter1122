package br.veilchapter.entity;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * The Tenant: unnatural stillness, slight head turns, and a deliberate retreat.
 * Strictly non-hostile in the initial entity pack. No loot, attack AI or griefing.
 */
public final class EntityTenant extends EntityCreature {
    private static final DataParameter<Byte> PHASE =
        EntityDataManager.createKey(EntityTenant.class,DataSerializers.BYTE);
    private int seenTicks;
    public EntityTenant(World world) {
        super(world);
        setSize(.52F,2.34F);
        experienceValue=0;
        enablePersistence();
    }
    @Override protected void entityInit() {
        super.entityInit();
        dataManager.register(PHASE,(byte)0);
    }
    public int getPhase(){return dataManager.get(PHASE);}
    private void phase(int v){if(getPhase()!=v)dataManager.set(PHASE,(byte)v);}
    @Override protected void initEntityAI() {}
    @Override protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12.0);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.06);
    }
    @Override public void onLivingUpdate() {
        super.onLivingUpdate();
        motionX=motionY=motionZ=0;
        if(world.isRemote)return;
        if(ticksExisted>240) {setDead();return;}
        EntityPlayer p=world.getClosestPlayerToEntity(this,32);
        if(p==null || p.isDead || p.dimension!=dimension) {setDead();return;}
        if(getDistance(p)<3.1) {setDead();return;}
        double x=p.posX-posX,z=p.posZ-posZ;
        rotationYaw=(float)Math.toDegrees(Math.atan2(-x,z));
        rotationYawHead=rotationYaw;
        renderYawOffset=rotationYaw;
        if(ticksExisted%4==0) {
            Vec3d eye=p.getPositionEyes(1.0F);
            Vec3d target=new Vec3d(posX,posY+1.6,posZ);
            Vec3d delta=target.subtract(eye);
            double dist=delta.lengthVector();
            boolean facing=dist>0.01 && p.getLookVec().normalize().dotProduct(
                    delta.normalize())>.965;
            boolean occluded=world.rayTraceBlocks(eye,target,false,true,false)!=null;
            if(facing && !occluded)seenTicks+=4;
            else seenTicks=Math.max(0,seenTicks-4);
            if(seenTicks>=20)phase(1);
            if(seenTicks>=62)phase(2);
        }
        if(getPhase()==2 && seenTicks>=80)setDead();
    }
    @Override public boolean attackEntityFrom(DamageSource source,float damage){
        if(!world.isRemote)setDead();
        return false;
    }
    @Override protected net.minecraft.util.SoundEvent getAmbientSound(){return null;}
    @Override protected net.minecraft.util.SoundEvent getHurtSound(DamageSource source){return null;}
    @Override protected net.minecraft.util.SoundEvent getDeathSound(){return null;}
    @Override protected void dropFewItems(boolean wasRecentlyHit,int lootingModifier){}
    @Override public boolean canBePushed(){return false;}
    @Override protected void readEntityFromNBT(NBTTagCompound compound){
        super.readEntityFromNBT(compound);
        phase(compound.getInteger("veilPhase"));
    }
    @Override protected void writeEntityToNBT(NBTTagCompound compound){
        super.writeEntityToNBT(compound);
        compound.setInteger("veilPhase",getPhase());
    }
}
