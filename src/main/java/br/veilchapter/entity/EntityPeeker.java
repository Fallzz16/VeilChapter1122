package br.veilchapter.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.world.World;

/** Temporary, passive apparition: never attacks, moves, drops items or causes collision. */
public final class EntityPeeker extends Entity {
    private static final DataParameter<Boolean> EYES_ONLY =
        EntityDataManager.createKey(EntityPeeker.class, DataSerializers.BOOLEAN);
    public EntityPeeker(World world) {
        super(world);
        setSize(.55F,.72F);
        noClip=true;
    }
    @Override protected void entityInit() { dataManager.register(EYES_ONLY,false); }
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
