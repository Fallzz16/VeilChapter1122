package br.veilchapter.entity;

import net.minecraft.entity.Entity;
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
public final class EntityPeeker extends Entity implements IAnimatable {
    private final AnimationFactory animationFactory=new AnimationFactory(this);
    private static final DataParameter<Boolean> EYES_ONLY =
        EntityDataManager.createKey(EntityPeeker.class, DataSerializers.BOOLEAN);
    public EntityPeeker(World world) {
        super(world);
        setSize(.55F,.72F);
        noClip=true;
    }
    @Override protected void entityInit() { dataManager.register(EYES_ONLY,false); }
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
    @Override protected void readEntityFromNBT(NBTTagCompound n) {
        setEyesOnly(n.getBoolean("eyesOnly"));
    }
    @Override protected void writeEntityToNBT(NBTTagCompound n) {
        n.setBoolean("eyesOnly", isEyesOnly());
    }
    @Override public boolean canBeCollidedWith() { return false; }
    @Override public boolean canBePushed() { return false; }
}
