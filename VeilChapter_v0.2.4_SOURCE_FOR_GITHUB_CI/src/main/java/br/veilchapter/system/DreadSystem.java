package br.veilchapter.system;

import br.veilchapter.config.VeilConfig;
import br.veilchapter.state.StoryData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import java.util.List;
import java.util.Iterator;
import java.util.Random;

/** World deterioration. Reversible entity stasis is disabled by default until in-game validation. */
public final class DreadSystem {
    private final Random random=new Random();
    public void tick(EntityPlayerMP p,StoryData d,long now) {
        if (d.clock.day()<6 || d.clock.completed())return;
        WorldServer world=(WorldServer)p.world;
        if (VeilConfig.enableEntityStasis && now%200==0) hideLife(world,p,d);
        if (now%3600<100) spawnNearBed(world,p,d);
        if (VeilConfig.allowWorldDamage && now%600==0 && random.nextInt(5)==0) tryFire(world,p,d);
    }
    private void hideLife(WorldServer w,EntityPlayerMP p,StoryData d) {
        if (d.suspendedMobs.size()>320)return;
        AxisAlignedBB region=p.getEntityBoundingBox().grow(36,22,36);
        List<Entity> entities=w.getEntitiesWithinAABBExcludingEntity(p,region);
        int count=0;
        for(Entity e:entities) {
            if (count>=6) break;
            if(!(e instanceof EntityAnimal || e instanceof EntityVillager))continue;
            if(e.hasCustomName())continue; // Protect named animals while system remains experimental.
            if(!w.isBlockLoaded(e.getPosition(),false))continue;
            NBTTagCompound saved=new NBTTagCompound();
            if(!e.writeToNBTOptional(saved))continue;
            d.suspendedMobs.add(saved);
            e.setDead();count++;
        }
        if(count>0)d.markDirty();
    }
    public void restore(WorldServer w,StoryData d) {
        for (Iterator<NBTTagCompound> it=d.suspendedMobs.iterator();it.hasNext();) {
            NBTTagCompound saved=it.next();
            BlockPos pos=new BlockPos(saved.getDouble("PosX"),saved.getDouble("PosY"),saved.getDouble("PosZ"));
            // Entity 'Pos' data is a list; use it when direct coordinates are not present.
            if(saved.hasKey("Pos",9)){
                net.minecraft.nbt.NBTTagList coords=saved.getTagList("Pos",6);
                if(coords.tagCount()>=3)pos=new BlockPos(coords.getDoubleAt(0),coords.getDoubleAt(1),coords.getDoubleAt(2));
            }
            if(!w.isBlockLoaded(pos,false))continue;
            Entity clone=EntityList.createEntityFromNBT(saved,w);
            if(clone!=null && w.spawnEntity(clone)){it.remove();d.markDirty();}
        }
        // Removal of explicitly tagged extra mobs, never normal or third-party ones.
        for(Entity e: w.loadedEntityList) if (e.getEntityData().getBoolean("veilExtra")) e.setDead();
    }
    private void spawnNearBed(WorldServer w,EntityPlayerMP player,StoryData d) {
        if (d.bed.equals(BlockPos.ORIGIN) || d.bed.distanceSq(player.getPosition())>100*100)return;
        int existing=0;
        for(Entity e:w.loadedEntityList)if(e.getEntityData().getBoolean("veilExtra"))existing++;
        if(existing>=6)return;
        for(int i=0;i<16;i++) {
            BlockPos hint=d.bed.add(random.nextInt(45)-22,0,random.nextInt(45)-22);
            if(!w.isBlockLoaded(hint,false))continue;
            BlockPos at=w.getHeight(hint);
            if(!w.isAirBlock(at)||!w.isAirBlock(at.up())||!w.getBlockState(at.down()).isFullCube())continue;
            if(at.distanceSq(player.getPosition())<9*9)continue;
            EntityZombie z=new EntityZombie(w);
            z.setPosition(at.getX()+.5,at.getY(),at.getZ()+.5);
            if(!w.getCollisionBoxes(z,z.getEntityBoundingBox()).isEmpty())continue;
            z.getEntityData().setBoolean("veilExtra",true);
            w.spawnEntity(z);return;
        }
    }
    private void tryFire(WorldServer w,EntityPlayerMP p,StoryData d) {
        if(d.managedFires.size()>=3)return;
        // Avoid accidental spread. Fires can only start on isolated trees, 50+ blocks from bed.
        for(int i=0;i<22;i++) {
            BlockPos hint=p.getPosition().add(random.nextInt(77)-38,0,random.nextInt(77)-38);
            if(!w.isBlockLoaded(hint,false)||hint.distanceSq(d.bed)<50*50)continue;
            BlockPos top=w.getHeight(hint);
            BlockPos below=top.down();
            if((w.getBlockState(below).getBlock()!=Blocks.LEAVES && w.getBlockState(below).getBlock()!=Blocks.LEAVES2)
                || !w.isAirBlock(top))continue;
            // Real isolated fire, only after explicit opt-in. No global gamerule change.
            w.setBlockState(top,Blocks.FIRE.getDefaultState(),3);
            NBTTagCompound site=new NBTTagCompound();
            site.setInteger("x",top.getX());site.setInteger("y",top.getY());site.setInteger("z",top.getZ());
            site.setLong("until",w.getTotalWorldTime()+100);
            d.managedFires.add(site);d.markDirty();return;
        }
    }
    /** Limit mod-ignited flames to a small area and stop them after 5 seconds, even after reloading. */
    public void containFire(WorldServer w,StoryData d,long now) {
        for(Iterator<NBTTagCompound> it=d.managedFires.iterator();it.hasNext();) {
            NBTTagCompound site=it.next();
            BlockPos center=new BlockPos(site.getInteger("x"),site.getInteger("y"),site.getInteger("z"));
            if(!w.isBlockLoaded(center,false))continue;
            boolean expire=now>=site.getLong("until") || d.clock.completed();
            // Never erase another mod's/player's fire in a 11x11x11 cube.
            // Fire spread cannot safely be attributed to this source using BlockPos alone;
            // experimental real fires stay disabled by default pending in-game containment tests.
            if(expire){
                if(w.getBlockState(center).getBlock()==Blocks.FIRE)w.setBlockToAir(center);
                it.remove();d.markDirty();
            }
        }
    }
    public void playUnsettlingSound(EntityPlayerMP p,int day,Random rng) {
        WorldServer w=(WorldServer)p.world;
        BlockPos pos=p.getPosition().add(rng.nextInt(29)-14,rng.nextInt(7)-3,rng.nextInt(29)-14);
        if(day>=4 && rng.nextInt(4)==0) {
            // Illusory door: audio alone, does not change any block.
            w.playSound(null,pos, rng.nextBoolean()?SoundEvents.BLOCK_WOODEN_DOOR_OPEN:SoundEvents.BLOCK_WOODEN_DOOR_CLOSE,
                    SoundCategory.AMBIENT,.35F,.8F+rng.nextFloat()*.35F);
        } else if(day>=4 && rng.nextInt(5)==0) {
            w.playSound(null,pos,SoundEvents.BLOCK_WOOD_STEP,SoundCategory.AMBIENT,
                    .18F,.65F+rng.nextFloat()*.2F);
        } else {
            w.playSound(null,pos, br.veilchapter.sound.VeilSounds.PSST,SoundCategory.AMBIENT,
                    .25F+rng.nextFloat()*.2F,.82F+rng.nextFloat()*.35F);
        }
    }
}
