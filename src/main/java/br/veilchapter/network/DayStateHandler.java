package br.veilchapter.network;

import br.veilchapter.VeilMod;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/** Common registration; client render access lives entirely in the sided proxy. */
public final class DayStateHandler implements IMessageHandler<DayStatePacket,IMessage> {
    @Override public IMessage onMessage(DayStatePacket msg, MessageContext context) {
        VeilMod.proxy.acceptDayState(msg);
        return null;
    }
}
