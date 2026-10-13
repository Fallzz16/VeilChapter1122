package br.veilchapter.network;
import br.veilchapter.VeilMod;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
public final class CinematicStateHandler implements IMessageHandler<CinematicStatePacket,IMessage> {
    @Override public IMessage onMessage(CinematicStatePacket msg,MessageContext context){
        VeilMod.proxy.acceptCinematicState(msg);
        return null;
    }
}
