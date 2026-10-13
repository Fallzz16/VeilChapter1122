package br.veilchapter.core;

/**
 * Pure, testable encounter rules. No dependency on Minecraft or rendering code.
 * Inputs are authoritative world measurements; random chance is evaluated separately.
 */
public final class EntityEncounterRules {
    private EntityEncounterRules() {}
    public static boolean tenantAllowed(int day, boolean active, boolean complete,
            boolean overworld, boolean nearHome, boolean nighttime) {
        return day >= 4 && day <= 7 && active && !complete &&
                overworld && nearHome && nighttime;
    }
    public static long nextAttempt(long now, int day, int randomUnit) {
        // A 4–8 minute cadence; randomUnit is clamped to prevent bad inputs.
        int bounded=Math.max(0,Math.min(240,randomUnit));
        int seconds=(day >= 6 ? 180 : 240)+bounded;
        return now+seconds*20L;
    }
    public static boolean safeY(int feet, int headroom) {
        return feet > 4 && feet < 248 && headroom >= 3;
    }
    public static boolean withinHome(double distanceSq) {
        return distanceSq >= 5*5 && distanceSq <= 22*22;
    }
}
