package br.veilchapter.sound;

import br.veilchapter.VeilMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** All clips are mono OGG, spatialized by vanilla OpenAL at playSound position. */
@Mod.EventBusSubscriber(modid = VeilMod.ID)
public final class VeilSounds {
    private static SoundEvent register(String id) {
        return new SoundEvent(new ResourceLocation(VeilMod.ID, id)).setRegistryName(VeilMod.ID, id);
    }
    public static final SoundEvent PSST = register("psst");
    public static final SoundEvent HEY = register("hey");
    public static final SoundEvent LAUGH = register("laugh");
    public static final SoundEvent BREATH = register("breath");

    @SubscribeEvent public static void sounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(PSST, HEY, LAUGH, BREATH);
    }
}
