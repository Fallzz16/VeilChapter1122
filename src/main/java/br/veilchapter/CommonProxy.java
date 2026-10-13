package br.veilchapter;
public class CommonProxy {
    public void preInit() {}
    /** No client Minecraft class is loaded by the dedicated server. */
    public void acceptDayState(br.veilchapter.network.DayStatePacket packet) {}
    public void acceptCinematicState(br.veilchapter.network.CinematicStatePacket packet) {}
}
