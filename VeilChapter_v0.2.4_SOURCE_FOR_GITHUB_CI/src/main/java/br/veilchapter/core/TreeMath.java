package br.veilchapter.core;
/** Deterministic shapes. Pure math, no world access, suitable for offline testing. */
public final class TreeMath {
    private TreeMath() {}
    public static double centerX(int y) { return 2.7 * Math.sin(y * 0.055); }
    public static double centerZ(int y) { return 2.3 * Math.cos(y * 0.047); }
    public static double trunkRadius(int y) { return Math.max(5.8, 11.0 - y * 0.068); }
    public static boolean trunk(int x, int y, int z) {
        if (y < 0 || y >= 83) return false;
        double dx = x - centerX(y), dz = z - centerZ(y);
        double r = trunkRadius(y) + (y < 8 ? (8 - y) * 0.6 : 0);
        boolean bark = dx*dx + dz*dz <= r*r;
        // A walkable hollow 4 high; aperture between roots facing south.
        if (y >= 1 && y <= 4 && dx*dx + dz*dz < 6.0*6.0) return false;
        if (y >= 1 && y <= 4 && z >= 0 && Math.abs(x) <= 2 && z < 22) return false;
        return bark;
    }
    public static boolean foliage(int x, int y, int z) {
        if (y < 62 || y > 109) return false;
        double rx = 28.0 * Math.sqrt(Math.max(0.0, 1 - Math.pow((y - 86.0)/25.0, 2)));
        double rz = 25.0 * Math.sqrt(Math.max(0.0, 1 - Math.pow((y - 86.0)/25.0, 2)));
        if (rx < 1 || rz < 1) return false;
        double dx = x - centerX(78), dz = z - centerZ(78);
        // Irregular crown silhouette, not a perfect sphere.
        double noise = 1.0 + 0.10*Math.sin(x*.42 + y*.13)*Math.cos(z*.31);
        return (dx*dx)/(rx*rx)+(dz*dz)/(rz*rz) <= noise;
    }
}
