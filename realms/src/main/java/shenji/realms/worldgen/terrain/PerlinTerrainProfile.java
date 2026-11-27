package shenji.realms.worldgen.terrain;

import shenji.realms.internal.fastnoiselite.FastNoiseLite;
import shenji.realms.worldgen.noise.NoiseFactory;
import shenji.realms.worldgen.noise.NoiseSampler2D;

public class PerlinTerrainProfile implements TerrainProfile {
    private final NoiseSampler2D elevation;

    public PerlinTerrainProfile(NoiseFactory noiseFactory) {
        this.elevation = noiseFactory.create2D(
                "perlin_terrain/elevation",
                noise -> {
                    noise.SetNoiseType(FastNoiseLite.NoiseType.Perlin);
                    noise.SetFrequency(0.01f);
                }
                );
    }

    @Override
    public double sampleDensity(double x, double y, double z) {
        return -y + 64 + 30 * elevation.sample(x, z);
    }
}
