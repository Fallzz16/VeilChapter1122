package br.veilchapter;
import br.veilchapter.state.StoryData;
import br.veilchapter.entity.EntityTenant;
import br.veilchapter.sound.VeilSounds;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import br.veilchapter.system.DreadSystem;
import net.minecraft.util.text.TextComponentString;
/** Cheat/OP-only diagnostic features; no debug items or blocks injected into gameplay. */
public final class VeilDebugCommands extends CommandBase {
    @Override public String getName() {return "veiltest";}
    @Override public String getUsage(ICommandSender s) {return "/veiltest day <2..7> | psst | audio <psst|hey|laugh|breath> <left|right|front|behind> | tenant | watcher | peeker";}
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
        if(args.length==1 && (args[0].equals("watcher") || args[0].equals("peeker"))) {
            net.minecraft.world.WorldServer w=(net.minecraft.world.WorldServer)p.world;
            net.minecraft.util.math.Vec3d look=p.getLookVec();
            double mag=Math.sqrt(look.x*look.x+look.z*look.z);
            if(mag<.05)throw new CommandException("Olhe para a frente, nao para cima/baixo.");
            int distance=args[0].equals("watcher")?19:13;
            net.minecraft.util.math.BlockPos hint=p.getPosition().add(
                look.x/mag*distance,0,look.z/mag*distance);
            if(!w.isBlockLoaded(hint,false))
                throw new CommandException("Chunk de destino nao carregado.");
            net.minecraft.util.math.BlockPos at=w.getHeight(hint);
            if(!w.isAirBlock(at) || !w.isAirBlock(at.up()) ||
                    !w.getBlockState(at.down()).isFullCube())
                throw new CommandException("Chao invalido neste ponto.");
            if(args[0].equals("watcher")) {
                br.veilchapter.entity.EntityWatcher e=new br.veilchapter.entity.EntityWatcher(w);
                e.setPosition(at.getX()+.5,at.getY(),at.getZ()+.5);
                if(!w.getCollisionBoxes(e,e.getEntityBoundingBox()).isEmpty() || !w.spawnEntity(e))
                    throw new CommandException("Sem espaco para Watcher.");
            }else {
                br.veilchapter.entity.EntityPeeker e=new br.veilchapter.entity.EntityPeeker(w);
                e.setPosition(at.getX()+.5,at.getY()+.08,at.getZ()+.5);
                e.setEyesOnly(false);
                if(!w.spawnEntity(e))throw new CommandException("Nao foi possivel criar Peeker.");
            }
            sender.sendMessage(new TextComponentString("[Veil] "+args[0]+" criado a "+distance+" blocos."));
            return;
        }
        if(args.length==1 && args[0].equals("tenant")) {
            if(!br.veilchapter.GameEvents.TEST_TENANT_AVAILABLE)
                throw new CommandException("Sistema de debug nao habilitado.");
            boolean ok=br.veilchapter.GameEvents.debugTenantViaInstance(p,d);
            if(!ok) throw new CommandException("Sem espaco valido em chunks carregados; tente perto de casa ou no plano.");
            sender.sendMessage(new TextComponentString("[Veil] Inquilino invocado; observe a silhueta a distancia."));
            return;
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
