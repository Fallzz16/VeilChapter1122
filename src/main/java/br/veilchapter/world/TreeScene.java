package br.veilchapter.world;

import br.veilchapter.VeilMod;
import br.veilchapter.config.VeilConfig;
import br.veilchapter.core.TreeMath;
import br.veilchapter.core.TreeRitualPacing;
import br.veilchapter.entity.EntityWatcher;
import br.veilchapter.sound.VeilSounds;
import br.veilchapter.sound.TreeRitualAudio;
import br.veilchapter.state.StoryData;
import br.veilchapter.system.DreadSystem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

/** Isolated chapter finale. Protected build + persistent cursor for resumability. */
public final class TreeScene {
    public static final BlockPos CHEST = new BlockPos(0,5,0);
    private static List<Placement> plan;
    private static int[] planCounts;
    // Five COMPLETE spoken passages, mastered as mono OGG and separated by silence.
    // Timeline fits into the existing 150-second Tree Ritual without delaying the chest.
    private static final int[] VOICE_SECONDS = {12, 34, 59, 95, 137};
    private static final int[] VOICE_DURATION_TICKS = {92, 124, 280, 172, 98};
    private static final String[] SUBTITLES = {
        "Até que enfim... eu estava esperando por você.",
        "Tudo parece estranho... mas eu confio em você para salvá-lo.",
        "Tudo foi perdido naquela vila...",
        "Não há esperanças naquele lugar...",
        "Mas agora... nós temos você."
    };
    private static final DreadSystem RESTORER = new DreadSystem();
    private static final class Placement {
        final BlockPos pos; final IBlockState state;
        Placement(BlockPos p,IBlockState b) {pos=p;state=b;}
    }
    /** Only trusted when in Overworld and within ~6 blocks of local natural height. */
    public static boolean onSurface(EntityPlayerMP player) {
        World w=player.world;
        BlockPos at=player.getPosition();
        if(!w.isBlockLoaded(at,false))return false;
        BlockPos height=w.getHeight(at);
        int difference=height.getY()-(int)Math.floor(player.posY);
        return difference<=6 && difference>=-12; // Inside shallow surface buildings also valid.
    }
    public boolean request(EntityPlayerMP p,StoryData d) {
        if(d.treeTriggerConsumed || d.treeActive || d.clock.completed())return false;
        if(p.dimension!=0 || !VeilConfig.enableTreeSequence || br.veilchapter.GameEvents.dimensionUnavailable)return false;
        d.savedTime=p.world.getWorldTime();
        d.storedFood=p.getFoodStats().getFoodLevel();
        d.storedSaturation=p.getFoodStats().getSaturationLevel();
        d.originDim=0;d.originX=p.posX;d.originY=p.posY;d.originZ=p.posZ;
        d.originYaw=p.rotationYaw;d.originPitch=p.rotationPitch;
        d.treeTriggerConsumed=true;d.treeActive=true;d.treeCursor=0;
        d.treeStage=0; d.treeStarted=0;d.treeWatcherFlags=0;d.returnCommitted=false;
        d.markDirty();
        return true;
    }
    public void enter(EntityPlayerMP player,StoryData d) {
        if(player.dimension!=0 || !d.treeActive) return;
        try {
            Entity changed=player.changeDimension(VeilMod.CLEARING_DIM,new FixedPoint(0.5,4, 25.5, 180F, 0F));
            if (changed == null || changed.dimension!=VeilMod.CLEARING_DIM) {
                VeilMod.LOG.error("Transicao para clearing nao confirmada; mantendo gatilho pendente.");
                d.treeActive=false;d.treeTriggerConsumed=false;d.markDirty();
            }
        } catch (Exception exc) {
            VeilMod.LOG.error("Falha na transicao da arvore. Jogador preservado?",exc);
            d.treeActive=false;d.treeTriggerConsumed=false;d.markDirty();
        }
    }
    /** The player remains in control; the 150-second ritual limits block and chunk work. */
    public void tick(EntityPlayerMP p,StoryData d) {
        if(p.dimension!=VeilMod.CLEARING_DIM || !d.treeActive)return;
        WorldServer w=(WorldServer)p.world;
        if(d.treeStarted==0){d.treeStarted=w.getTotalWorldTime();d.markDirty();}
        p.getFoodStats().setFoodLevel(d.storedFood);
        p.getFoodStats().setFoodSaturationLevel(d.storedSaturation);
        if(d.returnCommitted){finishReturn(p,d);return;}
        long elapsed=Math.max(0,w.getTotalWorldTime()-d.treeStarted);

        // Prefetch at most one chunk per ten ticks while the clearing is still quiet.
        // The closer observation point also keeps most of the crown within normal view distance.
        if(elapsed<400 && elapsed%10==0){
            int idx=(int)(elapsed/10);
            if(idx<25){
                int cx=(idx%5)-2, cz=(idx/5)-2;
                w.getChunkProvider().provideChunk(cx,cz);
            }
        }

        ritualVoices(p,w,elapsed,d);
        if (elapsed%20==0)clearExpiredWatchers(w);

        // Keep the tree in view from the ground. Movement is never locked.
        if(p.posX*p.posX+p.posZ*p.posZ>180*180){
            p.connection.setPlayerLocation(-p.posX*.20,4,-p.posZ*.20,p.rotationYaw,p.rotationPitch);
        }

        List<Placement> shape=plan();
        int target=Math.min(shape.size(),TreeRitualPacing.target(elapsed,planCounts));
        int built=0, generatedChunks=0;
        while(d.treeCursor<target && built<TreeRitualPacing.MAX_PLACEMENTS_PER_TICK) {
            Placement op=shape.get(d.treeCursor);
            if(!w.isBlockLoaded(op.pos,false)){
                if(generatedChunks>=1)break;
                generatedChunks++;
                w.getChunkProvider().provideChunk(op.pos.getX()>>4,op.pos.getZ()>>4);
                if(!w.isBlockLoaded(op.pos,false))break;
            }
            d.treeCursor++;
            if(!op.pos.equals(CHEST) &&
                    !p.getEntityBoundingBox().intersects(new net.minecraft.util.math.AxisAlignedBB(op.pos))){
                IBlockState old=w.getBlockState(op.pos);
                if(old.getBlock()==Blocks.AIR || old.getBlock()==Blocks.TALLGRASS)
                    w.setBlockState(op.pos,op.state,2);
            }
            built++;
        }
        if(built>0 && w.getTotalWorldTime()%20==0)d.markDirty();

        // Never reveal the chest until *every* placed block has finished, even on a slow PC.
        if(d.treeCursor>=shape.size() && elapsed>=TreeRitualPacing.TOTAL_TICKS) {
            if(!w.isBlockLoaded(CHEST,false))w.getChunkProvider().provideChunk(0,0);
            if(!p.getEntityBoundingBox().intersects(new net.minecraft.util.math.AxisAlignedBB(CHEST))){
                if(w.getBlockState(CHEST).getBlock()!=Blocks.CHEST)
                    w.setBlockState(CHEST,Blocks.CHEST.getDefaultState(),3);
                TileEntity tile=w.getTileEntity(CHEST);
                if(tile instanceof TileEntityChest){
                    TileEntityChest chest=(TileEntityChest)tile;
                    if(chest.getStackInSlot(0).isEmpty())chest.setInventorySlotContents(0,book());
                }
                if(d.treeStage!=2){d.treeStage=2;d.markDirty();}
            }
        }
        if(d.treeStage==2){
            if(elapsed%36==0 && elapsed<TreeRitualPacing.TOTAL_TICKS+240)
                p.sendStatusMessage(new TextComponentString("Agora, entre."),true);
        }
    }

