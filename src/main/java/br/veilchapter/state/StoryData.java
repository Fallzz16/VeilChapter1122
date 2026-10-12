package br.veilchapter.state;

import br.veilchapter.core.StoryClock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import java.util.ArrayList;
import java.util.List;

/** The Overworld owns all chapter state. Loaded/saved by vanilla MapStorage. */
public class StoryData extends WorldSavedData {
    private static final String KEY = "veilchapter_story_v2";
    public final StoryClock clock = new StoryClock();
    public BlockPos bed = BlockPos.ORIGIN;
    public long savedTime;
    public int storedFood = 20;
    public float storedSaturation = 5F;
    public long waitUntil;
    public long nextEvent;
    public boolean awakeningPending;
    public long awakeningStartedTick;
    public boolean treeActive;
    public boolean treeTriggerConsumed;
    public boolean returnCommitted;
    public boolean bookPending;
    public int treeStage;
    public long treeStarted;
    public int treeCursor;
    public int originDim;
    public double originX, originY, originZ;
    public float originYaw, originPitch;
    public final List<NBTTagCompound> suspendedMobs = new ArrayList<>();
    public final List<NBTTagCompound> anomalousBlocks = new ArrayList<>();
    public final List<NBTTagCompound> managedFires = new ArrayList<>();

    public StoryData() { super(KEY); }
    public StoryData(String key) { super(key); }
    public static StoryData get(World world) {
        // The story has ONE canonical Overworld record even when accessed in the tree dimension.
        // Never let a custom dimension accidentally create a separate chapter timeline.
        World canonical = world;
        if (!world.isRemote && world.getMinecraftServer()!=null && world.getMinecraftServer().getWorld(0)!=null)
            canonical = world.getMinecraftServer().getWorld(0);
        MapStorage store = canonical.getMapStorage();
        StoryData data = (StoryData) store.getOrLoadData(StoryData.class, KEY);
        if (data == null) { data = new StoryData(); store.setData(KEY, data); data.markDirty(); }
        return data;
    }
    @Override public void readFromNBT(NBTTagCompound tag) {
        clock.restore(tag.getInteger("day"), tag.getLong("calendar"), tag.getBoolean("active"), tag.getBoolean("complete"));
        bed = new BlockPos(tag.getInteger("bedX"),tag.getInteger("bedY"),tag.getInteger("bedZ"));
        savedTime = tag.getLong("savedTime");
        storedFood=tag.getInteger("storedFood");storedSaturation=tag.getFloat("storedSaturation"); waitUntil = tag.getLong("waitUntil");
        nextEvent=tag.getLong("nextEvent"); awakeningPending=tag.getBoolean("awakeningPending");
        awakeningStartedTick=tag.getLong("awakeningStartedTick"); treeActive=tag.getBoolean("treeActive");
        treeTriggerConsumed=tag.getBoolean("treeTriggerConsumed"); returnCommitted=tag.getBoolean("returnCommitted");
        bookPending=tag.getBoolean("bookPending");treeStage=tag.getInteger("treeStage");
        treeStarted=tag.getLong("treeStarted");treeCursor=tag.getInteger("treeCursor");
        originDim=tag.getInteger("originDim");originX=tag.getDouble("originX");
        originY=tag.getDouble("originY");originZ=tag.getDouble("originZ");
        originYaw=tag.getFloat("originYaw");originPitch=tag.getFloat("originPitch");
        suspendedMobs.clear(); anomalousBlocks.clear(); managedFires.clear();
        NBTTagList mobs=tag.getTagList("mobs",10);
        for (int i=0;i<mobs.tagCount();i++) suspendedMobs.add(mobs.getCompoundTagAt(i));
        NBTTagList blocks=tag.getTagList("floating",10);
        for (int i=0;i<blocks.tagCount();i++) anomalousBlocks.add(blocks.getCompoundTagAt(i));
        NBTTagList fires=tag.getTagList("fires",10);
        for (int i=0;i<fires.tagCount();i++) managedFires.add(fires.getCompoundTagAt(i));
    }
    @Override public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        tag.setInteger("schema",2);tag.setInteger("day",clock.day());
        tag.setLong("calendar",clock.lastCalendarDay());tag.setBoolean("active",clock.active());
        tag.setBoolean("complete",clock.completed());
        tag.setInteger("bedX",bed.getX());tag.setInteger("bedY",bed.getY());tag.setInteger("bedZ",bed.getZ());
        tag.setLong("savedTime",savedTime);tag.setInteger("storedFood",storedFood);
        tag.setFloat("storedSaturation",storedSaturation);tag.setLong("waitUntil",waitUntil);
        tag.setLong("nextEvent",nextEvent);tag.setBoolean("awakeningPending",awakeningPending);
        tag.setLong("awakeningStartedTick",awakeningStartedTick);
        tag.setBoolean("treeActive",treeActive);tag.setBoolean("treeTriggerConsumed",treeTriggerConsumed);
        tag.setBoolean("returnCommitted",returnCommitted);tag.setBoolean("bookPending",bookPending);
        tag.setInteger("treeStage",treeStage);tag.setLong("treeStarted",treeStarted);
        tag.setInteger("treeCursor",treeCursor);tag.setInteger("originDim",originDim);
        tag.setDouble("originX",originX);tag.setDouble("originY",originY);tag.setDouble("originZ",originZ);
        tag.setFloat("originYaw",originYaw);tag.setFloat("originPitch",originPitch);
        NBTTagList mobs=new NBTTagList(); for(NBTTagCompound n:suspendedMobs)mobs.appendTag(n.copy()); tag.setTag("mobs",mobs);
        NBTTagList blocks=new NBTTagList();for(NBTTagCompound n:anomalousBlocks)blocks.appendTag(n.copy());tag.setTag("floating",blocks);
        NBTTagList fires=new NBTTagList();for(NBTTagCompound n:managedFires)fires.appendTag(n.copy());tag.setTag("fires",fires);
        return tag;
    }
}
