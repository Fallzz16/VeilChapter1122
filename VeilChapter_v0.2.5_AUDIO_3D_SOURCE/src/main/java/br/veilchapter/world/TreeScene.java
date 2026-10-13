package br.veilchapter.world;

import br.veilchapter.VeilMod;
import br.veilchapter.config.VeilConfig;
import br.veilchapter.core.TreeMath;
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
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.ITeleporter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Isolated chapter finale. Protected build + persistent cursor for resumability. */
public final class TreeScene {
    public static final BlockPos CHEST = new BlockPos(0,5,0);
    private static final int WAIT_TICKS=160; // 8 seconds
    private static final int GROW_TICKS=600; // 30 seconds
    private static List<Placement> plan;
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
        d.treeStage=0; d.treeStarted=0;d.returnCommitted=false;
        d.markDirty();
        return true;
    }
    public void enter(EntityPlayerMP player,StoryData d) {
        if(player.dimension!=0 || !d.treeActive) return;
        try {
            Entity changed=player.changeDimension(VeilMod.CLEARING_DIM,new FixedPoint(0.5,4, -36.5));
            if (changed == null || changed.dimension!=VeilMod.CLEARING_DIM) {
                VeilMod.LOG.error("Transicao para clearing nao confirmada; mantendo gatilho pendente.");
                d.treeActive=false;d.treeTriggerConsumed=false;d.markDirty();
            }
        } catch (Exception exc) {
            VeilMod.LOG.error("Falha na transicao da arvore. Jogador preservado?",exc);
            d.treeActive=false;d.treeTriggerConsumed=false;d.markDirty();
        }
    }
    public void tick(EntityPlayerMP p,StoryData d) {
        if(p.dimension!=VeilMod.CLEARING_DIM || !d.treeActive)return;
        WorldServer w=(WorldServer)p.world;
        if(d.treeStarted==0){d.treeStarted=w.getTotalWorldTime();d.markDirty();}
        // Hunger freezes at the EXACT entry values, without granting free food.
        p.getFoodStats().setFoodLevel(d.storedFood);
        p.getFoodStats().setFoodSaturationLevel(d.storedSaturation);
        if(d.returnCommitted) { finishReturn(p,d); return; }
        // Loop across the empty terrain only after tree is no longer visible behind the player.
        if(p.posX*p.posX+p.posZ*p.posZ>180*180){
            // This loop is intentionally obvious in code but should be visually tested in game.
            p.connection.setPlayerLocation(-p.posX*.22,4,-p.posZ*.22,p.rotationYaw,p.rotationPitch);
        }
        long elapsed=w.getTotalWorldTime()-d.treeStarted;
        if(elapsed<WAIT_TICKS)return;
        List<Placement> shape=plan();
        long growElapsed=Math.min(GROW_TICKS,elapsed-WAIT_TICKS);
        int target=(int)Math.min(shape.size(),(shape.size()*growElapsed)/GROW_TICKS);
        int budget=220; // Max ~220 block operations/tick; prefer stable TPS over exact duration under load.
        int built=0;
        int generatedChunksThisTick=0;
        while(d.treeCursor<target && built<budget) {
            Placement op=shape.get(d.treeCursor++);
            if(!w.isBlockLoaded(op.pos,false)) {
                // Only this isolated dimension: generate at most one tree-area chunk
                // per tick. Otherwise 2-chunk render-distance worlds could stall.
                if (generatedChunksThisTick>=1) {d.treeCursor--;break;}
                generatedChunksThisTick++;
                w.getChunkProvider().provideChunk(op.pos.getX()>>4,op.pos.getZ()>>4);
                if(!w.isBlockLoaded(op.pos,false)) {d.treeCursor--;break;}
            }
            if(!op.pos.equals(CHEST)) {
                if(p.getEntityBoundingBox().intersects(new net.minecraft.util.math.AxisAlignedBB(op.pos))) {
                    // Do not suffocate the player; leave an intentionally safe gap.
                    built++;continue;
                }
                IBlockState old=w.getBlockState(op.pos);
                if(old.getBlock()==Blocks.AIR || old.getBlock()==Blocks.TALLGRASS || old.getBlock()==Blocks.GRASS || old.getBlock()==Blocks.LOG) {
                    w.setBlockState(op.pos,op.state,2);
                }
            }
            built++;
        }
        if(built>0 && w.getTotalWorldTime()%20==0)d.markDirty();
        if(d.treeCursor>=shape.size() && elapsed>=WAIT_TICKS+GROW_TICKS) {
            // Chest exists only once the complete structure has been prepared.
            if(!p.getEntityBoundingBox().intersects(new net.minecraft.util.math.AxisAlignedBB(CHEST))) {
                if(w.getBlockState(CHEST).getBlock()!=Blocks.CHEST)w.setBlockState(CHEST,Blocks.CHEST.getDefaultState(),3);
                TileEntity tile=w.getTileEntity(CHEST);
                if(tile instanceof TileEntityChest){
                    TileEntityChest chest=(TileEntityChest)tile;
                    if(chest.getStackInSlot(0).isEmpty())chest.setInventorySlotContents(0,book());
                }
                if(d.treeStage!=2){d.treeStage=2;d.markDirty();}
            }
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
        Entity changed=p.changeDimension(0,new FixedPoint(dest.getX()+.5,dest.getY(),dest.getZ()+.5));
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
    private static synchronized List<Placement> plan() {
        if(plan!=null)return plan;
        List<Placement> out=new ArrayList<>(80000);
        // Trunk rises first, roots are broad, 4-block-high dark chamber remains hollow.
        for(int y=0;y<83;y++)for(int x=-20;x<=20;x++)for(int z=-20;z<=20;z++) {
            if(TreeMath.trunk(x,y,z))out.add(new Placement(new BlockPos(x,4+y,z),Blocks.LOG.getDefaultState()));
        }
        // Grass tufts interleaved with trunk formation: blocks are all staged in order.
        Random random=new Random(134865L);
        List<Placement> tufts=new ArrayList<>();
        for(int i=0;i<650;i++) {
            int x=random.nextInt(151)-75,z=random.nextInt(151)-75;
            if(x*x+z*z<16*16||x*x+z*z>76*76)continue;
            tufts.add(new Placement(new BlockPos(x,4,z),Blocks.TALLGRASS.getDefaultState()
                    .withProperty(net.minecraft.block.BlockTallGrass.TYPE,net.minecraft.block.BlockTallGrass.EnumType.GRASS)));
        }
        // Add vegetation regularly throughout construction (including while trunk forms).
        int every=Math.max(1,out.size()/Math.max(1,tufts.size()));
        for(int i=0;i<tufts.size();i++)out.add(Math.min(out.size(),i*every),tufts.get(i));
        for(int y=62;y<110;y++)for(int x=-31;x<=31;x++)for(int z=-31;z<=31;z++) {
            if(TreeMath.foliage(x,y,z) && ((x*73+y*13+z*19)&15)<11)
                out.add(new Placement(new BlockPos(x,4+y,z),Blocks.LEAVES.getDefaultState()));
        }
        // Natural timber floor for the hollow room (at ground level y=4).
        for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)if(x*x+z*z<=25)
            out.add(new Placement(new BlockPos(x,4,z),Blocks.PLANKS.getDefaultState()));
        plan=out;return plan;
    }
    private static class FixedPoint implements ITeleporter {
        private final double x,y,z;
        FixedPoint(double x,double y,double z){this.x=x;this.y=y;this.z=z;}
        @Override public void placeEntity(World world,Entity entity,float yaw) {
            entity.setLocationAndAngles(x,y,z,entity.rotationYaw,entity.rotationPitch);
            entity.motionX=entity.motionY=entity.motionZ=0;
        }
    }
}
