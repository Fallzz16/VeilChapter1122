package br.veilchapter.network;
import br.veilchapter.state.StoryData;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
/** Server-only chapter state snapshot for visual effects. */
public final class DayStatePacket implements IMessage {
    public boolean active,completed;
    public int day;
    public DayStatePacket() {}
    public DayStatePacket(StoryData d) {active=d.clock.active();completed=d.clock.completed();day=d.clock.day();}
    @Override public void fromBytes(ByteBuf buf) {active=buf.readBoolean();completed=buf.readBoolean();day=buf.readUnsignedByte();}
    @Override public void toBytes(ByteBuf buf) {buf.writeBoolean(active);buf.writeBoolean(completed);buf.writeByte(day);}
}
