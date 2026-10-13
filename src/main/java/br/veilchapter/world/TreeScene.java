package br.veilchapter.world;

import br.veilchapter.VeilMod;
import br.veilchapter.config.VeilConfig;
import br.veilchapter.core.TreeMath;
import br.veilchapter.core.TreeRitualPacing;
import br.veilchapter.core.TreeCinematicTimeline;
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
    // Tree is complete before teleport. The cinematic is an ~41s dialogue.
    public static final double VIEW_X=.5, VIEW_Y=4, VIEW_Z=45.5;
    public static final float VIEW_YAW=180F, VIEW_PITCH=-14F;
    private static final BlockPos SPEAKER=new BlockPos(2,4,37);
    private static final int GATE_Z=3;
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
    /** Build at most 128 blocks and one new chunk per tick BEFORE teleport. */
    public void enter(EntityPlayerMP player,StoryData d) {
        if(player.dimension!=0 || !d.treeActive)return;
        try {
            if(net.minecraftforge.common.DimensionManager.getWorld(VeilMod.CLEARING_DIM)==null)
                net.minecraftforge.common.DimensionManager.initDimension(VeilMod.CLEARING_DIM);
            WorldServer w=player.getServer().getWorld(VeilMod.CLEARING_DIM);
            if(w==null)return;
            List<Placement> shape=plan();
            int placed=0,loaded=0;
            while(d.treeCursor<shape.size() && placed<TreeCinematicTimeline.PREBUILD_BUDGET){
                Placement op=shape.get(d.treeCursor);
                if(!w.isBlockLoaded(op.pos,false)){
                    if(loaded>=1)break;
                    w.getChunkProvider().provideChunk(op.pos.getX()>>4,op.pos.getZ()>>4);
                    loaded++;
                    if(!w.isBlockLoaded(op.pos,false))break;
                }
                if(!op.pos.equals(CHEST)){
                    IBlockState old=w.getBlockState(op.pos);
                    if(old.getBlock()==Blocks.AIR || old.getBlock()==Blocks.TALLGRASS)
                        w.setBlockState(op.pos,op.state,2);
                }
                d.treeCursor++;
                placed++;
            }
            if(placed>0)d.markDirty();
            if(d.treeCursor<shape.size())return;
            sealGate(w);
            prepareChest(w);
            Entity changed=player.changeDimension(VeilMod.CLEARING_DIM,
                    new FixedPoint(VIEW_X,VIEW_Y,VIEW_Z,VIEW_YAW,VIEW_PITCH));
            if(changed==null || changed.dimension!=VeilMod.CLEARING_DIM){
                VeilMod.LOG.error("Tree scene teleport failed; checkpoint retained.");
                d.treeActive=false;d.treeTriggerConsumed=false;
            }else{
                d.treeStage=1;d.treeStarted=0;d.treeWatcherFlags=0;
                VeilMod.NETWORK.sendTo(new br.veilchapter.network.CinematicStatePacket(true),(EntityPlayerMP)changed);
                VeilMod.LOG.info("TREE_PREBUILT_SEALED: {} placements",shape.size());
            }
            d.markDirty();
        }catch(Exception ex){
            VeilMod.LOG.error("Tree scene preload failed; will retry safely.",ex);
        }
    }
    public void tick(EntityPlayerMP p,StoryData d){
        if(p.dimension!=VeilMod.CLEARING_DIM || !d.treeActive)return;
        WorldServer w=(WorldServer)p.world;
        p.getFoodStats().setFoodLevel(d.storedFood);
        p.getFoodStats().setFoodSaturationLevel(d.storedSaturation);
        if(d.returnCommitted){finishReturn(p,d);return;}
        if(d.treeStage==2){
            if(w.getTotalWorldTime()%80==0)p.sendStatusMessage(new TextComponentString("Agora, entre."),true);
            return;
        }
        if(d.treeStage!=1)return;
        if(d.treeStarted==0){d.treeStarted=w.getTotalWorldTime();d.markDirty();}
        long elapsed=Math.max(0,w.getTotalWorldTime()-d.treeStarted);
        freezePlayer(p);
        if(elapsed%20==0){
            ensureWatcher(w);
            VeilMod.NETWORK.sendTo(new br.veilchapter.network.CinematicStatePacket(true),p);
        }
        playSpeeches(p,w,elapsed,d);
        ritualWoodSounds(w,elapsed);
        if(TreeCinematicTimeline.mayOpen(elapsed,d.treeWatcherFlags)){
            unsealGate(w);
            for(EntityWatcher watcher:w.getEntities(EntityWatcher.class,
                    e->e.getEntityData().getBoolean("veilRitual")))watcher.setDead();
            d.treeStage=2;d.markDirty();
            VeilMod.NETWORK.sendTo(new br.veilchapter.network.CinematicStatePacket(false),p);
            p.sendStatusMessage(new TextComponentString("Agora, entre."),true);
            VeilMod.LOG.info("TREE_GATE_OPEN_AFTER_ALL_FIVE_VOICES: {}",elapsed);
        }
    }
    private static void freezePlayer(EntityPlayerMP p){
        p.motionX=p.motionY=p.motionZ=0;
        p.fallDistance=0;
        double dx=p.posX-VIEW_X,dy=p.posY-VIEW_Y,dz=p.posZ-VIEW_Z;
        if(dx*dx+dy*dy+dz*dz>.01 ||
                Math.abs(net.minecraft.util.math.MathHelper.wrapDegrees(p.rotationYaw-VIEW_YAW))>1 ||
                Math.abs(p.rotationPitch-VIEW_PITCH)>1)
            p.connection.setPlayerLocation(VIEW_X,VIEW_Y,VIEW_Z,VIEW_YAW,VIEW_PITCH);
    }
    private static void sealGate(WorldServer w){
        for(int y=5;y<=8;y++)for(int x=-2;x<=2;x++)
            w.setBlockState(new BlockPos(x,y,GATE_Z),Blocks.LOG.getDefaultState(),3);
    }
    private static void unsealGate(WorldServer w){
        for(int y=5;y<=8;y++)for(int x=-2;x<=2;x++){
            BlockPos at=new BlockPos(x,y,GATE_Z);
            if(w.getBlockState(at).getBlock()==Blocks.LOG)w.setBlockToAir(at);
        }
    }
    private static void prepareChest(WorldServer w){
        if(w.getBlockState(CHEST).getBlock()!=Blocks.CHEST)
            w.setBlockState(CHEST,Blocks.CHEST.getDefaultState(),3);
        TileEntity tile=w.getTileEntity(CHEST);
        if(tile instanceof TileEntityChest){
            TileEntityChest chest=(TileEntityChest)tile;
            if(chest.getStackInSlot(0).isEmpty())chest.setInventorySlotContents(0,book());
        }
    }
    /** Reduced spatial creaks for the shorter continuous dialogue scene. */
    private void ritualWoodSounds(WorldServer w,long elapsed){
        int[] seconds={5,11,18,26,35};
        int[] xs={-5,6,-9,7,3},zs={10,8,7,-4,-7},ys={6,7,12,15,23};
        for(int i=0;i<seconds.length;i++){
            if(elapsed!=seconds[i]*20L)continue;
            BlockPos pos=new BlockPos(xs[i],4+ys[i],zs[i]);
            if(w.isBlockLoaded(pos,false))w.playSound(null,pos,VeilSounds.TREE_CREAKS[i%6],
                    SoundCategory.BLOCKS,.55F,.96F+(i%3)*.025F);
            return;
        }
    }
    private static void ensureWatcher(WorldServer w){
        if(!w.getEntities(EntityWatcher.class,
                e->e.getEntityData().getBoolean("veilRitual")).isEmpty())return;
        if(!w.isBlockLoaded(SPEAKER,false)||!w.isAirBlock(SPEAKER)||!w.isAirBlock(SPEAKER.up()))return;
        EntityWatcher watcher=new EntityWatcher(w);
        watcher.setLocationAndAngles(SPEAKER.getX()+.5,SPEAKER.getY(),SPEAKER.getZ()+.5,0,0);
        watcher.getEntityData().setBoolean("veilRitual",true);
        watcher.getEntityData().setLong("veilRitualUntil",Long.MAX_VALUE);
        if(!w.spawnEntity(watcher))VeilMod.LOG.warn("Cinematic Watcher failed to spawn.");
    }
    private void playSpeeches(EntityPlayerMP player,WorldServer w,long elapsed,StoryData d){
        for(int cue=0;cue<5;cue++){
            int flag=1<<cue;
            if((d.treeWatcherFlags&flag)!=0)continue;
            int start=TreeCinematicTimeline.start(cue);
            if(elapsed<start)break;
            if(elapsed>start+2){
                // Restart coherently instead of speaking several clips at once after reload.
                d.treeStarted=w.getTotalWorldTime();d.treeWatcherFlags=0;d.markDirty();
                return;
            }
            ensureWatcher(w);
            TreeRitualAudio.play(w,player,new net.minecraft.util.math.Vec3d(
                    SPEAKER.getX()+.5,SPEAKER.getY()+1.35,SPEAKER.getZ()+.5),cue);
            d.treeWatcherFlags|=flag;d.markDirty();
            player.sendStatusMessage(new TextComponentString(SUBTITLES[cue]),true);
            break;
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
        if(TreeCinematicTimeline.start(0)!=40 || TreeCinematicTimeline.start(4)<=TreeCinematicTimeline.start(3))
            throw new IllegalStateException("Invalid Watcher cinematic timing");
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
        VeilMod.LOG.info("TREE_CINEMATIC_PLAN_OK: gate=sealed, totalVoiceTicks={}, prebuild={}",
                TreeCinematicTimeline.END_TICK,TreeCinematicTimeline.PREBUILD_BUDGET);
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
