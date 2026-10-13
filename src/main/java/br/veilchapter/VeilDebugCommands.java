package br.veilchapter;
import br.veilchapter.config.VeilConfig;
import br.veilchapter.state.StoryData;
import br.veilchapter.sound.VeilSounds;
import br.veilchapter.sound.TreeRitualAudio;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import br.veilchapter.system.DreadSystem;
import net.minecraft.util.text.TextComponentString;
/** Cheat/OP-only diagnostic features; no debug items or blocks injected into gameplay. */
public final class VeilDebugCommands extends CommandBase {
    @Override public String getName() {return "veiltest";}
    @Override public String getUsage(ICommandSender s) {return "/veiltest day <2..7> | psst | audio <psst|hey|laugh|breath> <left|right|front|behind> | watcher voice <1..5> [left|right|front|behind] | tree";}
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
        if (args.length==1 && args[0].equals("psst")) {
            testDirectional(p,VeilSounds.PSST,"behind");
            sender.sendMessage(new TextComponentString("[Veil] Psst tocado 7 blocos ATRAS. Use fones e gire a camera."));return;
        }
        if (args.length==3 && args[0].equals("audio")) {
            SoundEvent sound;
            if(args[1].equals("psst")) sound=VeilSounds.PSST;
            else if(args[1].equals("hey")) sound=VeilSounds.HEY;
            else if(args[1].equals("laugh")) sound=VeilSounds.LAUGH;
            else if(args[1].equals("breath")) sound=VeilSounds.BREATH;
            else throw new CommandException("Sons: psst, hey, laugh, breath");
            if(!args[2].equals("left")&&!args[2].equals("right")&&!args[2].equals("front")&&!args[2].equals("behind"))
                throw new CommandException("Direcoes: left, right, front, behind");
            testDirectional(p,sound,args[2]);
            sender.sendMessage(new TextComponentString("[Veil] Som: "+args[1]+" | direcao: "+args[2]));return;
        }
        if((args.length==3 || args.length==4) && args[0].equals("watcher") && args[1].equals("voice")) {
            int speech = parseInt(args[2],1,5)-1;
            String side=args.length==4?args[3]:"front";
            if(!side.equals("left")&&!side.equals("right")&&!side.equals("front")&&!side.equals("behind"))
                throw new CommandException("Direcoes: left, right, front, behind");
            Vec3d facing=p.getLookVec();
            double mag=Math.sqrt(facing.x*facing.x+facing.z*facing.z);
            double fx=mag<.01?0:facing.x/mag, fz=mag<.01?1:facing.z/mag;
            double rx=-fz, rz=fx;
            double x=p.posX, z=p.posZ;
            if(side.equals("front")) {x+=fx*7;z+=fz*7;}
            else if(side.equals("behind")) {x-=fx*7;z-=fz*7;}
            else if(side.equals("right")) {x+=rx*7;z+=rz*7;}
            else {x-=rx*7;z-=rz*7;}
            TreeRitualAudio.Room acoustic=TreeRitualAudio.play((net.minecraft.world.WorldServer)p.world,
                    p,new Vec3d(x,p.posY+1.35,z),speech);
            sender.sendMessage(new TextComponentString("[Veil] Watcher fala "+(speech+1)+
                    " | direcao "+side+" | ambiente "+acoustic+
                    " (use fones; paredes entre os pontos provocam abafamento)."));
            return;
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
    private void testDirectional(EntityPlayerMP player, SoundEvent event, String direction) {
        Vec3d look=player.getLookVec();
        double magnitude=Math.sqrt(look.x*look.x+look.z*look.z);
        double forwardX = magnitude < .01 ? 0 : look.x/magnitude;
        double forwardZ = magnitude < .01 ? 1 : look.z/magnitude;
        double rightX=-forwardZ;
        double rightZ=forwardX;
        double x,y,z;
        y=player.posY + 1.35;
        if(direction.equals("left")) {x=player.posX-rightX*7;z=player.posZ-rightZ*7;}
        else if(direction.equals("right")) {x=player.posX+rightX*7;z=player.posZ+rightZ*7;}
        else if(direction.equals("front")) {x=player.posX+forwardX*7;z=player.posZ+forwardZ*7;}
        else {x=player.posX-forwardX*7;z=player.posZ-forwardZ*7;}
        float volume = event==VeilSounds.BREATH?.55F:event==VeilSounds.LAUGH?.65F:.7F;
        DreadSystem.playSpatial(player,event,x,y,z,volume,1.0F);
    }
}
