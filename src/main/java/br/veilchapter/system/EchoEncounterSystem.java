package br.veilchapter.system;

import br.veilchapter.config.VeilConfig;
import br.veilchapter.entity.EntityEcho;
import br.veilchapter.state.StoryData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/** A spatial three-cue apparition in caves; independent of combat and griefing. */
public final class EchoEncounterSystem {
    private final Random random=new Random();
    private final Map<UUID,Long> due=new HashMap<>();
    private final Map<UUID,EntityEcho> active=new HashMap<>();
    public void tick(EntityPlayerMP p,StoryData d,long now){
        if(!VeilConfig.enableEcho || p.dimension!=0 || d.clock.day()<5 ||
                !d.clock.active() || d.clock.completed() || p.isPlayerSleeping())return;
        UUID id=p.getUniqueID();
        EntityEcho existing=active.get(id);
        if(existing!=null && existing.isDead)active.remove(id);
        Long target=due.get(id);
        if(target==null){due.put(id,now+(230+random.nextInt(260))*20L);return;}
        if(now<target)return;
        due.put(id,now+(230+random.nextInt(260))*20L);
        if(active.containsKey(id) || random.nextInt(100)>=VeilConfig.echoAttemptChance)return;
        spawn(p);
    }
    public boolean spawn(EntityPlayerMP p){
        if(p.dimension!=0)return false;
        WorldServer world=(WorldServer)p.world;
        if(world.canSeeSky(p.getPosition()) || world.getLight(p.getPosition())>11)
            return false;
        if(!world.getEntities(EntityEcho.class,e->e.getDistanceSq(p)<40*40).isEmpty())
            return false;
        for(int i=0;i<36;i++){
            double a=random.nextDouble()*Math.PI*2;
            int distance=6+random.nextInt(10);
            BlockPos at=p.getPosition().add(Math.cos(a)*distance,
                    random.nextInt(5)-2,Math.sin(a)*distance);
            if(!world.isBlockLoaded(at,false) || !world.isAirBlock(at))continue;
            EntityEcho e=new EntityEcho(world);
            e.setPosition(at.getX()+.5,at.getY()+.5,at.getZ()+.5);
            if(world.spawnEntity(e)){
                active.put(p.getUniqueID(),e);
                return true;
            }
        }
        return false;
    }
    public void cleanup(EntityPlayerMP player){
        due.remove(player.getUniqueID());
        EntityEcho e=active.remove(player.getUniqueID());
        if(e!=null && !e.isDead)e.setDead();
    }
}
