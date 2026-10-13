package br.veilchapter.client;

import br.veilchapter.sound.VeilSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;

/**
 * Single non-positional 150s background bed, with no birds and no music.
 * The five Watcher voices remain independent WORLD-POSITIONED sounds.
 * The audio master already contains five natural ducking windows.
 */
public final class ClearingAmbientSound extends MovingSound {
    private int ticks;

    public ClearingAmbientSound() {
        super(VeilSounds.TREE_DRONE, SoundCategory.AMBIENT);
        this.attenuationType = ISound.AttenuationType.NONE;
        this.repeat = false;
        this.volume = 0.72F;
        this.pitch = 1.0F;
    }

    @Override
    public void update() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.player.world == null ||
            mc.player.dimension != br.veilchapter.VeilMod.CLEARING_DIM) {
            donePlaying = true;
            return;
        }
        // The OGG's fade in/out and the Watcher's five ducking windows were mastered offline.
        // Audio length is 150s; no accidental replay while player waits for a delayed build.
        if (++ticks >= 3002) donePlaying = true;
    }
}
