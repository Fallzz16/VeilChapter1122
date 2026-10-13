package br.veilchapter.client;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraft.client.Minecraft;

/** Lightweight day-six fog attenuation. No retired tree camera lock or ambience. */
public final class ClientEvents {
    @SubscribeEvent public void fog(EntityViewRenderEvent.FogColors event) {
        Minecraft mc=Minecraft.getMinecraft();
        if(mc.world==null || mc.player==null || mc.player.dimension!=0)return;
        if(ClientDayState.active && !ClientDayState.completed && ClientDayState.day>=6) {
            event.setRed(event.getRed()*.62F);
            event.setGreen(event.getGreen()*.62F);
            event.setBlue(event.getBlue()*.68F);
        }
    }
}
