package br.veilchapter.client;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.client.event.InputUpdateEvent;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
/** Conservative visual gloom; survival simulation separately increases hostile events. */
public final class ClientEvents {
    private ClearingAmbientSound clearingAmbient;
    private boolean playedThisVisit;

    @SubscribeEvent public void fog(EntityViewRenderEvent.FogColors event) {
        Minecraft mc = Minecraft.getMinecraft();
        if(mc.world == null || mc.player == null) return;
        if(mc.player.dimension == br.veilchapter.VeilMod.CLEARING_DIM) {
            // Pale, melancholic clearing: color-only tint, no camera motion,
            // no expensive shader and no thick fog obscuring the complete tree.
            event.setRed(event.getRed()*.85F);
            event.setGreen(event.getGreen()*.88F);
            event.setBlue(event.getBlue()*.92F);
            return;
        }
        if(mc.player.dimension != 0)return;
        if(ClientDayState.active && !ClientDayState.completed && ClientDayState.day>=6) {
            // Atmospheric-only sky/fog attenuation. Actual skylight simulation is NOT implemented.
            event.setRed(event.getRed()*.62F);
            event.setGreen(event.getGreen()*.62F);
            event.setBlue(event.getBlue()*.68F);
        }
    }
    @SubscribeEvent public void tick(TickEvent.ClientTickEvent event) {
        if(event.phase != TickEvent.Phase.END) return;
        Minecraft mc=Minecraft.getMinecraft();
        boolean clearing=mc.player!=null && mc.world!=null &&
                !br.veilchapter.GameEvents.dimensionUnavailable &&
                mc.player.dimension==br.veilchapter.VeilMod.CLEARING_DIM;
        if(!clearing) {
            if(clearingAmbient!=null)mc.getSoundHandler().stopSound(clearingAmbient);
            clearingAmbient=null;
            playedThisVisit=false;
            ClientCinematicState.locked=false;
            return;
        }
        if(ClientCinematicState.locked){
            mc.player.rotationYaw=mc.player.prevRotationYaw=180F;
            mc.player.rotationPitch=mc.player.prevRotationPitch=-14F;
            mc.player.rotationYawHead=180F;
            mc.player.renderYawOffset=180F;
            mc.player.motionX=mc.player.motionY=mc.player.motionZ=0;
            mc.player.setSprinting(false);
            if(mc.player.movementInput!=null){
                mc.player.movementInput.moveForward=0;
                mc.player.movementInput.moveStrafe=0;
                mc.player.movementInput.jump=false;
                mc.player.movementInput.sneak=false;
            }
        }
        if(!playedThisVisit) {
            clearingAmbient=new ClearingAmbientSound();
            mc.getSoundHandler().playSound(clearingAmbient);
            playedThisVisit=true;
        }
    }
    @SubscribeEvent public void input(InputUpdateEvent event) {
        Minecraft mc=Minecraft.getMinecraft();
        if(!ClientCinematicState.locked || mc.player==null ||
                mc.player.dimension!=br.veilchapter.VeilMod.CLEARING_DIM)return;
        event.getMovementInput().moveForward=0;
        event.getMovementInput().moveStrafe=0;
        event.getMovementInput().jump=false;
        event.getMovementInput().sneak=false;
    }
    @SubscribeEvent public void audio(PlaySoundEvent event) {
        Minecraft mc=Minecraft.getMinecraft();
        if(mc.player==null || br.veilchapter.GameEvents.dimensionUnavailable || mc.player.dimension!=br.veilchapter.VeilMod.CLEARING_DIM) return;
        if(event.getSound()!=null && event.getSound().getCategory()!=SoundCategory.PLAYERS) {
            // Silence the clearing without blocking the three scripted positional voices.
            boolean ritualVoice=event.getSound().getSoundLocation()!=null &&
                    event.getSound().getSoundLocation().toString().startsWith(br.veilchapter.VeilMod.ID + ":");
            if(!ritualVoice)event.setResultSound(null);
        }
    }
}
