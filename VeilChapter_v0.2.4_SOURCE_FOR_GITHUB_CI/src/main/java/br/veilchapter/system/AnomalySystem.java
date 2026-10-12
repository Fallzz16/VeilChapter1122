package br.veilchapter.system;

import br.veilchapter.config.VeilConfig;
import br.veilchapter.state.StoryData;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

/** Natural-block anomalies; never touch tile entities, unexplored chunks or player builds near beds. */
public final class AnomalySystem {
    private final Map<UUID,BlockPos> slowed = new HashMap<>();
    private final Map<UUID,Long> slowExpiry = new HashMap<>();
    private final Map<UUID,Long> lastSlow = new HashMap<>();
    public void run(EntityPlayerMP p,StoryData d,Random rng) {
        if (p.dimension != 0) return;
        int day=d.clock.day();
        int event=rng.nextInt(day<4?1:4);
        if(event==0) floatBlock(p,d,rng);
        else if(event==1) delayedMining(p,rng);
        else if(event==2 && VeilConfig.allowWorldDamage) rareBreak(p,d,rng);
        else if(event==3 && VeilConfig.enableSeamlessTeleports) seamlessTeleport(p,rng);
    }
    private void floatBlock(EntityPlayerMP p,StoryData d,Random r) {
        WorldServer w=(WorldServer)p.world;
        if(d.anomalousBlocks.size()>=32) return;
        for(int i=0;i<22;i++) {
            BlockPos hint=p.getPosition().add(r.nextInt(33)-16,0,r.nextInt(33)-16);
            if(!w.isBlockLoaded(hint,false)) continue;
            BlockPos ground=w.getHeight(hint);
            BlockPos at=ground.up(5+r.nextInt(7));
            if(at.getY()>245||!w.isAirBlock(at)||!w.isAirBlock(at.up()))continue;
            if(at.distanceSq(d.bed)<24*24)continue;
            IBlockState block=r.nextBoolean()?Blocks.STONE.getDefaultState():Blocks.DIRT.getDefaultState();
            w.setBlockState(at,block,3);
            NBTTagCompound n=new NBTTagCompound();
            n.setInteger("x",at.getX());n.setInteger("y",at.getY());n.setInteger("z",at.getZ());
            n.setInteger("block",Block.getStateId(block));
            n.setLong("expires",w.getTotalWorldTime()+(2+r.nextInt(4))*60*20L);
            d.anomalousBlocks.add(n);d.markDirty();return;
        }
    }
    /** Stop owning a floating block when a player breaks it; later replacements belong to the player. */
    public void onPlayerBreak(BlockPos pos, StoryData d) {
        for(Iterator<NBTTagCompound> it=d.anomalousBlocks.iterator(); it.hasNext();) {
            NBTTagCompound n=it.next();
            if(n.getInteger("x")==pos.getX() && n.getInteger("y")==pos.getY() && n.getInteger("z")==pos.getZ()) {
                it.remove(); d.markDirty();
            }
        }
    }
    public void cleanup(WorldServer w,StoryData d,long now) {
        for(Iterator<NBTTagCompound> it=d.anomalousBlocks.iterator();it.hasNext();) {
            NBTTagCompound n=it.next();
            BlockPos at=new BlockPos(n.getInteger("x"),n.getInteger("y"),n.getInteger("z"));
            if (!w.isBlockLoaded(at,false))continue;
            // If player mined it or replaced it, stop tracking and do not touch the replacement.
            if (Block.getStateId(w.getBlockState(at)) != n.getInteger("block")) {
                it.remove();d.markDirty();continue;
            }
            if (now>=n.getLong("expires")) {
                w.setBlockToAir(at);it.remove();d.markDirty();
            }
        }
    }
    private void delayedMining(EntityPlayerMP p,Random r) {
        World w=p.world;
        Vec3d eye=p.getPositionEyes(1);
        RayTraceResult hit=w.rayTraceBlocks(eye,eye.add(p.getLookVec().scale(5.0)),false,true,false);
        if(hit==null||hit.typeOfHit!=RayTraceResult.Type.BLOCK)return;
        BlockPos at=hit.getBlockPos();
        if(w.getTileEntity(at)!=null)return;
        IBlockState state=w.getBlockState(at);
        if(state.getBlock()!=Blocks.STONE && state.getBlock()!=Blocks.DIRT && state.getBlock()!=Blocks.GRASS)return;
        slowed.put(p.getUniqueID(),at);
        slowExpiry.put(p.getUniqueID(),w.getTotalWorldTime()+100L);
    }
    public void adjustBreakSpeed(PlayerEvent.BreakSpeed ev,long now) {
        UUID id=ev.getEntityPlayer().getUniqueID();
        Long until=slowExpiry.get(id);
        if(until==null||until<=now)return;
        BlockPos at=slowed.get(id);
        if(at!=null && at.equals(ev.getPos()))ev.setNewSpeed(ev.getOriginalSpeed()*.25F);
    }
    private void rareBreak(EntityPlayerMP p,StoryData d,Random r) {
        World w=p.world;
        for(int i=0;i<15;i++) {
            BlockPos base=p.getPosition().add(r.nextInt(23)-11,r.nextInt(8)-3,r.nextInt(23)-11);
            if (!w.isBlockLoaded(base,false) || base.distanceSq(d.bed)<32*32) continue;
            IBlockState s=w.getBlockState(base);
            // Only wild leaves, do not destroy arbitrary custom or inventory blocks.
            if(s.getBlock()==Blocks.LEAVES || s.getBlock()==Blocks.LEAVES2) {
                w.setBlockToAir(base);return;
            }
        }
    }
    private void seamlessTeleport(EntityPlayerMP p,Random r) {
        if(p.isRiding() || p.isElytraFlying() || p.isInWater() || p.isBurning())return;
        WorldServer w=(WorldServer)p.world;
        BlockPos feet=p.getPosition();
        if(!w.isBlockLoaded(feet,false))return;
        RayTraceResult origin=w.rayTraceBlocks(p.getPositionEyes(1),
                p.getPositionEyes(1).add(p.getLookVec().scale(7.0)),false,true,false);
        if(origin==null || origin.typeOfHit!=RayTraceResult.Type.BLOCK)return;
        Block originType=w.getBlockState(origin.getBlockPos()).getBlock();
        int light=w.getLight(feet);
        for(int i=0;i<90;i++) {
            int dx=r.nextInt(201)-100,dz=r.nextInt(201)-100;
            if(dx*dx+dz*dz>200*200 || dx*dx+dz*dz<24*24) continue;
            BlockPos target=feet.add(dx,r.nextInt(5)-2,dz);
            if(!w.isBlockLoaded(target,false)||!w.isAirBlock(target)||!w.isAirBlock(target.up()))continue;
            if(!w.getBlockState(target.down()).isFullCube())continue;
            if(Math.abs(w.getLight(target)-light)>2)continue;
            Vec3d eye=new Vec3d(target.getX()+.5,target.getY()+p.getEyeHeight(),target.getZ()+.5);
            RayTraceResult look=w.rayTraceBlocks(eye,eye.add(p.getLookVec().scale(7)),false,true,false);
            if(look==null || look.typeOfHit!=RayTraceResult.Type.BLOCK)continue;
            if(w.getBlockState(look.getBlockPos()).getBlock()!=originType)continue;
            p.connection.setPlayerLocation(target.getX()+.5,target.getY(),target.getZ()+.5,p.rotationYaw,p.rotationPitch);
            return;
        }
    }
}
