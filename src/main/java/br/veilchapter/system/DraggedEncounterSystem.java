package br.veilchapter.system;

import br.veilchapter.config.VeilConfig;
import br.veilchapter.entity.EntityDragged;
import br.veilchapter.state.StoryData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/** Very rare cave-only physical encounter. No spawn in unloaded chunks. */
public final class DraggedEncounterSystem {
    private final Random random=new Random();
    private final Map<UUID,Long> due=new HashMap<>();
    private final Map<UUID,EntityDragged> active=new HashMap<>();
    public void tick(EntityPlayerMP p,StoryData d,long now){
        if(!VeilConfig.enableDragged || p.dimension!=0 || d.clock.day()<6 ||
                d.clock.completed() || !d.clock.active() || p.isPlayerSleeping())return;
        UUID id=p.getUniqueID();
        EntityDragged old=active.get(id);
        if(old!=null && old.isDead)active.remove(id);
        Long target=due.get(id);
        if(target==null){due.put(id,now+(350+random.nextInt(380))*20L);return;}
        if(now<target)return;
        due.put(id,now+(350+random.nextInt(380))*20L);
        if(active.containsKey(id) || random.nextInt(100)>=VeilConfig.draggedAttemptChance)return;
        spawn(p);
    }
    public boolean spawn(EntityPlayerMP p){
        if(p.dimension!=0)return false;
        WorldServer w=(WorldServer)p.world;
        if(w.canSeeSky(p.getPosition()) || w.getLight(p.getPosition())>=12)return false;
        if(!w.getEntities(EntityDragged.class,e->e.getDistanceSq(p)<48*48).isEmpty())
            return false;
        for(int i=0;i<64;i++){
            double angle=random.nextDouble()*Math.PI*2;
            int dist=8+random.nextInt(13);
            BlockPos hint=p.getPosition().add(Math.cos(angle)*dist,0,Math.sin(angle)*dist);
            for(int dy=-2;dy<=3;dy++){
                BlockPos at=hint.add(0,dy,0);
                if(!w.isBlockLoaded(at,false) || at.getY()<5 || at.getY()>245 ||
                        !w.getBlockState(at.down()).isFullCube() ||
                        !w.isAirBlock(at) || !w.isAirBlock(at.up()) ||
                        w.getLight(at)>11)continue;
                EntityDragged e=new EntityDragged(w);
                e.setLocationAndAngles(at.getX()+.5,at.getY(),at.getZ()+.5,
                    random.nextFloat()*360F,0);
                if(!w.getCollisionBoxes(e,e.getEntityBoundingBox()).isEmpty())continue;
                if(w.spawnEntity(e)){
                    active.put(p.getUniqueID(),e);
                    return true;
                }
            }
        }
        return false;
    }
    public void cleanup(EntityPlayerMP p){
        due.remove(p.getUniqueID());
        EntityDragged e=active.remove(p.getUniqueID());
        if(e!=null && !e.isDead)e.setDead();
    }
}
