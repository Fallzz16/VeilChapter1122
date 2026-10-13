package br.veilchapter.entity;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

/** Temporary, passive apparition: never attacks, moves, drops items or causes collision. */
public final class EntityPeeker extends EntityCreature implements IAnimatable {
    private final AnimationFactory animationFactory=new AnimationFactory(this);
    private static final DataParameter<Boolean> EYES_ONLY =
        EntityDataManager.createKey(EntityPeeker.class, DataSerializers.BOOLEAN);
    public EntityPeeker(World world) {
        super(world);
        setSize(.55F,.72F);
        noClip=true;
        setNoGravity(true);
        experienceValue=0;
    }
    @Override protected void entityInit() { super.entityInit();dataManager.register(EYES_ONLY,false); }
    @Override protected void initEntityAI(){}
    @Override protected void applyEntityAttributes(){
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1.0);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0);
    }
    @Override public AnimationFactory getFactory(){return animationFactory;}
    @Override public void registerControllers(AnimationData data){
        data.addAnimationController(new AnimationController<>(this,"peeker_hiding",4,event->{
            String clip=ticksExisted<13?"animation.peeker.emerge":
                ticksExisted>=47?"animation.peeker.retract":"animation.peeker.hold";
            boolean loop=clip.equals("animation.peeker.hold");
            event.getController().setAnimation(new AnimationBuilder().addAnimation(clip,loop));
            return PlayState.CONTINUE;
        }));
    }
    public void setEyesOnly(boolean value) { dataManager.set(EYES_ONLY,value); }
    public boolean isEyesOnly() { return dataManager.get(EYES_ONLY); }
    @Override public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote && ticksExisted >= 60) setDead();
    }
    @Override public void readEntityFromNBT(NBTTagCompound n) {
        super.readEntityFromNBT(n);
        setEyesOnly(n.getBoolean("eyesOnly"));
    }
    @Override public void writeEntityToNBT(NBTTagCompound n) {
        super.writeEntityToNBT(n);
        n.setBoolean("eyesOnly", isEyesOnly());
    }
    @Override public void onLivingUpdate(){
        super.onLivingUpdate();
        motionX=motionY=motionZ=0;
    }
    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean canBePushed() { return false; }
}
