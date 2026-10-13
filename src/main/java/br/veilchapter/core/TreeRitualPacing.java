package br.veilchapter.core;

/**
 * Deterministic 150-second Tree Ritual pacing, independent of Minecraft/Forge.
 * The scene advances only as fast as the world accepts small placement batches.
 */
public final class TreeRitualPacing {
    public static final int TOTAL_TICKS = 150 * 20;
    public static final int MAX_PLACEMENTS_PER_TICK = 24;
    private static final int[] PHASE_START = {20 * 20, 45 * 20, 85 * 20, 125 * 20};
    private static final int[] PHASE_DURATION = {25 * 20, 40 * 20, 40 * 20, 25 * 20};

    private TreeRitualPacing() { }

    /** Number of operations allowed by the narrative timeline (not a per-tick budget). */
    public static int target(long elapsedTicks, int[] phaseSizes) {
        if (phaseSizes == null || phaseSizes.length != 4)
            throw new IllegalArgumentException("Tree ritual requires exactly 4 stages");
        int target = 0;
        for (int i = 0; i < 4; i++) {
            if (phaseSizes[i] < 0) throw new IllegalArgumentException("negative stage size");
            long span = Math.max(0L, Math.min(PHASE_DURATION[i], elapsedTicks - PHASE_START[i]));
            target += (int) (((long) phaseSizes[i] * span) / PHASE_DURATION[i]);
        }
        return target;
    }

    public static int total(int[] phaseSizes) {
        if (phaseSizes == null || phaseSizes.length != 4)
            throw new IllegalArgumentException("Tree ritual requires exactly 4 stages");
        int sum = 0;
        for (int size : phaseSizes) {
            if (size < 0) throw new IllegalArgumentException("negative stage size");
            sum += size;
        }
        return sum;
    }
}
