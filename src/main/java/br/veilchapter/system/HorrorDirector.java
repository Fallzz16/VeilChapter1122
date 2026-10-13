package br.veilchapter.system;

import br.veilchapter.config.VeilConfig;
import br.veilchapter.state.StoryData;
import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Single scheduling point for ambient entity appearances.
 * Does not replace the existing anomaly/audio systems yet.
 */
public final class HorrorDirector {
    private final PresenceSystem watcher;
    private final TenantEncounterSystem tenant=new TenantEncounterSystem();
    private final EchoEncounterSystem echo=new EchoEncounterSystem();
    private final DraggedEncounterSystem dragged=new DraggedEncounterSystem();
    public HorrorDirector(PresenceSystem watcher){this.watcher=watcher;}
    public void tick(EntityPlayerMP player,StoryData story,long now) {
        if(player.dimension!=0 || !story.clock.active() ||
                story.clock.completed() || story.awakeningPending)return;
        if(VeilConfig.enableWatcher)watcher.ambientTick(player,story,now);
        if(VeilConfig.enableTenant)tenant.tick(player,story,now);
        if(VeilConfig.enableEcho)echo.tick(player,story,now);
        if(VeilConfig.enableDragged)dragged.tick(player,story,now);
    }
    public boolean debugTenant(EntityPlayerMP player,StoryData story){
        return tenant.debugSpawn(player,story);
    }
    public boolean debugEcho(EntityPlayerMP p){return echo.spawn(p);}
    public boolean debugDragged(EntityPlayerMP p){return dragged.spawn(p);}
    public void cleanup(EntityPlayerMP p){
        tenant.cleanup(p);
        echo.cleanup(p);
        dragged.cleanup(p);
    }
}
