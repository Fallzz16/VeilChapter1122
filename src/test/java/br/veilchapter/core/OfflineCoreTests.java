package br.veilchapter.core;
import java.util.Random;
public final class OfflineCoreTests {
    private static int assertions;
    private static void check(boolean b, String message) {
        assertions++; if (!b) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        StoryClock c = new StoryClock();
        check(c.day() == 1, "first day");
        for (int i=0;i<50;i++) c.tickCalendar(i);
        check(c.day() == 1, "no horror before sleep");
        c.awaken(50); check(c.day() == 2, "first awakening");
        c.awaken(51); check(c.day() == 2, "not repeated");
        c.tickCalendar(51); check(c.day() == 3, "dawn");
        c.tickCalendar(48); check(c.day() == 3, "backward time");
        c.tickCalendar(50); check(c.day() == 3, "time bounce after /time set must not advance");
        c.tickCalendar(51); check(c.day() == 3, "same real day must not double count");
        c.tickCalendar(999); check(c.day() == 4, "jump can't skip");
        for (int i=1000; i<1008;i++) c.tickCalendar(i);
        check(c.day() == 7, "cap");
        c.complete(); check(c.completed() && c.day()==7, "completed");
        ProximityGate gate = new ProximityGate();
        check(!gate.update(12.0), "outside");
        check(gate.update(9.99), "entry");
        for (int i=0;i<100;i++) check(!gate.update(9.0), "must not reapply");
        check(!gate.update(11.99), "not yet rearmed");
        check(!gate.update(12.0), "rearm itself not trigger");
        check(gate.update(9.9), "trigger again");
        HorrorPacing p = new HorrorPacing();
        p.postpone(100, 4, new Random(2)); check(p.next()>100,"schedule");
        check(!p.ready(100) && p.ready(p.next()),"ready");
        for (int y=1;y<=4;y++) for (int x=-3;x<=3;x++) for (int z=-3;z<=3;z++) {
            if (x*x + z*z <= 9) check(!TreeMath.trunk(x,y,z), "hollow interior");
        }
        check(TreeMath.trunk(10,0,0), "roots");
        check(TreeMath.foliage(0,85,0), "foliage");
        check(!TreeMath.foliage(40,85,0), "crown bounds");

        // Ritual stages are deterministic, monotonic and never bypass the protected finale.
        int[] stageSizes={5817,10376,11176,9213};
        int sum=TreeRitualPacing.total(stageSizes);
        check(sum==36582,"tree stage inventory");
        check(TreeRitualPacing.MAX_PLACEMENTS_PER_TICK<=24,"per-tick scene budget");
        check(TreeRitualPacing.target(0,stageSizes)==0,"quiet phase");
        check(TreeRitualPacing.target(399,stageSizes)==0,"wait for roots");
        check(TreeRitualPacing.target(900,stageSizes)==stageSizes[0],"roots completed");
        check(TreeRitualPacing.target(1700,stageSizes)==stageSizes[0]+stageSizes[1],"trunk completed");
        check(TreeRitualPacing.target(2500,stageSizes)==sum-stageSizes[3],"crown outline completed");
        check(TreeRitualPacing.target(3000,stageSizes)==sum,"all detail completed");
        check(TreeRitualPacing.target(9000,stageSizes)==sum,"do not overshoot scene");
        int previous=0;
        for(int t=0;t<=3300;t++){
            int value=TreeRitualPacing.target(t,stageSizes);
            check(value>=previous && value<=sum,"monotonic tree scheduling at tick "+t);
            previous=value;
        }
        check(stageSizes[0] <= 500 * TreeRitualPacing.MAX_PLACEMENTS_PER_TICK, "root budget");
        check(stageSizes[1] <= 800 * TreeRitualPacing.MAX_PLACEMENTS_PER_TICK, "trunk budget");
        check(stageSizes[2] <= 800 * TreeRitualPacing.MAX_PLACEMENTS_PER_TICK, "crown budget");
        check(stageSizes[3] <= 500 * TreeRitualPacing.MAX_PLACEMENTS_PER_TICK, "detail budget");
        System.out.println("PASS: " + assertions + " verificacoes offline (Java puro)");
    }
}
