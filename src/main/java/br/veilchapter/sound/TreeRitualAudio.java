package br.veilchapter.sound;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

/**
 * Lightweight acoustic selection for the five Watcher speeches.
 * No per-tick DSP, no chunk generation, no audio/music outside the speech.
 * OpenAL handles stereo spatialization; we select an offline-mastered
 * voice based on wall obstruction and approximate roof/wall enclosure.
 */
public final class TreeRitualAudio {
    private TreeRitualAudio() { }

    public enum Room { OPEN, MUFFLED, INDOOR }

    public static Room classify(WorldServer world, EntityPlayerMP listener, Vec3d source) {
        Vec3d ears = listener.getPositionEyes(1.0F);
        boolean blockedAtEars = world.rayTraceBlocks(
            source, ears, false, true, false) != null;
        boolean blockedAtChest = world.rayTraceBlocks(
            source, ears.addVector(0,-0.55,0), false, true, false) != null;
        // Partial obstruction is often just a low root or leaves.
        if (blockedAtEars && blockedAtChest) return Room.MUFFLED;
        if (isEnclosed(world,listener.getPosition()) || isEnclosed(world,new BlockPos(source)))
            return Room.INDOOR;
        return Room.OPEN;
    }

    private static boolean solid(WorldServer world, BlockPos pos) {
        return world.isBlockLoaded(pos,false) && world.getBlockState(pos).isOpaqueCube();
    }

    private static boolean isEnclosed(WorldServer world, BlockPos center) {
        // Avoid mistaking an isolated tree canopy for a closed room:
        // require a low roof plus at least two nearby lateral solid surfaces.
        boolean roof = false;
        for(int y=3;y<=7;y++)if(solid(world,center.up(y))){roof=true;break;}
        if(!roof)return false;
        int walls=0;
        if(solid(world,center.add(4,1,0)))walls++;
        if(solid(world,center.add(-4,1,0)))walls++;
        if(solid(world,center.add(0,1,4)))walls++;
        if(solid(world,center.add(0,1,-4)))walls++;
        return walls>=2;
    }

    public static SoundEvent event(int cue,Room room) {
        if(cue<0 || cue>=5)throw new IllegalArgumentException("Five watcher speeches only");
        if(room==Room.MUFFLED)return VeilSounds.WATCHER_MUFFLED[cue];
        if(room==Room.INDOOR)return VeilSounds.WATCHER_INDOOR[cue];
        return VeilSounds.WATCHER_OPEN[cue];
    }

    public static Room play(WorldServer world, EntityPlayerMP player,
                            Vec3d speaker, int cue) {
        Room room = classify(world,player,speaker);
        world.playSound(null,speaker.x,speaker.y,speaker.z,
                event(cue,room),SoundCategory.VOICE,1.0F,1.0F);
        return room;
    }
}
