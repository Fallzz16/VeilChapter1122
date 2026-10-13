package br.veilchapter;

import br.veilchapter.config.VeilConfig;
import br.veilchapter.state.StoryData;
import br.veilchapter.system.AnomalySystem;
import br.veilchapter.system.DreadSystem;
import br.veilchapter.system.PresenceSystem;
import br.veilchapter.world.TreeScene;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.Random;

/** Minecraft-facing event coordinator. Work is throttled and only on the server. */
public final class GameEvents {
    public static boolean dimensionUnavailable;
    private final PresenceSystem presence = new PresenceSystem();
    private final AnomalySystem anomalies = new AnomalySystem();
    private final DreadSystem dread = new DreadSystem();
    private final TreeScene tree = new TreeScene();
    private long lastLogicTick = -1;
    private final Random random = new Random();

    /** Restart the full dialogue if a player logs back in during the sealed cinematic. */
    @SubscribeEvent public void loggedIn(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent ev) {
        if (!(ev.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP p=(EntityPlayerMP)ev.player;
        if(p.dimension!=VeilMod.CLEARING_DIM || dimensionUnavailable)return;
        StoryData d=StoryData.get(p.world);
        if(d.treeActive && d.treeStage==1){
            d.treeStarted=0;d.treeWatcherFlags=0;d.markDirty();
        }
    }
    /** Schedule a sleep apparition, no forced sleep/wake or changes to the game's rules. */
    @SubscribeEvent public void goingToBed(PlayerSleepInBedEvent ev) {
        if(!(ev.getEntityPlayer() instanceof EntityPlayerMP))return;
        EntityPlayerMP p=(EntityPlayerMP)ev.getEntityPlayer();
        if(p.world.isRemote || p.dimension!=0 || !VeilConfig.enableBedWatcher ||
                !VeilConfig.enableWatcher)return;
        StoryData d=StoryData.get(p.world);
        if(d.clock.active() && !d.clock.completed() && d.clock.day()>=2)
            presence.scheduleBedWatcher(p, d.clock.day(), p.world.getTotalWorldTime(),
                    VeilConfig.bedWatcherChance);
    }
    @SubscribeEvent public void wake(PlayerWakeUpEvent ev) {
        if (!(ev.getEntityPlayer() instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) ev.getEntityPlayer();
        if (player.dimension != 0 || player.world.isRemote) return;
        StoryData d = StoryData.get(player.world);
        presence.endBedWatcher(player);
        if (!d.clock.active()) {
            d.clock.awaken(Math.floorDiv(player.world.getWorldTime(),24000L));
            d.awakeningPending = true;
            d.awakeningStartedTick = player.world.getTotalWorldTime();
            d.waitUntil = player.world.getTotalWorldTime() + 60;
            d.markDirty();
        }
        BlockPos b = player.getBedLocation(0);
        if (b != null && !b.equals(d.bed)) {d.bed = b; d.markDirty();}
    }
    @SubscribeEvent public void tick(TickEvent.PlayerTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || !(ev.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP p = (EntityPlayerMP) ev.player;
        if (p.world.isRemote) return;
        if (p.dimension == VeilMod.CLEARING_DIM && !dimensionUnavailable) {
            tree.tick(p, StoryData.get(p.world));
            return;
        }
        if (p.dimension != 0) return;
        WorldServer world = (WorldServer) p.world;
        StoryData d = StoryData.get(world);
        long now = world.getTotalWorldTime();
        if(VeilConfig.enableWatcher) {
            presence.tickBedWatcher(p,now);
            if(now%20==0 && d.clock.active() && !d.awakeningPending &&
                    !d.clock.completed() && !d.treeActive)presence.ambientTick(p,d,now);
        }
        if (lastLogicTick == now) return; // Singleplayer first, guard duplicate player ticks.
        lastLogicTick = now;
        if(now%40==0 && VeilMod.NETWORK!=null)VeilMod.NETWORK.sendTo(
           new br.veilchapter.network.DayStatePacket(d),p);
        if (d.awakeningPending) {
            if (now > d.awakeningStartedTick + 60) {
                // Normalize after sleep has changed world daytime.
                d.clock.restore(2,Math.floorDiv(world.getWorldTime(),24000L),true,false);
                d.awakeningPending = false; d.markDirty();
            }
        } else if (d.clock.active() && !d.clock.completed() && now % 20 == 0) {
            int before=d.clock.day();
            d.clock.tickCalendar(Math.floorDiv(world.getWorldTime(),24000L));
            if (before != d.clock.day()) d.markDirty();
        }
        if (d.treeActive && p.dimension == 0 && !dimensionUnavailable && VeilConfig.enableTreeSequence) {
            tree.enter(p,d); return;
        }
        if (d.bookPending && !d.treeActive && p.dimension == 0) tree.deliverBook(p,d);
        if (!d.clock.active() || d.awakeningPending) return;
        if (now%20==0 && VeilConfig.enableAnomalies) dread.audioTick(p,d,now);
        if (now%5==0 && VeilConfig.enableWatcher) presence.tick(p,d,now);
        if(now%5==0 && d.clock.day()>=2 && !d.clock.completed()) {
            int checked=0;
            for(EntityZombie zombie:world.getEntitiesWithinAABB(EntityZombie.class,
                    p.getEntityBoundingBox().grow(36))) {
                if(checked++>32)break;
                if(zombie.getEntityData().getBoolean("veilPassive")) {
                    zombie.setAttackTarget(null);
                    zombie.setRevengeTarget(null);
                }
            }
        }
        if(now%5==0 && !d.managedFires.isEmpty())dread.containFire(world,d,now);
        if (now%40==0) anomalies.cleanup(world,d,now);
        if (d.clock.day() >= 6 && !d.clock.completed() && now%100==0 && VeilConfig.enableDaySix)
            dread.tick(p,d,now);
        if (d.clock.completed() && now%100==0) dread.restore(world,d);
        if (VeilConfig.enableAnomalies && now>=d.nextEvent && !d.treeActive) {
            if (d.nextEvent==0) { d.nextEvent=now+150*20L;d.markDirty();return; }
            int day=d.clock.day();
            int choice=random.nextInt(100);
            if (choice<28 && VeilConfig.enableWatcher) presence.attemptAppearance(p,d);
            else if (!d.clock.completed() && choice<60) anomalies.run(p,d,random);
            else if (!d.clock.completed()) dread.playUnsettlingSound(p,day,random);
            else if (VeilConfig.enableWatcher) presence.attemptAppearance(p,d);
            int minSec=day<4?360:day<6?240:180;
            int maxSec=day<4?780:day<6?540:420;
            if (d.clock.completed()) {minSec=160;maxSec=400;}
            d.nextEvent=now+(minSec+random.nextInt(maxSec-minSec+1))*20L;
            d.markDirty();
        }
    }
    @SubscribeEvent public void attacked(LivingAttackEvent ev) {
        if (!(ev.getEntityLiving() instanceof EntityPlayerMP)) {
            return;
        }
        EntityPlayerMP p=(EntityPlayerMP)ev.getEntityLiving();
        if (p.world.isRemote) return;
        if (!dimensionUnavailable && p.dimension==VeilMod.CLEARING_DIM) {ev.setCanceled(true);return;}
        if (p.dimension!=0) return;
        StoryData d=StoryData.get(p.world);
        if (ev.getSource().getTrueSource() instanceof EntityZombie) {
            EntityZombie z=(EntityZombie)ev.getSource().getTrueSource();
            if (z.getEntityData().getBoolean("veilPassive") && !d.clock.completed()) {
                ev.setCanceled(true);return;
            }
        }
        if (VeilConfig.enableTreeSequence && !dimensionUnavailable && d.clock.active() && d.clock.day()>=7
                && !d.treeTriggerConsumed && !d.clock.completed() && TreeScene.onSurface(p)) {
            if (tree.request(p,d)) ev.setCanceled(true);
        }
    }
    @SubscribeEvent public void zombieSpawn(EntityJoinWorldEvent ev) {
        if(ev.getWorld().isRemote)return;
        if(ev.getWorld().provider.getDimension()==VeilMod.CLEARING_DIM && !dimensionUnavailable &&
                ev.getEntity() instanceof net.minecraft.entity.EntityLiving) {
            // The clearing only admits marked Watchers that belong to the scripted ritual.
            if(!(ev.getEntity() instanceof br.veilchapter.entity.EntityWatcher &&
                    ev.getEntity().getEntityData().getBoolean("veilRitual")))
                ev.setCanceled(true);
            return;
        }
        if (ev.getWorld().provider.getDimension()!=0) return;
        StoryData d=StoryData.get(ev.getWorld());
        if(d.clock.completed() && ev.getEntity().getEntityData().getBoolean("veilExtra")) {
            ev.setCanceled(true);return;
        }
        if (!(ev.getEntity() instanceof EntityZombie)) return;
        if (d.clock.active() && d.clock.day()>=2 && !d.clock.completed()) {
            EntityZombie zombie=(EntityZombie)ev.getEntity();
            if (zombie.getEntityData().getBoolean("veilExtra")) {
                zombie.getEntityData().setBoolean("veilPassive",false);
            } else if (!zombie.getEntityData().hasKey("veilPassive")) {
                zombie.getEntityData().setBoolean("veilPassive",zombie.getUniqueID().getLeastSignificantBits()%2==0);
            }
        }
    }
    @SubscribeEvent public void breakSpeed(PlayerEvent.BreakSpeed ev) {
        if (!VeilConfig.enableAnomalies || ev.getEntityPlayer().world.isRemote) return;
        if (ev.getEntityPlayer().dimension!=0) return;
        StoryData d=StoryData.get(ev.getEntityPlayer().world);
        if (d.clock.day()<4 || d.clock.completed()) return;
        anomalies.adjustBreakSpeed(ev,ev.getEntityPlayer().world.getTotalWorldTime());
    }
    @SubscribeEvent public void breakBlock(BlockEvent.BreakEvent ev) {
        int dim=ev.getWorld().provider.getDimension();
        if (!dimensionUnavailable && dim==VeilMod.CLEARING_DIM) {ev.setCanceled(true); return;}
        if (dim==0 && !ev.getWorld().isRemote) {
            anomalies.onPlayerBreak(ev.getPos(),StoryData.get((World) ev.getWorld()));
        }
    }
    @SubscribeEvent public void placeBlock(BlockEvent.PlaceEvent ev) {
        if (!dimensionUnavailable && ev.getWorld().provider.getDimension()==VeilMod.CLEARING_DIM) { ev.setCanceled(true); return; }
        if (ev.getWorld().provider.getDimension()==0 && !ev.getWorld().isRemote) {
            // A replacement block belongs to the player, never to our temporary anomaly.
            anomalies.onPlayerBreak(ev.getPos(), StoryData.get((World) ev.getWorld()));
        }
    }
    @SubscribeEvent public void interaction(PlayerInteractEvent.RightClickBlock ev) {
        if (!(ev.getEntityPlayer() instanceof EntityPlayerMP)) return;
        EntityPlayerMP p=(EntityPlayerMP)ev.getEntityPlayer();
        if (p.dimension!=VeilMod.CLEARING_DIM || dimensionUnavailable) return;
        ev.setCanceled(true);
        tree.openChest(p,ev.getPos(),StoryData.get(p.world));
    }
    @SubscribeEvent public void useItem(PlayerInteractEvent.RightClickItem ev) {
        if (!dimensionUnavailable && ev.getEntityPlayer().dimension==VeilMod.CLEARING_DIM) ev.setCanceled(true);
    }
    @SubscribeEvent public void toss(ItemTossEvent ev) {
        if (!dimensionUnavailable && ev.getPlayer().dimension==VeilMod.CLEARING_DIM) ev.setCanceled(true);
    }
    @SubscribeEvent public void spawn(LivingSpawnEvent.CheckSpawn ev) {
        if (!dimensionUnavailable && ev.getWorld().provider.getDimension()==VeilMod.CLEARING_DIM &&
                !(ev.getEntityLiving() instanceof br.veilchapter.entity.EntityWatcher &&
                  ev.getEntityLiving().getEntityData().getBoolean("veilRitual")))
            ev.setResult(Event.Result.DENY);
        if (ev.getWorld().provider.getDimension()==0 && !ev.getWorld().isRemote &&
           ev.getEntityLiving() instanceof net.minecraft.entity.passive.EntityAnimal) {
            StoryData d=StoryData.get((World)ev.getWorld());
            if (d.clock.day()>=6&&!d.clock.completed() && random.nextInt(100)<88)
                ev.setResult(Event.Result.DENY);
        }
    }
}
