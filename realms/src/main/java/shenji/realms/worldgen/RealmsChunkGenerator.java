package shenji.realms.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.densityfunction.SamplerContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jspecify.annotations.Nullable;
import shenji.realms.worldgen.noise.NoiseFactory;
import shenji.realms.worldgen.noise.SeedContext;
import shenji.realms.worldgen.terrain.KarstParameters;
import shenji.realms.worldgen.terrain.KarstTerrainProfile;
import shenji.realms.worldgen.terrain.TerrainProfile;
import shenji.realms.worldgen.terrain.interpolation.ChunkDensityCache;
import shenji.realms.worldgen.terrain.interpolation.ChunkDensityGrid;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class RealmsChunkGenerator extends ChunkGenerator {

    public static final MapCodec<RealmsChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            i -> i.group(BiomeSource.CODEC
                    .fieldOf("biome_source")
                    .forGetter(RealmsChunkGenerator::getBiomeSource))
            .apply(i, i.stable(RealmsChunkGenerator::new))
    );

    private final ConcurrentHashMap<RandomState, TerrainProfile> terrainProfiles = new ConcurrentHashMap<>();
    private final ChunkDensityCache densityCache = new ChunkDensityCache();

    public RealmsChunkGenerator(final BiomeSource biomesource) {
        super(biomesource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {

    }

    @Override
    public int getGenDepth() {
        return 319 - (-64) + 1;
    }

    @Override
    public CompletableFuture<ChunkAccess> buildTerrain(ChunkAccess chunk, Blender blender, RandomState randomState, StructureManager structureManager, BiomeManager biomeManager, @Nullable WorldGenRegion carverBiomeRegion, Set<Holder<Biome>> possibleBiomes) {
        TerrainProfile terrainProfile = getTerrainProfile(randomState);

        ChunkPos pos = chunk.getPos();
        int worldX = pos.getMinBlockX();
        int worldZ = pos.getMinBlockZ();

        int minY = chunk.getMinY();
        int maxY = chunk.getMaxY();

        ChunkDensityGrid densityGrid = densityCache.getDensityGrid(terrainProfile, randomState, pos.x(), pos.z(), minY, chunk.getHeight());

        for (int x = worldX; x < worldX + 16; x++) {
            for (int z = worldZ; z < worldZ + 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    double density = densityGrid.sampleDensity(x, y, z);

                    if (density > 0) {
                        chunk.setBlockState(new BlockPos(x, y, z), Blocks.STONE.defaultBlockState());
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor heightAccessor, RandomState randomState) {
        TerrainProfile terrainProfile = getTerrainProfile(randomState);

        int minY = heightAccessor.getMinY();
        int maxY = heightAccessor.getMaxY();

        int chunkX = Math.floorDiv(x, 16);
        int chunkZ = Math.floorDiv(z, 16);

        ChunkDensityGrid densityGrid = densityCache.getDensityGrid(terrainProfile, randomState, chunkX, chunkZ, minY, heightAccessor.getHeight());

        for (int y = maxY; y >= minY; y--) {
            double density = densityGrid.sampleDensity(x, y, z);
            if (density > 0) {
                return y + 1;
            }
        }

        return minY;
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor heightAccessor, RandomState randomState) {
        TerrainProfile terrainProfile = getTerrainProfile(randomState);

        int height = heightAccessor.getHeight();
        int minY = heightAccessor.getMinY();
        BlockState[] column = new BlockState[height];

        int chunkX = Math.floorDiv(x, 16);
        int chunkZ = Math.floorDiv(z, 16);

        ChunkDensityGrid densityGrid = densityCache.getDensityGrid(terrainProfile, randomState, chunkX, chunkZ, minY, heightAccessor.getHeight());

        for (int i = 0; i < height; i++) {
            double density = densityGrid.sampleDensity(x, minY + i, z);

            if (density > 0) {
                column[i] = Blocks.STONE.defaultBlockState();
            } else {
                column[i] = Blocks.AIR.defaultBlockState();
            }
        }

        return new NoiseColumn(minY, column);
    }

    @Override
    public void addDebugScreenInfo(List<String> result, RandomState randomState, BlockPos feetPos, SamplerContext samplerContext) {

    }

    @Override
    public void applyBiomeDecoration(final WorldGenLevel level, final ChunkAccess chunk, final StructureManager structureManager) {

    }

    @Override
    public void createStructures(
            final RegistryAccess registryAccess,
            final ChunkGeneratorStructureState state,
            final StructureManager structureManager,
            final ChunkAccess centerChunk,
            final StructureTemplateManager structureTemplateManager,
            final ResourceKey<Level> level
    ) {

    }



    private TerrainProfile getTerrainProfile(RandomState randomState) {
        return terrainProfiles.computeIfAbsent(
                randomState,
                key -> {
                    SeedContext seedContext = new SeedContext(key);
                    NoiseFactory noiseFactory = new NoiseFactory(seedContext);
                    return new KarstTerrainProfile(noiseFactory, KarstParameters.defaults());
                }
            );
    }
}
