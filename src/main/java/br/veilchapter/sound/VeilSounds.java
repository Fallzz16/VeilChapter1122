package br.veilchapter.sound;

import br.veilchapter.VeilMod;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Mono OGG events. Minecraft/OpenAL determines binaural left/right positioning. */
@Mod.EventBusSubscriber(modid = VeilMod.ID)
public final class VeilSounds {
    private static SoundEvent register(String id) {
        return new SoundEvent(new ResourceLocation(VeilMod.ID, id)).setRegistryName(VeilMod.ID, id);
    }
    public static final SoundEvent PSST = register("psst");
    public static final SoundEvent HEY = register("hey");
    public static final SoundEvent LAUGH = register("laugh");
    public static final SoundEvent BREATH = register("breath");

    public static final SoundEvent[] WATCHER_OPEN = new SoundEvent[5];
    public static final SoundEvent[] WATCHER_MUFFLED = new SoundEvent[5];
    public static final SoundEvent[] WATCHER_INDOOR = new SoundEvent[5];
    static {
        for (int i = 0; i < 5; i++) {
            String stem = "watcher_voice_" + String.format(java.util.Locale.ROOT, "%02d", i + 1);
            WATCHER_OPEN[i] = register(stem + "_open");
            WATCHER_MUFFLED[i] = register(stem + "_muffled");
            WATCHER_INDOOR[i] = register(stem + "_indoor");
        }
    }
    @SubscribeEvent public static void sounds(RegistryEvent.Register<SoundEvent> event) {
        event.getRegistry().registerAll(PSST, HEY, LAUGH, BREATH);
        event.getRegistry().registerAll(WATCHER_OPEN);
        event.getRegistry().registerAll(WATCHER_MUFFLED);
        event.getRegistry().registerAll(WATCHER_INDOOR);
    }
}
