package shenji.realms.worldgen.terrain.interpolation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.minecraft.world.level.levelgen.RandomState;
import shenji.realms.worldgen.terrain.TerrainProfile;

public class ChunkDensityCache {

    private record CacheKey(RandomState randomState, int chunkX, int chunkZ, int minY, int height) {}

    private final Cache<CacheKey, ChunkDensityGrid> cache = Caffeine.newBuilder().maximumSize(512).build();

    public ChunkDensityGrid getDensityGrid(TerrainProfile terrainProfile, RandomState randomState, int chunkX, int chunkZ, int minY, int height) {
        CacheKey cacheKey = new CacheKey(randomState, chunkX, chunkZ, minY, height);

        return cache.get(cacheKey, key -> {
            int originX = chunkX * 16;
            int originZ = chunkZ * 16;

            return new ChunkDensityGrid(terrainProfile, originX, originZ, minY, height);
        });
    }
}
