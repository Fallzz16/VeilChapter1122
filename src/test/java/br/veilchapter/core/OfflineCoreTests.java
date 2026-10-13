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
        // Day seven remains playable; removed finale cannot complete the story.
        StoryClock unfinished=new StoryClock();
        unfinished.restore(7,1,true,false);
        for(int day=2;day<=40;day++)unfinished.tickCalendar(day);
        check(unfinished.day()==7 && !unfinished.completed(),"no automatic chapter finale");
        for(int day=1;day<=7;day++){
            check(!EntityEncounterRules.tenantAllowed(day,true,false,true,true,true) || day>=4,
                    "ineligible early day");
        }
        check(EntityEncounterRules.tenantAllowed(4,true,false,true,true,true),"valid tenant conditions");
        check(!EntityEncounterRules.tenantAllowed(4,true,false,true,true,false),"tenant daytime");
        check(!EntityEncounterRules.tenantAllowed(4,true,false,true,false,true),"tenant away from home");
        check(!EntityEncounterRules.tenantAllowed(4,true,true,true,true,true),"no activity after completion");
        check(!EntityEncounterRules.tenantAllowed(4,true,false,false,true,true),"Overworld only");
        check(EntityEncounterRules.withinHome(25) && EntityEncounterRules.withinHome(484),"distance range");
        check(!EntityEncounterRules.withinHome(24) && !EntityEncounterRules.withinHome(485),"distance rejection");
        for(int i=-100;i<400;i++) {
            long next=EntityEncounterRules.nextAttempt(100,4,i);
            check(next>=100+240*20L && next<=100+480*20L,"capped event cadence");
        }
        System.out.println("PASS: " + assertions + " verificacoes offline (Java puro)");
    }
}
