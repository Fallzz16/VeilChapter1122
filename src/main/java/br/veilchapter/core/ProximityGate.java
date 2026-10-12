package br.veilchapter.core;
/** Entering strictly under 10 blocks fires; move to at least 12 to rearm. */
public final class ProximityGate {
    private boolean armed = true;
    public boolean update(double distance) {
        if (distance >= 12.0) armed = true;
        if (armed && distance < 10.0) { armed = false; return true; }
        return false;
    }
    public void reset() { armed = true; }
    public boolean isArmed() { return armed; }
}
