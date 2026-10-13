package br.veilchapter.core;
/** All five original mastered audio clips play consecutively, never cut mid-word. */
public final class TreeCinematicTimeline {
    public static final int START_DELAY = 40;
    // Rounded UP from the five longest (indoor) OGG variants, at 20 TPS.
    public static final int[] VOICE_TICKS = {95,126,283,175,101};
    public static final int COMPLETE_MASK = 31;
    public static final int PREBUILD_BUDGET = 128;
    public static final int END_TICK = START_DELAY + 95 + 126 + 283 + 175 + 101 + 2;
    private TreeCinematicTimeline() {}
    public static int start(int cue) {
        if(cue<0||cue>=5) throw new IllegalArgumentException("cue");
        int tick=START_DELAY;
        for(int i=0;i<cue;i++)tick+=VOICE_TICKS[i];
        return tick;
    }
    public static boolean mayOpen(long elapsed,int flags) {
        return elapsed>=END_TICK && (flags&COMPLETE_MASK)==COMPLETE_MASK;
    }
}
