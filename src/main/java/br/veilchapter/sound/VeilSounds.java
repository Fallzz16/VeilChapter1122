package br.veilchapter.sound;
import br.veilchapter.VeilMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid=VeilMod.ID)
public final class VeilSounds {
    public static final SoundEvent PSST = new SoundEvent(new ResourceLocation(VeilMod.ID,"psst"))
        .setRegistryName(VeilMod.ID,"psst");
    @SubscribeEvent public static void sounds(RegistryEvent.Register<SoundEvent> ev) {
        ev.getRegistry().register(PSST);
    }
}
