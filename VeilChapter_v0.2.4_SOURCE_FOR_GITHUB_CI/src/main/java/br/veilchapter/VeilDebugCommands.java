package br.veilchapter;
import br.veilchapter.config.VeilConfig;
import br.veilchapter.state.StoryData;
import br.veilchapter.sound.VeilSounds;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
/** Cheat/OP-only diagnostic features; no debug items or blocks injected into gameplay. */
public final class VeilDebugCommands extends CommandBase {
    @Override public String getName() {return "veiltest";}
    @Override public String getUsage(ICommandSender s) {return "/veiltest day <2..7> | psst | tree";}
    @Override public int getRequiredPermissionLevel() {return 2;}
    @Override public void execute(MinecraftServer server,ICommandSender sender,String[] args) throws CommandException {
        EntityPlayerMP p=getCommandSenderAsPlayer(sender);
        if(p.dimension!=0)throw new CommandException("Use no Overworld.");
        StoryData d=StoryData.get(p.world);
        if(args.length==2 && args[0].equals("day")){
            int day=parseInt(args[1],2,7);
            d.clock.restore(day,Math.floorDiv(p.world.getWorldTime(),24000L),true,false);
            d.awakeningPending=false;d.nextEvent=p.world.getTotalWorldTime()+60;d.markDirty();
            sender.sendMessage(new TextComponentString("[Veil] Dia narrativo definido: "+day));return;
        }
        if(args.length==1 && args[0].equals("psst")) {
            p.world.playSound(null,p.getPosition(),VeilSounds.PSST,SoundCategory.AMBIENT,.5F,1);
            sender.sendMessage(new TextComponentString("[Veil] Som emitido."));return;
        }
        if(args.length==1 && args[0].equals("tree")) {
            if(!VeilConfig.enableTreeSequence)throw new CommandException("Ative enableTreeSequence nas configuracoes antes.");
            if(GameEvents.dimensionUnavailable)throw new CommandException("ID da dimensao ja pertence a outro mod. Evento bloqueado.");
            if(!d.clock.active()||d.clock.day()<7)throw new CommandException("Defina antes /veiltest day 7.");
            if(!new br.veilchapter.world.TreeScene().request(p,d))throw new CommandException("Evento indisponivel ou ja concluido.");
            sender.sendMessage(new TextComponentString("[Veil] Transicao pendente, aguarde um tick."));return;
        }
        throw new CommandException(getUsage(sender));
    }
}
