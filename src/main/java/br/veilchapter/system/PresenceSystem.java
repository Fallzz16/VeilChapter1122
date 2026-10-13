package br.veilchapter.system;

import br.veilchapter.entity.EntityWatcher;
import br.veilchapter.entity.EntityPeeker;
import br.veilchapter.config.VeilConfig;
import br.veilchapter.core.ProximityGate;
import br.veilchapter.state.StoryData;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/** Event-driven watcher with no attack AI. Observation is approximate server-side view cone. */
public final class PresenceSystem {
    private EntityWatcher watcher;
    private long firstSeen=-1;
    private long expiry;
    private long lastSightDuration=30;
    private final Random rng=new Random();
    private final Map<UUID,ProximityGate> gates=new HashMap<>();
    private long nextDoorAction;
    private final Map<UUID,Long> nextHorizon=new HashMap<>();
    private final Map<UUID,Long> nextPeeking=new HashMap<>();
    private final Map<UUID,Long> pendingSleep=new HashMap<>();
    private final Map<UUID,EntityWatcher> bedWatchers=new HashMap<>();

    public void tick(EntityPlayerMP p, StoryData d, long tick) {
        if(tick%200==0) {
            for(net.minecraft.entity.Entity e:p.world.getEntitiesWithinAABB(EntityWatcher.class,
                 p.getEntityBoundingBox().grow(100))) {
                if(e!=watcher && !e.getEntityData().getBoolean("veilBedWatcher") &&
                        !e.getEntityData().getBoolean("veilRitual"))e.setDead();
            }
        }
        if (watcher==null || watcher.isDead || watcher.dimension!=p.dimension || watcher.world!=p.world) {
            watcher=null;firstSeen=-1;return;
        }
        if (tick>expiry) {vanish();return;}
        if (seen(p,watcher)) {
            if (firstSeen<0) {firstSeen=tick;lastSightDuration=d.clock.completed()?60+rng.nextInt(61):45+rng.nextInt(36);}
            if (tick-firstSeen>=lastSightDuration) {vanish();return;}
        } else {
            if (rng.nextInt(160)==0 && p.getDistance(watcher)<25) tryHiddenStep(p,watcher);
        }
        // Once seen, disappearance countdown persists even when the player turns away.
        if(firstSeen>=0 && tick-firstSeen>=lastSightDuration){vanish();return;}
        ProximityGate gate=gates.computeIfAbsent(p.getUniqueID(),id->new ProximityGate());
        if (gate.update(p.getDistance(watcher))) {
            p.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS,100,0,false,false));
            if (rng.nextBoolean()) tryHiddenStep(p,watcher);
        }
        if (d.clock.completed() && firstSeen<0 && tick>nextDoorAction && rng.nextInt(100)<3) {
            tryDoor(p,watcher);
            nextDoorAction=tick+140+rng.nextInt(320);
        }
    }
    /** Extra, throttled encounters: day 2+; unrelated to ordinary anomaly scheduler. */
    public void ambientTick(EntityPlayerMP p,StoryData d,long now) {
        if(!d.clock.active() || d.clock.day()<2 || d.clock.completed() ||
                p.dimension!=0 || p.isPlayerSleeping())return;
        UUID id=p.getUniqueID();
        if(VeilConfig.enableDistantWatcher) {
            Long next=nextHorizon.get(id);
            if(next==null)nextHorizon.put(id,now+(60+rng.nextInt(40))*20L);
            else if(now>=next) {
                if(watcher==null || watcher.isDead)attemptAppearance(p,d);
                int interval=d.clock.day()<4?100+rng.nextInt(75):58+rng.nextInt(65);
                nextHorizon.put(id,now+interval*20L);
            }
        }
        if(VeilConfig.enablePeeking && d.clock.day()>=3) {
            Long next=nextPeeking.get(id);
            if(next==null)nextPeeking.put(id,now+(65+rng.nextInt(55))*20L);
            else if(now>=next) {
                tryPeeking(p);
                nextPeeking.put(id,now+(80+rng.nextInt(95))*20L);
            }
        }
    }

    /** Two pale eyes or a partial shadow head above solid terrain/blocks. */
    private void tryPeeking(EntityPlayerMP player) {
        WorldServer w=(WorldServer)player.world;
        if(!w.getEntities(EntityPeeker.class,e->
                e.getDistanceSq(player)<50*50).isEmpty())return;
        Vec3d look=player.getLookVec();
        double mag=Math.sqrt(look.x*look.x+look.z*look.z);
        if(mag<.1)return;
        for(int attempt=0;attempt<36;attempt++) {
            double angle=(rng.nextDouble()-.5)*1.4;
            int distance=9+rng.nextInt(17);
            double fx=look.x/mag,fz=look.z/mag;
            double bx=fx*Math.cos(angle)-fz*Math.sin(angle);
            double bz=fx*Math.sin(angle)+fz*Math.cos(angle);
            BlockPos hint=player.getPosition().add(bx*distance,0,bz*distance);
            if(!w.isBlockLoaded(hint,false))continue;
            BlockPos cover=new BlockPos(hint.getX(),player.getPosition().getY()+1,hint.getZ());
            if(!w.isBlockLoaded(cover,false))continue;
            if(!w.getBlockState(cover).isFullCube()) {
                cover=w.getHeight(hint).down();
                if(cover.getY()<=player.getPosition().getY())continue;
            }
            if(cover.getY()>player.getPosition().getY()+3 ||
                !w.getBlockState(cover).isFullCube() || !w.isAirBlock(cover.up()) ||
                w.getTileEntity(cover)!=null)continue;
            Vec3d eyes=new Vec3d(cover.getX()+.5,cover.getY()+1.37,cover.getZ()+.5);
            if(!seenPoint(player,eyes))continue;
            EntityPeeker e=new EntityPeeker(w);
            e.setPosition(cover.getX()+.5,cover.getY()+1.09,cover.getZ()+.5);
            e.setEyesOnly(rng.nextInt(100)<48);
            if(w.spawnEntity(e))return;
        }
    }

    /** A chance on each bed attempt, but the watcher will never force sleep. */
    public void scheduleBedWatcher(EntityPlayerMP p,int day,long tick,int percent) {
        UUID id=p.getUniqueID();
        if(day<2 || pendingSleep.containsKey(id) || bedWatchers.containsKey(id))return;
        if(rng.nextInt(100)<Math.max(0,Math.min(100,percent)))
            pendingSleep.put(id,tick+20L);
    }

    /** Vanishes instantly on waking; vanilla may skip the night before this cue. */
    public void tickBedWatcher(EntityPlayerMP p,long now) {
        UUID id=p.getUniqueID();
        EntityWatcher old=bedWatchers.get(id);
        if(!p.isPlayerSleeping() || p.dimension!=0) {
            endBedWatcher(p);
            return;
        }
        if(old!=null) {
            if(old.isDead || now>=old.getEntityData().getLong("veilBedUntil"))
                endBedWatcher(p);
            return;
        }
        Long when=pendingSleep.get(id);
        if(when==null || now<when)return;
        pendingSleep.remove(id);
        WorldServer w=(WorldServer)p.world;
        for(int attempt=0;attempt<24;attempt++) {
            BlockPos hint=p.getPosition().add(rng.nextInt(19)-9,0,rng.nextInt(19)-9);
            if(!w.isBlockLoaded(hint,false))continue;
            BlockPos at=w.getHeight(hint);
            double dist=at.distanceSq(p.getPosition());
            if(dist<16 || dist>100 ||
                    !w.isAirBlock(at) || !w.isAirBlock(at.up()) ||
                    !w.getBlockState(at.down()).isFullCube())continue;
            EntityWatcher entity=new EntityWatcher(w);
            double dx=p.posX-(at.getX()+.5),dz=p.posZ-(at.getZ()+.5);
            float yaw=(float)Math.toDegrees(Math.atan2(-dx,dz));
            entity.setLocationAndAngles(at.getX()+.5,at.getY(),at.getZ()+.5,yaw,0);
            entity.getEntityData().setBoolean("veilBedWatcher",true);
            entity.getEntityData().setLong("veilBedUntil",now+75);
            if(w.spawnEntity(entity)) {
                bedWatchers.put(id,entity);
                return;
            }
        }
    }

    public void endBedWatcher(EntityPlayerMP p) {
        UUID id=p.getUniqueID();
        pendingSleep.remove(id);
        EntityWatcher e=bedWatchers.remove(id);
        if(e!=null && !e.isDead)e.setDead();
    }

    public void attemptAppearance(EntityPlayerMP p,StoryData d) {
        if (watcher!=null && !watcher.isDead) return;
        if (p.dimension!=0 || !d.clock.active()) return;
        WorldServer w=(WorldServer)p.world;
        boolean home=d.clock.day()>=4 && rng.nextInt(4)==0 && !d.bed.equals(BlockPos.ORIGIN) &&
                d.bed.distanceSq(p.getPosition()) < 60*60;
        for(int attempt=0;attempt<24;attempt++) {
            BlockPos at= home ? homePoint(w,d.bed) : distantPoint(p,w);
            if (at==null || !w.isBlockLoaded(at,false) || !w.isAirBlock(at) || !w.isAirBlock(at.up())) continue;
            if (w.isAirBlock(at.down())) continue;
            if (!home) {
                double sq=p.getDistanceSq(at.getX()+.5,at.getY(),at.getZ()+.5);
                if(sq<18*18 || sq>VeilConfig.watcherHorizonBlocks*VeilConfig.watcherHorizonBlocks)continue;
                // A distant Watcher must be in view rather than silently behind the player.
                if(!seenPoint(p,new Vec3d(at.getX()+.5,at.getY()+1.1,at.getZ()+.5)))continue;
            }
            EntityWatcher candidate=new EntityWatcher(w);
            double dx=p.posX-(at.getX()+.5),dz=p.posZ-(at.getZ()+.5);
            float yaw=(float)Math.toDegrees(Math.atan2(-dx,dz));
            candidate.setLocationAndAngles(at.getX()+.5,at.getY(),at.getZ()+.5,yaw,0);
            if (!w.getCollisionBoxes(candidate,candidate.getEntityBoundingBox()).isEmpty()) continue;
            if (w.spawnEntity(candidate)) {
                watcher=candidate; firstSeen=-1;
                expiry=w.getTotalWorldTime()+1200;
                gates.clear(); return;
            }
        }
    }
    /** Only choose existing chunks ahead of the player, within a conservative horizon. */
    private BlockPos distantPoint(EntityPlayerMP p,WorldServer w) {
        Vec3d look=p.getLookVec();
        double mag=Math.sqrt(look.x*look.x+look.z*look.z);
        if(mag<.1)return null;
        int serverView=p.getServer().getPlayerList().getViewDistance();
        int max=Math.min(VeilConfig.watcherHorizonBlocks,Math.max(19,serverView*16-12));
        int min=Math.max(16,max-16);
        int radius=min+rng.nextInt(Math.max(1,max-min+1));
        double angle=(rng.nextDouble()-.5)*.65;
        double fx=look.x/mag,fz=look.z/mag;
        double x=fx*Math.cos(angle)-fz*Math.sin(angle);
        double z=fx*Math.sin(angle)+fz*Math.cos(angle);
        BlockPos hint=p.getPosition().add(x*radius,0,z*radius);
        if(!w.isBlockLoaded(hint,false))return null;
        return w.getHeight(hint);
    }
    private BlockPos homePoint(WorldServer w,BlockPos bed) {
        BlockPos at=bed.add(rng.nextInt(23)-11,rng.nextInt(5)-2,rng.nextInt(23)-11);
        if (!w.isBlockLoaded(at,false)) return null;
        // Walkable floor + two air spaces; accepts doors and open rooms, but not through solid walls.
        return at;
    }
    private static boolean seenPoint(EntityPlayerMP p, Vec3d target) {
        Vec3d eye=p.getPositionEyes(1.0F);
        Vec3d ray=target.subtract(eye);
        double len=Math.sqrt(ray.x*ray.x+ray.y*ray.y+ray.z*ray.z);
        if (len<.01 || p.getLookVec().normalize().dotProduct(ray.normalize())<.89)return false;
        // Raytrace may treat glass as an obstruction; this is a known pre-playtest limitation.
        Vec3d dir=ray.normalize();
        for(double i=.55;i<len-.55;i+=.5) {
            Vec3d probe=eye.add(dir.scale(i));
            BlockPos block=new BlockPos(probe);
            if(!p.world.isBlockLoaded(block,false)) return false;
            if(p.world.getBlockState(block).isOpaqueCube())return false;
        }
        return true;
    }
    private static boolean seen(EntityPlayerMP p,EntityWatcher w) {
        if(p.isPotionActive(MobEffects.BLINDNESS))return false;
        return seenPoint(p,new Vec3d(w.posX,w.posY+1.1,w.posZ));
    }
    private void tryHiddenStep(EntityPlayerMP p,EntityWatcher w) {
        if (seen(p,w))return;
        for(int i=0;i<8;i++) {
            BlockPos target=new BlockPos(w.posX+rng.nextInt(7)-3,w.posY,w.posZ+rng.nextInt(7)-3);
            if (!w.world.isBlockLoaded(target,false))continue;
            if (!w.world.isAirBlock(target)||!w.world.isAirBlock(target.up())||w.world.isAirBlock(target.down()))continue;
            if (seenPoint(p,new Vec3d(target.getX()+.5,target.getY()+1.0,target.getZ()+.5)))continue;
            w.setPosition(target.getX()+.5,target.getY(),target.getZ()+.5);
            if(rng.nextInt(4)==0)w.world.playSound(null,target,
                net.minecraft.init.SoundEvents.BLOCK_GRASS_STEP,
                net.minecraft.util.SoundCategory.AMBIENT,.14F,.6F+rng.nextFloat()*.2F);
            return;
        }
    }
    private void tryDoor(EntityPlayerMP player,EntityWatcher w) {
        if(seen(player,w))return;
        WorldServer world=(WorldServer)w.world;
        BlockPos origin=w.getPosition();
        for(BlockPos p:BlockPos.getAllInBox(origin.add(-3,-1,-3),origin.add(3,2,3))) {
            if (!world.isBlockLoaded(p,false))continue;
            IBlockState state=world.getBlockState(p);
            if (state.getBlock() instanceof BlockDoor && state.getBlock()!=Blocks.IRON_DOOR &&
                    state.getValue(BlockDoor.HALF)==BlockDoor.EnumDoorHalf.LOWER) {
                BlockDoor door=(BlockDoor)state.getBlock();
                boolean open=state.getValue(BlockDoor.OPEN);
                door.toggleDoor(world,p,!open);
                return;
            }
        }
    }
    private void vanish() {
        if (watcher!=null&&!watcher.isDead) watcher.setDead();
        watcher=null;firstSeen=-1;gates.clear();
    }
}
