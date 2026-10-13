package br.veilchapter.network;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
public final class CinematicStatePacket implements IMessage {
    public boolean locked;
    public CinematicStatePacket(){}
    public CinematicStatePacket(boolean locked){this.locked=locked;}
    @Override public void fromBytes(ByteBuf buf){locked=buf.readBoolean();}
    @Override public void toBytes(ByteBuf buf){buf.writeBoolean(locked);}
}
