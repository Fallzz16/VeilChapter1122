package br.veilchapter.core;
/** Pure-Java regression checks: no Minecraft classes needed. */
public final class CinematicCoreTests {
    private static void check(boolean okay,String msg){if(!okay)throw new AssertionError(msg);}
    public static void main(String[] args){
        check(TreeCinematicTimeline.PREBUILD_BUDGET<=128,"preload budget");
        check(TreeCinematicTimeline.VOICE_TICKS.length==5,"five full recordings");
        check(TreeCinematicTimeline.start(0)==40,"initial view of completed tree");
        for(int i=1;i<5;i++)check(TreeCinematicTimeline.start(i)==
                TreeCinematicTimeline.start(i-1)+TreeCinematicTimeline.VOICE_TICKS[i-1],
                "no dialogue gap or overlap "+i);
        int end=TreeCinematicTimeline.start(4)+TreeCinematicTimeline.VOICE_TICKS[4];
        check(!TreeCinematicTimeline.mayOpen(end-1,31),"gate never opens early");
        check(!TreeCinematicTimeline.mayOpen(TreeCinematicTimeline.END_TICK,15),"fifth required");
        check(TreeCinematicTimeline.mayOpen(TreeCinematicTimeline.END_TICK,31),"all five complete");
        System.out.println("PASS: continuous Watcher speech and gated unlock QA");
    }
}
