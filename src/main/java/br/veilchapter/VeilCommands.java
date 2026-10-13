package br.veilchapter;
import br.veilchapter.state.StoryData;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
/** Read-only diagnostic command: no shortcuts that corrupt a story save. */
public final class VeilCommands extends CommandBase {
    @Override public String getName(){return "veilstatus";}
    @Override public String getUsage(ICommandSender sender){return "/veilstatus";}
    @Override public int getRequiredPermissionLevel(){return 0;}
    @Override public void execute(MinecraftServer server,ICommandSender sender,String[] args){
        World world = server.getWorld(0);
        StoryData d=StoryData.get(world);
        sender.sendMessage(new TextComponentString("[Veil] Dia="+d.clock.day()+
                " iniciado="+d.clock.active()+" concluido="+d.clock.completed()+
                " criaturasEmEspera="+d.suspendedMobs.size()+
                " proximoEventoTick="+d.nextEvent+
                " arvoreRemovida=true"));
    }
}