    private void ritualVoices(EntityPlayerMP p,WorldServer w,long elapsed,StoryData d) {
        if(d.treeStage==2)return; // Completed scene must not replay a spoken cue.
        for(int i=0;i<VOICE_SECONDS.length;i++){
            int mask=1<<i;
            if((d.treeWatcherFlags & mask)!=0)continue;
            long start=VOICE_SECONDS[i]*20L;
            if(elapsed<start)break;
            // If a save was resumed far beyond an earlier cue, skip rather than play
            // several lines simultaneously and spoil the end of the ceremony.
            if(elapsed>start+220){
                d.treeWatcherFlags|=mask;
                d.markDirty();
                continue;
            }
            if(spawnRitualWatcher(w,p,i,VOICE_DURATION_TICKS[i]+12)) {
                d.treeWatcherFlags|=mask;
                d.markDirty();
                p.sendStatusMessage(new TextComponentString(SUBTITLES[i]),true);
            }
            break; // At most one cue and one Watcher per tick.
        }
    }

    /** A visible but harmless speaker close to the player's current field of view.
        No chunk loading or player camera/position manipulation is needed. */
    private boolean spawnRitualWatcher(WorldServer w,EntityPlayerMP p,int cue,int life) {
        double facingX=p.getLookVec().x, facingZ=p.getLookVec().z;
        double facingLength=Math.sqrt(facingX*facingX+facingZ*facingZ);
        if(facingLength<0.1){
            facingX=-Math.sin(Math.toRadians(p.rotationYaw));
            facingZ=Math.cos(Math.toRadians(p.rotationYaw));
            facingLength=1.0;
        }
        facingX/=facingLength;facingZ/=facingLength;
        // Slight off-center approaches alternate sides. The scene can still be
        // observed with a free camera; nothing forces the player's attention.
        int sign=(cue%2==0)?1:-1;
        double distance=7.0+(cue%3)*1.5;
        double centerX=p.posX+facingX*distance + sign*(-facingZ)*2.5;
        double centerZ=p.posZ+facingZ*distance + sign*(facingX)*2.5;
        for(int trial=0;trial<24;trial++){
            int dx=(trial%5)-2;
            int dz=(trial/5)-2;
            BlockPos probe=new BlockPos(Math.floor(centerX)+dx,4,Math.floor(centerZ)+dz);
            if(!w.isBlockLoaded(probe,false))continue;
            BlockPos at=w.getHeight(probe);
            // Roots grow around the speaker. Keep it close to the field, not up in the canopy.
            if(at.getY()<4 || at.getY()>9)continue;
            if(!w.isAirBlock(at) || !w.isAirBlock(at.up()) || w.isAirBlock(at.down()))continue;
            if(p.getDistanceSq(at.getX()+.5,at.getY(),at.getZ()+.5)<16)continue;
            EntityWatcher watcher=new EntityWatcher(w);
            watcher.setLocationAndAngles(at.getX()+.5,at.getY(),at.getZ()+.5,
                    p.rotationYaw+180F,0F);
            watcher.getEntityData().setBoolean("veilRitual",true);
            watcher.getEntityData().setLong("veilRitualUntil",w.getTotalWorldTime()+life);
            if(!w.spawnEntity(watcher))continue;
            // The sound has a REAL 3D point and a low-cost environment-dependent variant.
            TreeRitualAudio.Room acoustic=TreeRitualAudio.play(w,p,
                    new net.minecraft.util.math.Vec3d(watcher.posX,watcher.posY+1.25,watcher.posZ),cue);
            VeilMod.LOG.debug("Watcher Tree Ritual voice {} at {}: {}",cue+1,at,acoustic);
            return true;
        }
        return false;
    }
    private static void clearExpiredWatchers(WorldServer w){
        for(EntityWatcher watcher:w.getEntities(EntityWatcher.class,e->e.getEntityData().getBoolean("veilRitual"))){
            if(w.getTotalWorldTime()>=watcher.getEntityData().getLong("veilRitualUntil"))
                watcher.setDead();
        }
    }
    public void openChest(EntityPlayerMP p,BlockPos pos,StoryData d) {
        if(!d.treeActive || d.returnCommitted || d.treeStage!=2 || !pos.equals(CHEST))return;
        // Commit return in state BEFORE altering world/time or delivering the book.
        d.returnCommitted=true;d.bookPending=true;d.markDirty();
        finishReturn(p,d);
    }
    /** Recoverably complete return, including after interruption during chest interaction. */
    private void finishReturn(EntityPlayerMP p,StoryData d) {
        WorldServer overworld=p.getServer().getWorld(0);
        if(overworld==null){VeilMod.LOG.error("Overworld ausente durante retorno da arvore");return;}
        BlockPos dest=safeBed(overworld,d);
        if(p.world instanceof WorldServer){
            for(EntityWatcher watcher:((WorldServer)p.world).getEntities(EntityWatcher.class,
                    e->e.getEntityData().getBoolean("veilRitual")))watcher.setDead();
        }
        Entity changed=p.changeDimension(0,new FixedPoint(dest.getX()+.5,dest.getY(),dest.getZ()+.5,d.originYaw,d.originPitch));
        if(changed==null||changed.dimension!=0){
            VeilMod.LOG.error("Nao foi possivel confirmar retorno; retry preservado.");return;
        }
        overworld.setWorldTime(d.savedTime); // World totalTime remains monotonic.
        p.getFoodStats().setFoodLevel(d.storedFood);
        p.getFoodStats().setFoodSaturationLevel(d.storedSaturation);
        d.treeActive=false; d.clock.complete();d.markDirty();
        RESTORER.restore(overworld,d);
        deliverBook(p,d);
    }
    public void deliverBook(EntityPlayerMP p,StoryData d) {
        if(!d.bookPending || p.dimension!=0)return;
        // Recognize already delivered book after an interrupted save, preventing duplication.
        for (ItemStack have:p.inventory.mainInventory) {
            if(!have.isEmpty() && have.getItem()==Items.WRITTEN_BOOK && have.hasTagCompound()
                    && have.getTagCompound().getBoolean("VeilChapterBook")) {
                d.bookPending=false;d.markDirty();return;
            }
        }
        // Inventory add is all-or-nothing when given one item; if full, retain pending state.
        ItemStack book=book();
        if(p.inventory.addItemStackToInventory(book)){
            d.bookPending=false;d.markDirty();
            p.inventoryContainer.detectAndSendChanges();
        }
    }
    private BlockPos safeBed(WorldServer world,StoryData d) {
        BlockPos target=d.bed;
        if(!target.equals(BlockPos.ORIGIN) && world.isBlockLoaded(target,false)) {
            for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++)for(int y=-1;y<=2;y++) {
                BlockPos at=target.add(x,y,z);
                if(world.isAirBlock(at)&&world.isAirBlock(at.up()) && world.getBlockState(at.down()).isFullCube())return at;
            }
        }
        BlockPos spawn=world.getSpawnPoint();
        if(world.isBlockLoaded(spawn,false))return world.getHeight(spawn);
        return spawn;
    }
    private static ItemStack book() {
        ItemStack stack=new ItemStack(Items.WRITTEN_BOOK);
        NBTTagCompound tag=new NBTTagCompound();
        tag.setString("title","3 Dias");tag.setString("author","?");
        tag.setBoolean("VeilChapterBook",true);
        NBTTagList pages=new NBTTagList();pages.appendTag(new net.minecraft.nbt.NBTTagString("{\"text\":\"3 Dias\"}"));
        tag.setTag("pages",pages);stack.setTagCompound(tag);
        return stack;
    }
    private static void addUnique(List<Placement> out,Set<BlockPos> used,BlockPos pos,IBlockState state){
        if(used.add(pos))out.add(new Placement(pos,state));
    }

    /**
     * Four contiguous chapters: exposed roots, lighter trunk/major branches,
     * crown outline, and sparse crown detail. The old tree used ~69k blocks,
     * roughly 50k of them leaves. This shape avoids most invisible interiors.
     */
    private static synchronized List<Placement> plan() {
        if(plan!=null)return plan;
        List<Placement> roots=new ArrayList<>();
        List<Placement> timber=new ArrayList<>();
        List<Placement> crown=new ArrayList<>();
        List<Placement> detail=new ArrayList<>();
        Set<BlockPos> used=new HashSet<>();
        IBlockState log=Blocks.LOG.getDefaultState();
        IBlockState leaves=Blocks.LEAVES.getDefaultState()
                .withProperty(net.minecraft.block.BlockLeaves.DECAYABLE,false)
                .withProperty(net.minecraft.block.BlockLeaves.CHECK_DECAY,false);

        // Walkable oak chamber floor; remains protected from every build/update.
        for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)
            if(x*x+z*z<=25)addUnique(roots,used,new BlockPos(x,4,z),Blocks.PLANKS.getDefaultState());

        // Exposed radial roots, avoiding the southern doorway (x=-2..2, z>0).
        for(int arm=0;arm<8;arm++){
            double ang=arm*Math.PI/4.0+.11;
            for(int t=6;t<=25;t++){
                double r=2.5-1.5*((t-6)/19.0);
                double centerX=Math.cos(ang)*t;
                double centerZ=Math.sin(ang)*t;
                double height=4+(t<14?2:1);
                for(int dx=-3;dx<=3;dx++)for(int dz=-3;dz<=3;dz++)
                    for(int dy=-1;dy<=2;dy++){
                        int x=(int)Math.round(centerX)+dx,z=(int)Math.round(centerZ)+dz;
                        if(z>=0 && z<=30 && Math.abs(x)<=3)continue;
                        if(dx*dx+dz*dz+dy*dy*.9>r*r)continue;
                        addUnique(roots,used,new BlockPos(x,(int)Math.round(height)+dy,z),log);
                    }
            }
        }
        // The lower trunk is thick and forms the dark hollow and the root connection.
        for(int y=0;y<=10;y++)for(int x=-20;x<=20;x++)for(int z=-20;z<=20;z++)
            if(TreeMath.trunk(x,y,z))addUnique(roots,used,new BlockPos(x,4+y,z),log);

        Random random=new Random(134865L);
        for(int i=0;i<260;i++){
            int x=random.nextInt(97)-48,z=random.nextInt(97)-48;
            if(x*x+z*z<25*25 || x*x+z*z>42*42)continue;
            addUnique(roots,used,new BlockPos(x,4,z),
                    Blocks.TALLGRASS.getDefaultState().withProperty(
                        net.minecraft.block.BlockTallGrass.TYPE,
                        net.minecraft.block.BlockTallGrass.EnumType.GRASS));
        }

        // Trunk retains its external bark and a narrow core, without dense invisible timber.
        for(int y=11;y<83;y++)for(int x=-20;x<=20;x++)for(int z=-20;z<=20;z++){
            if(!TreeMath.trunk(x,y,z))continue;
            double dx=x-TreeMath.centerX(y),dz=z-TreeMath.centerZ(y);
            double radius=TreeMath.trunkRadius(y);
            double sq=dx*dx+dz*dz;
            if(sq<(radius-2.4)*(radius-2.4) && sq>2.0*2.0)continue;
            addUnique(timber,used,new BlockPos(x,4+y,z),log);
        }
        // Long structural branches give the crown a believable silhouette.
        for(int arm=0;arm<9;arm++){
            double angle=arm*Math.PI*2/9.0;
            int initialY=57+(arm%4)*5;
            for(int t=0;t<=29;t++){
                double reach=TreeMath.trunkRadius(initialY)-1+t*.78;
                int cx=(int)Math.round(TreeMath.centerX(initialY)+Math.cos(angle)*reach);
                int cz=(int)Math.round(TreeMath.centerZ(initialY)+Math.sin(angle)*reach);
                int cy=4+initialY+(int)Math.round(t*.36-Math.pow(t/16.0,2));
                int radius=t<10?2:t<20?1:0;
                for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)
                    for(int dy=-radius;dy<=radius;dy++)
                        if(dx*dx+dy*dy+dz*dz<=radius*radius+1)
                            addUnique(timber,used,new BlockPos(cx+dx,cy+dy,cz+dz),log);
            }
        }

        // Thin outside leaves are the visible crown; a minority of internal leaves follow later.
        for(int y=62;y<110;y++){
            double rx=28.0*Math.sqrt(Math.max(0,1-Math.pow((y-86.0)/25.0,2)));
            double rz=25.0*Math.sqrt(Math.max(0,1-Math.pow((y-86.0)/25.0,2)));
            if(rx<1 || rz<1)continue;
            for(int x=-31;x<=31;x++)for(int z=-31;z<=31;z++){
                if(!TreeMath.foliage(x,y,z))continue;
                if(((x*73+y*13+z*19)&15)>=11)continue;
                double dx=x-TreeMath.centerX(78),dz=z-TreeMath.centerZ(78);
                double ellipse=dx*dx/(rx*rx)+dz*dz/(rz*rz);
                BlockPos at=new BlockPos(x,4+y,z);
                if(ellipse>=.77) addUnique(crown,used,at,leaves);
                else if(((x*37+z*17+y*11)&7)<2) addUnique(detail,used,at,leaves);
            }
        }
        planCounts=new int[]{roots.size(),timber.size(),crown.size(),detail.size()};
        List<Placement> out=new ArrayList<>(TreeRitualPacing.total(planCounts));
        out.addAll(roots);out.addAll(timber);out.addAll(crown);out.addAll(detail);
        plan=out;
        if(VeilMod.LOG!=null)VeilMod.LOG.info("Tree Ritual stages: roots={}, timber={}, crown={}, details={}, total={}",
            planCounts[0],planCounts[1],planCounts[2],planCounts[3],out.size());
        return plan;
    }
    /**
     * Production CI verifies the ACTUAL block plan after Forge has registered blocks.
     * This does not create chunks, affect player worlds, or expose debug gameplay.
     */
    public static void verifyPlanForCI(){
        List<Placement> operations=plan();
        int[] allowedTicks={500,800,800,500};
        for(int i=0;i<4;i++){
            if(planCounts[i] > allowedTicks[i]*TreeRitualPacing.MAX_PLACEMENTS_PER_TICK)
                throw new IllegalStateException("Ritual phase "+i+" exceeds its 150s budget: "+planCounts[i]);
        }
        if(operations.size()>50000 || operations.size()<15000)
            throw new IllegalStateException("Unexpectedly expensive or empty tree: "+operations.size());
        if(TreeRitualPacing.target(TreeRitualPacing.TOTAL_TICKS,planCounts)!=operations.size())
            throw new IllegalStateException("Tree does not finish in its schedule");
        Set<BlockPos> unique=new HashSet<>();
        for(Placement op:operations){
            if(!unique.add(op.pos))throw new IllegalStateException("Duplicate tree coordinate "+op.pos);
            if(op.state.getBlock() instanceof net.minecraft.block.BlockLeaves &&
                    op.state.getValue(net.minecraft.block.BlockLeaves.DECAYABLE))
                throw new IllegalStateException("Ritual crown leaves may decay at "+op.pos);

            // 4-block-high walkway leading from the south into the center.
            if(op.pos.getX()>=-2 && op.pos.getX()<=2 &&
               op.pos.getZ()>=1 && op.pos.getZ()<=15 &&
               op.pos.getY()>=5 && op.pos.getY()<=8)
                throw new IllegalStateException("Entrance obstructed at "+op.pos);
        }
        VeilMod.LOG.info("TREE_RITUAL_PLAN_OK: stage blocks [{}, {}, {}, {}], total={}, budget={}",
                planCounts[0],planCounts[1],planCounts[2],planCounts[3],
                operations.size(),TreeRitualPacing.MAX_PLACEMENTS_PER_TICK);
    }

    private static class FixedPoint implements ITeleporter {
        private final double x,y,z;
        private final float targetYaw,targetPitch;
        FixedPoint(double x,double y,double z,float yaw,float pitch){
            this.x=x;this.y=y;this.z=z;targetYaw=yaw;targetPitch=pitch;
        }
        @Override public void placeEntity(World world,Entity entity,float yaw) {
            entity.setLocationAndAngles(x,y,z,targetYaw,targetPitch);
            entity.motionX=entity.motionY=entity.motionZ=0;
        }
    }
}
