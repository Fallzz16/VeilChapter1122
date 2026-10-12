package br.veilchapter.core;
/** Pure narrative progression, independent of vanilla totalTime and calendar commands. */
public final class StoryClock {
    private int phaseDay = 1;
    private long lastCalendarDay = -1;
    private boolean active;
    private boolean complete;

    public void awaken(long calendarDay) {
        if (!active && !complete) {
            active = true;
            phaseDay = 2;
            lastCalendarDay = calendarDay;
        }
    }
    public void tickCalendar(long calendarDay) {
        if (!active || complete) return;
        if (lastCalendarDay < 0) { lastCalendarDay = calendarDay; return; }
        // No negative steps, at most one phase per observation: command/time jumps cannot skip a chapter.
        // A backward /time set must not create fake new days when the clock bounces forward.
        if (calendarDay <= lastCalendarDay) return;
        phaseDay = Math.min(7, phaseDay + 1);
        lastCalendarDay = calendarDay;
    }
    public int day() { return phaseDay; }
    public boolean active() { return active; }
    public boolean completed() { return complete; }
    public void complete() { complete = true; active = true; }
    public void restore(int day, long last, boolean started, boolean finished) {
        phaseDay = Math.max(1, Math.min(7, day)); lastCalendarDay = last; active = started; complete = finished;
    }
    public long lastCalendarDay() { return lastCalendarDay; }
}
