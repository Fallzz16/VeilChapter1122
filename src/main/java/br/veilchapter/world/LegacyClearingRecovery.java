package br.veilchapter.world;

import br.veilchapter.VeilMod;
import br.veilchapter.state.StoryData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;

/** Retains old dimension ID 713 only to recover existing 0.2.8/0.2.9 worlds. */
public final class LegacyClearingRecovery {
    public boolean rescue(EntityPlayerMP player, StoryData data) {
        if(player.dimension != VeilMod.LEGACY_CLEARING_DIM) return false;
        WorldServer overworld=player.getServer().getWorld(0);
        if(overworld==null)return false;
        final BlockPos destination=safeDestination(overworld,data);
        final float yaw=data.originDim==0?data.originYaw:player.rotationYaw;
        final float pitch=data.originDim==0?data.originPitch:player.rotationPitch;
        try {
            Entity moved=player.changeDimension(0,new ITeleporter(){
                @Override public void placeEntity(World world,Entity entity,float rotation){
                    entity.setLocationAndAngles(destination.getX()+.5,destination.getY(),
                            destination.getZ()+.5,yaw,pitch);
                    entity.motionX=entity.motionY=entity.motionZ=0;
                    entity.fallDistance=0;
                }
            });
            if(moved==null || moved.dimension!=0) {
                VeilMod.LOG.error("Legacy return failed; chapter state preserved for retry.");
                return false;
            }
            data.retireLegacyTree();
            ((EntityPlayerMP)moved).sendMessage(new TextComponentString(
                    "[Veil] Cena antiga removida. Voce retornou ao Overworld."));
            VeilMod.LOG.info("VEIL_LEGACY_CLEARING_RECOVERED: safe return to Overworld.");
            return true;
        } catch(Exception ex) {
            VeilMod.LOG.error("Failed to recover player from retired dimension 713",ex);
            return false;
        }
    }

    private static BlockPos safeDestination(WorldServer w,StoryData data) {
        if(data.originDim==0 && Double.isFinite(data.originX) &&
                Double.isFinite(data.originY) && Double.isFinite(data.originZ) &&
                Math.abs(data.originX)<29900000 && Math.abs(data.originZ)<29900000 &&
                data.originY>2 && data.originY<250) {
            BlockPos original=new BlockPos(data.originX,data.originY,data.originZ);
            BlockPos safe=findWalkable(w,original);
            if(safe!=null)return safe;
        }
        if(!data.bed.equals(BlockPos.ORIGIN)) {
            BlockPos safe=findWalkable(w,data.bed);
            if(safe!=null)return safe;
        }
        BlockPos spawn=w.getSpawnPoint();
        BlockPos safe=findWalkable(w,spawn);
        return safe==null?w.getHeight(spawn):safe;
    }

    private static BlockPos findWalkable(WorldServer w,BlockPos center) {
        w.getChunkProvider().provideChunk(center.getX()>>4,center.getZ()>>4);
        for(int dx=-2;dx<=2;dx++)for(int dz=-2;dz<=2;dz++)
            for(int dy=-2;dy<=3;dy++) {
                BlockPos at=center.add(dx,dy,dz);
                if(!w.isBlockLoaded(at,false) || at.getY()<2 || at.getY()>250)continue;
                if(w.isAirBlock(at) && w.isAirBlock(at.up()) &&
                        w.getBlockState(at.down()).isFullCube())return at;
            }
        BlockPos surface=w.getHeight(center);
        if(w.isBlockLoaded(surface,false) && surface.getY()>2 &&
                w.isAirBlock(surface) && w.isAirBlock(surface.up()) &&
                w.getBlockState(surface.down()).isFullCube())return surface;
        return null;
    }
}
