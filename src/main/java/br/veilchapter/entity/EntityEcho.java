package br.veilchapter.entity;

import br.veilchapter.sound.VeilSounds;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;

/**
 * Nonphysical, spatial audio apparition. No model, loot, collision or attacks.
 * Each instance forms one short sequence and then removes itself.
 */
public final class EntityEcho extends Entity {
    public EntityEcho(World world) {
        super(world);
        setSize(.2F,.2F);
        noClip=true;
    }
    @Override protected void entityInit(){}
    @Override public void onUpdate(){
        super.onUpdate();
        if(world.isRemote)return;
        if(ticksExisted==2) world.playSound(null,posX,posY,posZ,
                VeilSounds.PSST,SoundCategory.AMBIENT,.48F,.93F);
        if(ticksExisted==43) world.playSound(null,posX,posY,posZ,
                net.minecraft.init.SoundEvents.BLOCK_STONE_HIT,
                SoundCategory.AMBIENT,.31F,.64F);
        if(ticksExisted==81) world.playSound(null,posX,posY,posZ,
                VeilSounds.BREATH,SoundCategory.AMBIENT,.43F,.88F);
        if(ticksExisted>108)setDead();
    }
    @Override protected void readEntityFromNBT(NBTTagCompound tag){}
    @Override protected void writeEntityToNBT(NBTTagCompound tag){}
    @Override public boolean canBeCollidedWith(){return false;}
    @Override public boolean canBePushed(){return false;}
}
