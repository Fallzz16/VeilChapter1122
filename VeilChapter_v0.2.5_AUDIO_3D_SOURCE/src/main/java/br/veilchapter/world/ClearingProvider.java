package br.veilchapter.world;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldProviderSurface;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.init.Biomes;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.ChunkGeneratorFlat;
/** Separate flat dimension, keeps normal Overworld terrain untouched. */
public final class ClearingProvider extends WorldProviderSurface {
    @Override protected void init() {
        super.init();
        hasSkyLight = true;
        biomeProvider = new BiomeProviderSingle(Biomes.PLAINS);
    }
    @Override public IChunkGenerator createChunkGenerator() {
        return new ChunkGeneratorFlat(world, world.getSeed(), false,
                "3;minecraft:bedrock,2*minecraft:dirt,minecraft:grass;1;");
    }
    @Override public DimensionType getDimensionType() {return br.veilchapter.VeilMod.CLEARING_DIM >= 0
            ? net.minecraftforge.common.DimensionManager.getProviderType(br.veilchapter.VeilMod.CLEARING_DIM)
            : DimensionType.OVERWORLD;}
    @Override public float calculateCelestialAngle(long worldTime,float partialTicks) {return 0.0F;} // fixed midday
    @Override public boolean canRespawnHere() {return false;}
    @Override public boolean isSurfaceWorld() {return true;}
}
