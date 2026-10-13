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
            if(idx<9){
                int cx=(idx%3)-1, cz=(idx/3)-1;
                w.getChunkProvider().provideChunk(cx,cz);
            }
        }

        ritualVoices(p,w,elapsed,d);
        ritualWoodSounds(w,elapsed);
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

    /** Nine sparse, server-authoritative spatial creaks. No bird sounds or music. */
    private void ritualWoodSounds(WorldServer world,long elapsed) {
        final int[] seconds={22,29,43,51,78,87,111,124,131};
        final int[] x={-5,6,-9,7,13,-11,9,-8,3};
        final int[] z={10,8,7,-4,2,-2,0,5,-7};
        final int[] level={6,7,12,15,18,19,23,24,23};
        for(int i=0;i<seconds.length;i++){
            if(elapsed!=seconds[i]*20L)continue;
            BlockPos speaker=new BlockPos(x[i],4+level[i],z[i]);
            if(!world.isBlockLoaded(speaker,false))return;
            world.playSound(null,speaker,VeilSounds.TREE_CREAKS[i%6],
                SoundCategory.BLOCKS,1.35F,0.94F+(i%3)*.045F);
            return;
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
            // DO NOT use World.getHeight() here: once the gigantic crown exists,
            // that heightmap points to LEAVES ~100 blocks above the player's view.
            BlockPos at=null;
            for(int floorY=4;floorY<=9;floorY++){
                BlockPos candidate=new BlockPos(probe.getX(),floorY,probe.getZ());
                if(w.isAirBlock(candidate) && w.isAirBlock(candidate.up()) &&
                   !w.isAirBlock(candidate.down())){
                    at=candidate;break;
                }
            }
            if(at==null)continue;
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


    /** The same four narrative stages, but a detailed 30-block-high asymmetric tree. */
    private static synchronized List<Placement> plan() {
        if(plan!=null)return plan;
        List<Placement> roots=new ArrayList<>();
        List<Placement> timber=new ArrayList<>();
        List<Placement> crown=new ArrayList<>();
        List<Placement> details=new ArrayList<>();
        Set<BlockPos> used=new HashSet<>();
        IBlockState log=Blocks.LOG.getDefaultState();
        IBlockState leaves=Blocks.LEAVES.getDefaultState()
            .withProperty(net.minecraft.block.BlockLeaves.DECAYABLE,false)
            .withProperty(net.minecraft.block.BlockLeaves.CHECK_DECAY,false);

        // Walkable floor, darkness within a real hollow, chest always at (0,5,0).
        for(int x=-4;x<=4;x++)for(int z=-4;z<=4;z++){
            if(x*x+z*z<=16)addUnique(roots,used,new BlockPos(x,4,z),
                    Blocks.PLANKS.getDefaultState());
        }
        // Radial exposed roots: limited to ~13 blocks so their silhouette is readable.
        for(int arm=0;arm<8;arm++){
            double angle=arm*Math.PI/4.0+0.16;
            for(int t=3;t<=13;t++){
                int cx=(int)Math.round(Math.cos(angle)*t);
                int cz=(int)Math.round(Math.sin(angle)*t);
                double radius=t<6?1.6:t<10?1.2:0.7;
                int cy=(t<8?6:5);
                for(int x=cx-2;x<=cx+2;x++)for(int z=cz-2;z<=cz+2;z++)
                    for(int y=cy-1;y<=cy+1;y++){
                        if((x-cx)*(x-cx)+(z-cz)*(z-cz)+
                           (y-cy)*(y-cy)*1.5>radius*radius)continue;
                        if(Math.abs(x)<=2 && z>=0 && z<=16 && y>=5 && y<=8)continue;
                        addUnique(roots,used,new BlockPos(x,y,z),log);
                    }
            }
        }
        // Main lower trunk, 7 blocks wide on average, hollow for a four-block-high room.
        for(int level=0;level<=7;level++){
            double radius=level<3?4.1:3.65;
            for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++){
                double cx=0.23*Math.sin(level*.32),cz=0.19*Math.cos(level*.30);
                double dx=x-cx,dz=z-cz;
                if(dx*dx+dz*dz>radius*radius)continue;
                if(level>=1 && level<=4 && dx*dx+dz*dz<=2.55*2.55)continue;
                if(level>=1 && level<=4 && z>=0 && Math.abs(x)<=2)continue;
                int y=4+level;
                if(Math.abs(x)<=2 && z>=1 && z<=15 && y>=5 && y<=8)continue;
                addUnique(roots,used,new BlockPos(x,y,z),log);
            }
        }
        // Thin irregular upper trunk, reaching at most 29 blocks above the grass.
        for(int level=8;level<=28;level++){
            double centerX=0.7*Math.sin(level*.17);
            double centerZ=0.5*Math.cos(level*.14);
            double radius=Math.max(1.35,3.6-(level-8)*.115);
            for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++){
                double dx=x-centerX,dz=z-centerZ;
                if(dx*dx+dz*dz<=radius*radius)
                    addUnique(timber,used,new BlockPos(x,4+level,z),log);
            }
        }

        // Eleven long and sweeping, yet affordable, structural branches.
        for(int arm=0;arm<11;arm++){
            double angle=arm*Math.PI*2.0/11.0+0.11+0.13*Math.sin(arm*2.61);
            int startLevel=12+(arm%6)*2;
            int length=13+(arm%4)*2;
            IBlockState branchLog=log.withProperty(net.minecraft.block.BlockLog.LOG_AXIS,
                    Math.abs(Math.cos(angle))>Math.abs(Math.sin(angle))?
                    net.minecraft.block.BlockLog.EnumAxis.X:
                    net.minecraft.block.BlockLog.EnumAxis.Z);
            for(int t=0;t<=length;t++){
                double reach=1.8+t*0.98;
                int cx=(int)Math.round(Math.cos(angle)*reach+0.5*Math.sin(startLevel*.16));
                int cz=(int)Math.round(Math.sin(angle)*reach);
                int level=startLevel+(int)Math.round(t*.36-t*t*.013);
                level=Math.max(10,Math.min(27,level));
                int radius=t<6?1:0;
                for(int dx=-radius;dx<=radius;dx++)for(int dz=-radius;dz<=radius;dz++)
                    for(int dy=-radius;dy<=radius;dy++){
                        if(dx*dx+dz*dz+dy*dy>radius*radius+1)continue;
                        addUnique(timber,used,new BlockPos(cx+dx,4+level+dy,cz+dz),branchLog);
                    }
            }
            // Offshoots grow out of existing limbs, creating irregular negative space.
            if(arm%3!=1) {
                int forkAt=length-7;
                double forkAngle=angle+(arm%2==0?0.39:-0.47);
                int baseLevel=startLevel+(int)Math.round(forkAt*.36-forkAt*forkAt*.013);
                for(int step=0;step<=7;step++){
                    double reach=1.8+forkAt*.98;
                    int bx=(int)Math.round(Math.cos(angle)*reach+
                            Math.cos(forkAngle)*step+0.5*Math.sin(startLevel*.16));
                    int bz=(int)Math.round(Math.sin(angle)*reach+
                            Math.sin(forkAngle)*step);
                    int yLevel=Math.min(28,baseLevel+(int)Math.round(step*.21));
                    IBlockState forkLog=log.withProperty(net.minecraft.block.BlockLog.LOG_AXIS,
                            Math.abs(Math.cos(forkAngle))>Math.abs(Math.sin(forkAngle))?
                            net.minecraft.block.BlockLog.EnumAxis.X:
                            net.minecraft.block.BlockLog.EnumAxis.Z);
                    addUnique(timber,used,new BlockPos(bx,4+yLevel,bz),forkLog);
                    if(step==6)
                        canopyLobe(crown,details,used,leaves,bx,yLevel,bz,3,2,3);
                }
            }
            // Uneven leaf lobes at middle and extremities, not a spherical canopy.
            for(int lobe=0;lobe<3;lobe++){
                int t=(lobe==0?length-6:lobe==1?length-2:length);
                double reach=1.8+t*.98;
                int cx=(int)Math.round(Math.cos(angle)*reach+0.5*Math.sin(startLevel*.16));
                int cz=(int)Math.round(Math.sin(angle)*reach);
                int level=startLevel+(int)Math.round(t*.36-t*t*.013);
                int rx=3+(arm+lobe)%3,rz=3+(arm*2+lobe)%3;
                int ry=2+(arm+lobe)%2;
                canopyLobe(crown,details,used,leaves,cx,level,cz,rx,ry,rz);
            }
        }
        // Two extra high clumps to make the top uneven but never above 30 blocks.
        canopyLobe(crown,details,used,leaves,0,25,0,5,4,4);
        canopyLobe(crown,details,used,leaves,-2,25,2,3,4,4);

        // Sparing undergrowth; avoid the route to the chamber.
        Random random=new Random(134865L);
        for(int i=0;i<110;i++){
            int x=random.nextInt(55)-27,z=random.nextInt(55)-27;
            if(x*x+z*z<16*16 || x*x+z*z>27*27)continue;
            if(Math.abs(x)<=2 && z>=0 && z<=20)continue;
            addUnique(roots,used,new BlockPos(x,4,z),
                Blocks.TALLGRASS.getDefaultState().withProperty(
                    net.minecraft.block.BlockTallGrass.TYPE,
                    net.minecraft.block.BlockTallGrass.EnumType.GRASS));
        }

        planCounts=new int[]{roots.size(),timber.size(),crown.size(),details.size()};
        List<Placement> result=new ArrayList<>(TreeRitualPacing.total(planCounts));
        result.addAll(roots);result.addAll(timber);result.addAll(crown);result.addAll(details);
        plan=result;
        VeilMod.LOG.info("TREE_SMALL_BLUEPRINT: roots={}, trunk={}, leaves={}, detail={}, total={}",
                planCounts[0],planCounts[1],planCounts[2],planCounts[3],result.size());
        return result;
    }

    private static void canopyLobe(List<Placement> crown,List<Placement> detail,
            Set<BlockPos> used,IBlockState leaves,int cx,int cy,int cz,
            int rx,int ry,int rz){
        for(int x=cx-rx;x<=cx+rx;x++)for(int z=cz-rz;z<=cz+rz;z++)
            for(int y=Math.max(10,cy-ry);y<=Math.min(29,cy+ry);y++){
                double dx=(x-cx)/(double)rx,dy=(y-cy)/(double)ry,dz=(z-cz)/(double)rz;
                double irregularity=1.0+.10*Math.sin(x*.84+z*.29+y*.35);
                double d=dx*dx+dy*dy+dz*dz;
                if(d>irregularity)continue;
                int hash=(x*71+y*41+z*23)^((x*z+31*y)*17);
                // Air gaps prevent a dense green sphere. Roughly 35% internal foliage.
                if((hash&31)>=(d>.55?26:13))continue;
                if(d>.55)addUnique(crown,used,new BlockPos(x,4+y,z),leaves);
                else addUnique(detail,used,new BlockPos(x,4+y,z),leaves);
            }
    }

    /** Run against the real Forge blocks during server smoke tests. */
    public static void verifyPlanForCI(){
        List<Placement> operations=plan();
        int[] phaseTicks={500,800,800,500};
        if(operations.size()<1100 || operations.size()>14000)
            throw new IllegalStateException("Unreasonable small-tree size: "+operations.size());
        Set<BlockPos> unique=new HashSet<>();
        int leavesCount=0,woodCount=0;
        for(Placement op:operations){
            if(!unique.add(op.pos))throw new IllegalStateException("Duplicate placement: "+op.pos);
            if(op.pos.getY()>33)throw new IllegalStateException("Tree exceeds 30 block height: "+op.pos);
            if(op.pos.getY()<4)throw new IllegalStateException("Tree breaks ground: "+op.pos);
            if(op.state.getBlock() instanceof net.minecraft.block.BlockLeaves){
                leavesCount++;
                if(op.state.getValue(net.minecraft.block.BlockLeaves.DECAYABLE))
                    throw new IllegalStateException("Crown may decay: "+op.pos);
            }
            if(op.state.getBlock()==Blocks.LOG)woodCount++;
            if(Math.abs(op.pos.getX())<=2 && op.pos.getZ()>=1 && op.pos.getZ()<=15
                    && op.pos.getY()>=5 && op.pos.getY()<=8)
                throw new IllegalStateException("Entrance blocked: "+op.pos);
            if(op.pos.equals(CHEST))throw new IllegalStateException("Chest position blocked");
        }
        if(leavesCount<300 || woodCount<250)
            throw new IllegalStateException("Missing major tree geometry");
        for(int i=0;i<4;i++){
            if(planCounts[i]>phaseTicks[i]*TreeRitualPacing.MAX_PLACEMENTS_PER_TICK)
                throw new IllegalStateException("Too many blocks in phase "+i);
        }
        if(TreeRitualPacing.target(TreeRitualPacing.TOTAL_TICKS,planCounts)!=operations.size())
            throw new IllegalStateException("Tree cannot finish within schedule");
        VeilMod.LOG.info("TREE_RITUAL_PLAN_OK: treeHeight<=30, leaves={}, wood={}, stages=[{},{},{},{}], total={}, budget={}",
                leavesCount,woodCount,planCounts[0],planCounts[1],planCounts[2],
                planCounts[3],operations.size(),TreeRitualPacing.MAX_PLACEMENTS_PER_TICK);
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
