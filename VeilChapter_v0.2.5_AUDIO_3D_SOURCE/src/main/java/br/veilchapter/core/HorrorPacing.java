package br.veilchapter.core;
import java.util.Random;
/** Sparse probabilistic scheduler, one event at a time, no fixed per-minute heartbeat. */
public final class HorrorPacing {
    private long next;
    public boolean ready(long tick) { return tick >= next; }
    public void postpone(long tick, int day, Random rng) {
        int min = day < 4 ? 6 * 60 : day < 6 ? 4 * 60 : 3 * 60;
        int max = day < 4 ? 13 * 60 : day < 6 ? 9 * 60 : 7 * 60;
        next = tick + (min + rng.nextInt(max - min + 1)) * 20L;
    }
    public void restore(long next) { this.next = next; }
    public long next() { return next; }
}
