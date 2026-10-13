package br.veilchapter.entity;

import br.veilchapter.config.VeilConfig;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Rare, limited-life cave pursuer. No griefing, mining, forced teleports, or
 * persistent fights. Bright light and broken sight can end a pursuit.
 */
public final class EntityDragged extends EntityCreature {
    private static final DataParameter<Byte> MODE =
        EntityDataManager.createKey(EntityDragged.class,DataSerializers.BYTE);
    private int nextStrike;
    private int unseenTicks;
    public EntityDragged(World world){
        super(world);
        setSize(.78F,1.25F);
        experienceValue=0;
    }
    @Override protected void entityInit(){
        super.entityInit();
        dataManager.register(MODE,(byte)0);
    }
    public int getMode(){return dataManager.get(MODE);}
    private void mode(int i){if(getMode()!=i)dataManager.set(MODE,(byte)i);}
    @Override protected void initEntityAI(){}
    @Override protected void applyEntityAttributes(){
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(18);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.22D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(28D);
    }
    @Override public void onLivingUpdate(){
        super.onLivingUpdate();
        if(world.isRemote)return;
        if(ticksExisted>330){setDead();return;}
        if(nextStrike>0)nextStrike--;
        EntityPlayer target=world.getClosestPlayerToEntity(this,30);
        if(target==null || target.isDead || target.dimension!=dimension) {
            if(++unseenTicks>45)setDead();
            return;
        }
        double dist=getDistance(target);
        // High illumination or leaving the enclosed cave breaks its interest.
        if(world.getLight(target.getPosition())>=12 || world.canSeeSky(target.getPosition())){
            mode(2);
            getNavigator().clearPath();
            if(++unseenTicks>28)setDead();
            return;
        }
        if(dist>26){
            if(++unseenTicks>55)setDead();
            return;
        }
        if(ticksExisted%16==0){
            if(getEntitySenses().canSee(target)){
                unseenTicks=0;
                mode(1);
                getNavigator().tryMoveToEntityLiving(target,.62D);
            }else{
                unseenTicks+=16;
                if(unseenTicks>95){mode(2);getNavigator().clearPath();setDead();return;}
            }
        }
        if(dist<1.65 && getMode()==1 && nextStrike==0){
            if(target.attackEntityFrom(DamageSource.causeMobDamage(this),
                    (float)VeilConfig.draggedAttackDamage)){
                world.playSound(null,posX,posY,posZ,
                    net.minecraft.init.SoundEvents.ENTITY_ZOMBIE_STEP,
                    SoundCategory.HOSTILE,.38F,.58F);
            }
            nextStrike=42;
        }
    }
    @Override public boolean attackEntityFrom(DamageSource source,float amount){
        if(!world.isRemote){setDead();return true;}
        return false;
    }
    @Override protected net.minecraft.util.SoundEvent getAmbientSound(){return null;}
    @Override protected net.minecraft.util.SoundEvent getHurtSound(DamageSource source){return null;}
    @Override protected net.minecraft.util.SoundEvent getDeathSound(){return null;}
    @Override protected void dropFewItems(boolean hit,int looting){}
    @Override public void readEntityFromNBT(NBTTagCompound n){
        super.readEntityFromNBT(n);
        mode(n.getInteger("veilMode"));
    }
    @Override public void writeEntityToNBT(NBTTagCompound n){
        super.writeEntityToNBT(n);
        n.setInteger("veilMode",getMode());
    }
}
