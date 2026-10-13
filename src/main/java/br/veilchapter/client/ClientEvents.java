package br.veilchapter.client;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraft.client.Minecraft;
/** Conservative visual gloom; survival simulation separately increases hostile events. */
public final class ClientEvents {
    @SubscribeEvent public void fog(EntityViewRenderEvent.FogColors event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null || mc.player.dimension != 0) return;
        if(ClientDayState.active && !ClientDayState.completed && ClientDayState.day>=6) {
            // Atmospheric-only sky/fog attenuation. Actual skylight simulation is NOT implemented.
            event.setRed(event.getRed()*.62F);
            event.setGreen(event.getGreen()*.62F);
            event.setBlue(event.getBlue()*.68F);
        }
    }
    @SubscribeEvent public void audio(PlaySoundEvent event) {
        Minecraft mc=Minecraft.getMinecraft();
        if(mc.player==null || br.veilchapter.GameEvents.dimensionUnavailable || mc.player.dimension!=br.veilchapter.VeilMod.CLEARING_DIM) return;
        if(event.getSound()!=null && event.getSound().getCategory()!=SoundCategory.PLAYERS) {
            // Silence the clearing without blocking the three scripted positional voices.
            boolean ritualVoice=event.getSound().getSoundLocation()!=null &&
                    br.veilchapter.VeilMod.ID.equals(event.getSound().getSoundLocation().getResourceDomain());
            if(!ritualVoice)event.setResultSound(null);
        }
    }
}
