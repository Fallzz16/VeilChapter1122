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

    public static final SoundEvent WATCHER_VOICE_01 = register("watcher_voice_01");
    public static final SoundEvent WATCHER_VOICE_02 = register("watcher_voice_02");
    public static final SoundEvent WATCHER_VOICE_03 = register("watcher_voice_03");
    public static final SoundEvent WATCHER_VOICE_04 = register("watcher_voice_04");
    public static final SoundEvent WATCHER_VOICE_05 = register("watcher_voice_05");
    public static final SoundEvent WATCHER_VOICE_06 = register("watcher_voice_06");
    public static final SoundEvent WATCHER_VOICE_07 = register("watcher_voice_07");
    public static final SoundEvent WATCHER_VOICE_08 = register("watcher_voice_08");

    @SubscribeEvent public static void sounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(PSST, HEY, LAUGH, BREATH, WATCHER_VOICE_01, WATCHER_VOICE_02, WATCHER_VOICE_03, WATCHER_VOICE_04, WATCHER_VOICE_05, WATCHER_VOICE_06, WATCHER_VOICE_07, WATCHER_VOICE_08);
    }
}
