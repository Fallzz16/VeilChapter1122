package br.veilchapter.system;

import br.veilchapter.config.VeilConfig;
import br.veilchapter.core.EntityEncounterRules;
import br.veilchapter.entity.EntityTenant;
import br.veilchapter.state.StoryData;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/** Sparse house-adjacent encounters, never generates terrain and never modifies blocks. */
public final class TenantEncounterSystem {
    private final Random rng=new Random();
    private final Map<UUID,Long> nextAttempt=new HashMap<>();
    private final Map<UUID,EntityTenant> active=new HashMap<>();

    public void tick(EntityPlayerMP player,StoryData story,long now) {
        if(!VeilConfig.enableTenant || player.dimension!=0 || !story.clock.active() ||
                story.clock.completed() || player.isPlayerSleeping())return;
        UUID owner=player.getUniqueID();
        EntityTenant old=active.get(owner);
        if(old!=null && (old.isDead || old.dimension!=player.dimension ||
                old.world!=player.world))active.remove(owner);
        Long due=nextAttempt.get(owner);
        if(due==null){
            nextAttempt.put(owner,EntityEncounterRules.nextAttempt(now,story.clock.day(),rng.nextInt(241)));
            return;
        }
        if(now<due)return;
        nextAttempt.put(owner,EntityEncounterRules.nextAttempt(now,story.clock.day(),rng.nextInt(241)));
        if(active.containsKey(owner) || rng.nextInt(100)>=VeilConfig.tenantAttemptChance)return;
        if(!eligible(player,story))return;
        appear(player,story);
    }
    private boolean eligible(EntityPlayerMP p,StoryData d){
        if(d.bed.equals(BlockPos.ORIGIN))return false;
        long dayTime=p.world.getWorldTime()%24000L;
        return EntityEncounterRules.tenantAllowed(d.clock.day(),d.clock.active(),
                d.clock.completed(),p.dimension==0,
                d.bed.distanceSq(p.getPosition())<35*35,
                dayTime>=12500L && dayTime<=23500L);
    }
    /** Debug bypasses day/night/cooldown but never bypasses physical collision checks. */
    public boolean debugSpawn(EntityPlayerMP player,StoryData story){
        return player.dimension==0 && appear(player,story);
    }
    private boolean appear(EntityPlayerMP player,StoryData d){
        WorldServer w=(WorldServer)player.world;
        if(!w.getEntities(EntityTenant.class,e-> e.getDistanceSq(player)<64*64).isEmpty())
            return false;
        for(int i=0;i<48;i++){
            int distance=5+rng.nextInt(16);
            double angle=rng.nextDouble()*Math.PI*2;
            BlockPos hint=player.getPosition().add(Math.cos(angle)*distance,0,
                    Math.sin(angle)*distance);
            if(!w.isBlockLoaded(hint,false))continue;
            // Prefer house-height placements, avoid sudden appearances in deep pits or on roofs.
            for(int dy=-2;dy<=3;dy++){
                BlockPos feet=hint.add(0,dy,0);
                if(!w.isBlockLoaded(feet,false) ||
                        !EntityEncounterRules.safeY(feet.getY(),3) ||
                        !EntityEncounterRules.withinHome(feet.distanceSq(player.getPosition())))
                    continue;
                if(!w.getBlockState(feet.down()).isFullCube() ||
                        w.getTileEntity(feet.down())!=null ||
                        !w.isAirBlock(feet) || !w.isAirBlock(feet.up()) ||
                        !w.isAirBlock(feet.up(2)))continue;
                EntityTenant tenant=new EntityTenant(w);
                tenant.setLocationAndAngles(feet.getX()+.5,feet.getY(),
                        feet.getZ()+.5,0,0);
                if(!w.getCollisionBoxes(tenant,tenant.getEntityBoundingBox()).isEmpty())
                    continue;
                if(w.spawnEntity(tenant)){
                    active.put(player.getUniqueID(),tenant);
                    // Extremely quiet positional cue; no mandatory jump-scare.
                    if(rng.nextInt(3)==0)w.playSound(null,feet,
                            net.minecraft.init.SoundEvents.BLOCK_WOOD_STEP,
                            SoundCategory.AMBIENT,.18F,.52F+rng.nextFloat()*.12F);
                    return true;
                }
            }
        }
        return false;
    }
    public void cleanup(EntityPlayerMP p){
        nextAttempt.remove(p.getUniqueID());
        EntityTenant old=active.remove(p.getUniqueID());
        if(old!=null && !old.isDead)old.setDead();
    }
}
